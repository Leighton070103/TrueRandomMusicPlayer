package net.classicgarage.truerandommusicplayer.service;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.IntDef;
import android.util.Log;
import android.widget.RemoteViews;

import net.classicgarage.truerandommusicplayer.PlayerWidgetProvider;
import net.classicgarage.truerandommusicplayer.R;
import net.classicgarage.truerandommusicplayer.activity.MainActivity;
import net.classicgarage.truerandommusicplayer.db.SongDataSource;
import net.classicgarage.truerandommusicplayer.model.SongItem;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MusicService extends Service {

    private MediaPlayer mMediaPlayer;
    private SongDataSource mDataSource;
    private Timer mTimer = null;
    private TimerTask mTask = null;
    private int mCurrentSongIndex = 0;
    public boolean pReplayFlag = false;
    public boolean pRandomFlag = false;
    public boolean pPlayFlag = false;


    public MusicService() {
    }


    /**
     * On create method of the service.
     * Initialize the media player and the data source.
     * Set the current index if previously the service was killed.
     */
    @Override
    public void onCreate() {
        //Initialize media player
        mMediaPlayer = new MediaPlayer();
        mDataSource = SongDataSource.getInstance(this.getApplicationContext());
        SharedPreferences preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        mCurrentSongIndex = getCurrentSongIndexById(preferences.getLong( SongItem.SONG_ID,
                getCurrentPlayingSong().getId()));
        super.onCreate();
    }

    /**
     * Called every time start service method is called for this service.
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int action = intent.getIntExtra( PlayerWidgetProvider.WIDGET_ACTION, -1);
        switch (action){
            case PlayerWidgetProvider.WIDGET_OPERATE_CURRENT:
                if(isPlaying()) pause();
                else play();
                break;
            case PlayerWidgetProvider.WIDGET_PLAY_NEXT:
                playNextSong();
                break;
            case PlayerWidgetProvider.WIDGET_PLAY_PREVIOUS:
                playLastSong();
                break;
        }
        updateWidget();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MusicBinder();
    }


    /**
     * Play the song from the data source.
     */
    private void play() {
            try {
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(getSongFromListByIndex().getPath());
                Log.d("======play=====", getSongFromListByIndex().toString());
                mMediaPlayer.prepare();
                if(pPlayFlag)
                    mMediaPlayer.start();
                refereshSeekBar();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                mMediaPlayer = null;
                mMediaPlayer = new MediaPlayer();
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (pReplayFlag) {
                        mTask.cancel();
                        mTimer.cancel();
                        play();
                        refereshSeekBar();
                    } else {
                        mTask.cancel();
                        mTimer.cancel();
                        playNextSong();
                    }
                }
            });

    }

    /**
     * Use the current index to get the song.
     * @return
     */
    private SongItem getSongFromListByIndex() {
        try {
            return mDataSource.getSongsFromSD().get(mCurrentSongIndex);
        }
        catch (IndexOutOfBoundsException e){
            e.printStackTrace();
            return null;
        }

    }

    private void deleteCurrentPlayingSong(){
        try {
            mDataSource.deleteSongInIndex(mCurrentSongIndex);
            play();
        }
        catch (java.lang.IndexOutOfBoundsException e){
            e.printStackTrace();
        }
    }


    /**
     * Called when playing of current song is complete or the next song button is clicked.
     * Play the next song.
     */
    public void playNextSong(){
       if(pRandomFlag){
            randomSongIndex();
            play();
            refereshSeekBar();
        }
        else{
            updateCurrentSongIndex(1);
            play();
            refereshSeekBar();
        }
    }

    /**
     * Called when the previous song button is clicked.
     */
    public void playLastSong(){
        if(pRandomFlag){
            randomSongIndex();
            play();
            refereshSeekBar();
        }
        else {
            updateCurrentSongIndex(0);
            play();
            refereshSeekBar();
        }
    }
    private void refereshSeekBar() {
        mTimer = new Timer();
        mTask = new TimerTask() {
            @Override
            public void run() {
                if(mMediaPlayer == null) return;
                int position = mMediaPlayer.getCurrentPosition();
                int duration = mMediaPlayer.getDuration();
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putInt("duration",duration);
                data.putInt("position",position);
                msg.setData(data);
                MainActivity.handler.sendMessage(msg);

            }
        };
        mTimer.schedule(mTask,100,1000);
    }

    public void seekTo(int position) {
        mMediaPlayer.seekTo(position);
    }

    private void stop(){
        mMediaPlayer.stop();
    }
    private void continueMusic() {
        mMediaPlayer.start();
    }

    private void pause() {
        mMediaPlayer.pause();
    }

    private SongItem getCurrentPlayingSong(){
        return getSongFromListByIndex();
    }

    public void updateCurrentSongIndex(int action){
        if(action == 1) mCurrentSongIndex++;
        if(action == 0) mCurrentSongIndex--;
        if( mCurrentSongIndex > mDataSource.getSongsFromSD().size() - 1) mCurrentSongIndex = 0;
        if( mCurrentSongIndex < 0) mCurrentSongIndex = mDataSource.getSongsFromSD().size() - 1;
        SharedPreferences preferences = getSharedPreferences("user",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong( SongItem.SONG_ID, getSongFromListByIndex().getId());
        editor.commit();
    }

    public int getCurrentSongIndexById(long songId){
        for(int i = 0;i <= mDataSource.getSongsFromSD().size() - 1;i++){
            if(songId == mDataSource.getSongsFromSD().get(i).getId())
                return i;
        }
        return 0;
    }

    public void randomSongIndex(){
        java.util.Random r = new java.util.Random();
        mCurrentSongIndex = r.nextInt(mDataSource.getSongsFromSD().size());
    }

    public void setCurrentSongIndex(int index){
        mCurrentSongIndex = index;
    }

    private boolean isPlaying(){
        if( mMediaPlayer == null ) return false;
        return mMediaPlayer.isPlaying();
    }

    private void playSongAtPosition(int position) {
        Log.d("===playAtPosition===", mDataSource.getSongsFromSD().size()+" pos:"+position);
        setCurrentSongIndex(position);
        play();

    }

    private void setCurrentSongFavorite(){
        mDataSource.setSongFavorite(getCurrentPlayingSong().getId());
    }

    /**
     * To update the screen widget.
     */
    private void updateWidget() {
        RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(),
                R.layout.widget_layout);
        SongItem song = getCurrentPlayingSong();
        if(song != null){
            remoteViews.setTextViewText(R.id.widget_title_tv, song.getTitle());
            remoteViews.setTextViewText(R.id.widget_artist_tv, song.getArtist());
//            if(isPlaying()) remoteViews.setInt(R.id.widget_play_btn, "setBackground",
//                    R.mipmap.widget_play_btn);
//            else remoteViews.setInt(R.id.widget_play_btn, "setBackground",
//                    R.mipmap.widget_pause_btn);
        }
        else {
            remoteViews.setTextViewText(R.id.widget_title_tv, getString(R.string.no_song_hint));
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        appWidgetManager.updateAppWidget(new ComponentName(getApplicationContext(),
                PlayerWidgetProvider.class), remoteViews);
    }

    /**
     * Change the random flag.
     */
    private void changeRandomFlag(){
        if(!pRandomFlag) {
            pRandomFlag = true;
        }
        else
            pRandomFlag = false;
    }

    /**
     * Change the replayflag.
     */
    private void changeReplayFlag(){
        if(!pReplayFlag) {
            pReplayFlag = true;
        }
        else
            pReplayFlag = false;
    }

    private void changPlayingFlag() {
        if(!pPlayFlag) {
            pPlayFlag = true;
        }
        else
            pPlayFlag = false;
    }

    class MusicBinder extends Binder implements BaseService{
        @Override
        public void callPlay() {
            play();
        }

        @Override
        public void callStop() {
            stop();
        }

        @Override
        public void callPause() {
            pause();
        }

        @Override
        public void callContinueMusic() {
            continueMusic();
        }

        @Override
        public void callSeekTo(int position) {
            seekTo(position);
        }

        @Override
        public boolean isPlaying() {
            return mMediaPlayer.isPlaying();
        }

        @Override
        public void callPlayNextSong(){
            playNextSong();
        }

        @Override
        public void callPlaySongAtPosition(int position) { playSongAtPosition(position);}

        @Override
        public SongItem getPlayingSong() {
            return getCurrentPlayingSong();
        }

        @Override
        public void deleteCurrentSong() { deleteCurrentPlayingSong(); }

        @Override
        public void setCurrentSongFavorite() {
            MusicService.this.setCurrentSongFavorite();
        }

        @Override
        public void callPlayLastSong(){ playLastSong();}

        @Override
        public void callChangeReplayFlag(){ changeReplayFlag(); }

        @Override
        public void callChangeRandomFlag(){changeRandomFlag();}

        @Override
        public void callChangePlayFlag(){changPlayingFlag();}
    }
}
