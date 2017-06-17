package net.classicgarage.truerandommusicplayer.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import net.classicgarage.truerandommusicplayer.activity.MainActivity;
import net.classicgarage.truerandommusicplayer.db.SongDataSource;
import net.classicgarage.truerandommusicplayer.model.SongItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class MusicService extends Service {
    private MediaPlayer mMediaPlayer;
    private SongDataSource mDataSource;
    private Timer timer = null;
    private TimerTask task = null;
    private int mCurrentSongIndex = 0;
    public boolean replayFlag = false;
    public boolean randomFlag = false;

    public MusicService() {
    }

    @Override
    public void onCreate() {
        //初始化mediaplayer
        mMediaPlayer = new MediaPlayer();
        mDataSource = SongDataSource.getInstance(this.getApplicationContext());
//        try {
//            mediaPlayer.setDataSource(mDataSource.getSongsFromSD().get(0).getPath());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                task.cancel();
                timer.cancel();
            }
        });
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MusicBinder();
    }


    private void play() {
        try {
//            if(mMediaPlayer.isPlaying()){
//
//            }
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource( getSongFromListByIndex().getPath() );
            Log.d("======play=====", getSongFromListByIndex().toString());
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (IllegalStateException e){
            mMediaPlayer = null;
            mMediaPlayer = new MediaPlayer();
            e.printStackTrace();
        }

        mMediaPlayer.start();
        refereshSeekBar();
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playNextSong();
            }
        });
//        if(mediaPlayer.isPlaying()) mediaPlayer.pause();
//        else mediaPlayer.start();
    }

    private SongItem getSongFromListByIndex() {
//        LinkedList<SongItem> songlist = mDataSource.getSongsFromSD();
//        if(currentSongIndex >= songlist.size()){
//            currentSongIndex = 0;
//        }
        SongItem songItem = mDataSource.getSongsFromSD().get(mCurrentSongIndex);
        //return songItem;
        //return songItem;
        return songItem;
    }

    private void deleteCurrentPlayingSong(){
        try {
            mDataSource.deletSong(mCurrentSongIndex);
            play();
        }
        catch (java.lang.IndexOutOfBoundsException e){
            e.printStackTrace();
        }
    }



    public void playNextSong(){
        if(replayFlag){
            play();
            refereshSeekBar();
        }
        else if(randomFlag){
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
    public void playLastSong(){
        if(replayFlag){
            play();
            refereshSeekBar();
        }
        else if(randomFlag){
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
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                if(mMediaPlayer == null) return ;
                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        int position = mMediaPlayer.getCurrentPosition();
                        int duration = mMediaPlayer.getDuration();
                        Message msg = new Message();
                        Bundle data = new Bundle();
                        data.putInt("duration",duration);
                        data.putInt("position",position);
                        msg.setData(data);
                        MainActivity.handler.sendMessage(msg);
                    }
                });

            }
        };
        timer.schedule(task,100,1000);
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
    }

    public void randomSongIndex(){
        java.util.Random r=new java.util.Random();
        mCurrentSongIndex = r.nextInt(mDataSource.getSongsFromSD().size());
    }

    public void setCurrentSongIndex(int index){
        mCurrentSongIndex = index;
    }

    private boolean isPlaying(){ return mMediaPlayer.isPlaying(); }

    private void playSongAtPosition(int position) {
        Log.d("===playAtPosition===", mDataSource.getSongsFromSD().size()+" pos:"+position);
        setCurrentSongIndex(position);
        play();

    }

    private void setCurrentSongFavorite(){
        mDataSource.setSongFavorite(getCurrentPlayingSong().getId());
    }

    private void changeRandomFlag(){
        if(!randomFlag) {
            randomFlag = true;
        }
        else
            randomFlag = false;
    }

    private void changeReplayFlag(){
        if(!replayFlag) {
            replayFlag = true;
        }
        else
            replayFlag = false;
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
        public void deleteCurrentSong() {
            deleteCurrentPlayingSong();
        }

        @Override
        public void setCurrentSongFavorite() {
            MusicService.this.setCurrentSongFavorite();
        }

        @Override
        public void callPlayLastSong(){playLastSong();}

        @Override
        public void callChangeReplayFlag(){changeReplayFlag();}

        @Override
        public void callChangeRandomFlag(){changeRandomFlag();}
    }
}
