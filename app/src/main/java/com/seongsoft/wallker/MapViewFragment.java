package com.seongsoft.wallker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.GeoApiContext;
import com.google.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by BeINone on 2016-09-08.
 */
public class MapViewFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        Person.PersonLocationListener {

    private Context mContext;
    private MapView mMapView;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLocaion;
    private GeoApiContext mGeoContext;
    private Person mPerson;
    private Marker mCurrentMarker;
    private boolean walkState = false;
    private RoadTracker mRoadTracker;
    private boolean mRequestingLocationUpdates;
    private ArrayList<LatLng> mCheckedLocations = new ArrayList<LatLng>();        //지나간 좌표 들을 저장하는 List
    private com.google.android.gms.maps.model.LatLng startLatLng = new  com.google.android.gms.maps.model.LatLng(0, 0);
    private com.google.android.gms.maps.model.LatLng endLatLng = new  com.google.android.gms.maps.model.LatLng(0, 0);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getContext();
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        createLocationRequest();
        mGeoContext = new GeoApiContext().setApiKey("");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);

        mMapView = (MapView) v.findViewById(R.id.mapview);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();    // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bitmap treasureBitmap = resizeMarker(R.drawable.treasure, 100, 100);
        final BitmapDescriptor treasureBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(treasureBitmap);

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                Treasure treasure = new Treasure(mContext, googleMap);
                treasure.createTreasure();
                mRoadTracker = new RoadTracker(mMap, mGeoContext);
//                mMap.addGroundOverlay(new GroundOverlayOptions()
//                        .position(new LatLng(-33.8688184, 151.20930), 10f)
//                        .image(treasureBitmapDescriptor));
            }
        });

        return v;
    }

    private Bitmap resizeMarker(int id, int width, int height) {
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), id);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);

        return resizedBitmap;
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();

        if (mPerson != null) {
            if (mPerson.isPermissionDenied()) {
                // Permission was not granted, display error dialog.
                showMissingPermissionError();
                mPerson.setPermissionDenied(false);
            }

            if (!mRequestingLocationUpdates) {
                mPerson.startLocationUpdates();
                mRequestingLocationUpdates = true;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        mPerson.stopLocationUpdates();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mPerson == null) {
            mPerson = new Person(mContext, mGoogleApiClient, mLocationRequest, this);
        }

        if (!mRequestingLocationUpdates) {
            mPerson.startLocationUpdates();
            mRequestingLocationUpdates = true;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onPersonLocationChanged(Location location) {
        // 이전 위치 마커 지우기
        mLocaion = location;
        if (mCurrentMarker != null) mCurrentMarker.remove();

        /* 현재 위치에 마커 생성 */
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new com.google.android.gms.maps.model.LatLng(location.getLatitude(), location.getLongitude()));
        mCurrentMarker = mMap.addMarker(markerOptions);

        // 현재 위치로 시점 이동
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new com.google.android.gms.maps.model.LatLng(location.getLatitude(), location.getLongitude()), 18));
        if(walkState){
            endLatLng = new  com.google.android.gms.maps.model.LatLng(location.getLatitude(), location.getLongitude());
            mCheckedLocations.add(new LatLng(location.getLatitude(), location.getLongitude()));
            drawPath();
            startLatLng = endLatLng;
        }
    }
    private void drawPath(){
        PolylineOptions options = new PolylineOptions().add(startLatLng)
                .add(endLatLng).width(15).color(Color.BLACK).geodesic(true);
        mMap.addPolyline(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(endLatLng, 18));
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getChildFragmentManager(), "dialog");
    }
    public void changeWalkState(){
        walkState = !walkState;
    }
    public boolean isWalkOn(){
        return walkState;
    }
    public void walkStart(){
        mCheckedLocations.add(new LatLng(mLocaion.getLatitude(), mLocaion.getLongitude()));
        startLatLng = new  com.google.android.gms.maps.model.LatLng(mLocaion.getLatitude(), mLocaion.getLongitude());
    }
    public void walkEnd(){
        mRoadTracker.drawCorrentPath(mCheckedLocations);
    }
}
