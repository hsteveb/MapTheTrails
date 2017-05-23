package com.corp.barrios.mapthetrails.Model;

import com.google.android.gms.maps.model.Polyline;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hectorbarrios on 5/4/17.
 */

public class TrailInfoLab {

    private Map<Polyline, TrailInfo> mMap;

    public TrailInfoLab()
    {
        mMap = new HashMap<>();
    }

    public void addKVP(Polyline polyline, TrailInfo trailInfo)
    {
        mMap.put(polyline, trailInfo);
    }

    public TrailInfo getTrailInfo(Polyline polyline)
    {
        return mMap.get(polyline);
    }

    public void removePolyline(Polyline polyline)
    {
        mMap.remove(polyline);
    }
}
