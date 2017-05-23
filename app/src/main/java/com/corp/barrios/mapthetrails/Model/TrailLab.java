package com.corp.barrios.mapthetrails.Model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.corp.barrios.mapthetrails.Database.DbCursorWrapper;
import com.corp.barrios.mapthetrails.Database.DbHelper;
import com.corp.barrios.mapthetrails.Database.DbSchema;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hector on 3/1/17.
 */

public class TrailLab {

    private static final String TAG = "TrailLab";

    private static TrailLab sTrailLab;
    private Context mContext;
    private SQLiteDatabase mDatabase;
    private DbHelper mDbHelper;

    public static TrailLab get(Context context)
    {
        if(sTrailLab == null)
        {
            sTrailLab = new TrailLab(context);
        }
        return sTrailLab;
    }

    private TrailLab(Context context)
    {
        mContext = context;
        mDbHelper = new DbHelper(mContext);
        mDatabase = mDbHelper.getWritableDatabase();
    }

    private DbCursorWrapper queryTrails(String whereClause, String[] whereArgs)
    {

        Cursor cursor = mDatabase.query(DbSchema.DbEntry.TABLE, null, whereClause, whereArgs, null, null, null);

        return new DbCursorWrapper(cursor);
    }

    private ContentValues getContentValues(Trail trail)
    {
        Gson gson = new Gson();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbSchema.DbEntry.UUID, trail.getUUID().toString());
        contentValues.put(DbSchema.DbEntry.TRAIL, gson.toJson(trail.getList()));

        return contentValues;
    }

    public List<Trail> getLists() {
        List<Trail> list = new ArrayList<>();

            DbCursorWrapper cursor = queryTrails(null, null);

            try
            {
                cursor.moveToFirst();
                while(!cursor.isAfterLast())
                {
                    list.add(cursor.getTrail());
                    cursor.moveToNext();
                }
            } finally
            {
                cursor.close();
            }

        return list;
    }

    public void addTrail(Trail trail)
    {
        mDatabase.insert(DbSchema.DbEntry.TABLE, null, getContentValues(trail));
    }

    public void deleteTrail(Trail trail)
    {

        String args[] = { trail.getUUID().toString() };

        int value = mDatabase.delete(DbSchema.DbEntry.TABLE, DbSchema.DbEntry.UUID + " = ?", args);
        Log.d(TAG, value + "");
    }

    public void closeSqlTable()
    {
        mDbHelper.close();
    }
}
