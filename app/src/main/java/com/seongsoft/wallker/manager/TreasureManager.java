package com.seongsoft.wallker.manager;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.GeoApiContext;
import com.seongsoft.wallker.R;
import com.seongsoft.wallker.beans.Treasure;
import com.seongsoft.wallker.utils.BitmapUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by BeINone on 2016-09-11.
 */
public class TreasureManager {

    private static final double MIN_LATITUDE = 34.289166;
    private static final double MAX_LATITUDE = 38.583333;
    private static final double MIN_LONGITUDE = 126.533333;
    private static final double MAX_LONGITUDE = 129.571666;
    private static final double LATITUDE_RANGE = MAX_LATITUDE - MIN_LATITUDE;
    private static final double LONGITUDE_RANGE = MAX_LONGITUDE - MIN_LONGITUDE;
    private static final float GROUNDOVERLAY_WIDTH = 10.0f;

    private Context mContext;
    private GeoApiContext mGeoApiContext;
    private DatabaseManager mDBManager;
    private ProgressDialog mDialog;

    private List<GroundOverlay> mFlags;

    public TreasureManager(Context context, GeoApiContext geoApiContext) {
        mContext = context;
        mGeoApiContext = geoApiContext;
        mDBManager = new DatabaseManager(context);
        mFlags = new ArrayList<>();
    }

    public void createTreasure(LatLngBounds bounds, GoogleMap map) {
        final double latitudeRange = bounds.northeast.latitude - bounds.southwest.latitude;
        final double longitudeRange = bounds.northeast.longitude - bounds.southwest.longitude;

        double latitude = bounds.southwest.latitude + (latitudeRange * Math.random());
        double longitude = bounds.southwest.longitude + (longitudeRange * Math.random());
//        com.google.maps.model.LatLng tresLatLng = new com.google.maps.model.LatLng(latitude, longitude);
//        ArrayList<com.google.android.gms.maps.model.LatLng> points = new RoadTracker(map).getJsonData(tresLatLng, latLng);

        double newLat = latitude;
        double newLng = longitude;
        Treasure treasure = new Treasure(newLat, newLng);
        mDBManager.insertTreasure(treasure);

        displayTreasure(new LatLng(newLat, newLng), map);
//        new CreateTreasureTask().execute();
    }

    public void displayTreasure(LatLng latLng, GoogleMap map) {
        mFlags.add(map.addGroundOverlay(new GroundOverlayOptions()
                .position(latLng, GROUNDOVERLAY_WIDTH)
                .image(getTreasureBitmapDescriptor())));
    }

    public ArrayList<Treasure> displayTreasure(LatLngBounds bounds, GoogleMap map) {
        BitmapDescriptor treasureBitmapDescriptor = getTreasureBitmapDescriptor();

        DatabaseManager dbManager = new DatabaseManager(mContext);
        List<Treasure> treasures = dbManager.selectTreasure(bounds);

        for (GroundOverlay flag : mFlags) {
            flag.remove();
        }

        mFlags = new ArrayList<>();

        for (int index = 0; index < treasures.size(); index++) {
            mFlags.add(map.addGroundOverlay(new GroundOverlayOptions()
                    .position(new LatLng(treasures.get(index).getLatitude(),
                            treasures.get(index).getLongitude()), GROUNDOVERLAY_WIDTH)
                    .image(treasureBitmapDescriptor)));
        }

        return (ArrayList<Treasure>) treasures;
    }



    private class CreateTreasureTask extends AsyncTask<Void, Void, LatLng[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mDialog = new ProgressDialog(mContext);
            mDialog.setMessage("보물 생성중입니다. 잠시만 기다려주세요.");
            mDialog.show();
        }

        @Override
        protected LatLng[] doInBackground(Void... params) {
            LatLng[] treasureLocations = new LatLng[15000];
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
            DatabaseManager dbManager = new DatabaseManager(mContext);

            for (int index = 0; index < 15000; index++) {
                double latitude = MIN_LATITUDE + LATITUDE_RANGE * Math.random();
                double longitude = MIN_LONGITUDE + LONGITUDE_RANGE * Math.random();

                /* size가 계속 0임. 수정 필요 */
//                try {
//                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
//                    // 바다가 아닐 때
//                    if (addresses != null && addresses.size() > 0) {
//                        treasureLocations[index] = new LatLng(latitude, longitude);
//                        index++;
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

//                LatLng location = null;
//
//                try {
//                    location = findRoad(mGeoApiContext, new com.google.maps.model.LatLng(latitude, longitude));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                if (location != null) {
//                    dbManager.insertTreasure(new Treasure(location.latitude, location.longitude));
//                } else {
//                    dbManager.insertTreasure(new Treasure(latitude, longitude));
//                }

                dbManager.insertTreasure(new Treasure(latitude, longitude));

//                treasureLocations[index] = new LatLng(latitude, longitude);
                Log.d("treasure", String.valueOf(index));
            }

            return treasureLocations;
        }

        @Override
        protected void onPostExecute(LatLng[] treasureLocations) {
//            Bitmap treasureBitmap = resizeMarker(R.drawable.treasure, 50, 50);
//            BitmapDescriptor treasureBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(treasureBitmap);
//
//            for (int index = 0; index < treasureLocations.length; index++) {
//                mMap.addMarker(new MarkerOptions()
//                        .position(treasureLocations[index])
//                        .icon(treasureBitmapDescriptor));
//
//                Log.d("marker", String.valueOf(index));
//            }

            mDialog.dismiss();
        }

    }

    private BitmapDescriptor getTreasureBitmapDescriptor() {
        Bitmap treasureBitmap = BitmapUtils.resizeBitmap(mContext, R.drawable.ic_flag, 50, 50);
        return BitmapDescriptorFactory.fromBitmap(treasureBitmap);
    }

}