package com.seongsoft.wallker.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.GeoApiContext;
import com.google.maps.model.LatLng;
import com.seongsoft.wallker.BitmapUtils;
import com.seongsoft.wallker.DatabaseManager;
import com.seongsoft.wallker.DistanceUtils;
import com.seongsoft.wallker.PermissionUtils;
import com.seongsoft.wallker.R;
import com.seongsoft.wallker.RoadTracker;
import com.seongsoft.wallker.Treasure;
import com.seongsoft.wallker.TreasureManager;
import com.seongsoft.wallker.User;
import com.seongsoft.wallker.Walking;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.seongsoft.wallker.DistanceUtils.calDistance;

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

    private User mUser;

    private String walk_name;
    private String currentDate;
    private List<com.google.android.gms.maps.model.LatLng> walkAllPath = new ArrayList<>();
    private int totalDistance;

    private MapView mMapView;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private TreasureManager mTreasureManager;
    private DatabaseManager mDBManager;

    private UpdateTimeTask mUpdateTimeTask;
    private Timer mUpdateTimeTimer;

    private boolean mRequestingLocationUpdates;
    private Location mPrevLocation;
    private Location mCurrLocation;
    private Marker mCurrentMarker;
    private double mMovedDistance;

    private int mSeconds;
    private int mMinutes;
    private int mHours;

    private GeoApiContext mGeoContext;
    private boolean walkState = false;
    private RoadTracker mRoadTracker;
    private ArrayList<LatLng> mCheckedLocations = new ArrayList<>();        //지나간 좌표 들을 저장하는 List
    private LatLng startLatLng = new LatLng(0, 0);
    private LatLng endLatLng = new LatLng(0, 0);

    private boolean mPermissionDenied;
    private boolean mCameraMoveStarted;
    private boolean mFirstStart = true;

    private TextView mNumFlagsTV;

    private TextView mTimeTV;
    private TextView mDistanceTV;
    private TextView mKcalTV;

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

        mDBManager = new DatabaseManager(getContext());
        mTreasureManager = new TreasureManager(getContext(), mGeoContext);

        mUser = new User(55);
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

                mMap.setOnCameraMoveStartedListener(MapViewFragment.this);
                mMap.setOnCameraIdleListener(MapViewFragment.this);

