package net.classicgarage.truerandommusicplayer.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by tomat on 2017-05-17.
 */


public class SongDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "favorite_songs";
    private static int DATABASE_VERSION = 1;

    public static final String TABLE_SONGS = "favorite_songs";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_FAVORITE = "is_favorite";

    public static final String TABLE_CREATE =
            "CREATE TABLE" + TABLE_SONGS + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_FAVORITE + "BOOLEAN, " +
                    ")";

    public SongDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null,DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
