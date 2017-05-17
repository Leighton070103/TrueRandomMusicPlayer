package net.classicgarage.truerandommusicplayer.helper;

import net.classicgarage.truerandommusicplayer.model.SongItem;

import java.util.ArrayList;

/**
 * This class provides data for app testing
 * Created by tomat on 2017-05-17.
 *
 */

public class TestSongTagHelper {
    ArrayList<SongItem> mTestSongs;

    public TestSongTagHelper(){
        addSongs();
    }

    public void addSongs(){
        SongItem songItem;
        songItem= new SongItem("Favorite 1", true);
        mTestSongs.add(new SongItem("You Are We", false));
        mTestSongs.add(new SongItem("Favorite 2", true));
        mTestSongs.add(new SongItem("The Void", false));
        mTestSongs.add(new SongItem("Favorite 3", true));
        mTestSongs.add(new SongItem("Adrenalize", false));
        mTestSongs.add(new SongItem("Favorite 4", true));
        mTestSongs.add(new SongItem("A Boy Brushed Red Living", false));
        mTestSongs.add(new SongItem("Favorite 5", true));
        mTestSongs.add(new SongItem("Violence", false));
    }

    public ArrayList<SongItem> getAllTestSongs(){
        return mTestSongs;
    }

    public ArrayList<SongItem> getFavoriteTestSongs() {
        ArrayList<SongItem> favoriteSongs = null;
        for (int i = 0; i < mTestSongs.size();i++){
            if(mTestSongs.get(i).getFavorite()){
                favoriteSongs.add(mTestSongs.get(i));
            }
        }
        return favoriteSongs;
    }
}
