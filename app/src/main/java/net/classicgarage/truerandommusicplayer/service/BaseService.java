package net.classicgarage.truerandommusicplayer.service;

import android.app.Service;

import net.classicgarage.truerandommusicplayer.model.SongItem;

/**
 * Created by Tong on 2017/5/17.
 */

public interface BaseService{
    void callPlay();
    void callStop();
    void callPause();
    void callContinueMusic();
    void callSeekTo(int position);
    void callPlaySongAtPosition(int position);
    boolean isPlaying();
    void callPlayNextSong();
    void callPlayLastSong();
    Integer getPlayingSongIndex();
    SongItem getPlayingSong();
    SongItem getNextSong();
    SongItem getPreviousSong();
    void deleteCurrentSong();
    void setCurrentSongFavorite();
    void callChangeReplayFlag();
    void callChangeReplayFlag(boolean replayFlag);
    boolean callGetReplayFlag();
    Integer getPlayMode();
    void setPlayMode( Integer playMode);
    void setPlayMode( Integer isFavorite, Integer playMode);
}
