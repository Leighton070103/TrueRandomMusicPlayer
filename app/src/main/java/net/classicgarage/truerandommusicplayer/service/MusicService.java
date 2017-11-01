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
import android.os.Messenger;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

import net.classicgarage.truerandommusicplayer.broadcastreceiver.PhoneStatusReceiver;
import net.classicgarage.truerandommusicplayer.widget.PlayerWidgetProvider;
import net.classicgarage.truerandommusicplayer.R;
import net.classicgarage.truerandommusicplayer.activity.MainActivity;
import net.classicgarage.truerandommusicplayer.db.SongDataSource;
import net.classicgarage.truerandommusicplayer.model.SongItem;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This service is to provide operations of playing, pausing, deleting songs.
 */
public class MusicService extends Service {

    Messenger mMessenger = null;
    private MediaPlayer mMediaPlayer;
    private SongDataSource mDataSource;
    private Timer mTimer = null;
    private TimerTask mTask = null;
    private int mCurrentSongIndex = 0;

    private Integer mPlayMode = 1;

    private boolean mReplayFlag = false;
    private boolean mPlayFlag = false;

    public static final String PLAY_MODE = "play mode";
    public static final String REPLAY_FLAG = "Replay flag";
    public static final String INTENT_ACTION = "Intent action";
    public static final int ACTION_CONTINUE = 5;
    public static final int ACTION_PAUSE = 6;
    public static final short REQUESTING_BINDING = 78;
    public static final short REFRESH_ALBUM_VIEW = 74;
    public static final short REFRESH_SEEK_BAR_ = 99;

    /**
     * Constants for operating the widget.
     */
    public static final int ACTION_PLAY_PREVIOUS = 0;
    public static final int OPERATE_CURRENT = 1;
    public static final int ACTION_PLAY_NEXT = 2;

