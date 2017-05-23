package com.corp.barrios.mapthetrails.Model;


import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.UUID;

/**
 * Created by hector on 3/1/17.
 */

public class Trail {

    private UUID mUUID;
    private List<LatLng> mList;

    public Trail(List<LatLng> list)
    {
        this(UUID.randomUUID(), list);
    }

    public Trail(Trail trail)
    {
        this(trail.getUUID(), trail.getList());
    }

    public Trail(UUID uuid, List<LatLng> list)
    {
        mUUID = uuid;
        mList = list;
    }


    public UUID getUUID() {
        return mUUID;
    }

    public List<LatLng> getList() {
        return mList;
    }

    public void setList(List<LatLng> list) {
        mList = list;
    }

}
