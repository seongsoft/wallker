package com.seongsoft.wallker.manager;

import com.seongsoft.wallker.beans.Member;
import com.seongsoft.wallker.beans.Zone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by dsm_025 on 2016-10-04.
 */

public class JSONManager {

    public static String bindJSON(ArrayList<com.google.android.gms.maps.model.LatLng> list){
        JSONArray jsonArray = new JSONArray();
        for(int i = 0; i < list.size(); i++){
            JSONObject latJson = new JSONObject();
            JSONObject lngJson = new JSONObject();
            try{
                latJson.put("lat", list.get(i).latitude);
                lngJson.put("lng", list.get(i).longitude);
            }catch (JSONException e){
                e.printStackTrace();
            }
            jsonArray.put(latJson);
            jsonArray.put(lngJson);
        }
        return jsonArray.toString();
    }

    public static ArrayList<com.google.android.gms.maps.model.LatLng> parseJSON(String data){
        ArrayList<com.google.android.gms.maps.model.LatLng> list = new ArrayList<>();
        try{
            JSONArray jsonArray = new JSONArray(data);
            for(int i = 0; i < jsonArray.length(); i+=2){
                JSONObject latObject = jsonArray.getJSONObject(i);
                JSONObject lngObject = jsonArray.getJSONObject(i + 1);
                com.google.android.gms.maps.model.LatLng latLng =
                        new com.google.android.gms.maps.model.LatLng(Double.parseDouble(latObject.get("lat").toString()), Double.parseDouble(lngObject.get("lng").toString()));
                list.add(latLng);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return list;
    }

    public static Member parseMemberJSON(JSONObject memberJSON) throws JSONException {
        String id = memberJSON.getString("id");
        String password = memberJSON.getString("password");
        int weight = memberJSON.getInt("weight");
        int numFlags = memberJSON.getInt("numFlags");

        return new Member(id, password, weight, numFlags);
    }

    public static Zone parseZoneJSON(JSONObject zoneJSON) throws JSONException {
        double latitude = zoneJSON.getDouble("latitude");
        double longitude = zoneJSON.getDouble("longitude");
        int numFlags = zoneJSON.getInt("numFlags");
        String userid = zoneJSON.getString("userid");

        return new Zone(latitude, longitude, numFlags, userid);
    }

    public static String getPostDataString(JSONObject dataJSON)
            throws JSONException, UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean isFirst = true;

        Iterator<String> itr = dataJSON.keys();
        while (itr.hasNext()) {
            String key = itr.next();
            Object value = dataJSON.get(key);

            if (isFirst) isFirst = false;
            else result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));
        }

        return result.toString();
    }

}
