package net.classicgarage.truerandommusicplayer.service;

/**
 * Created by Tong on 2017/5/17.
 */

public interface BaseService {
    void callPlay();
    void callStop();
    void callPause();
    void callContinueMusic();
    void callSeekTo(int position);
    boolean isPlaying();
    void callPlayNextSong();
    void callPlaySongAtPosition(int position);
}
