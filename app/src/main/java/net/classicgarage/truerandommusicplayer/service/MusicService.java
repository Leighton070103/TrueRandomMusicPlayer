package net.classicgarage.truerandommusicplayer.service;

import android.app.KeyguardManager;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.RemoteViews;

import net.classicgarage.truerandommusicplayer.PlayerWidgetProvider;
import net.classicgarage.truerandommusicplayer.R;
import net.classicgarage.truerandommusicplayer.activity.MainActivity;
import net.classicgarage.truerandommusicplayer.broadcastreceiver.LockScreenBroadcastReceiver;
import net.classicgarage.truerandommusicplayer.db.SongDataSource;
import net.classicgarage.truerandommusicplayer.model.SongItem;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This service is to provide operations of playing, pausing, deleting songs.
 */
public class MusicService extends Service {

    private MediaPlayer mMediaPlayer;
    private SongDataSource mDataSource;
    private Timer mTimer = null;
    private TimerTask mTask = null;
    private int mCurrentSongIndex = 0;
    private LockScreenBroadcastReceiver mLockScreenBroadcastReceiver;
    public boolean pReplayFlag = false;
    public boolean pRandomFlag = false;
    public boolean pPlayFlag = false;

    public static final String INTENT_ACTION = "Intent action";
    public static final int PLAY_PREVIOUS = 0;
    public static final int OPERATE_CURRENT = 1;
    public static final int PLAY_NEXT = 2;
    public static final int PLAY_MODE = 3;


