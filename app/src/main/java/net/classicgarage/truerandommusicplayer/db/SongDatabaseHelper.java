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

    public LinkedList<SongFavItem> updateFavoriteForSongs(LinkedList<SongItem> songs){
        SQLiteDatabase db = getWritableDatabase();
        LinkedList<SongFavItem> songFavItems = new LinkedList<SongFavItem>();
        Cursor cursor = db.query(
                TABLE_SONGS, ALL_COLUMNS, null, null, null, null, null);

        if( cursor.getCount() != songs.size() ){
            if( cursor.moveToFirst() ){
                do{
                    songFavItems.add( readSongFav(cursor) );
                }while (cursor.moveToNext());
            }
            for( SongItem:)
        }


    }

    public SongFavItem findSongById(Long id, LinkedList<SongFavItem> favItems){
        for( SongFavItem item: favItems){
            if( id == item.getSongId() ) return item;
        }
        return null;
    }

    public void addFavoriteSong(SongItem song) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FAVORITE, song.getFavorite());
        values.put(COLUMN_MUSIC_ID, song.getId());
        db.insert(TABLE_SONGS, null, values);
        // contentValues.put(SongDatabaseHelper.COLUMN_FAVORITE, );
        // TODO: insert favorite song to database
    }
    public void deleteFavoriteSong(String n) {
        // TODO: delete favorite song from database
    }

    public Cursor getAllFavoriteData(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_SONGS, ALL_COLUMNS, null, null, null, null, null);
        return cursor;

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

    private class SongFavItem{
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
    }
}