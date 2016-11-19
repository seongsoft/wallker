package com.seongsoft.wallker.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.seongsoft.wallker.constants.PrefConst;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        getSharedPreferences(PrefConst.APP_INFO_PREF, 0).edit().clear().apply();
//        getSharedPreferences(PrefConst.USER_PREF, 0).edit().clear().apply();
//        getSharedPreferences(PrefConst.WALKING_DISTANCE_PREF, 0).edit().clear().apply();

        SharedPreferences loginPref = getSharedPreferences(PrefConst.USER_PREF, 0);
        if (!loginPref.getString(PrefConst.ID, "").equals("")) {
            startActivity(new Intent(MainActivity.this, MapActivity.class));
        } else {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
        finish();
    }

}
