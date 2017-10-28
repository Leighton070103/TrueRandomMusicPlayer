package net.classicgarage.truerandommusicplayer.db;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import net.classicgarage.truerandommusicplayer.model.SongItem;

import java.io.File;
import java.util.LinkedList;

/**
 * Created by Tong on 2017/5/16.
 * This class is to read from the android internal storage to provide data source for this music
 * player.
 */

public class SongDataSource {

    private static SongDataSource sInstance;
    private LinkedList<SongItem> mSongs = null;
    private LinkedList<SongItem> mFavSongs = null;
    private Context mContext;
    private FavLabelDatabaseHelper favoriteHelper;

    /**
     * The constructor.
     * @param applicationContext
     */
    public SongDataSource(Context applicationContext){
        mContext = applicationContext;
        favoriteHelper = FavLabelDatabaseHelper.getInstance(mContext);
    }

    /**
     * Return an instance of this datasource.
     * @param context
     * @return
     */
    public static synchronized SongDataSource getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        if ( sInstance == null ) {
            sInstance = new SongDataSource(context.getApplicationContext());
        }
        return sInstance;
    }

    public LinkedList<SongItem> getAllSongs(){
        if( mSongs != null) return mSongs;
        initializeSongs();
        return mSongs;
    }

    public LinkedList<SongItem> getAllSongs(boolean isFavorite){
        if(isFavorite) return getFavoriteSongs();
        return getAllSongs();
    }

    public SongItem getNextSong(int currentPosition){
        if( mSongs != null) return getSongAtPosition(currentPosition + 1);
        else {
            initializeSongs();
            return null;
        }
    }

    public SongItem getPreviousSong(int currentPosition){
        if (mSongs != null && currentPosition > 0) return getSongAtPosition(currentPosition - 1);
        else {
            initializeSongs();
            return null;
        }
    }

    public void setSongFavorite(long songId){
        SongItem song = findSongItemById(songId);
        if( song != null ) {
            song.resetIsFavorite();
        }
        getFavoriteSongs();
        if(song.getFavorite()){
            mFavSongs.add(song);
            favoriteHelper.labelFavSongInDb(songId);
        }
        else {
            mFavSongs.remove(song);
            favoriteHelper.deleteSongFav(songId);
        }
    }

    public LinkedList<SongItem> getFavoriteSongs(){
        if( mFavSongs == null) {
            mFavSongs = new LinkedList<SongItem>();
            for(SongItem song: getAllSongs()){
                if(song.getFavorite() && !(mFavSongs.contains(song))) mFavSongs.add(song);
            }
        }
        return mFavSongs;
    }

    /**
     * Return the song item by its id.
     * @param songId
     * @return
     */
    public SongItem findSongItemById(long songId){
        if( mSongs == null ) getAllSongs(); //new LinkedList<SongItem>();
        for(SongItem song: mSongs){
            if( song.getId() == songId ) return song;
        }
        return null;
    }

    /**
     * Return the item index by id.
     * @param songId
     * @return
     */
    public int findSongIndexById(long songId){
        if( mSongs == null ) mSongs = new LinkedList<SongItem>();
        for( int i = 0; i< mSongs.size(); i++){
            if( mSongs.get(i).getId() == songId ) return i;
        }
        return 0;
    }

    /**
     * Initialize the mSongs.
     * Form the music data from the internal database into song item model.
     */
    private void initializeSongs(){
        mSongs = new LinkedList<SongItem>();
        ContentResolver cr = mContext.getContentResolver();
        if(cr != null) {
                Cursor cursor = cr.query(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null,
                        null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
                if(cursor == null){
                    cursor = cr.query(
                            MediaStore.Audio.Media.INTERNAL_CONTENT_URI, null, null,
                            null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
                }
                if (cursor == null) {
                    mSongs = null;
                } else if (cursor.moveToFirst()) {
                    do {
                        SongItem song = readSong(cursor);
                        cursor.getPosition();
                        if (song != null) mSongs.add(song);
                    } while (cursor.moveToNext());
                }
                if (cursor != null) {
                    cursor.close();
                }
        }
        mSongs = favoriteHelper.updateFavoriteForSongs(mSongs);
    }


    /**
     * Get song item from the mSongList by a specific position.
     */
    public SongItem getSongAtPosition(int position) {
        if( mSongs != null) return  mSongs.get(position);
        initializeSongs();
        return mSongs.get(position);
    }

    /**
     * Get song item in favorite list or all song list.
     * @param isFavorite
     * @param position
     * @return
     */
    public SongItem getSongAtPosition(Boolean isFavorite, int position){
        if( isFavorite ) return getFavoriteSongs().get( position );
        else return getSongAtPosition( position );
    }


    /**
     * Form the data into song item model.
     * @param cursor
     * @return
     */
    @Nullable
    private SongItem readSong(Cursor cursor){
        SongItem song = new SongItem();
        song.setTitle(cursor.getString(cursor
                .getColumnIndex(MediaStore.Audio.Media.TITLE)));

        song.setId( cursor.getLong(cursor
                .getColumnIndex(MediaStore.Audio.Media._ID)));

        song.setPath( cursor.getString(cursor
                .getColumnIndex(MediaStore.Audio.Media.DATA)));

        song.setAlbum( cursor.getString(cursor
                .getColumnIndex(MediaStore.Audio.Media.ALBUM)));
        song.setAlbumId( cursor.getLong(cursor
                .getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));

        song.setArtist( cursor.getString(cursor
                .getColumnIndex(MediaStore.Audio.Media.ARTIST)));
        song.setDuration(cursor.getLong(cursor
                .getColumnIndex(MediaStore.Audio.Media.DURATION)));

        String name = cursor
                .getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
        String sbr = name.substring(name.length() - 3,name.length());
        if(sbr.equals("mp3")){
            return song;
        }
        return null;
    }


    /**
     * Delete a song.
     * @param songId
     */
    public void deleteSong(long songId){
        for (int i = 0; i < mSongs.size();i++){
            if(mSongs.get(i).getId() == songId){
                File f = new File(mSongs.get(i).getPath());
                f.delete();
                SongItem song = mSongs.get(i);
                deletePlaylistTracks(mContext, song);

                if(song.getFavorite()){
                    mFavSongs.remove(song);
                    favoriteHelper.deleteSongFav(songId);
                }
                mSongs.remove(i);
            }
        }
    }

    /**
     * Delete a song according to its index in the list.
     * @param mCurrentSongIndex
     */
    public void deleteSongInIndex(int mCurrentSongIndex) {
        deleteSong(mSongs.get(mCurrentSongIndex).getId());
    }

    /**
     * Delete playlist tracks.
     * @param context
     * @param song
     * @return
     */
    private int deletePlaylistTracks(Context context, SongItem song){
        ContentResolver resolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String where = MediaStore.Audio.Playlists.Members._ID + "=?";
        String[] whereArgs = new String[] {Long.toString(song.getId())};
        int rowsDeleted = resolver.delete(uri,where,whereArgs);
        Log.d("TAG", "tracks deleted=" + rowsDeleted);
        return rowsDeleted;
    }
}
