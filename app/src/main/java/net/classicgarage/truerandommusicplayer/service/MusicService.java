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
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class MusicService extends Service {
    private MediaPlayer mediaPlayer;
    private SongDataSource mDataSource;
    private Timer timer = null;
    private TimerTask task = null;
    private int currentSongIndex = 0;

    public MusicService() {
    }

    @Override
    public void onCreate() {
        //初始化mediaplayer
        mediaPlayer = new MediaPlayer();
        mDataSource = SongDataSource.getInstance(this.getApplicationContext());
//        try {
//            mediaPlayer.setDataSource(mDataSource.getSongsFromSD().get(0).getPath());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                task.cancel();
                timer.cancel();
            }
        });
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource( getSongFromList().getPath() );
            Log.d("======play=====", mDataSource.getSongsFromSD().toString());
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MusicBinder();
    }


    private void play() {
//        try {
//            mediaPlayer.reset();
//            mediaPlayer.setDataSource( getSongFromList().getPath() );
//            Log.d("======play=====", mDataSource.getSongsFromSD().toString());
//            mediaPlayer.prepare();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        mediaPlayer.start();
        refereshSeekBar();
//        if(mediaPlayer.isPlaying()) mediaPlayer.pause();
//        else mediaPlayer.start();
    }

    private SongItem getSongFromList() {
        LinkedList<SongItem> songlist = mDataSource.getSongsFromSD();
        if(currentSongIndex >= songlist.size()){
            currentSongIndex = 0;
        }
        SongItem songItem = songlist.get(currentSongIndex);
        //return songItem;
        //return songItem;
        return songItem;
    }



    public void playNextSong(){
        currentSongIndex ++;
        play();
    }
    private void refereshSeekBar() {
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                int position = mediaPlayer.getCurrentPosition();
                int duration = mediaPlayer.getDuration();
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putInt("duration",duration);
                data.putInt("position",position);
                msg.setData(data);
                MainActivity.handler.sendMessage(msg);
            }
        };
        timer.schedule(task,100,1000);
    }

    public void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }
    private void stop(){
        mediaPlayer.stop();
    }
    private void continueMusic() {
        mediaPlayer.start();
    }

    private void pause() { mediaPlayer.pause(); }

    private SongItem getCurrentPlayingSong(){
        return getSongFromList();
    }

    private boolean isPlaying(){ return mediaPlayer.isPlaying(); }

    private void playSongAtPosition(int position) {
        Log.d("===playAtPosition===", mDataSource.getSongsFromSD().size()+" pos:"+position);
        try {
            // TODO: fix the crush when trying to get song at position
            mediaPlayer.reset();
            mediaPlayer.setDataSource(mDataSource.getSongAtPosition(position).getPath());
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
    }
    private int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }
    private int getDuration() {
        return mediaPlayer.getDuration();
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
            return mediaPlayer.isPlaying();
        }

        @Override
        public void callPlayNextSong() {} {
            playNextSong();
        }

        @Override
        public void callPlaySongAtPosition(int position) { playSongAtPosition(position);}


        @Override
        public SongItem getPlayingSong() {
            return getCurrentPlayingSong();
        }

        @Override
        public int callGetCurrentPosition() {return getCurrentPosition();}

        @Override
        public int callGetDuration() {return getDuration();}
    }
}
