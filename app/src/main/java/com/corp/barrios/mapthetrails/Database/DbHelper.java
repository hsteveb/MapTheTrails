package com.corp.barrios.mapthetrails.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by hector on 3/1/17.
 */

public class DbHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "mapthetrails.db";


    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + DbSchema.DbEntry.TABLE + " (" +
                DbSchema.DbEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DbSchema.DbEntry.UUID + ", " +
                DbSchema.DbEntry.TRAIL +
        ")");

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


}
