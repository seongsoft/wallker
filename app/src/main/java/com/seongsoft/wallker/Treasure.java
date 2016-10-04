package com.seongsoft.wallker;
public class Treasure {


    /**
     * Created by BeINone on 2016-09-16.
     */
    private double latitude;
    private double longitude;

    public Treasure(double latitude, double longitude) {
        setLatitude(latitude);
        setLongitude(longitude);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
