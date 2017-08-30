package net.classicgarage.truerandommusicplayer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import net.classicgarage.truerandommusicplayer.model.SongItem;

import java.util.LinkedList;


/**
 * Created by tomat on 2017-05-17.
 * This class is to store and update the favorite label information in the database.
 */


public class FavLabelDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "favorite_songs";
    private static int DATABASE_VERSION = 1;
    private static FavLabelDatabaseHelper sInstance;

    public static final String TABLE_SONGS = "favorite_songs";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_MUSIC_ID = "music_id";

    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_MUSIC_ID};
    public static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_SONGS + "( " +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_MUSIC_ID + " LONG "
                    + ")";

    /**
     * The constructor.
     *
     * @param context
     */
    private FavLabelDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    /**
     * Return an instance of this helper.
     *
     * @param context
     * @return
     */
    public static synchronized FavLabelDatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.

        if (sInstance == null) {
            sInstance = new FavLabelDatabaseHelper(context);
        }
        return sInstance;
    }

    /**
     * Called when the database is first created.
     *
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    /**
     * Called when the database is updated.
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * For the list of songs, add the favorite label information according to records stored in the
     * database.
     * If there is no corresponding favorite record for a specific song, create one.
     *
     * @param songs
     * @return
     */
    public LinkedList<SongItem> updateFavoriteForSongs(LinkedList<SongItem> songs) {

        //Get favorite record from the database.
        if (songs == null) return songs;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_SONGS, ALL_COLUMNS, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndex(COLUMN_MUSIC_ID));
                for (SongItem song : songs) {
                    if (song.getId() == id) song.setFavorite(true);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return songs;
    }


    /**
     * Delete song favorite information
     *
     * @param songId
     */
    public void deleteSongFav(long songId) {
        SQLiteDatabase db = getWritableDatabase();
        String[] args = {String.valueOf(songId)};
        db.delete(TABLE_SONGS, COLUMN_MUSIC_ID + "=?", args);
        db.close();

    }


    /**
     * Label a specific song as favorite by its id.
     *
     * @param songId
     */
    public void labelFavSongInDb(long songId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FavLabelDatabaseHelper.COLUMN_MUSIC_ID, songId);
        db.insert(TABLE_SONGS, null, contentValues);
        db.close();
    }
}