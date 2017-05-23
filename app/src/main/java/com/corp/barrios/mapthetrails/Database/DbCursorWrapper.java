package com.corp.barrios.mapthetrails.Database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.corp.barrios.mapthetrails.Model.Trail;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.UUID;

/**
 * Created by hector on 3/1/17.
 */

public class DbCursorWrapper extends CursorWrapper {

    public DbCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Trail getTrail()
    {
        UUID uuid = UUID.fromString(getString(getColumnIndex(DbSchema.DbEntry.UUID)));
        Gson gson = new Gson();
        List<LatLng> trail = gson.fromJson(getString(getColumnIndex(DbSchema.DbEntry.TRAIL)), new TypeToken<List<LatLng>>(){}.getType());

        return new Trail(uuid, trail);
    }
}
