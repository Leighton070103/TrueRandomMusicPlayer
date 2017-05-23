package net.classicgarage.truerandommusicplayer.db;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.widget.LinearLayout;

import net.classicgarage.truerandommusicplayer.model.SongItem;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Tong on 2017/5/16.
 */

public class SongDataSource {

    private static SongDataSource sInstance;
    private LinkedList<SongItem> mSongs = null;
    private Context mContext;
    private SongDatabaseHelper favoriteHelper;

    private SongDataSource(Context applicationContext){
        mContext = applicationContext;
        favoriteHelper = SongDatabaseHelper.getInstance(mContext);
//        getPermissons(activity);
    }

    public static synchronized SongDataSource getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.

        if ( sInstance == null ) {
            sInstance = new SongDataSource(context.getApplicationContext());
        }
        return sInstance;
    }

//    private void getPermissons(Activity activity) {
//        int code = ActivityCompat.checkSelfPermission(
//                activity,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        if (code != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
//        }
//     }


    public LinkedList<SongItem> getSongsFromSD(){
        if( mSongs != null) return mSongs;
        initializeSongs();
        return mSongs;

    }

    private void initializeSongs(){
        mSongs = new LinkedList<SongItem>();
        ContentResolver cr = mContext.getContentResolver();
        if(cr != null){
            Cursor cursor = cr.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null,
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
            cursor.close();
        }

    }

    // get song item from specific position
    public SongItem getSongAtPosition(int position) {
        return  mSongs.get(position);
    }

    // change song item's favorite statu at certain position
    public void setSongItemFavStatuAtPosition(Boolean statu, int position) {
        mSongs.get(position).setFavorite(statu);
    }


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
