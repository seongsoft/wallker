package com.seongsoft.wallker;

import android.graphics.Color;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.GeoApiContext;
import com.google.maps.RoadsApi;
import com.google.maps.model.LatLng;
import com.google.maps.model.SnappedPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dsm_025 on 2016-09-18.
 */
public class RoadTracker {

    private GoogleMap mMap;
    private List<SnappedPoint> mSnappedPoints;
    private GeoApiContext mGeoApiContext;
    private ArrayList<LatLng> mCapturedLocations = new ArrayList<>();        //지나간 좌표 들을 저장하는 List
    private static final int PAGINATION_OVERLAP = 5;
    private static final int PAGE_SIZE_LIMIT = 100;


    public RoadTracker(GoogleMap map, GeoApiContext geoApiContext){
        mMap = map;
        mGeoApiContext = geoApiContext;
    }

    private void drawSnappedLine(List<SnappedPoint> snappedPoints){
        mSnappedPoints = snappedPoints;

        com.google.android.gms.maps.model.LatLng[] mapPoints =
                new com.google.android.gms.maps.model.LatLng[mSnappedPoints.size()];
        int i = 0;
        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        for (SnappedPoint point : mSnappedPoints) {
            mapPoints[i] = new com.google.android.gms.maps.model.LatLng(point.location.lat,
                    point.location.lng);
            bounds.include(mapPoints[i]);
            i += 1;
        }

        mMap.addPolyline(new PolylineOptions().add(mapPoints).color(Color.BLUE));
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 0));
    }

    private List<SnappedPoint> snapToRoads(GeoApiContext context) throws Exception {
        List<SnappedPoint> snappedPoints = new ArrayList<>();

        int offset = 0;
        while (offset < mCapturedLocations.size()) {
            // Calculate which points to include in this request. We can't exceed the APIs
            // maximum and we want to ensure some overlap so the API can infer a good location for
            // the first few points in each request.
            if (offset > 0) {
                offset -= PAGINATION_OVERLAP;   // Rewind to include some previous points
            }
            int lowerBound = offset;
            int upperBound = Math.min(offset + PAGE_SIZE_LIMIT, mCapturedLocations.size());

            // Grab the data we need for this page.
            LatLng[] page = mCapturedLocations
                    .subList(lowerBound, upperBound)
                    .toArray(new LatLng[upperBound - lowerBound]);

            // Perform the request. Because we have interpolate=true, we will get extra data points
            // between our originally requested path. To ensure we can concatenate these points, we
            // only start adding once we've hit the first new point (i.e. skip the overlap).
            SnappedPoint[] points = RoadsApi.snapToRoads(context, true, page).await();
            boolean passedOverlap = false;
            for (SnappedPoint point : points) {
                if (offset == 0 || point.originalIndex >= PAGINATION_OVERLAP) {
                    passedOverlap = true;
                }
                if (passedOverlap) {
                    snappedPoints.add(point);
                }
            }

            offset = upperBound;
        }

        return snappedPoints;
    }

    public void drawCurrentPath(ArrayList<LatLng> checkedLocations){
        mCapturedLocations = checkedLocations;
        try {
            drawSnappedLine(snapToRoads(mGeoApiContext));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
