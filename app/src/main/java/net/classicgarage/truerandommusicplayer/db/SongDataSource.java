package net.classicgarage.truerandommusicplayer.db;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;

import net.classicgarage.truerandommusicplayer.activity.SongListActivity;
import net.classicgarage.truerandommusicplayer.model.SongItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tong on 2017/5/16.
 */

public class SongDataSource {

    private ArrayList<SongItem> mSongs = null;
    private Context mContext;

    public SongDataSource(Context applicationContext, Activity activity){
        mContext = applicationContext;
        getPermissons(activity);
    }

    private void getPermissons(Activity activity) {
        int code = ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (code != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
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
