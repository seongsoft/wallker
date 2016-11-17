package com.seongsoft.wallker.manager;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;

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

/**
 * Created by BeINone on 2016-09-11.
 */
public class TreasureManager {

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
        Treasure treasure = new Treasure(latitude, longitude);
        mDBManager.insertTreasure(treasure);

        displayTreasure(new LatLng(latitude, longitude), map);
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

    private void displayTreasure(LatLng latLng, GoogleMap map) {
        mFlags.add(map.addGroundOverlay(new GroundOverlayOptions()
                .position(latLng, GROUNDOVERLAY_WIDTH)
                .image(getTreasureBitmapDescriptor())));
    }

    private BitmapDescriptor getTreasureBitmapDescriptor() {
        Bitmap treasureBitmap = BitmapUtils.resizeBitmap(mContext, R.drawable.ic_flag, 50, 50);
        return BitmapDescriptorFactory.fromBitmap(treasureBitmap);
    }

}
