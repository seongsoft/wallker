package com.seongsoft.wallker.manager;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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

    public ZoneDrawer(Context context, GoogleMap map, Member member) {
        mContext = context;
        mMap = map;
        mMember = member;
        mPolygons = new ArrayList<>();
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

    public void drawZone(LatLngBounds zoneBounds, String zoneStatus) {
        com.google.android.gms.maps.model.LatLng southwest = zoneBounds.southwest;
        com.google.android.gms.maps.model.LatLng northeast = zoneBounds.northeast;
        com.google.android.gms.maps.model.LatLng southeast =
                new com.google.android.gms.maps.model.LatLng(
                        southwest.latitude, northeast.longitude);
        com.google.android.gms.maps.model.LatLng northwest =
                new com.google.android.gms.maps.model.LatLng(
                        northeast.latitude, southwest.longitude);

        int color = 0;
        if (zoneStatus != null) {
            if (zoneStatus.equals("mine")) {
                color = ContextCompat.getColor(mContext, R.color.mine);
            } else if (zoneStatus.equals("others")) {
                color = ContextCompat.getColor(mContext, R.color.others);
            }
        }

        int location = 0;
        if ((location =
                findPolygonLocation((ArrayList<Polygon>) mPolygons, zoneBounds.southwest)) == -1) {
            if (color == 0) {
                mPolygons.add(mMap.addPolygon(
                        new PolygonOptions().add(southwest, southeast, northeast, northwest)));
            } else {
                mPolygons.add(mMap.addPolygon(new PolygonOptions()
                        .add(southwest, southeast, northeast, northwest)
                        .fillColor(color)));
            }
        } else if (mPolygons.get(location).getFillColor() != color) {
            mPolygons.get(location).setFillColor(color);
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
                    String zoneStatus = null;
                    if (jsonObject.has("userid")) {
                        if (jsonObject.getString("userid").equals(mMember.getId())) {
                            zoneStatus = "mine";
                        } else {
                            zoneStatus = "others";
                        }
                    }
                    drawZone(new LatLngBounds(
                            new LatLng(jsonObject.getDouble("southwestLat"),
                                    jsonObject.getDouble("southwestLng")),
                            new LatLng(jsonObject.getDouble("northeastLat"),
                                    jsonObject.getDouble("northeastLng"))), zoneStatus);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
