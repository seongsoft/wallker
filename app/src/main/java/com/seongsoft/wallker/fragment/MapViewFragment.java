package com.seongsoft.wallker.fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.GeoApiContext;
import com.seongsoft.wallker.R;
import com.seongsoft.wallker.beans.Member;
import com.seongsoft.wallker.beans.Treasure;
import com.seongsoft.wallker.beans.Walking;
import com.seongsoft.wallker.beans.Zone;
import com.seongsoft.wallker.constants.HttpConst;
import com.seongsoft.wallker.constants.PrefConst;
import com.seongsoft.wallker.constants.ZoneConst;
import com.seongsoft.wallker.dialog.PutFlagDialogFragment;
import com.seongsoft.wallker.manager.DataManager;
import com.seongsoft.wallker.manager.DatabaseManager;
import com.seongsoft.wallker.manager.JSONManager;
import com.seongsoft.wallker.manager.TreasureManager;
import com.seongsoft.wallker.manager.ZoneDrawer;
import com.seongsoft.wallker.utils.BitmapUtils;
import com.seongsoft.wallker.utils.PermissionUtils;
import com.seongsoft.wallker.utils.RoadTracker;
import com.seongsoft.wallker.utils.TCPClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
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

    private static final String IP = "10.156.145.88";
    private static final int PORT = 52925;
    public static final float ZOOM = 18.0f;

    private SharedPreferences mAppInfoPref;
    private SharedPreferences.Editor mAppInfoPrefEditor;
    private SharedPreferences mUserPref;
    private SharedPreferences.Editor mUserPrefEditor;
    private SharedPreferences mWalkingDistancePref;
    private SharedPreferences.Editor mWalkingDistancePrefEditor;

    private ConnectionTask mConnectionTask;

    private Member mMember;
    private ZoneDrawer mZoneDrawer;

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
    private com.google.maps.model.LatLng mPrevLatLng;
    private com.google.maps.model.LatLng mCurrLatLng;
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
    private boolean mFirstStart;

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

        mAppInfoPref = getContext().getSharedPreferences(PrefConst.APP_INFO_PREF, 0);
        mAppInfoPrefEditor = mAppInfoPref.edit();
        mUserPref = getContext().getSharedPreferences(PrefConst.USER_PREF, 0);
        mUserPrefEditor = mUserPref.edit();
        mWalkingDistancePref = getContext().getSharedPreferences(PrefConst.WALKING_DISTANCE_PREF, 0);
        mWalkingDistancePrefEditor = mWalkingDistancePref.edit();

        if (mFirstStart = mAppInfoPref.getBoolean(PrefConst.IS_FIRST_EXECUTE, true)) {
            mAppInfoPrefEditor.putBoolean(PrefConst.IS_FIRST_EXECUTE, false).apply();
        }

        mMember = new Member(mUserPref.getString(PrefConst.ID, ""),
                mUserPref.getString(PrefConst.PASSWORD, ""),
                mUserPref.getInt(PrefConst.WEIGHT, 0),
                mUserPref.getInt(PrefConst.NUM_FLAGS, 0));

        mTotalDistance = mWalkingDistancePref.getFloat(PrefConst.TOTAL_DISTANCE, 0);

        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        createLocationRequest();

        mGeoContext = new GeoApiContext().setApiKey("");

        mDBManager = new DatabaseManager(getContext());
        mTreasureManager = new TreasureManager(getContext(), mGeoContext);

//        mConnectionTask = new ConnectionTask();
//        mConnectionTask.execute();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);

        SharedPreferences loginPref = getContext().getSharedPreferences(PrefConst.USER_PREF, 0);
        Toast.makeText(getContext(), loginPref.getString(PrefConst.ID, "") + "님, 환영합니다.",
                Toast.LENGTH_SHORT).show();

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

                mZoneDrawer = new ZoneDrawer(getContext(), mMap, mMember);

                mMap.setOnCameraMoveStartedListener(MapViewFragment.this);
                mMap.setOnCameraIdleListener(MapViewFragment.this);

