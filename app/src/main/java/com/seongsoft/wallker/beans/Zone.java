package com.seongsoft.wallker.beans;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by BeINone on 2016-10-19.
 */

public class Zone implements Parcelable {

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

    public Zone(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        numFlags = in.readInt();
        userid = in.readString();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeInt(numFlags);
        dest.writeString(userid);
    }

    public static final Creator<Zone> CREATOR = new Creator<Zone>() {
        @Override
        public Zone createFromParcel(Parcel source) {
            return new Zone(source);
        }

        @Override
        public Zone[] newArray(int size) {
            return new Zone[size];
        }
    };

}
