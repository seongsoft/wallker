package com.seongsoft.wallker;

import java.util.ArrayList;

/**
 * Created by dsm_025 on 2016-10-04.
 */

public class Walking {
    private String walk_name;
    private double distance;
    private ArrayList<com.google.android.gms.maps.model.LatLng> lines;
    private String date;

    public Walking(String walk_name, double distance, ArrayList<com.google.android.gms.maps.model.LatLng> lines, String date){
        this.walk_name = walk_name;
        this.distance = distance;
        this.lines = lines;
        this.date = date;
    }

    public ArrayList<com.google.android.gms.maps.model.LatLng> getLines(){
        return lines;
    }
    public double getDistance(){
        return distance;
    }
    public String getWlak_name(){
        return walk_name;
    }
    public String getDate(){
        return date;
    }


}