//                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//                        new com.google.android.gms.maps.model.LatLng(
//                                37.2635727, 127.02860090000001), ZOOM));
            }
        });

        mNumFlagsTV = (TextView) v.findViewById(R.id.tv_num_flag);
        displayNumFlags();

        mTimeTV = (TextView) v.findViewById(R.id.tv_time);
        mDistanceTV = (TextView) v.findViewById(R.id.tv_distance);
        mKcalTV = (TextView) v.findViewById(R.id.tv_kcal);

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
        final LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        if (mCameraMoveStarted) {
            if (mMap.getCameraPosition().zoom == ZOOM) {
                if (mMovedDistance >= 100.0 || mFirstStart) {
                    mTreasureManager.createTreasure(bounds, mMap);
                    mMovedDistance = 0;
                    mFirstStart = false;
                }

                List<Treasure> treasures
                        = mTreasureManager.displayTreasure(bounds, mMap);
                if (treasures != null) {
                    for (int index = 0; index < treasures.size(); index++) {
                        final double treasureLat = treasures.get(index).getLatitude();
                        final double treasureLng = treasures.get(index).getLongitude();
                        // 보물 획득 확인
                        if (checkGetTreasure(treasureLat, treasureLng)) {
                            Snackbar.make(getView(), R.string.get_flag, Snackbar.LENGTH_LONG)
                                    .setAction(R.string.open, null).show();
                            mDBManager.deleteTreasure(treasureLat, treasureLng);
                            mDBManager.increaseNumFlags(1);
                            mTreasureManager.displayTreasure(bounds, mMap);
                            displayNumFlags();
                        }
                    }
                }
            }
            mCameraMoveStarted = false;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
//        Log.i("LocationChanged", "Latitude: " + location.getLatitude()
//                + ", Longitude: " + location.getLongitude());
//        Toast.makeText(getContext(), "Latitude: " + location.getLatitude()
//                + ", Longitude: " + location.getLongitude(), Toast.LENGTH_SHORT).show();

        // 현재 위치와 이전 위치 저장
        if (mPrevLocation == null) mPrevLocation = location;
        else mPrevLocation = mCurrLocation;
        mCurrLocation = location;

        // 이동거리 계산 및 저장
        double movedDistance = calDistance(mPrevLocation.getLatitude(), mPrevLocation.getLongitude(),
                mCurrLocation.getLatitude(), mCurrLocation.getLongitude());
        mMovedDistance += movedDistance;

        // 이동거리 업데이트
        if (!checkLastUpdateDateIsToday()) mDBManager.initTodayDistance();
        if (movedDistance > 0) mDBManager.updateDistance(movedDistance);

        // 이동거리 및 칼로리 디스플레이
        double sMovedDistance = mDBManager.selectDistance()[0];
        mDistanceTV.setText(String.format(Locale.getDefault(), "%.2f", sMovedDistance));
        mKcalTV.setText(String.format(Locale.getDefault(), "%.2f",
                DistanceUtils.calKcal(mUser.getWeight(), sMovedDistance / 1000)));

        // 이전 마커 제거
        if (mCurrentMarker != null) mCurrentMarker.remove();

        // 현재 위치에 마커 생성
        mCurrentMarker = addMarker(location.getLatitude(), location.getLongitude());

        // 현재 위치로 시점 이동
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new com.google.android.gms.maps.model.LatLng(location.getLatitude(), location.getLongitude()), ZOOM));

        if (walkState) {
            endLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            mCheckedLocations.add(new LatLng(location.getLatitude(), location.getLongitude()));
            ArrayList<com.google.android.gms.maps.model.LatLng> path = mRoadTracker.getJsonData(startLatLng, endLatLng);
            if(path == null) {
                Toast.makeText(getContext(), "거리가 너무 짧습니다", Toast.LENGTH_SHORT).show();
                return;
            }walkAllPath.addAll(path);
            totalDistance += mRoadTracker.getDistance();
            drawPath(path);
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

    public void changeWalkState() {
        walkState = !walkState;
    }

    public boolean isWalkOn() {
        return walkState;
    }

    public void walkStart(String name) {
        mRoadTracker = new RoadTracker(mMap);
        walk_name = name;
                currentDate = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
        startLatLng = new  LatLng(mCurrLocation.getLatitude(), mCurrLocation.getLongitude());
        startLocationUpdates();
        currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        startLatLng = new LatLng(mCurrLocation.getLatitude(), mCurrLocation.getLongitude());
    }

    public void walkEnd() {
        Walking walk = new Walking(walk_name, totalDistance,
                (ArrayList<com.google.android.gms.maps.model.LatLng>) walkAllPath, currentDate);
    }

    private void displayNumFlags() {
        mNumFlagsTV.setText(String.valueOf(mDBManager.selectNumFlags()));
    }

    private boolean checkLastUpdateDateIsToday() {
        String lastUpdateDate = mDBManager.selectDate();
        String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());

        if (lastUpdateDate.equals(currentDate)) return true;
        else return false;
    }

    private boolean checkGetTreasure(final double treasureLat, final double treasureLng) {
        if (calDistance(
                mCurrLocation.getLatitude(), mCurrLocation.getLongitude(),
                treasureLat, treasureLng) <= 10.0) {
            return true;
        }

        return false;
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void startLocationUpdates() {
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

        if (mUpdateTimeTask == null) mUpdateTimeTask = new UpdateTimeTask();
        if (mUpdateTimeTimer == null) {
            mUpdateTimeTimer = new Timer();
            mUpdateTimeTimer.schedule(mUpdateTimeTask, 0, 1000);
        }
    }

    public void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    private void drawPath(ArrayList<com.google.android.gms.maps.model.LatLng> mapPoints) {
        com.google.android.gms.maps.model.LatLng[] pathPoints = new com.google.android.gms.maps.model.LatLng[mapPoints.size()];
        pathPoints = mapPoints.toArray(pathPoints);
        mMap.addPolyline(new PolylineOptions().add(pathPoints).color(Color.BLUE));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pathPoints[pathPoints.length - 1], 18));
    }

    private Marker addMarker(double latitude, double longitude) {
        return mMap.addMarker(new MarkerOptions()
                .position(new com.google.android.gms.maps.model.LatLng(latitude, longitude))
                .icon(getPersonBitmapDescriptor()));
    }

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getChildFragmentManager(), "dialog");
    }

    private BitmapDescriptor getPersonBitmapDescriptor() {
        Bitmap treasureBitmap = BitmapUtils.resizeBitmap(getContext(), R.drawable.ic_person, 25, 50);
        return BitmapDescriptorFactory.fromBitmap(treasureBitmap);
    }

    private void setTime() {
        mSeconds++;
        if (mSeconds >= 60) {
            mMinutes++;
            mSeconds = 0;
        }
        if (mHours >= 60) {
            mHours++;
            mMinutes = 0;
        }
    }

    private class UpdateTimeTask extends TimerTask {

        @Override
        public void run() {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    setTime();
                    mTimeTV.setText(String.format(
                            Locale.getDefault(), "%02d : %02d : %02d", mHours, mMinutes, mSeconds));
                }
            });
        }

    }

}
