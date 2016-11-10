package com.seongsoft.wallker.beans;

/**
 * Created by BeINone on 2016-10-19.
 */

public class Zone {

    private double latitude;
    private double longitude;
    private int numFlags;
    private String userid;

    public Zone(double latitude, double longitude, int numFlags, String userid) {
        setLatitude(latitude);
        setLongitude(longitude);
        setNumFlags(numFlags);
        setUserid(userid);
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setNumFlags(int numFlags) {
        this.numFlags = numFlags;
    }

    public int getNumFlags() {
        return numFlags;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUserid() {
        return userid;
    }

}
