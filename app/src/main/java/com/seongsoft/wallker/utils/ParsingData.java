package com.seongsoft.wallker.utils;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.seongsoft.wallker.beans.Zone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BeINone on 2016-10-17.
 */

public class ParsingData {

    public void parseMessage(String message) {
        try {
            JSONObject messageJObject = new JSONObject(message);
            if (messageJObject.has("zone")) {
                parseZone(messageJObject.getJSONArray("zone"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // { zone :
    //   [ { southwest : { latitude : x, longitude : y }, northeast : { latitude : x, longitude : y }, userId : z },
    //     { southwest : { latitude : x, longitude : y }, northeast : { latitude : x, longitude : y }, userId : z } ]
    // }

    private ArrayList<Zone> parseZone(JSONArray zoneJArray) {
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
                String userid = zoneJObject.getString("userid");

//                zones.add(new Zone(bounds, userid));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return (ArrayList<Zone>) zones;
    }

}
