package net.classicgarage.truerandommusicplayer.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by tomat on 2017-05-17.
 */


public class SongDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "songs";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_SONGS = "songs";
    public static final String COLUM_ID = "id";
    public static final String COLUM_

    public SongDatabaseHelper(Context context) {
        super(context, name, factory, );

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
