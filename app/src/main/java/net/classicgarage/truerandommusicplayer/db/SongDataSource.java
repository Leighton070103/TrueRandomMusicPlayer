package net.classicgarage.truerandommusicplayer.db;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import net.classicgarage.truerandommusicplayer.model.SongItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tong on 2017/5/16.
 */

public class SongDataSource {

    private ArrayList<SongItem> mSongs = null;
    private Context mContext;

    public SongDataSource(Context context){
        mContext = context;
    }

    public ArrayList<SongItem> getSongsFromSD(){
        if( mSongs != null) return mSongs;
        initializeSongs();
        return mSongs;

    }

    private void initializeSongs(){
        mSongs = new ArrayList<SongItem>();
        ContentResolver cr = mContext.getContentResolver();
        if(cr != null){
            Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null,
                    null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            if(null == cursor){
                mSongs = null;
            }
            if(cursor.moveToFirst()){
                do{
                    SongItem song = readSong(cursor);
                    if( song != null ) mSongs.add(song);
                }while(cursor.moveToNext());
            }
        }

    }

    private SongItem readSong(Cursor cursor){
        SongItem song = new SongItem();
        song.setTitle(cursor.getString(cursor
                .getColumnIndex(MediaStore.Audio.Media.TITLE)));

        String song_name = cursor
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

        String name = cursor
                .getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
        String sbr = name.substring(name.length() - 3,name.length());
        if(sbr.equals("mp3")){
            return song;
        }
        return null;


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
