package com.seongsoft.wallker.manager;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.seongsoft.wallker.R;
import com.seongsoft.wallker.beans.Member;
import com.seongsoft.wallker.constants.HttpConst;
import com.seongsoft.wallker.constants.ZoneConst;

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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by BeINone on 2016-11-17.
 */

public class ZoneDrawer {

    private Context mContext;
    private GoogleMap mMap;
    private Member mMember;

    private List<Polygon> mPolygons;
    private List<Marker> mMarkers;

    public ZoneDrawer(Context context, GoogleMap map, Member member) {
        mContext = context;
        mMap = map;
        mMember = member;
        mPolygons = new ArrayList<>();
        mMarkers = new ArrayList<>();
    }

    public void drawZones(LatLngBounds bounds) {
        long mapSouthwestLat = (long) (bounds.southwest.latitude * Math.pow(10, 14));
        long mapSouthwestLng = (long) (bounds.southwest.longitude * Math.pow(10, 14));

        double southwestLat = (mapSouthwestLat - (mapSouthwestLat % ZoneConst.LAT_INTERVAL)) / Math.pow(10, 14);
        double southwestLng = (mapSouthwestLng - (mapSouthwestLng % ZoneConst.LNG_INTERVAL)) / Math.pow(10, 14);
        double northeastLat = (southwestLat * Math.pow(10, 14) + ZoneConst.LAT_INTERVAL) / Math.pow(10, 14);
        double northeastLng = (southwestLng * Math.pow(10, 14) + ZoneConst.LNG_INTERVAL) / Math.pow(10, 14);
//        double northeastLat = southwestLat + D_LAT_INTERVAL;
//        double northeastLng = southwestLng + D_LNG_INTERVAL;

        double currSouthwestLng = southwestLng;
        double currNortheastLng = northeastLng;
        while (currSouthwestLng <= bounds.northeast.longitude) {
            double currSouthwestLat = southwestLat;
            double currNortheastLat = northeastLat;
            while (currSouthwestLat <= bounds.northeast.latitude) {
                LatLng southwest = new LatLng(currSouthwestLat, currSouthwestLng);
                LatLng northeast = new LatLng(currNortheastLat, currNortheastLng);

                new HttpDrawZoneTask().execute(new LatLngBounds(southwest, northeast));
//                drawZone(new LatLngBounds(southwest, northeast));

                currSouthwestLat = (currSouthwestLat * Math.pow(10, 14) + ZoneConst.LAT_INTERVAL) / Math.pow(10, 14);
                currNortheastLat = (currNortheastLat * Math.pow(10, 14) + ZoneConst.LAT_INTERVAL) / Math.pow(10, 14);
//                currSouthwestLat += D_LAT_INTERVAL;
//                currNortheastLat += D_LAT_INTERVAL;
            }

            currSouthwestLng = (currSouthwestLng * Math.pow(10, 14) + ZoneConst.LNG_INTERVAL) / Math.pow(10, 14);
            currNortheastLng = (currNortheastLng * Math.pow(10, 14) + ZoneConst.LNG_INTERVAL) / Math.pow(10, 14);
//            currSouthwestLng += D_LNG_INTERVAL;
//            currNortheastLng += D_LNG_INTERVAL;
        }
    }

