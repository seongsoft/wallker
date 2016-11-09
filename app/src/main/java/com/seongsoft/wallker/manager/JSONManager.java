package com.seongsoft.wallker.manager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

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
}
