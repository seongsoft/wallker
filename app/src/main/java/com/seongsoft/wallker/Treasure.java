package com.seongsoft.wallker;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

/**
 * Created by BeINone on 2016-09-11.
 */
public class Treasure {

    private static final double MIN_LATITUDE = 34.289166;
    private static final double MAX_LATITUDE = 38.583333;
    private static final double MIN_LONGITUDE = 126.533333;
    private static final double MAX_LONGITUDE = 129.571666;
    private static final double LATITUDE_RANGE = MAX_LATITUDE - MIN_LATITUDE;
    private static final double LONGITUDE_RANGE = MAX_LONGITUDE - MIN_LONGITUDE;

    private Context mContext;
    private DatabaseManager mDBManager;
    private GoogleMap mMap;
    private ProgressDialog mDialog;

    public Treasure(Context context, GoogleMap googleMap) {
        mContext = context;
        mMap = googleMap;
        mDBManager = new DatabaseManager(context);
    }

    public void createTreasure() {
        new CreateTreasureTask().execute();
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

                treasureLocations[index] = new LatLng(latitude, longitude);
                Log.d("treasure", String.valueOf(index));
            }

            return treasureLocations;
        }

        @Override
        protected void onPostExecute(LatLng[] treasureLocations) {
            Bitmap treasureBitmap = resizeMarker(R.drawable.treasure, 50, 50);
            BitmapDescriptor treasureBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(treasureBitmap);

            for (int index = 0; index < treasureLocations.length; index++) {
                mMap.addMarker(new MarkerOptions()
                        .position(treasureLocations[index])
                        .icon(treasureBitmapDescriptor));

                Log.d("marker", String.valueOf(index));
            }

            mDialog.dismiss();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(treasureLocations[0], 18));
        }

    }

    private Bitmap resizeMarker(int id, int width, int height) {
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), id);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);

        return resizedBitmap;
    }

}