    /**
     * Constants for different modes.
     */
    public static final int NORMAL_MODE = -1;
    public static final int FAV_MODE = 0;
    public static final int NORMAL_SEQUENCE = 1;
    public static final int NORMAL_RANDOM = 2;
    public static final int FAV_SEQUENCE = 3;
    public static final int FAV_RANDOM = 4;

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
        if(mDataSource.getAllSongs()!=null && mDataSource.getAllSongs().size()!= 0){
            switch (action){
                case OPERATE_CURRENT:
                    if (mPlayFlag) pause();
                    else play();
                    break;
                case ACTION_PLAY_NEXT:
                    playNextSong();
                    break;
                case ACTION_PLAY_PREVIOUS:
                    playLastSong();
                    break;
                case ACTION_CONTINUE:
                    play();
                    break;
                case ACTION_PAUSE:
                    pause();
                    break;
                case -1:
                    updateWidget();
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    /**
     * Called when the service is started.
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        //IBinder musicBinder = new MusicBinder();
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
                if (mReplayFlag) {
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
        mPlayFlag = true;
        updateWidget();
        mMediaPlayer.start();
        refreshSeekBar();
    }

    /**
     * Use the current index to get the song according to the playing mode.
     * @return
     */
    private SongItem getSongFromListByIndex() {
        try {
            return mDataSource.getAllSongs(isFavorite()).get( mCurrentSongIndex );
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
        if(!getReplayflag()){
            if(isRandom()){
                randomSongIndex();
            }
            else{
                updateCurrentSongIndex(ACTION_PLAY_NEXT);
            }
        }
        if(mPlayFlag) play();
        else prepare();
    }

    public boolean isRandom(){
        return mPlayMode == FAV_RANDOM || mPlayMode == NORMAL_RANDOM;
    }

    public boolean isFavorite(){
        return mPlayMode == FAV_RANDOM || mPlayMode == FAV_SEQUENCE;
    }

    /**
     * Called when the previous song button is clicked.
     */
    public void playLastSong(){
        if(!mReplayFlag){
            if(isRandom()){
                randomSongIndex();
            }
            else {
                updateCurrentSongIndex(ACTION_PLAY_PREVIOUS);
            }
        }
        if(mPlayFlag) play();
        else prepare();
    }

    private void refreshAlbumView(){
        Messenger client = new Messenger(mMessenger.getBinder());
        Message refreshAlbumViewMsg = Message.obtain(null,MusicService.REFRESH_ALBUM_VIEW);
        Bundle data = new Bundle();
        //data.putInt("initialAlbum",REFRESH_ALBUM_VIEW);
        refreshAlbumViewMsg.setData(data);
        try {
            client.send(refreshAlbumViewMsg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //MainActivity.handler.sendMessage(refreshAlbumViewMsg);
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
//                Messenger client = new Messenger(mMessenger.getBinder());
                Message refreshSeekBarMsg = Message.obtain(null,MusicService.REFRESH_SEEK_BAR_);
                Bundle data = new Bundle();
                data.putInt("duration", duration);
                data.putInt("position", position);
                refreshSeekBarMsg.setData(data);
//                try {
//                    client.send(refreshSeekBarMsg);
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
                MainActivity.musicInfoHandler.sendMessage(refreshSeekBarMsg);
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
        mPlayFlag = true;
    }

    /**
     * Called when the song is paused.
     */
    private void pause() {
        mMediaPlayer.pause();
        mPlayFlag = false;
        updateWidget();
    }

    /**
     * Return the current playing song.
     * @return
     */
    private SongItem getCurrentPlayingSong(){
        return getSongFromListByIndex();
    }

    private int getCurrentPlayingSongIndex(){
        return mCurrentSongIndex;
    }

    private SongItem getNextPlayingSong(){
        return null;
    }

    private SongItem getPreviousPlayingSong(){
        return null;
    }

    /**
     * Update the current index of the song, while also store the current id in the shared
     * preference.
     * @param action
     */
    public void updateCurrentSongIndex(int action){
        if(action == ACTION_PLAY_NEXT) mCurrentSongIndex++;
        if(action == ACTION_PLAY_PREVIOUS) mCurrentSongIndex--;
        if( mCurrentSongIndex > mDataSource.getAllSongs(isFavorite()).size() - 1) mCurrentSongIndex = 0;
        if( mCurrentSongIndex < 0) mCurrentSongIndex = mDataSource.getAllSongs(isFavorite()).size() - 1;

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
     * Get a random index of the song according to the play mode.
     */
    public void randomSongIndex(){
        java.util.Random r = new java.util.Random();
        boolean isAllPlayedInARow = true;


        int randomSongIndex = r.nextInt(mDataSource.getAllSongs( isFavorite() ).size());
        if (mDataSource.getSongAtPosition( isFavorite(), randomSongIndex).getmPlayedTime() == 0){
            mCurrentSongIndex = randomSongIndex;
            mDataSource.getSongAtPosition( isFavorite(), randomSongIndex).setmPlayedTime(1);
            isAllPlayedInARow = false;
        }
        else if (mDataSource.getSongAtPosition( isFavorite(), randomSongIndex).getmPlayedTime() > 0){
            for(int i = 0;i < mDataSource.getAllSongs( isFavorite()).size();i++){
                if(mDataSource.getSongAtPosition( isFavorite(), i).getmPlayedTime() == 0){
                    mCurrentSongIndex = i;
                    isAllPlayedInARow = false;
                }
            }
            if(isAllPlayedInARow){
                for (int i = 0;i < mDataSource.getAllSongs(isFavorite()).size();i++){
                    mDataSource.getSongAtPosition(isFavorite(), i).setmPlayedTime(0);
                }
            }
        }
        mCurrentSongIndex = r.nextInt(mDataSource.getAllSongs(isFavorite()).size());

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
        if(isFavorite()){
            Log.d("===playAtPosition===", mDataSource.getAllSongs().size()+" pos:" + position);
        }
        else {
            Log.d("====playAtPosition===", mDataSource.getFavoriteSongs().size()+ "pos: " +
                    position);
        }
        setCurrentSongIndex(position);
        if( mPlayFlag ) play();
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
            if(mPlayFlag) remoteViews.setInt(R.id.widget_play_btn, "setBackgroundResource",
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

    private void setReplayFlag(boolean mReplayFlag) {
        this.mReplayFlag = mReplayFlag;
    }

    /**
     * Change the replayflag.
     */
    private void changeReplayFlag(){
        if(!mReplayFlag) {
            mReplayFlag = true;
        }
        else
            mReplayFlag = false;
    }


    /**
     *  Return the replayflag.
     */
    private boolean getReplayflag(){
        return mReplayFlag;
    }

//    private void registerScreenBroadcastReceiver() {
//        mLockScreenBroadcastReceiver = new LockScreenBroadcastReceiver();
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);//当屏幕锁屏的时候触发
//        intentFilter.addAction(Intent.ACTION_SCREEN_ON);//当屏幕解锁的时候触发
//        intentFilter.addAction(Intent.ACTION_USER_PRESENT);//当用户重新唤醒手持设备时触发
//        getApplicationContext().registerReceiver(mLockScreenBroadcastReceiver, intentFilter);
//        Log.i("screenBR", "screenBroadcastReceiver注册了");
//    }

    public Integer getPlayMode() {
        return mPlayMode;
    }

    public void setPlayMode(Integer playMode) {
        this.mPlayMode = playMode;
    }

    /**
     * Set the play mode to normal or favorite mode.
     * @param isFavorite
     */
    public void setPlayMode(boolean isFavorite){
        //If the user wants it to be favorite mode.
        if( isFavorite ){
            // If now it is not favorite mode, change it..
            if(!isFavorite()){
                if( isRandom() ) mPlayMode = FAV_RANDOM;
                else mPlayMode = FAV_SEQUENCE;
            }
        }
        else {
            // If now it is favorite mode, change it to normal.
            if(isFavorite){
                if(isRandom()) mPlayMode = NORMAL_RANDOM;
                else mPlayMode = NORMAL_SEQUENCE;
            }
        }

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
            return mPlayFlag;
        }

        @Override
        public void callPlayNextSong(){
            playNextSong();
        }

        @Override
        public void callPlaySongAtPosition(int position) { playSongAtPosition(position);}

        @Override
        public SongItem getPlayingSong() { return getCurrentPlayingSong();}

        @Override
        public Integer getPlayingSongIndex() { return getCurrentPlayingSongIndex();}

        @Override
        public SongItem getNextSong() { return getNextPlayingSong();}

        @Override
        public SongItem getPreviousSong() { return getPreviousPlayingSong();}

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
        public void callChangeReplayFlag(boolean replayFlag) {
            MusicService.this.setReplayFlag(replayFlag);
        }

        @Override
        public boolean callGetReplayFlag(){ return getReplayflag();}

        @Override
        public Integer getPlayMode() {
            return MusicService.this.getPlayMode();
        }

        @Override
        public void setPlayMode(Integer playMode) {
            MusicService.this.setPlayMode(playMode);
        }

        @Override
        public void setPlayMode(Integer isFavorite, Integer playMode) {
            MusicService.this.setPlayMode( isFavorite == FAV_MODE );
        }
    }


}
