package net.classicgarage.truerandommusicplayer.service;

import net.classicgarage.truerandommusicplayer.model.SongItem;

/**
 * Created by Tong on 2017/5/17.
 */

public interface BaseService {
    void callPlay();
    void callStop();
    void callPause();
    void callContinueMusic();
    void callSeekTo(int position);
    void callPlaySongAtPosition(int position);
    boolean isPlaying();
    void callPlayNextSong();
    SongItem getPlayingSong();
    int callGetCurrentPosition();
    int callGetDuration();
}