    public void drawZone(LatLngBounds zoneBounds, int numFlags, String userid) {
        LatLng southwest = zoneBounds.southwest;
        LatLng northeast = zoneBounds.northeast;
        LatLng southeast = new LatLng(southwest.latitude, northeast.longitude);
        LatLng northwest = new LatLng(northeast.latitude, southwest.longitude);

        int color = 0;
        if (userid != null) {
            if (userid.equals(mMember.getId())) {
                color = ContextCompat.getColor(mContext, R.color.mine);
            } else {
                color = ContextCompat.getColor(mContext, R.color.others);
            }
        }

        double markerLat = southwest.latitude + ZoneConst.MARKER_LAT_INTERVAL;
        double markerLng = southwest.longitude + ZoneConst.MARKER_LNG_INTERVAL;
        int polygonLoc = 0;
        if ((polygonLoc = findPolygonLocation((ArrayList<Polygon>) mPolygons,
                zoneBounds.southwest)) == -1) {
            if (userid == null) {
                mPolygons.add(mMap.addPolygon(
                        new PolygonOptions().add(southwest, southeast, northeast, northwest)
                                .strokeWidth(3.0f)
                                .strokeColor(ContextCompat.getColor(mContext,
                                        R.color.colorPrimaryDark))));
            } else {
                mPolygons.add(mMap.addPolygon(new PolygonOptions()
                        .add(southwest, southeast, northeast, northwest)
                        .fillColor(color)
                        .strokeWidth(3.0f)
                        .strokeColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark))));
                mMarkers.add(mMap.addMarker(new MarkerOptions()
                        .title(userid + "   " + numFlags + "개")
                        .position(new LatLng(markerLat, markerLng))));
            }
        } else {
            if (mPolygons.get(polygonLoc).getFillColor() != color) {
                mPolygons.get(polygonLoc).setFillColor(color);
            }
            int markerLoc = findMarkerLocation((ArrayList<Marker>) mMarkers,
                    new LatLng(markerLat, markerLng));
            if (markerLoc > -1 &&
                    mMarkers.get(markerLoc).getTitle().equals(userid + "   " + numFlags + "개")) {
                mMarkers.get(markerLoc).setTitle(userid + "   " + numFlags + "개");
            }
        }

    }

    private int findPolygonLocation(ArrayList<Polygon> polygons, LatLng southwest) {
        for (int location = 0; location < polygons.size(); location++) {
            if (polygons.get(location) != null &&
                    polygons.get(location).getPoints().get(0).latitude == southwest.latitude &&
                    polygons.get(location).getPoints().get(0).longitude == southwest.longitude) {
                return location;
            }
        }

        return -1;
    }

    private int findMarkerLocation(ArrayList<Marker> markers, LatLng latLng) {
        for (int location = 0; location < markers.size(); location++) {
            if (markers.get(location) != null &&
                    markers.get(location).getPosition().latitude == latLng.latitude &&
                    markers.get(location).getPosition().longitude == latLng.longitude) {
                return location;
            }
        }

        return -1;
    }

    private class HttpDrawZoneTask extends AsyncTask<LatLngBounds, Void, JSONObject> {

        private static final String MSG_ERROR = "구역을 불러오지 못했습니다.";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected JSONObject doInBackground(LatLngBounds... params) {
            HttpURLConnection conn = null;
            JSONObject zoneJObject = null;
            try {
                conn = (HttpURLConnection) new URL(HttpConst.SERVER_URL + "/drawzone/index.jsp")
                        .openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                JSONObject dataJObject = new JSONObject();
                dataJObject.put("latitude", params[0].southwest.latitude);
                dataJObject.put("longitude", params[0].southwest.longitude);
                writer.write(JSONManager.getPostDataString(dataJObject));
                writer.flush();
                os.close();
                writer.close();

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
                    JSONObject jsonObject = new JSONObject()
                            .put("southwestLat", params[0].southwest.latitude)
                            .put("southwestLng", params[0].southwest.longitude)
                            .put("northeastLat", params[0].northeast.latitude)
                            .put("northeastLng", params[0].northeast.longitude);
                    if (zoneJObject.length() > 0) {
                        jsonObject.put("numFlags", zoneJObject.getInt("numFlags"))
                                .put("userid", zoneJObject.getString("userid"));
                    }
                    return jsonObject;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject == null) {
                Toast.makeText(mContext, MSG_ERROR, Toast.LENGTH_SHORT).show();
            } else {
                try {
                    int numFlags = 0;
                    String userid = null;
                    if (jsonObject.has("numFlags") && jsonObject.has("userid")) {
                        numFlags = jsonObject.getInt("numFlags");
                        userid = jsonObject.getString("userid");
                    }
                    drawZone(new LatLngBounds(
                            new LatLng(jsonObject.getDouble("southwestLat"),
                                    jsonObject.getDouble("southwestLng")),
                            new LatLng(jsonObject.getDouble("northeastLat"),
                                    jsonObject.getDouble("northeastLng"))), numFlags, userid);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
