package com.seongsoft.wallker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by BeINone on 2016-09-10.
 */
public class Person implements
        ActivityCompat.OnRequestPermissionsResultCallback,
        LocationListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private Location mCurrentLocation;

    private boolean mPermissionDenied;

    PersonLocationListener mListener;

    public Person(Context context, GoogleApiClient googleApiClient, PersonLocationListener listener) {
        mContext = context;
        mGoogleApiClient = googleApiClient;
        mListener = listener;

        createLocationRequest();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
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

    @Override
    public void onLocationChanged(Location location) {
        Log.i("LocationChanged", "Latitude: " + location.getLatitude()
                + ", Longitude: " + location.getLongitude());
        Toast.makeText(mContext, "Latitude: " + location.getLatitude()
                + ", Longitude: " + location.getLongitude(), Toast.LENGTH_SHORT).show();

        mCurrentLocation = location;

        mListener.onPersonLocationChanged(location);
    }

    public void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission((AppCompatActivity) mContext,
                    LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    true);
        }

        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        // Start location updates.
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

        if (mCurrentLocation != null) {
            Log.i("Location", "Latitude: " + mCurrentLocation.getLatitude()
                    + ", Longitude: " + mCurrentLocation.getLongitude());
        }
    }

    public void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    public boolean isPermissionDenied() { return mPermissionDenied; }

    public void setPermissionDenied(boolean permissionDenied) { mPermissionDenied = permissionDenied; }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * 위치 변경이 감지되었을 때 알림
     */
    public interface PersonLocationListener {
        void onPersonLocationChanged(Location location);
    }

}
