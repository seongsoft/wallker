package com.seongsoft.wallker.beans;

/**
 * Created by BeINone on 2016-11-21.
 */

public class Ranking {

    private int ranking;
    private String id;
    private int numZones;

    public Ranking(int ranking, String id, int numZones) {
        this.ranking = ranking;
        this.id = id;
        this.numZones = numZones;
    }

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNumZones() {
        return numZones;
    }

    public void setNumZones(int numZones) {
        this.numZones = numZones;
    }

}
