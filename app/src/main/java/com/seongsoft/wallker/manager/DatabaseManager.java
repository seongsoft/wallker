package com.seongsoft.wallker.manager;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLngBounds;
import com.seongsoft.wallker.beans.Treasure;
import com.seongsoft.wallker.beans.Walking;

import java.util.ArrayList;

/**
 * Created by BeINone on 2016-09-11.
 */
public class DatabaseManager {

    private static final String DB_NAME = "wallker.db";
    public static final int DB_VERSION = 1;

    private static final String TREASURE_TABLE = "treasure";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";

    private static final String DATE_TABLE = "date";
    private static final String LAST_UPDATE_DATE = "last_update_date";

    private static final String WALKING_TABLE = "walking";
    private static final String WALKING_NAME = "walking_name";
    private static final String DISTANCE = "distance";
    private static final String LINES = "lines";
    private static final String DATES = "dates";
    private static final String STEP = "step";
    private static final String SPEEDAVER = "speedaver";
    private static final String NUMFLAG = "numflag";
    private static final String TIME = "time";


    private static final String DISTANCE_TABLE = "distance";
    private static final String TODAY_DISTANCE = "today_distance";
    private static final String TOTAL_DISTANCE = "total_distance";

    private static final String INVENTORY_TABLE = "inventory";
    private static final String NUM_FLAGS = "num_flags";

    private DatabaseHelper mDBHelper;

    private Context mContext;

    public DatabaseManager(Context context) {
        mContext = context;
        mDBHelper = new DatabaseHelper(context, DB_NAME, null, DB_VERSION);
    }

    public void insertWalking(Walking walking){
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String sql = "INSERT INTO " + WALKING_TABLE
                + " ("  + WALKING_NAME + ", " + DISTANCE + ", " + LINES + ", " + STEP + ", "
                + SPEEDAVER + ", " + NUMFLAG + ", " + TIME + ", "+ DATES  + ")"
                + " VALUES ('" + walking.getWlak_name()
                + "', " + walking.getDistance()
                + ", '" + JSONManager.bindJSON(walking.getLines())
                + "', " + walking.getStep()
                +", " + walking.getSppedAverage()
                +", " + walking.getNumflag()
                +", '" + walking.getTime()
                + "', '" + walking.getDate() + "');";
        System.out.println("sql : " + sql);
        db.execSQL(sql);
        db.close();
    }
    public ArrayList<String> selectAllDate(){
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        ArrayList<String> dateList = new ArrayList<>();
        String sql = "SELECT DISTINCT SUBSTR("+ DATES+", 1, 10) AS 'date' FROM " + WALKING_TABLE + " ORDER BY " + DATES + " ASC";
        Log.d("selectAllDate: " , sql);
        Cursor cursor = db.rawQuery(sql, null);
        while(cursor.moveToNext()){
            String date = cursor.getString(cursor.getColumnIndex("date"));
            dateList.add(date);
        }
        return dateList;
    }
    public ArrayList<Walking> selectDayWalking(String date){
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String sql = "SELECT * FROM " + WALKING_TABLE + " WHERE " + DATES + " LIKE " + "'" + date + "%" + "'"
                + " ORDER BY " + DATES + " ASC";

        Cursor cursor1 = db.rawQuery(sql, null);
        ArrayList<Walking> walklist  = new ArrayList<>();
        while(cursor1.moveToNext() ){
            String walk_name = cursor1.getString(cursor1.getColumnIndex(WALKING_NAME));
            double distance = cursor1.getDouble(cursor1.getColumnIndex(DISTANCE));
            ArrayList<com.google.android.gms.maps.model.LatLng> list = JSONManager.parseJSON(cursor1.getString(cursor1.getColumnIndex(LINES)));
            int step = cursor1.getInt(cursor1.getColumnIndex(STEP));
            double speedAver = cursor1.getDouble(cursor1.getColumnIndex(SPEEDAVER));
            int numFlag = cursor1.getInt(cursor1.getColumnIndex(NUMFLAG));
            int time = cursor1.getInt(cursor1.getColumnIndex(TIME));
            String dates = cursor1.getString(cursor1.getColumnIndex(DATES));
            Walking walking = new Walking(walk_name, distance, list, dates, time, numFlag, speedAver, step);
            walklist.add(walking);
        }
        return walklist;
    }

    public void insertTreasure(Treasure treasure) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String sql = "INSERT INTO " + TREASURE_TABLE
                + " VALUES (" + treasure.getLatitude() + ", " + treasure.getLongitude() + ");";
        db.execSQL(sql);
        db.close();
    }

    public ArrayList<Treasure> selectTreasure(LatLngBounds bounds) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String sql = "SELECT * FROM " + TREASURE_TABLE
                + " WHERE " + LATITUDE + ">=" + bounds.southwest.latitude
                + " AND " + LONGITUDE + ">=" + bounds.southwest.longitude
                + " AND " + LATITUDE + "<=" + bounds.northeast.latitude
                + " AND " + LONGITUDE + "<=" + bounds.northeast.longitude + ";";

        Cursor cursor = db.rawQuery(sql, null);

        ArrayList<Treasure> treasures = new ArrayList<>();

        while (cursor.moveToNext()) {
            double latitude = cursor.getDouble(cursor.getColumnIndex(LATITUDE));
            double longitude = cursor.getDouble(cursor.getColumnIndex(LONGITUDE));

            Treasure treasure = new Treasure(latitude, longitude);
            treasures.add(treasure);
        }

        cursor.close();
        db.close();

        return treasures;
    }

    public void deleteTreasure(double latitude, double longitude) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String sql = "DELETE FROM " + TREASURE_TABLE
                + " WHERE " + LATITUDE + "=" + latitude + " AND " + LONGITUDE + "=" + longitude + ";";
        db.execSQL(sql);
        db.close();
    }

    private class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                              int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = "CREATE TABLE " + TREASURE_TABLE + " ("
                    + LATITUDE + " REAL NOT NULL, "
                    + LONGITUDE + " REAL NOT NULL, "
                    + "PRIMARY KEY(" + LATITUDE + ", " + LONGITUDE + ")"
                    + ");";
            db.execSQL(sql);

            sql = "CREATE TABLE " + WALKING_TABLE + " ("
                    + WALKING_NAME + " TEXT NOT NULL, "
                    + DISTANCE + " DOUBLE NOT NULL, "
                    + LINES + " TEXT NOT NULL, "
                    + STEP + " INT NOT NULL, "
                    + SPEEDAVER + " DOUBLE NOT NULL, "
                    + NUMFLAG + " INT NOT NULL, "
                    + TIME + " TEXT NOT NULL, "
                    + DATES + " TEXT NOT NULL PRIMARY KEY);";
            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}