package com.seongsoft.wallker.manager;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
                + " VALUES (" + walking.getWlak_name()
                + ", " + walking.getDistance()
                + ", " + JSONManager.bindJSON(walking.getLines())
                + ", " + walking.getDate() + ");";
        db.execSQL(sql);
        db.close();
    }

    public void insertTreasure(Treasure treasure) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String sql = "INSERT INTO " + TREASURE_TABLE
                + " VALUES (" + treasure.getLatitude() + ", " + treasure.getLongitude() + ");";
        db.execSQL(sql);
        db.close();
    }

    public void insertDate(String date) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String sql = "INSERT INTO " + DATE_TABLE
                + " VALUES (" + date + ");";
        db.execSQL(sql);
        db.close();
    }

    public void insertDistance() {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String sql = "INSERT INTO " + DISTANCE_TABLE
                + " VALUES (0, 0);";
        db.execSQL(sql);
        db.close();
    }

    public void insertInventory() {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String sql = "INSERT INTO " + INVENTORY_TABLE
                + " VALUES (0);";
        db.execSQL(sql);
        db.close();
    }

    public ArrayList<Walking> selectAllWalking(){
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String sql = "SELECT * FROM " + WALKING_TABLE
                + " ORDER BY " + DATES + " ASC";
        Cursor cursor = db.rawQuery(sql, null);
        ArrayList<Walking> allList  = new ArrayList<>();
        while(cursor.moveToNext()){
            String walk_name = cursor.getString(cursor.getColumnIndex("walk_name"));
            double distance = cursor.getDouble(cursor.getColumnIndex("distance"));
            ArrayList<com.google.android.gms.maps.model.LatLng> list = JSONManager.parseJSON(cursor.getString(cursor.getColumnIndex("lines")));
            String date = cursor.getString(cursor.getColumnIndex("date"));
            Walking walking = new Walking(walk_name, distance, list, date);
            allList.add(walking);
        }
        return allList;
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

    public String selectDate() {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String sql = "SELECT * FROM " + DATE_TABLE + ";";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndex(LAST_UPDATE_DATE));
    }

    public double[] selectDistance() {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String sql = "SELECT * FROM " + DISTANCE_TABLE + ";";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();

        double[] distances = new double[2];
        distances[0] = cursor.getDouble(cursor.getColumnIndex(TODAY_DISTANCE));
        distances[1] = cursor.getDouble(cursor.getColumnIndex(TOTAL_DISTANCE));

        return distances;
    }

    public int selectNumFlags() {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String sql = "SELECT " + NUM_FLAGS + " FROM " + INVENTORY_TABLE + ";";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();

        return cursor.getInt(cursor.getColumnIndex(NUM_FLAGS));
    }

    public void deleteTreasure(double latitude, double longitude) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String sql = "DELETE FROM " + TREASURE_TABLE
                + " WHERE " + LATITUDE + "=" + latitude + " AND " + LONGITUDE + "=" + longitude + ";";
        db.execSQL(sql);
        db.close();
    }

    public void reduceNumFlags(int numFlags) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String sql = "UPDATE " + INVENTORY_TABLE
                + " SET " + NUM_FLAGS + "=" + NUM_FLAGS + "-" + numFlags + ";";
        db.execSQL(sql);
        db.close();
    }

    public void initTodayDistance() {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String sql = "UPDATE " + DISTANCE_TABLE + " SET " + TODAY_DISTANCE + "=0;";
        db.execSQL(sql);
        db.close();
    }

    public void increaseNumFlags(int numFlags) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String sql = "UPDATE " + INVENTORY_TABLE
                + " SET " + NUM_FLAGS + "=" + NUM_FLAGS + "+" + numFlags + ";";
        db.execSQL(sql);
        db.close();
    }

    public void updateDate(String date) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String sql = "UPDATE " + DATE_TABLE + " SET " + LAST_UPDATE_DATE + "=" + date + ";";
        db.execSQL(sql);
        db.close();
    }

    public void updateDistance(double distance) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String sql = "UPDATE " + DISTANCE_TABLE + " SET "
                + TODAY_DISTANCE + "=" + TODAY_DISTANCE + "+" + distance + ", "
                + TOTAL_DISTANCE + "=" + TOTAL_DISTANCE + "+" + distance + ";";
        db.execSQL(sql);
        db.close();
    }

    public boolean dateExists() {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String sql = "SELECT * FROM " + DATE_TABLE + ";";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor == null || cursor.getCount() <= 0) return false;
        else return true;
    }

    public boolean distanceExists() {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String sql = "SELECT * FROM " + DISTANCE_TABLE + ";";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor == null || cursor.getCount() <= 0) return false;
        else return true;
    }

    public boolean inventoryExists() {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String sql = "SELECT * FROM " + INVENTORY_TABLE + ";";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor == null || cursor.getCount() <= 0) return false;
        else return true;
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

            sql = "CREATE TABLE " + DATE_TABLE + " ("
                    + LAST_UPDATE_DATE + " TEXT PRIMARY KEY);";
            db.execSQL(sql);

            sql = "CREATE TABLE " + DISTANCE_TABLE + " ("
                    + TODAY_DISTANCE + " REAL, "
                    + TOTAL_DISTANCE + " REAL);";
            db.execSQL(sql);

            sql = "CREATE TABLE " + WALKING_TABLE + " ("
                    + WALKING_NAME + " TEXT NOT NULL, "
                    + DISTANCE + " REAL NOT NULL, "
                    + LINES + " TEXT NOT NULL, "
                    + DATES + "TEXT NOT NULL PRIMARY KEY);";
            db.execSQL(sql);

            sql = "CREATE TABLE " + INVENTORY_TABLE + " ("
                    + NUM_FLAGS + " INTEGER);";
            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
