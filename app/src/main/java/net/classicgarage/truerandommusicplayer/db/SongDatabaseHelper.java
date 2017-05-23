package net.classicgarage.truerandommusicplayer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.ImageButton;

import net.classicgarage.truerandommusicplayer.model.SongItem;

import java.util.HashMap;
import java.util.LinkedList;


/**
 * Created by tomat on 2017-05-17.
 */


public class SongDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "favorite_songs";
    private static int DATABASE_VERSION = 1;
    private static SongDatabaseHelper sInstance;

    private LinkedList<SongFavItem> mSongFavItems = null;
    public static final String TABLE_SONGS = "favorite_songs";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FAVORITE = "is_favorite";
    public static final String COLUMN_MUSIC_ID = "music_id";

    public static final String[] ALL_COLUMNS={COLUMN_ID, COLUMN_FAVORITE, COLUMN_FAVORITE};
    public static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_SONGS + "( " +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_FAVORITE + "INTEGER, " + COLUMN_MUSIC_ID + "LONG "
                    + ")";

    public SongDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null,DATABASE_VERSION);

    }

    public static synchronized SongDatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.

        if ( sInstance == null ) {
            sInstance = new SongDatabaseHelper(context);
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public LinkedList<SongItem> updateFavoriteForSongs(LinkedList<SongItem> songs){
        SQLiteDatabase db = getWritableDatabase();
        //Get favorite record from the database.
        LinkedList<SongFavItem> songFavItems = new LinkedList<SongFavItem>();
        Cursor cursor = db.query(
                TABLE_SONGS, ALL_COLUMNS, null, null, null, null, null);

        getAllFavoriteData();
        if( mSongFavItems == null || songFavItems.size() == 0){
            for ( SongItem song: songs ){
                addSongFav(song);
            }
        }
        else {
            for( SongItem song: songs){
                SongFavItem songFavItem = getSongFavBySongId( song.getId(), songFavItems);
                if( songFavItem != null){
                    song.setFavorite( songFavItem.getIsFavorite() );
                }
                else{
                    addSongFav(song);
                }
            }
        }

        return songs;
    }



    public SongFavItem getSongFavBySongId(Long id, LinkedList<SongFavItem> favItems){
        for( SongFavItem item: favItems){
            if( id == item.getSongId() ) return item;
        }
        return null;
    }

    public void addSongFav(SongItem song) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        if(song.getFavorite()) values.put(COLUMN_FAVORITE, 1);
        else values.put(COLUMN_FAVORITE, 0);
        values.put(COLUMN_MUSIC_ID, song.getId());
        db.insert(TABLE_SONGS, null, values);

        SongFavItem item = new SongFavItem();
        item.setSongId(song.getId());
        item.setIsFavorite(song.getFavorite());
        if( mSongFavItems == null) mSongFavItems = new LinkedList<SongFavItem>();
        mSongFavItems.add(item);
    }
    public void deleteFavoriteSong(String n) {
        // TODO: delete favorite song from database
    }

    public LinkedList<SongFavItem> getAllFavoriteData(){
        if( mSongFavItems != null) return mSongFavItems;
        mSongFavItems = new LinkedList<SongFavItem>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_SONGS, ALL_COLUMNS, null, null, null, null, null);
        if( cursor.moveToFirst() ){
            do{
                mSongFavItems.add( readSongFav(cursor) );
            }while (cursor.moveToNext());
        }
        cursor.close();
        return mSongFavItems;

    }


    private SongFavItem readSongFav(Cursor cursor){
        SongFavItem songFavItem = new SongFavItem();
        songFavItem.setFavorite(cursor.getInt(cursor.getColumnIndex( COLUMN_FAVORITE )));
        songFavItem.setSongId( cursor.getLong(cursor.getColumnIndex( COLUMN_MUSIC_ID)) );
        return songFavItem;
    }

    public void updateFavoriteSong(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(SongDatabaseHelper.COLUMN_FAVORITE, "is_favorite");

        String selection = SongDatabaseHelper.COLUMN_FAVORITE + "like ?";
        //String[] selectionArgs = {""}

        /*int count = db.update(
                TABLE_SONGS,contentValues
        );*/
    }

    class SongFavItem{
        private Long mId;
        private Integer mFavorite;
        private Long mSongId;


        public Long getId() {
            return mId;
        }

        public void setId(Long id) {
            this.mId = id;
        }

        public Integer getFavorite() {
            return mFavorite;
        }

        public void setFavorite(Integer favorite) {
            this.mFavorite = favorite;
        }

        public Long getSongId() {
            return mSongId;
        }

        public void setSongId(Long songId) {
            this.mSongId = songId;
        }

        public void setIsFavorite(boolean isFavorite){
            if( isFavorite) mFavorite = 1;
            else mFavorite = 0;
        }

        public boolean getIsFavorite(){
            return mFavorite == 1;
        }
    }
}