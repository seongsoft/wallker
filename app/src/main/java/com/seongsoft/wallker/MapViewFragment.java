package com.seongsoft.wallker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.GeoApiContext;
import com.google.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by BeINone on 2016-09-08.
 */
public class MapViewFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraIdleListener,
        LocationListener {

    public static final float ZOOM = 18.0f;

    private MapView mMapView;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private TreasureManager mTreasureManager;
    private DatabaseManager mDBManager;

    private Location mPrevLocation;
    private Location mCurrLocation;
    private GeoApiContext mGeoContext;
    private Marker mCurrentMarker;
    private boolean walkState = false;
    private RoadTracker mRoadTracker;
    private boolean mRequestingLocationUpdates;
    private ArrayList<LatLng> mCheckedLocations = new ArrayList<>();        //지나간 좌표 들을 저장하는 List
    private com.google.android.gms.maps.model.LatLng startLatLng = new  com.google.android.gms.maps.model.LatLng(0, 0);
    private com.google.android.gms.maps.model.LatLng endLatLng = new  com.google.android.gms.maps.model.LatLng(0, 0);

    private boolean mPermissionDenied;
    private boolean mCameraMoveStarted;
    private double mMovedDistance = 100.0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        createLocationRequest();

        mGeoContext = new GeoApiContext().setApiKey("");

        mTreasureManager = new TreasureManager(getContext(), mGeoContext);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);

        mMapView = (MapView) v.findViewById(R.id.mapview);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();    // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;

//                mMap.setOnCameraChangeListener(MapViewFragment.this);
                mMap.setOnCameraMoveStartedListener(MapViewFragment.this);
                mMap.setOnCameraIdleListener(MapViewFragment.this);

//                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//                        new com.google.android.gms.maps.model.LatLng(
//                                37.2635727, 127.02860090000001), ZOOM));

                mRoadTracker = new RoadTracker(mMap, mGeoContext);
//                mMap.addGroundOverlay(new GroundOverlayOptions()
//                        .position(new LatLng(-33.8688184, 151.20930), 10f)
//                        .image(treasureBitmapDescriptor));
            }
        });

        return v;
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

        if (mPermissionDenied) {
            showMissingPermissionError();
            mPermissionDenied = false;
        }

        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
            mRequestingLocationUpdates = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        stopLocationUpdates();
        mRequestingLocationUpdates = false;
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
        if (!mRequestingLocationUpdates) {
            startLocationUpdates();
            mRequestingLocationUpdates = true;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("onConnectionSuspended", "");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("onConnectionFailed", "");
    }

    @Override
    public void onCameraMoveStarted(int i) {
        mCameraMoveStarted = true;
    }

    @Override
    public void onCameraIdle() {
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        if (mCameraMoveStarted) {
            if (mMap.getCameraPosition().zoom == ZOOM) {
                if (mMovedDistance >= 100.0) {
                    mTreasureManager.createTreasure(bounds, mMap);
                    mCameraMoveStarted = false;
                    mMovedDistance = 0;
                }

                // 보물 획득 확인
                List<Treasure> treasures = mTreasureManager.displayTreasure(bounds, mMap);
                if (treasures != null) {
                    for (int index = 0; index < treasures.size(); index++) {
//                        double minLatitude = treasures.get(index).getLatitude() - 0.0001;
//                        double maxLatitude = treasures.get(index).getLatitude() + 0.0001;
//                        double minLongitude = treasures.get(index).getLongitude() - 0.0001;
//                        double maxLongitude = treasures.get(index).getLongitude() + 0.0001;
//                        if (mCurrLocation.getLatitude() >= minLatitude
//                                && mCurrLocation.getLatitude() <= maxLatitude
//                                && mCurrLocation.getLongitude() >= minLongitude
//                                && mCurrLocation.getLongitude() <= maxLongitude) {
//                            Toast.makeText(getContext(), "보물 획득", Toast.LENGTH_SHORT).show();
//                        }

                        if (calDistance(
                                mCurrLocation.getLatitude(),
                                mCurrLocation.getLongitude(),
                                treasures.get(index).getLatitude(),
                                treasures.get(index).getLongitude()) <= 10.0) {
                            Toast.makeText(getContext(), "보물 획득", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("LocationChanged", "Latitude: " + location.getLatitude()
                + ", Longitude: " + location.getLongitude());
        Toast.makeText(getContext(), "Latitude: " + location.getLatitude()
                + ", Longitude: " + location.getLongitude(), Toast.LENGTH_SHORT).show();

        if (mPrevLocation == null) mPrevLocation = location;
        mCurrLocation = location;

        mMovedDistance += calDistance(mPrevLocation.getLatitude(), mPrevLocation.getLongitude(),
                mCurrLocation.getLatitude(), mCurrLocation.getLongitude());

        if (mCurrentMarker != null) mCurrentMarker.remove();
        mMap.clear();

        /* 현재 위치에 마커 생성 */
        mCurrentMarker = mMap.addMarker(new MarkerOptions()
                .position(new com.google.android.gms.maps.model.LatLng(
                        location.getLatitude(), location.getLongitude())));

        // 현재 위치로 시점 이동
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new com.google.android.gms.maps.model.LatLng(location.getLatitude(), location.getLongitude()), ZOOM));

        if (walkState) {
            endLatLng = new  com.google.android.gms.maps.model.LatLng(location.getLatitude(), location.getLongitude());
            mCheckedLocations.add(new LatLng(location.getLatitude(), location.getLongitude()));
            drawPath();
            startLatLng = endLatLng;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != PermissionUtils.LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            startLocationUpdates();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    public void changeWalkState(){
        walkState = !walkState;
    }

    public boolean isWalkOn(){
        return walkState;
    }

    public void walkStart(){
        mCheckedLocations.add(new LatLng(mCurrLocation.getLatitude(), mCurrLocation.getLongitude()));
        startLatLng = new  com.google.android.gms.maps.model.LatLng(mCurrLocation.getLatitude(), mCurrLocation.getLongitude());
    }

    public void walkEnd(){
        mRoadTracker.drawCurrentPath(mCheckedLocations);
    }

    public double calDistance(double lat1, double lon1, double lat2, double lon2){

        double theta, dist;
        theta = lon1 - lon2;
        dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);

        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;    // 단위 mile 에서 km 변환.
        dist = dist * 1000.0;      // 단위  km 에서 m 로 변환

        return dist;
    }

    private boolean isFirstUpdateOnToday() {
        String lastUpdateDate = mDBManager.selectDate();
        String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());

        if (lastUpdateDate == null) return false;

        return !lastUpdateDate.equals(currentDate);
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission((AppCompatActivity) getContext(),
                    PermissionUtils.LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    true);
        }

        mCurrLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        // Start location updates.
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    private void drawPath() {
        PolylineOptions options = new PolylineOptions().add(startLatLng)
                .add(endLatLng).width(15).color(Color.BLACK).geodesic(true);
        mMap.addPolyline(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(endLatLng, ZOOM));
    }

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getChildFragmentManager(), "dialog");
    }

    // 주어진 도(degree) 값을 라디언으로 변환
    private double deg2rad(double deg){
        return (double)(deg * Math.PI / (double)180d);
    }

    // 주어진 라디언(radian) 값을 도(degree) 값으로 변환
    private double rad2deg(double rad){
        return (double)(rad * (double)180d / Math.PI);
    }

}
