package com.seongsoft.wallker.beans;

/**
 * Created by BeINone on 2016-10-10.
 */

public class Member {

    private String id;
    private String password;
    private int weight;
    private int numFlags;

    public Member(String id, String password, int weight, int numFlags) {
        setId(id);
        setPassword(password);
        setWeight(weight);
        setNumFlags(numFlags);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getNumFlags() {
        return numFlags;
    }

    public void setNumFlags(int numFlags) {
        this.numFlags = numFlags;
    }

}
