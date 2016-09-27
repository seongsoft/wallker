package com.seongsoft.wallker;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.GeoApiContext;
import com.google.maps.RoadsApi;
import com.google.maps.model.SnappedPoint;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    private Context mContext;
    private GeoApiContext mGeoApiContext;
    private DatabaseManager mDBManager;
    private ProgressDialog mDialog;

    private GoogleMap mMap;

    public TreasureManager(Context context, GeoApiContext geoApiContext) {
        mContext = context;
        mGeoApiContext = geoApiContext;
        mDBManager = new DatabaseManager(context);
    }

    public void createTreasure() {
        new CreateTreasureTask().execute();
    }

    public void displayTreasure(GoogleMap map, LatLngBounds bounds) {
        List<Treasure> treasures = mDBManager.selectTreasure(bounds);

        Bitmap treasureBitmap = resizeMarker(R.drawable.treasure, 50, 50);
        BitmapDescriptor treasureBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(treasureBitmap);

        map.clear();

        for (Treasure treasure : treasures) {
            map.addMarker(new MarkerOptions()
                    .position(new LatLng(treasure.getLatitude(), treasure.getLongitude()))
                    .icon(treasureBitmapDescriptor));
        }
    }

    private Bitmap resizeMarker(int id, int width, int height) {
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), id);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);

        return resizedBitmap;
    }

    private void setLastUpdateDate() {
        try {
            FileOutputStream fos = mContext.openFileOutput(DataConst.LAST_UPDATE_DATE_FILE_NAME,
                    Context.MODE_PRIVATE);
            String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
            fos.write(currentDate.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private LatLng findRoad(GeoApiContext context, com.google.maps.model.LatLng location) throws Exception {
        com.google.maps.model.LatLng[] page = {location};

        SnappedPoint[] points = RoadsApi.snapToRoads(context, true, page).await();

        return new LatLng(points[0].location.lat, points[0].location.lng);
    }

    private class CreateTreasureTask extends AsyncTask<Void, Void, LatLng[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mDialog = new ProgressDialog(mContext);
            mDialog.setMessage("보물 생성중입니다. 잠시만 기다려주세요.");
            mDialog.show();
        }

//        @Override
//        protected ArrayList<double[]> doInBackground(Void... params) {
//            Random rand = new Random(System.currentTimeMillis());
//            ArrayList<double[]> treasureLocations = new ArrayList<>();
//
//            int cnt = 0;
//
//            for (double minLatitude = MIN_LATITUDE, minLongitude = MIN_LONGITUDE,
//                 maxLatitude = MIN_LATITUDE + 0.01, maxLongitude = MIN_LATITUDE + 0.02;
//                 maxLatitude <= MAX_LATITUDE && maxLongitude <= MAX_LONGITUDE;
//                 minLatitude += 0.01, minLongitude += 0.02, maxLatitude += 0.01, maxLongitude += 0.02) {
//                for (int i = 0; i < 10; i++) {
//                    double randLatitude = minLatitude + (maxLatitude - minLatitude) * rand.nextDouble();
//                    double randLongitude = minLongitude + (maxLongitude - minLongitude) + rand.nextDouble();
//
////                mDBManager.insertTreasure(randLatitude, randLongitude);
//
//                    treasureLocations.add(new double[]{randLatitude, randLongitude});
//                    cnt++;
//                }
//
//                Log.d("treasure", "latitude: " + maxLatitude + ", longitude: " + maxLongitude);
//                Log.d("count", String.valueOf(cnt));
//            }
//
//            return treasureLocations;
//        }

//        @Override
//        protected void onPostExecute(ArrayList<double[]> treasureLocations) {
//            Bitmap treasureBitmap = resizeMarker(R.drawable.treasure, 100, 100);
//            BitmapDescriptor treasureBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(treasureBitmap);
//
//            for (int index = 0; index < treasureLocations.size(); index++) {
//                double latitude = treasureLocations.get(index)[0];
//                double longitude = treasureLocations.get(index)[1];
//
//                mMap.addGroundOverlay(new GroundOverlayOptions()
//                        .position(new LatLng(latitude, longitude), 10f)
//                        .image(treasureBitmapDescriptor));
//
//                Log.d("marker", String.valueOf(latitude));
//            }
//
//            mDialog.dismiss();
//        }

//        @Override
//        protected ArrayList<LatLng> doInBackground(Void... params) {
//            ArrayList<LatLng> treasureLocations = new ArrayList<>();
//
//            for (int count = 0; count < 15000; count++) {
//                double latitude = MIN_LATITUDE + LATITUDE_RANGE * Math.random();
//                double longitude = MIN_LONGITUDE + LONGITUDE_RANGE * Math.random();
//                LatLng location = new LatLng(latitude, longitude);
//
//                treasureLocations.add(location);
//
//                Log.d("treasure", String.valueOf(count));
//            }
//
//            return treasureLocations;
//        }

//        @Override
//        protected void onPostExecute(ArrayList<LatLng> treasureLocations) {
//            Bitmap treasureBitmap = resizeMarker(R.drawable.treasure, 100, 100);
//            BitmapDescriptor treasureBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(treasureBitmap);
//
//            for (int index = 0; index < treasureLocations.size(); index++) {
//                mMap.addGroundOverlay(new GroundOverlayOptions()
//                        .position(treasureLocations.get(index), 10f)
//                        .image(treasureBitmapDescriptor));
//
//                Log.d("groundOverlay", String.valueOf(index));
//            }
//
//            mDialog.dismiss();
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(treasureLocations.get(0), 18));
//        }

//        @Override
//        protected LatLng[] doInBackground(Void... params) {
//            LatLng[] treasureLocations = new LatLng[15000];
//
//            for (int index = 0; index < 15000; index++) {
//                double latitude = MIN_LATITUDE + LATITUDE_RANGE * Math.random();
//                double longitude = MIN_LONGITUDE + LONGITUDE_RANGE * Math.random();
//                LatLng location = new LatLng(latitude, longitude);
//
//                treasureLocations[index] = location;
//
//                Log.d("treasure", String.valueOf(index));
//            }
//
//            return treasureLocations;
//        }

//        @Override
//        protected void onPostExecute(LatLng[] treasureLocations) {
//            Bitmap treasureBitmap = resizeMarker(R.drawable.treasure, 100, 100);
//            BitmapDescriptor treasureBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(treasureBitmap);
//
//            for (int index = 0; index < treasureLocations.length; index++) {
//                mMap.addGroundOverlay(new GroundOverlayOptions()
//                        .position(treasureLocations[index], 20)
//                        .image(treasureBitmapDescriptor));
//
//                Log.d("groundOverlay", String.valueOf(index));
//            }
//
//            mDialog.dismiss();
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(treasureLocations[0], 18));
//        }

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

            setLastUpdateDate();

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

}