    public MusicService() {}



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
        try{
            mCurrentSongIndex = mDataSource.findSongIndexById(preferences.getLong( SongItem.SONG_ID,
                getCurrentPlayingSong().getId()));
        }
        catch ( NullPointerException e){
            e.printStackTrace();
            mCurrentSongIndex = 0;
        }
        KeyguardManager.KeyguardLock key;
        KeyguardManager km = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
        key = km.newKeyguardLock("IN");
        key.disableKeyguard();
        registerScreenBroadcastReceiver();
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
        int action = intent.getIntExtra( INTENT_ACTION, -1);
        switch (action){
            case OPERATE_CURRENT:
                if (pPlayFlag) pause();
                else play();
                break;
            case PLAY_NEXT:
                playNextSong();
                break;
            case PLAY_PREVIOUS:
                playLastSong();
                break;
            case -1:
                updateWidget();
                break;
        }
        return START_NOT_STICKY;
    }

    /**
     * Called when the service is binded.
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        return new MusicBinder();
    }

    /**
     * Called for the preperation of the music.
     */
    private void prepare(){
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(getSongFromListByIndex().getPath());
            Log.d("======play=====", getSongFromListByIndex().toString());
            mMediaPlayer.prepare();
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
                    refreshSeekBar();
                } else {
                    mTask.cancel();
                    mTimer.cancel();
                    playNextSong();
                }
            }
        });
        updateWidget();
    }

    /**
     * Play the song from the data source.
     */
    private void play() {
        prepare();
        pPlayFlag = true;
        updateWidget();
        mMediaPlayer.start();
        refreshSeekBar();
    }

    /**
     * Use the current index to get the song.
     * @return
     */
    private SongItem getSongFromListByIndex() {
        try {
            return mDataSource.getAllSongs().get(mCurrentSongIndex);
        }
        catch (IndexOutOfBoundsException e){
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Delete the song that is currently displayed.
     */
    private void deleteCurrentPlayingSong(){
        try {
            mDataSource.deleteSongInIndex(mCurrentSongIndex);
            play();
            updateWidget();
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
        }
        else{
            updateCurrentSongIndex(PLAY_NEXT);
        }
        if( pPlayFlag ) play();
        else prepare();
    }

    /**
     * Called when the previous song button is clicked.
     */
    public void playLastSong(){
        if(pRandomFlag){
            randomSongIndex();
        }
        else {
            updateCurrentSongIndex(PLAY_PREVIOUS);
        }
        if(pPlayFlag) play();
        else prepare();
    }

    /**
     * Update the seek bar of the main activity.
     */
    private void refreshSeekBar() {
        mTimer = new Timer();
        mTask = new TimerTask() {
            @Override
            public void run() {
                if(mMediaPlayer == null) return;
                int position = mMediaPlayer.getCurrentPosition();
                int duration = mMediaPlayer.getDuration();
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putInt("duration", duration);
                data.putInt("position", position);
                msg.setData(data);
                MainActivity.handler.sendMessage(msg);

            }
        };
        mTimer.schedule(mTask,100,1000);
    }

    /**
     * Called when the song is continued.
     * @param position
     */
    public void seekTo(int position) {
        mMediaPlayer.seekTo(position);
    }

    private void stop(){
        mMediaPlayer.stop();
    }

    /**
     * Called when the music is supposed to continue.
     */
    private void continueMusic() {
        mMediaPlayer.start();
        pPlayFlag = true;
    }

    /**
     * Called when the song is paused.
     */
    private void pause() {
        mMediaPlayer.pause();
        pPlayFlag = false;
        updateWidget();
    }

    /**
     * Return the current playing song.
     * @return
     */
    private SongItem getCurrentPlayingSong(){
        return getSongFromListByIndex();
    }

    /**
     * Update the current index of the song, while also store the current id in the shared
     * preference.
     * @param action
     */
    public void updateCurrentSongIndex(int action){
        if(action == PLAY_NEXT) mCurrentSongIndex++;
        if(action == PLAY_PREVIOUS) mCurrentSongIndex--;
        if( mCurrentSongIndex > mDataSource.getAllSongs().size() - 1) mCurrentSongIndex = 0;
        if( mCurrentSongIndex < 0) mCurrentSongIndex = mDataSource.getAllSongs().size() - 1;
        SharedPreferences preferences = getSharedPreferences("user",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong( SongItem.SONG_ID, getSongFromListByIndex().getId());
        editor.commit();
    }

    /**
     * Get the current song index.
     * @param songId
     * @return
     */
    public int getCurrentSongIndexById(long songId){
        for(int i = 0; i <= mDataSource.getAllSongs().size() - 1; i++){
            if(songId == mDataSource.getAllSongs().get(i).getId())
                return i;
        }
        return 0;
    }

    /**
     * Get a random index of the song.
     */
    public void randomSongIndex(){
        java.util.Random r = new java.util.Random();
        boolean isAllPlayedInARow = true;
        int randomSongIndex = r.nextInt(mDataSource.getAllSongs().size());
        if (mDataSource.getSongAtPosition(randomSongIndex).getmPlayedTime() == 0){
            mCurrentSongIndex = randomSongIndex;
            mDataSource.getSongAtPosition(randomSongIndex).setmPlayedTime(1);
            isAllPlayedInARow = false;
        }
        else if (mDataSource.getSongAtPosition(randomSongIndex).getmPlayedTime() > 0){
            for(int i = 0;i < mDataSource.getAllSongs().size();i++){
                if(mDataSource.getSongAtPosition(i).getmPlayedTime() == 0){
                    mCurrentSongIndex = i;
                    isAllPlayedInARow = false;
                }
            }
            if(isAllPlayedInARow){
                for (int i = 0;i < mDataSource.getAllSongs().size();i++){
                    mDataSource.getSongAtPosition(i).setmPlayedTime(0);
                }
            }
        }
        mCurrentSongIndex = r.nextInt(mDataSource.getAllSongs().size());

    }

    /**
     * Set the current song index
     * @param index
     */
    public void setCurrentSongIndex(int index){
        mCurrentSongIndex = index;
    }


    /**
     * Call a song at a specific position.
     * @param position
     */
    private void playSongAtPosition(int position) {
        Log.d("===playAtPosition===", mDataSource.getAllSongs().size()+" pos:"+position);
        setCurrentSongIndex(position);
        if(pPlayFlag) play();
        else prepare();

    }

    /**
     * Set the current playing song as favorite.
     */
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
            if( pPlayFlag ) remoteViews.setInt(R.id.widget_play_btn, "setBackgroundResource",
                    R.mipmap.widget_pause_btn);
            else remoteViews.setInt(R.id.widget_play_btn, "setBackgroundResource",
                    R.mipmap.widget_play_btn);
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

    /**
     *  Return the ramdomflag.
     */
    private boolean getRandomflag(){
        return pRandomFlag;
    }

    /**
     *  Return the replayflag.
     */
    private boolean getReplayflag(){
        return pReplayFlag;
    }

    private void registerScreenBroadcastReceiver() {
        mLockScreenBroadcastReceiver = new LockScreenBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);//当屏幕锁屏的时候触发
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);//当屏幕解锁的时候触发
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);//当用户重新唤醒手持设备时触发
        getApplicationContext().registerReceiver(mLockScreenBroadcastReceiver, intentFilter);
        Log.i("screenBR", "screenBroadcastReceiver注册了");
    }

    /**
     * This class is to provide a binder which has some basic functions for activities to operate
     * in music service.
     */
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
            return pPlayFlag;
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
        public void callChangeRandomFlag(){ changeRandomFlag(); }

        @Override
        public boolean callGetRandomFlag(){ return getRandomflag(); }

        @Override
        public boolean callGetReplayFlag(){ return getReplayflag();}

    }
}
