package com.seongsoft.wallker;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;

/**
 * Created by BeINone on 2016-09-11.
 */
public class DatabaseManager {

    private static final String DB_NAME = "wallker.db";
    private static final String TREASURE_TABLE = "treasure";
    private static final String DATE_TABLE = "date";
    public static final int DB_VERSION = 1;

    private static final String WALKING_TABLE = "walking";
    private static final String WALKING_NAME = "walking_name";
    private static final String DISTANCE = "distance";
    private static final String LINES = "lines";
    private static final String DATES = "dates";

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
                + " VAULES (" + date + ");";
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
//    public ArrayList<Walking> selectWalkingByDate(long data){
//
//    }

    public ArrayList<Treasure> selectTreasure(LatLngBounds bounds) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String sql = "SELECT * FROM " + TREASURE_TABLE
                + " WHERE latitude>=" + bounds.southwest.latitude
                + " AND longitude>=" + bounds.southwest.longitude
                + " AND latitude<=" + bounds.northeast.latitude
                + " AND longitude<=" + bounds.northeast.longitude + ";";

        Cursor cursor = db.rawQuery(sql, null);

        ArrayList<Treasure> treasures = new ArrayList<>();

        while (cursor.moveToNext()) {
            double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
            double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));

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
                + " WHERE latitude=" + latitude + "AND longitude=" + longitude + ";";
        db.execSQL(sql);
        db.close();
    }

    public void updateDate(String date) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String sql = "UPDATE " + DATE_TABLE + " SET last_update_date=" + date + ";";
        db.execSQL(sql);
        db.close();
    }

    public String selectDate() {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String sql = "SELECT * FROM " + DATE_TABLE;
        Cursor cursor = db.rawQuery(sql, null);
        return cursor.getString(cursor.getColumnIndex("last_update_date"));
    }

    private class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                              int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = "CREATE TABLE " + TREASURE_TABLE + " ("
                    + "latitude REAL NOT NULL, "
                    + "longitude REAL NOT NULL, "
                    + "PRIMARY KEY(latitude, longitude)"
                    + ");";
            db.execSQL(sql);

            sql = "CREATE TABLE " + DATE_TABLE + " ("
                    + "last_update_date TEXT PRIMARY KEY);";
            db.execSQL(sql);

            sql = "CREATE TABLE " + WALKING_TABLE + " ("
                    + WALKING_NAME + " TEXT NOT NULL, "
                    + DISTANCE + " DOUBLE NOT NULL, "
                    + LINES + " TEXT NOT NULL, "
                    + DATES + "TEXT NOT NULL PRIMARY KEY);";
            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