//                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//                        new com.google.android.gms.maps.model.LatLng(
//                                37.2635727, 127.02860090000001), ZOOM));
            }
        });

//        mNumFlagsCV = (CardView) v.findViewById(R.id.cv_num_flags);
//        mNumFlagsCV.setAlpha(0.5f);
//        mNumFlagsCV.setVisibility(View.INVISIBLE);
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
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onCameraMoveStarted(int i) {
        mCameraMoveStarted = true;
    }

    @Override
    public void onCameraIdle() {
        if (mCameraMoveStarted) {
            final LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
            mZoneDrawer.drawZones(bounds);

            if (mCheckingDistance >= 0.1 || mFirstStart) {
                mTreasureManager.createTreasure(bounds, mMap);
                mWalkingDistancePrefEditor
                        .putFloat(PrefConst.CHECKING_DISTANCE, 0)
                        .apply();
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
                        Snackbar.make(getView(), "깃발 획득", Snackbar.LENGTH_LONG).show();
                        mDBManager.deleteTreasure(treasureLat, treasureLng);
                        int resultNumFlags = mMember.getNumFlags() + 1;
                        mUserPrefEditor.putInt(PrefConst.NUM_FLAGS, resultNumFlags).apply();
                        mMember.setNumFlags(resultNumFlags);
                        new HttpUpdateNumFlagsTask().execute(mMember);
                        mTreasureManager.displayTreasure(bounds, mMap);
                        displayNumFlags();
                    }
                }
            }

            mCameraMoveStarted = false;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // 현재 위치와 이전 위치 저장
        if (mPrevLatLng == null) {
            mPrevLatLng = new com.google.maps.model.LatLng(
                    location.getLatitude(), location.getLongitude());
            mCurrLatLng = new com.google.maps.model.LatLng(
                    location.getLatitude(), location.getLongitude());
        } else {
            mPrevLatLng.lat = mCurrLatLng.lat;
            mPrevLatLng.lng = mCurrLatLng.lng;
            mCurrLatLng.lat = location.getLatitude();
            mCurrLatLng.lng = location.getLongitude();
        }

        if (mPrevLatLng.lat != mCurrLatLng.lat && mPrevLatLng.lng != mCurrLatLng.lng) {
            double movedDistance = calDistance(mPrevLatLng.lat, mPrevLatLng.lng,
                    mCurrLatLng.lat, mCurrLatLng.lng);
            // 100m 체크 이동거리 업데이트
            mCheckingDistance += movedDistance;
            mWalkingDistancePrefEditor
                    .putFloat(PrefConst.CHECKING_DISTANCE, (float) mCheckingDistance)
                    .apply();

            // 오늘 이동거리 초기화
            if (!isLastUpdatedDateToday()) {
                mWalkingDistancePrefEditor.putFloat(PrefConst.TODAY_DISTANCE, 0).apply();
            }
            // 이동거리 업데이트
            mTotalDistance += movedDistance;
            if (movedDistance > 0) {
                mWalkingDistancePrefEditor.putFloat(PrefConst.TODAY_DISTANCE,
                        mWalkingDistancePref.getFloat(PrefConst.TODAY_DISTANCE, 0) +
                                (float) movedDistance)
                        .apply();
                mWalkingDistancePrefEditor.putFloat(PrefConst.TOTAL_DISTANCE,
                        mWalkingDistancePref.getFloat(PrefConst.TOTAL_DISTANCE, 0) +
                                (float) movedDistance)
                        .apply();
            }
        }

        if (walkState) {
            // 이동거리 및 칼로리 디스플레이
            mDistanceTV.setText(String.format(Locale.getDefault(), "%.2f", mTotalDistance));
            mStepTV.setText(String.format(Locale.getDefault(), "%.2f",
                    calKcal(mMember.getWeight(), mTotalDistance)));

            endLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            mCheckedLocations.add(new LatLng(location.getLatitude(), location.getLongitude()));
            ArrayList<com.google.android.gms.maps.model.LatLng> path =
                    mRoadTracker.getJsonData(startLatLng, endLatLng);
            if (path == null) {
//                Toast.makeText(getContext(), "거리가 너무 짧습니다", Toast.LENGTH_SHORT).show();
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

    public void putFlag() {
        long lLatitude = (long) (mCurrLatLng.lat * Math.pow(10, 14));
        long lLongitude = (long) (mCurrLatLng.lng * Math.pow(10, 14));
        double latitude = (lLatitude - (lLatitude % ZoneConst.LAT_INTERVAL)) / Math.pow(10, 14);
        double longitude = (lLongitude - (lLongitude % ZoneConst.LNG_INTERVAL)) / Math.pow(10, 14);

        new HttpCheckZoneTask().execute(latitude, longitude);
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
        mNumFlagsTV.setText(String.valueOf(mMember.getNumFlags()));
    }

    private boolean isLastUpdatedDateToday() {
        String lastUpdatedDate = mWalkingDistancePref.getString(PrefConst.LAST_UPDATED_DATE, "");
        String todayDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        return lastUpdatedDate.equals(todayDate);
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

    private void updateNumFlags(int numFlags) {
        mUserPrefEditor.putInt(PrefConst.NUM_FLAGS, numFlags).apply();
        mMember.setNumFlags(numFlags);
        new HttpUpdateNumFlagsTask().execute(mMember);
        displayNumFlags();
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

    private class HttpCheckZoneTask extends AsyncTask<Double, Void, String> {

        private static final String MSG_ALREADY_YOURS = "이미 소유 중인 구역입니다.";
        private static final String MSG_NOT_ENOUGH_FLAGS = "깃발이 부족합니다.";
        private static final String MSG_ERROR = "깃발을 꽂지 못했습니다.";

        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage("구역 체크 중...");
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(Double... params) {
            HttpURLConnection conn = null;
            JSONObject zoneJObject = null;
            try {
                conn = (HttpURLConnection) new URL(HttpConst.SERVER_URL + "/checkzone/index.jsp")
                        .openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                JSONObject dataJObject = new JSONObject();
                dataJObject.put("latitude", params[0]);
                dataJObject.put("longitude", params[1]);
                writer.write(JSONManager.getPostDataString(dataJObject));
                writer.flush();
                os.close();
                writer.close();

                Log.d("testTag", "checkZone lat: " + params[0] + ", lng: " + params[1]);

                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String zoneJString = reader.readLine();
                zoneJObject = new JSONObject(zoneJString);
                is.close();
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn != null) conn.disconnect();
            }

            try {
                if (zoneJObject != null) {
                    if (zoneJObject.length() == 0) {
                        if (mMember.getNumFlags() >= 1) {
                            return new JSONObject()
                                    .put("latitude", params[0])
                                    .put("longitude", params[1])
                                    .put("numFlags", 1)
                                    .put("userid", mMember.getId())
                                    .toString();
                        } else {
                            return MSG_NOT_ENOUGH_FLAGS;
                        }
                    } else if (zoneJObject.getString("userid").equals(mMember.getId())) {
                        return MSG_ALREADY_YOURS;
                    } else if (zoneJObject.getInt("numFlags") > mMember.getNumFlags()) {
                        return MSG_NOT_ENOUGH_FLAGS;
                    } else {
                        return new JSONObject()
                                .put("latitude", params[0])
                                .put("longitude", params[1])
                                .put("numFlags", zoneJObject.getInt("numFlags") + 1)
                                .put("userid", mMember.getId())
                                .toString();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String message) {
            mProgressDialog.dismiss();
            if (message == null) {
                Toast.makeText(getContext(), MSG_ERROR, Toast.LENGTH_SHORT).show();
            } else if (message.equals(MSG_ALREADY_YOURS) || message.equals(MSG_NOT_ENOUGH_FLAGS)) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            } else {
                try {
                    PutFlagDialogFragment
                            .newInstance(JSONManager.parseZoneJSON(new JSONObject(message)),
                                    new PutFlagDialogFragment.OnOKButtonClickListener() {
                                        @Override
                                        public void onOKButtonClick(Zone zone) {
                                            new HttpPutFlagTask().execute(zone);
                                        }
                                    })
                            .show(getChildFragmentManager(), "putFlag");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private class HttpPutFlagTask extends AsyncTask<Zone, Void, String> {

        private static final String MSG_SUCCEED = "깃발을 꽂았습니다.";
        private static final String MSG_FAILED = "한 발 늦었습니다.";
        private static final String MSG_ERROR = "깃발을 꽂지 못했습니다.";

        private ProgressDialog mProgressDialog;
        private int usedNumFlags;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage("깃발 꽂는 중...");
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(Zone... params) {
            HttpURLConnection conn = null;
            String result = null;
            usedNumFlags = params[0].getNumFlags();
            try {
                conn = (HttpURLConnection) new URL(HttpConst.SERVER_URL + "/putflag/index.jsp")
                        .openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                JSONObject dataJObject = new JSONObject();
                dataJObject.put("latitude", params[0].getLatitude());
                dataJObject.put("longitude", params[0].getLongitude());
                dataJObject.put("numFlags", params[0].getNumFlags());
                dataJObject.put("userid", params[0].getUserid());
                writer.write(JSONManager.getPostDataString(dataJObject));
                writer.flush();
                os.close();
                writer.close();

                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                result = reader.readLine();
                is.close();
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn != null) conn.disconnect();
            }

            if (result != null) {
                switch (result) {
                    case "succeed":
                        return MSG_SUCCEED;
                    case "failed":
                        return MSG_FAILED;
                    case "error":
                        return MSG_ERROR;
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String message) {
            mProgressDialog.dismiss();
            if (message == null) {
                Toast.makeText(mContext, MSG_ERROR, Toast.LENGTH_SHORT).show();
            } else {
                if (message.equals(MSG_SUCCEED)) {
                    updateNumFlags(mMember.getNumFlags() - usedNumFlags);
                    mZoneDrawer.drawZones(mMap.getProjection().getVisibleRegion().latLngBounds);
                }
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            }
        }

    }

    private class HttpUpdateNumFlagsTask extends AsyncTask<Member, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(Member... params) {
            HttpURLConnection conn = null;
            String result = null;
            try {
                conn = (HttpURLConnection) new URL(HttpConst.SERVER_URL +
                        "/updatenumflags/index.jsp")
                        .openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                JSONObject dataJObject = new JSONObject();
                dataJObject.put("id", params[0].getId());
                dataJObject.put("numFlags", params[0].getNumFlags());
                writer.write(JSONManager.getPostDataString(dataJObject));
                writer.flush();
                os.close();
                writer.close();

                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                result = reader.readLine();
                is.close();
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn != null) conn.disconnect();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String message) {
            if (message == null) {
                Toast.makeText(getContext(), "깃발 수를 업데이트하지 못했습니다.",
                        Toast.LENGTH_SHORT).show();
            } else {
                Log.d("testTag", "updatenumflags: " + message);
            }
        }

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
//            mHandler.sendEmptyMessageDelayed(MapActivity.SENT, 4000);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

//            Log.d(TAG, "In progress update, values: " + values.toString());

//            if (values[0].equals("shutdown")) {
//                mTCPClient.sendMessage(COMMAND);
//                mTCPClient.stopClient();
//                mTCPClient.stop();
//                mHandler.sendEmptyMessageDelayed(MapActivity.SHUTDOWN, 2000);

//            } else {
//                mTCPClient.sendMessage("wrong");
//                mHandler.sendEmptyMessageDelayed(MapActivity.ERROR, 2000);
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
