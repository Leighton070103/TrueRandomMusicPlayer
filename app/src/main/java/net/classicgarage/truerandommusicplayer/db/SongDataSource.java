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
    private SongDatabaseHelper favoriteHelper;

    /**
     * The constructor.
     * @param applicationContext
     */
    private SongDataSource(Context applicationContext){
        mContext = applicationContext;
        favoriteHelper = SongDatabaseHelper.getInstance(mContext);
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

    public void setSongFavorite(long songId){
        SongItem song = findSongItemById(songId);
        if( song != null ) {
            song.resetIsFavorite();
            favoriteHelper.updateFavoriteSong(songId);
        }
        getFavoriteSongs();
        if(song.getFavorite()){
            mFavSongs.add(song);
        }
        else {
            mFavSongs.remove(song);
        }
    }

    public LinkedList<SongItem> getFavoriteSongs(){
        if( mFavSongs == null) {
            mFavSongs = new LinkedList<SongItem>();
            for(SongItem song: getAllSongs()){
                if(song.getFavorite()) mFavSongs.add(song);
            }
        }
        return mFavSongs;
    }

    public SongItem findSongItemById(long songId){
        if( mSongs == null ) mSongs = new LinkedList<SongItem>();
        for(SongItem song: mSongs){
            if( song.getId() == songId ) return song;
        }
        return null;
    }

    public int findSongIndexById(long songId){
        if( mSongs == null ) mSongs = new LinkedList<SongItem>();
        for( int i = 0; i< mSongs.size(); i++){
            if( mSongs.get(i).getId() == songId ) return i;
        }
        return 0;
    }

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
     *get song item from a specific position.
     */
    public SongItem getSongAtPosition(int position) {
        if( mSongs != null) return  mSongs.get(position);
        initializeSongs();
        return mSongs.get(position);
    }

    // change song item's favorite statu at certain position
    public void setSongItemFavStatuAtPosition(Boolean statu, int position) {
        mSongs.get(position).setFavorite(statu);
    }

    @Nullable
    private SongItem readSong(Cursor cursor){
        SongItem song = new SongItem();
        song.setTitle(cursor.getString(cursor
                .getColumnIndex(MediaStore.Audio.Media.TITLE)));

        String song_name = cursor// whrrtszt
                .getString( cursor
                        .getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
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

    public SongItem getSongWithPath(String path){
        for( SongItem song: mSongs ){
            if(song.getPath().equals(path))
                return song;
        }
        return null;
    }

    public void deleteSong(long songId){
        for (int i = 0; i < mSongs.size();i++){
            if(mSongs.get(i).getId() == songId){
                File f = new File(mSongs.get(i).getPath());
                f.delete();
                deletePlaylistTracks(mContext,mSongs.get(i));
                favoriteHelper.deleteSongFav(songId);
                mSongs.remove(i);
            }
        }
    }

    public void deleteSongInIndex(int mCurrentSongIndex) {
        deleteSong(mSongs.get(mCurrentSongIndex).getId());
    }

    private int deletePlaylistTracks(Context context, SongItem song){
        ContentResolver resolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String where = MediaStore.Audio.Playlists.Members._ID + "=?";
        String[] whereArgs = new String[] {Long.toString(song.getId())};
        int rowsDeleted = resolver.delete(uri,where,whereArgs);
        Log.d("TAG", "tracks deleted=" + rowsDeleted);
        return rowsDeleted;
    }
//    public List<String> getMusicData(Context context){
//        List<String> list = new ArrayList<String>();
//        ContentResolver cr = context.getContentResolver();
//        if(cr != null){
//            Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null,
//                    null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
//            if(null == cursor){
//                return null;
//            }
//            if(cursor.moveToFirst()){
//                do{
//                    String title = cursor.getString(cursor
//                            .getColumnIndex(MediaStore.Audio.Media.TITLE));
//                    String name = cursor
//                            .getString(cursor
//                                    .getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
//                    String sbr = name.substring(name.length() - 3,name.length());
//                    if(sbr.equals("mp3")){
//                        list.add(title);
//                    }
//                }while(cursor.moveToNext());
//            }
//        }
//        return list;
//    }


//    public void getAllSongsFromSDCARD() {
//        String[] STAR = { "*" };
//        Cursor cursor;
//        Uri allsongsuri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
//
//        cursor = managedQuery(allsongsuri, STAR, selection, null, null);
//
//        if (cursor != null) {
//            if (cursor.moveToFirst()) {
//                do {
//                    String song_name = cursor
//                            .getString(cursor
//                                    .getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
//                    int song_id = cursor.getInt(cursor
//                            .getColumnIndex(MediaStore.Audio.Media._ID));
//
//                    String fullpath = cursor.getString(cursor
//                            .getColumnIndex(MediaStore.Audio.Media.DATA));
//
//                    String album_name = cursor.getString(cursor
//                            .getColumnIndex(MediaStore.Audio.Media.ALBUM));
//                    int album_id = cursor.getInt(cursor
//                            .getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
//
//                    String artist_name = cursor.getString(cursor
//                            .getColumnIndex(MediaStore.Audio.Media.ARTIST));
//                    int artist_id = cursor.getInt(cursor
//                            .getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
//                    System.out.println("sonng name"+fullpath);
//                } while (cursor.moveToNext());
//
//            }
//            cursor.close();
//        }


}
