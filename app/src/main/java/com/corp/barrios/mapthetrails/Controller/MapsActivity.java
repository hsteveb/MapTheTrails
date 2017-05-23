package com.corp.barrios.mapthetrails.Controller;
/* 1. Make google maps zoom to your location.
   2. Get the button to work so that it creates a line on maps.
   3. Work on sqlite to save all the coordinates.
 */

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.corp.barrios.mapthetrails.Model.Trail;
import com.corp.barrios.mapthetrails.Model.TrailInfo;
import com.corp.barrios.mapthetrails.Model.TrailInfoLab;
import com.corp.barrios.mapthetrails.Model.TrailLab;
import com.corp.barrios.mapthetrails.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import static com.corp.barrios.mapthetrails.R.id.map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    private static final String TAG = "MapsActivity";

    private Button mButton;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    private Boolean mRequestingLocationUpdates;
    private LocationRequest mLocationRequest;

    private List<Circle> mCircles;
    private PolylineOptions mPolylineOptions;
    private Polyline mPolyline, clickedPolyline;

    private TrailInfo clickedTrail;
    private TrailLab mTrailLab;
    private TrailInfoLab mInfoLab;

    private final double radius = .5;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        createLocationRequest();
        mRequestingLocationUpdates = false;
        mTrailLab = TrailLab.get(this);
        mInfoLab = new TrailInfoLab();

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().hide();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
        } else {
            Toast.makeText(this, "Permission failed", Toast.LENGTH_SHORT).show();
        }

        /*mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                Location mLocation = null;
                if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                } else {
                    Log.d(TAG, "Permission failed");
                }
                if (mLocation != null)
                    Toast.makeText(getApplicationContext(), String.format("Longitude: %f Latitude: %f", mLocation.getLongitude(), mLocation.getLatitude()), Toast.LENGTH_SHORT).show();
                return false;
            }
        });*/

        mButton = (Button) findViewById(R.id.start_stop);

        /* This is where we shall start the gps mapping and stop and save the trail.
            Saving all info will be done with sqlite for now.
            * Using LocationServices since it does everything for you.
         */
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mGoogleApiClient.isConnected()) {
                    if(!mRequestingLocationUpdates)
                    {
                        mButton.setText("Stop");
                        mRequestingLocationUpdates = true;
                        startLocationUpdates();
                    }
                    else {
                        mButton.setText("Start");
                        mRequestingLocationUpdates = false;
                        Trail trail = new Trail(mPolyline.getPoints());
                        mTrailLab.addTrail(trail);
                        Log.d(TAG, "saved trail heres the updated list: " + mTrailLab.getLists().size());

                        mPolyline.setClickable(true);
                        mInfoLab.addKVP(mPolyline, new TrailInfo(trail, mCircles));

                        mPolylineOptions = null;
                        mCircles = null;
                        mPolyline = null;
                        stopLocationUpdates();
                    }
                }
            }
        });

        for(Trail trail: mTrailLab.getLists())
        {
            List<Circle> circles = new ArrayList<>();
            PolylineOptions lines = new PolylineOptions().width(5).color(Color.RED);


            for(LatLng latLng: trail.getList())
            {
                lines.add(latLng);
                CircleOptions circleOptions = new CircleOptions().radius(radius).center(latLng).fillColor(Color.RED).strokeColor(Color.RED).visible(false);
                Circle circle = mMap.addCircle(circleOptions);
                circles.add(circle);
            }

            Polyline polyline = mMap.addPolyline(lines);
            polyline.setClickable(true);
            
            mInfoLab.addKVP(polyline, new TrailInfo(trail, circles));
        }

        /*
        - Check and see if clicked variables are not null and remove the visibility;
         */
        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {

                LatLngBounds latLngBounds;
                LatLng point1 = polyline.getPoints().get(0), point2 = polyline.getPoints().get(polyline.getPoints().size() -1);

                if(clickedPolyline != null || clickedTrail != null)
                    clearClickedPolylineInfo();


                if(point1.latitude > point2.latitude)
                    latLngBounds = new LatLngBounds(point2, point1);
                else
                    latLngBounds = new LatLngBounds(point1, point2);


                clickedPolyline = polyline;
                clickedTrail = mInfoLab.getTrailInfo(polyline);
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 5));
                getSupportActionBar().show();

            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(getSupportActionBar().isShowing())
                {
                    clearClickedPolylineInfo();
                    getSupportActionBar().hide();
                }
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.delete_menu_item:
                mTrailLab.deleteTrail(clickedTrail);
                mInfoLab.removePolyline(clickedPolyline);
                clickedPolyline.remove();
                clearClickedPolylineInfo();
                getSupportActionBar().hide();
                return true;
            case R.id.points_menu_item:
                if(item.getTitle().toString() == getString(R.string.show_points))
                {
                    item.setTitle(R.string.hide_points);
                    if(clickedTrail != null)
                        changeTrailCirclesVisibility(true);
                }
                else
                {
                    item.setTitle(R.string.show_points);
                    changeTrailCirclesVisibility(false);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onStart() {

        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mTrailLab.closeSqlTable();
        super.onDestroy();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Location mLocation;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(mLocation != null)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), 18));
        } else {
            Log.d(TAG, "Permission failed");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CircleOptions circleOptions = new CircleOptions().radius(radius).center(latLng).fillColor(Color.RED).strokeColor(Color.RED).visible(false);

        if(mPolylineOptions == null)
        {
            mCircles = new ArrayList<>();

            Log.d(TAG, "Creating new polylineoption");
            mPolylineOptions = new PolylineOptions().add(latLng).color(Color.RED).width(5);
            mPolyline = mMap.addPolyline(mPolylineOptions);

            mCircles.add(mMap.addCircle(circleOptions));
        }
        else
        {
            Boolean same = false;
            List<LatLng> temp = mPolyline.getPoints();

            for(LatLng point: temp)
            {
                if(point.latitude == location.getLatitude() && point.longitude == location.getLongitude())
                {
                    Log.d(TAG, "points are the same not adding");
                    same = true;
                    break;
                }
            }

            if(!same)
            {
                Log.d(TAG, "Adding point");

                temp.add(latLng);
                mPolyline.setPoints(temp);
                mCircles.add(mMap.addCircle(circleOptions));
            }

        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 20));
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest().setInterval(10000).setFastestInterval(5000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        } else {
            Log.d(TAG, "Location permission not enabled.");
        }
    }

    private void stopLocationUpdates()
    {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    private void clearClickedPolylineInfo()
    {
        getSupportActionBar().invalidateOptionsMenu();
        if(clickedTrail != null)
            clickedTrail.setCircleVisibility(false);
        clickedTrail = null;
        clickedPolyline = null;
    }

    private void changeTrailCirclesVisibility(boolean value)
    {
        if(value)
        {
            clickedTrail.setCircleVisibility(value);
        }
        else
            clickedTrail.setCircleVisibility(value);
    }

}
