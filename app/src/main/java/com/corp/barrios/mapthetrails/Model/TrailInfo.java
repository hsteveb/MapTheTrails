package com.corp.barrios.mapthetrails.Model;

import com.google.android.gms.maps.model.Circle;

import java.util.List;

/**
 * Created by hectorbarrios on 5/4/17.
 */

public class TrailInfo extends Trail {

    private List<Circle> mCircleList;


    public TrailInfo(Trail trail, List<Circle> circles)
    {
        super(trail);
        mCircleList = circles;
    }

    public void setCircleVisibility(Boolean value)
    {
        for(Circle circle: mCircleList)
        {
            if(value)
                circle.setVisible(true);
            else
                circle.setVisible(false);
        }
    }
}
