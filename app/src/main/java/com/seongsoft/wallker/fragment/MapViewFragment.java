package com.seongsoft.wallker.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
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
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.GeoApiContext;
import com.google.maps.model.LatLng;
import com.seongsoft.wallker.R;
import com.seongsoft.wallker.beans.Treasure;
import com.seongsoft.wallker.beans.User;
import com.seongsoft.wallker.beans.Walking;
import com.seongsoft.wallker.beans.Zone;
import com.seongsoft.wallker.manager.DataManager;
import com.seongsoft.wallker.manager.DatabaseManager;
import com.seongsoft.wallker.manager.TreasureManager;
import com.seongsoft.wallker.utils.BitmapUtils;
import com.seongsoft.wallker.utils.PermissionUtils;
import com.seongsoft.wallker.utils.RoadTracker;
import com.seongsoft.wallker.utils.TCPClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.seongsoft.wallker.utils.DistanceUtils.calDistance;
import static com.seongsoft.wallker.utils.DistanceUtils.calKcal;

/**
 * Created by BeINone on 2016-09-08.
 */

public class MapViewFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraIdleListener,
        LocationListener {

    /*
        Latitude Interval: 0.00261688764829
        Longitude Interval: 0.00340909090909
     */

    private static final double D_LAT_INTERVAL = 0.00261688764829;
    private static final double D_LNG_INTERVAL = 0.00340909090909;
    private static final long L_LAT_INTERVAL = 261688764829L;
    private static final long L_LNG_INTERVAL = 340909090909L;
    private static final String IP = "10.156.145.88";
    private static final int PORT = 52925;
    public static final float ZOOM = 18.0f;

    private ConnectionTask mConnectionTask;

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
    private LatLng mPrevLatLng;
    private LatLng mCurrLatLng;
    private Marker mCurrentMarker;
    private double mCheckingDistance;      // 단위는 km
    private double mTotalDistance;

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

    private CardView mNumFlagsCV;
    private CardView mTimeCV;
    private CardView mDistanceCV;
    private CardView mStepCV;

    private TextView mNumFlagsTV;
    private TextView mTimeTV;
    private TextView mDistanceTV;
    private TextView mStepTV;

    private Context mContext;

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

//        mConnectionTask = new ConnectionTask();
//        mConnectionTask.execute();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);

        mContext = v.getContext();
        mMapView = (MapView) v.findViewById(R.id.mapview);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();    // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getContext());
        } catch (Exception e) {
            e.printStackTrace();
            return v;
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

        mNumFlagsCV = (CardView) v.findViewById(R.id.cv_num_flags);
        mNumFlagsCV.setAlpha(0.5f);
        mNumFlagsCV.setVisibility(View.INVISIBLE);
        mTimeCV = (CardView) v.findViewById(R.id.cv_time);
        mTimeCV.setAlpha(0.5f);
        mTimeCV.setVisibility(View.INVISIBLE);
        mDistanceCV = (CardView) v.findViewById(R.id.cv_distance);
        mDistanceCV.setAlpha(0.5f);
        mDistanceCV.setVisibility(View.INVISIBLE);
        mStepCV = (CardView) v.findViewById(R.id.cv_step);
        mStepCV.setAlpha(0.5f);
        mStepCV.setVisibility(View.INVISIBLE);

        mNumFlagsTV = (TextView) v.findViewById(R.id.tv_num_flags);
        displayNumFlags();

        mTimeTV = (TextView) v.findViewById(R.id.tv_time);
        mDistanceTV = (TextView) v.findViewById(R.id.tv_distance);
        mStepTV = (TextView) v.findViewById(R.id.tv_step);

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
        if (walkState) {
            final LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
            if (mCameraMoveStarted) {
                if (mMap.getCameraPosition().zoom == ZOOM) {
//                    Log.d(TAG, bounds.toString());

                    drawZones(bounds);
//                    try {
//                        JSONObject messageJSONObject = DataManager.createBoundsJSONObject(bounds);
//                        mConnectionTask.sendMessage(messageJSONObject.toString());
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }

                    if (mCheckingDistance >= 0.1 || mFirstStart) {
                        mTreasureManager.createTreasure(bounds, mMap);
                        mCheckingDistance = 0;
                        mFirstStart = false;
                    }

                    List<Treasure> treasures = mTreasureManager.displayTreasure(bounds, mMap);
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
    }

    @Override
    public void onLocationChanged(Location location) {
//        Log.i("LocationChanged", "Latitude: " + location.getLatitude()
//                + ", Longitude: " + location.getLongitude());
//        Toast.makeText(getContext(), "Latitude: " + location.getLatitude()
//                + ", Longitude: " + location.getLongitude(), Toast.LENGTH_SHORT).show();

        // 현재 위치와 이전 위치 저장
        if (mPrevLatLng == null) {
            mPrevLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            mCurrLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        } else {
            mPrevLatLng.lat = mCurrLatLng.lat;
            mPrevLatLng.lng = mCurrLatLng.lng;
            mCurrLatLng.lat = location.getLatitude();
            mCurrLatLng.lng = location.getLongitude();
        }

        Log.d("mylocation", "prev:" + mPrevLatLng.lat + ", " + mPrevLatLng.lng
                + "  curr:" + mCurrLatLng.lat + ", " + mCurrLatLng.lng);

        if (walkState) {
            if (mPrevLatLng.lat != mCurrLatLng.lat && mPrevLatLng.lng != mCurrLatLng.lng) {
                double movedDistance = calDistance(mPrevLatLng.lat, mPrevLatLng.lng,
                        mCurrLatLng.lat, mCurrLatLng.lng);
                mTotalDistance += movedDistance;
                mCheckingDistance += movedDistance;

                // 이동거리 업데이트
                if (!checkLastUpdateDateIsToday()) mDBManager.initTodayDistance();
                if (movedDistance > 0) mDBManager.updateDistance(movedDistance);

                Log.d("distance", String.valueOf(movedDistance));
            }

            // 이동거리 및 칼로리 디스플레이
            mDistanceTV.setText(String.format(Locale.getDefault(), "%.2f", mTotalDistance));
            mStepTV.setText(String.format(Locale.getDefault(), "%.2f",
                    calKcal(mUser.getWeight(), mTotalDistance)));

            endLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            mCheckedLocations.add(new LatLng(location.getLatitude(), location.getLongitude()));
            ArrayList<com.google.android.gms.maps.model.LatLng> path = mRoadTracker.getJsonData(startLatLng, endLatLng);
            if (path == null) {
                Toast.makeText(getContext(), "거리가 너무 짧습니다", Toast.LENGTH_SHORT).show();
                return;
            }
            walkAllPath.addAll(path);
            totalDistance += mRoadTracker.getDistance();
            drawPath(path);
            startLatLng = endLatLng;

            if (mCurrentMarker != null) mCurrentMarker.remove();

            mCurrentMarker = addMarker(path.get(path.size() - 1).latitude, path.get(path.size() - 1).longitude);

        } else {
            if (mCurrentMarker != null) mCurrentMarker.remove();
            mCurrentMarker = addMarker(location.getLatitude(), location.getLongitude());

        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new com.google.android.gms.maps.model.LatLng(location.getLatitude(),
                        location.getLongitude()), ZOOM));
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
        currentDate = new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new Date());
        startLatLng = new LatLng(mCurrLatLng.lat, mCurrLatLng.lng);

        if (mUpdateTimeTask == null) mUpdateTimeTask = new UpdateTimeTask();
        if (mUpdateTimeTimer == null) {
            mUpdateTimeTimer = new Timer();
            mUpdateTimeTimer.schedule(mUpdateTimeTask, 0, 1000);
        }

        mNumFlagsCV.setVisibility(View.VISIBLE);
        mTimeCV.setVisibility(View.VISIBLE);
        mDistanceCV.setVisibility(View.VISIBLE);
        mStepCV.setVisibility(View.VISIBLE);

        startLocationUpdates();
    }

    public void walkEnd() {
        int step = 0;

        mUpdateTimeTimer.cancel();
        mUpdateTimeTimer = null;
        mUpdateTimeTask.cancel();
        mUpdateTimeTask = null;

        int time = mHours * 3600 + mMinutes * 60 + mSeconds;
        double speedAver = totalDistance * 3600 / time;

        Walking walk = new Walking(walk_name, totalDistance,
                (ArrayList<com.google.android.gms.maps.model.LatLng>) walkAllPath,
                currentDate, time, Integer.parseInt(mNumFlagsTV.getText().toString()),
                speedAver, step);
        mDBManager.insertWalking(walk);
        mMap.clear();


        mHours = 0;
        mMinutes = 0;
        mSeconds = 0;

        mNumFlagsCV.setVisibility(View.INVISIBLE);
        mTimeCV.setVisibility(View.INVISIBLE);
        mDistanceCV.setVisibility(View.INVISIBLE);
        mStepCV.setVisibility(View.INVISIBLE);


        mTimeTV.setText(null);
        mDistanceTV.setText(null);
        mStepTV.setText(null);
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
        if (calDistance(mCurrLatLng.lat, mCurrLatLng.lng, treasureLat, treasureLng) <= 0.01) {
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

        // Start location updates.
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

//        if (mUpdateTimeTask == null) mUpdateTimeTask = new UpdateTimeTask();
//        if (mUpdateTimeTimer == null) {
//            mUpdateTimeTimer = new Timer();
//            mUpdateTimeTimer.schedule(mUpdateTimeTask, 0, 1000);
//        }
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

    private void drawZones(LatLngBounds bounds) {
        long mapSouthwestLat = (long) (bounds.southwest.latitude * Math.pow(10, 14));
        long mapSouthwestLng = (long) (bounds.southwest.longitude * Math.pow(10, 14));

        double southwestLat = (mapSouthwestLat - (mapSouthwestLat % L_LAT_INTERVAL)) / Math.pow(10, 14);
        double southwestLng = (mapSouthwestLng - (mapSouthwestLng % L_LNG_INTERVAL)) / Math.pow(10, 14);
        double northeastLat = southwestLat + D_LAT_INTERVAL;
        double northeastLng = southwestLng + D_LNG_INTERVAL;

//        com.google.android.gms.maps.model.LatLng southwest =
//                new com.google.android.gms.maps.model.LatLng(southwestLat, southwestLng);
//        com.google.android.gms.maps.model.LatLng southeast =
//                new com.google.android.gms.maps.model.LatLng(southwestLat, northeastLng);
//        com.google.android.gms.maps.model.LatLng northeast =
//                new com.google.android.gms.maps.model.LatLng(northeastLat, northeastLng);
//        com.google.android.gms.maps.model.LatLng northwest =
//                new com.google.android.gms.maps.model.LatLng(northeastLat, southwestLng);

        double currSouthwestLng = southwestLng;
        double currNortheastLng = northeastLng;
        while (currSouthwestLng <= bounds.northeast.longitude) {
            double currSouthwestLat = southwestLat;
            double currNortheastLat = northeastLat;
            while (currSouthwestLat <= bounds.northeast.latitude) {
                com.google.android.gms.maps.model.LatLng southwest =
                        new com.google.android.gms.maps.model.LatLng(
                                currSouthwestLat, currSouthwestLng);
                com.google.android.gms.maps.model.LatLng northeast =
                        new com.google.android.gms.maps.model.LatLng(
                                currNortheastLat, currNortheastLng);
                drawZone(new LatLngBounds(southwest, northeast));

                currSouthwestLat += D_LAT_INTERVAL;
                currNortheastLat += D_LAT_INTERVAL;
            }

            currSouthwestLng += D_LNG_INTERVAL;
            currNortheastLng += D_LNG_INTERVAL;
        }
    }

    private void drawZone(LatLngBounds zoneBounds) {
        com.google.android.gms.maps.model.LatLng southwest = zoneBounds.southwest;
        com.google.android.gms.maps.model.LatLng northeast = zoneBounds.northeast;
        com.google.android.gms.maps.model.LatLng southeast =
                new com.google.android.gms.maps.model.LatLng(
                        southwest.latitude, northeast.longitude);
        com.google.android.gms.maps.model.LatLng northwest =
                new com.google.android.gms.maps.model.LatLng(
                        northeast.latitude, southwest.longitude);

        mMap.addPolygon(new PolygonOptions()
                .add(southwest, southeast, northeast, northwest));
    }

    // { bounds :
    //   { southwest : { latitude : x, longitude : y }, northeast : { latitude : x, longitue : y } }
    // }

    private com.google.android.gms.maps.model.LatLng getZoneSouthwest(
            com.google.android.gms.maps.model.LatLng currLatLng) {
        double southwestLat = (currLatLng.latitude - (currLatLng.latitude % L_LAT_INTERVAL)) /
                Math.pow(10, 14);
        double southwestLng = (currLatLng.longitude - (currLatLng.longitude % L_LNG_INTERVAL)) /
                Math.pow(10, 14);

        return new com.google.android.gms.maps.model.LatLng(southwestLat, southwestLng);
    }

    public void putFlag() {
        long lLatitude = (long) (mCurrLatLng.lat * Math.pow(10, 14));
        long lLongitude = (long) (mCurrLatLng.lng * Math.pow(10, 14));
        double latitude = (lLatitude - (lLatitude % L_LAT_INTERVAL)) / Math.pow(10, 14);
        double longitude = (lLongitude - (lLongitude % L_LAT_INTERVAL)) / Math.pow(10, 14);

        try {
            JSONObject zoneJObject = DataManager.createZoneJSONObject(latitude, longitude);
            mConnectionTask.sendMessage(zoneJObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        drawZone(mMap.getProjection().getVisibleRegion().latLngBounds);
    }

    private class ConnectionTask extends AsyncTask<Void, Void, TCPClient>
            implements TCPClient.MessageCallback {

        private TCPClient mTCPClient;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected TCPClient doInBackground(Void... params) {
//            Log.d(TAG, "In do in background");

//            mTCPClient = new TCPClient(mHandler, IP, PORT,
//                    new TCPClient.MessageCallback() {
//                        @Override
//                        public void callbackMessageReceiver(String message) {
//                            Log.d(TAG, "Message receive");
//                            List<Zone> zones = DataManager.parseMessage(message);
//                            drawZones(zones);
//                        }
//                    });
            mTCPClient = new TCPClient(this);
            mTCPClient.run();

            return null;
        }

        @Override
        protected void onPostExecute(TCPClient result) {
            super.onPostExecute(result);

//            Log.d(TAG, "In on post execute");
            if (result != null && result.isRunning()) {
                result.stopClient();
//                Log.d(TAG, "Stopped");
            }
//            mHandler.sendEmptyMessageDelayed(MainActivity.SENT, 4000);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

//            Log.d(TAG, "In progress update, values: " + values.toString());

//            if (values[0].equals("shutdown")) {
//                mTCPClient.sendMessage(COMMAND);
//                mTCPClient.stopClient();
//                mTCPClient.stop();
//                mHandler.sendEmptyMessageDelayed(MainActivity.SHUTDOWN, 2000);

//            } else {
//                mTCPClient.sendMessage("wrong");
//                mHandler.sendEmptyMessageDelayed(MainActivity.ERROR, 2000);
//                mTCPClient.stopClient();
//            }
        }

        @Override
        public void callbackMessageReceiver(String message) {
//            Log.d(TAG, "Message receive");
            List<Zone> zones = DataManager.parseMessage(message);
//            drawZones(zones);
        }

        public void sendMessage(String message) {
            mTCPClient.sendMessage(message);
        }

    }

}
