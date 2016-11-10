package com.seongsoft.wallker.manager;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.seongsoft.wallker.beans.Zone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BeINone on 2016-10-18.
 */

public class DataManager {

    public static JSONObject createZoneJSONObject(double latitude, double longitude) throws JSONException {
        JSONObject zoneJObject = new JSONObject();
        zoneJObject.put("latitude", latitude);
        zoneJObject.put("longitude", longitude);

        return zoneJObject;
    }

    public static JSONObject createBoundsJSONObject(LatLngBounds bounds) throws JSONException {
        JSONObject messageJObject = new JSONObject();

        JSONObject boundsJObject = new JSONObject();

        JSONObject southwestJObject = new JSONObject();
        southwestJObject.put("latitude", bounds.southwest.latitude);
        southwestJObject.put("longitude", bounds.southwest.longitude);

        boundsJObject.put("southwest", southwestJObject);

        JSONObject northeastJObject = new JSONObject();
        northeastJObject.put("latitude", bounds.northeast.latitude);
        northeastJObject.put("longitude", bounds.northeast.longitude);

        boundsJObject.put("northeast", northeastJObject);

        messageJObject.put("bounds", boundsJObject);

        return messageJObject;
    }

    public static ArrayList<Zone> parseMessage(String message) {
        List<Zone> datas = new ArrayList<>();

        try {
            JSONObject messageJObject = new JSONObject(message);
            if (messageJObject.has("zone")) {
                datas = parseZone(messageJObject.getJSONArray("zone"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return (ArrayList<Zone>) datas;
    }

    // { zone :
    //   [ { southwest : { latitude : x, longitude : y }, northeast : { latitude : x, longitude : y }, userId : z }
    //     { southwest : { latitude : x, longitude : y }, northeast : { latitude : x, longitude : y }, userId : z } ]
    // }

    private static ArrayList<Zone> parseZone(JSONArray zoneJArray) {
        List<Zone> zones = new ArrayList<>();

        try {
            for (int index = 0; index < zoneJArray.length(); index++) {
                JSONObject zoneJObject = zoneJArray.getJSONObject(index);

                JSONObject southwestJObject = zoneJObject.getJSONObject("southwest");
                double southwestLat = southwestJObject.getDouble("latitude");
                double southwestLng = southwestJObject.getDouble("longitude");
                LatLng southwest = new LatLng(southwestLat, southwestLng);

                JSONObject northeastJObject = zoneJObject.getJSONObject("northeast");
                double northeastLat = northeastJObject.getDouble("latitude");
                double northeastLng = northeastJObject.getDouble("longitude");
                LatLng northeast = new LatLng(northeastLat, northeastLng);

                LatLngBounds bounds = new LatLngBounds(southwest, northeast);
                String userId = zoneJObject.getString("userId");

//                zones.add(new Zone(bounds, userId));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return (ArrayList<Zone>) zones;
    }

}
