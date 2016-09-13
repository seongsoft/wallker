package com.seongsoft.wallker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by BeINone on 2016-09-11.
 */
public class DatabaseManager {

    private static final String DB_NAME = "wallker.db";
    private static final String TABLE_NAME = "treasure";
    public static final int DB_VERSION = 1;

    private DatabaseHelper mDBHelper;

    private Context mContext;

    public DatabaseManager(Context context) {
        mContext = context;
        mDBHelper = new DatabaseHelper(context, DB_NAME, null, DB_VERSION);
    }

    public void insertTreasure(double latitude, double longitude) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String sql = "INSERT INTO " + TABLE_NAME
                + " VALUES (" + latitude + ", " + longitude + ");";

        db.execSQL(sql);
    }

    public void deleteTreasure(double latitude, double longitude) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String sql = "DELETE FROM " + TABLE_NAME
                + " WHERE latitude=" + latitude + "AND longitude=" + longitude + ";";

        db.execSQL(sql);
    }

    private class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                              int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = "CREATE TABLE " + TABLE_NAME + " ("
                    + "latitude REAL NOT NULL, "
                    + "longitude REAL NOT NULL, "
                    + "PRIMARY KEY(latitude, longitude)"
                    + ");";

            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

    }

}
