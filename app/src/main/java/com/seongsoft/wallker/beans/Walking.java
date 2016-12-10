package com.seongsoft.wallker.beans;

import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by dsm_025 on 2016-10-04.
 */

public class Walking{
    private String walk_name;
    private double distance;
    private ArrayList<com.google.android.gms.maps.model.LatLng> lines;
    private String date;
    private int time;
    private double sppedAverage;
    private int step;
    private int numflag;

    public Walking(String walk_name, double distance,
                   ArrayList<com.google.android.gms.maps.model.LatLng> lines,
                   String date, int time, int numflag, double speedAver, int step){
        this.walk_name = walk_name;
        this.distance = distance;
        this.lines = lines;
        this.step = step;
        this.sppedAverage = speedAver;
        this.numflag = numflag;
        this.time = time;
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
    public int getStep(){return step;}
    public double getSppedAverage(){return sppedAverage;}
    public int getNumflag(){return numflag;}
    public int getTime(){return time;}
}
