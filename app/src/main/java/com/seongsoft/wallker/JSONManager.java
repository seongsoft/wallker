package com.seongsoft.wallker;

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
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                com.google.android.gms.maps.model.LatLng latLng =
                        new com.google.android.gms.maps.model.LatLng(Double.parseDouble(jsonObject.get("lat").toString()), Double.parseDouble(jsonObject.get("lat").toString()));
                list.add(latLng);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return list;
    }
}
