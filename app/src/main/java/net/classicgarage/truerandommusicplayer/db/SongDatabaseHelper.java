package net.classicgarage.truerandommusicplayer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import net.classicgarage.truerandommusicplayer.model.SongItem;


/**
 * Created by tomat on 2017-05-17.
 */


public class SongDatabaseHelper extends SQLiteOpenHelper {

    SQLiteDatabase db = this.getWritableDatabase();

    private static final String DATABASE_NAME = "favorite_songs";
    private static int DATABASE_VERSION = 1;
    private static SongDatabaseHelper sInstance;

    public static final String TABLE_SONGS = "favorite_songs";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SONG_NAME = "song_name";
    public static final String COLUMN_FAVORITE = "is_favorite";
    public static final String COLUMN_MUSIC_ID = "music_id";

    public static final String[] ALL_COLUMNS={COLUMN_ID, COLUMN_FAVORITE, COLUMN_FAVORITE};
    public static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_SONGS + " ( " +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_SONG_NAME + " STRING, " +
                    COLUMN_FAVORITE + " BOOLEAN, " + COLUMN_MUSIC_ID + " LONG "
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

    public void addFavoriteSong(SongItem favItem) {

        ContentValues contentValues = new ContentValues();
        // TODO: insert favorite song to database
    }

    public void deleteSong(SongItem delItem) {

        String songName = delItem.getTitle();
        String selection = COLUMN_SONG_NAME + " LIKE ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = { songName };
        // Issue SQL statement.
        db.delete(TABLE_SONGS, selection, selectionArgs);
    }

    public Cursor getAllFavoriteData(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_SONGS, ALL_COLUMNS, null, null, null, null, null);
        return cursor;
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

}