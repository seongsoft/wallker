package com.seongsoft.wallker.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.seongsoft.wallker.dialog.AddNameDialogFragment;
import com.seongsoft.wallker.fragment.MapRecordFrgment;
import com.seongsoft.wallker.manager.DatabaseManager;
import com.seongsoft.wallker.fragment.MapViewFragment;
import com.seongsoft.wallker.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements
    NavigationView.OnNavigationItemSelectedListener {

    private DatabaseManager mDBManager;

    private boolean isMap = true;
    private FloatingActionButton walkStartFAB;
    private AlertDialog mDialog = null;
    private MapViewFragment mapViewFragment;
    private boolean isGPSButton = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


//        deleteDatabase("wallker.db");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mDBManager = new DatabaseManager(this);

        mapViewFragment = new MapViewFragment();

        getSupportFragmentManager().beginTransaction().add(R.id.container, mapViewFragment)
                .commit();

//        FloatingActionButton switchFAB = (FloatingActionButton) findViewById(R.id.fab_switch);
//        switchFAB.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (isMap) {
//                    getSupportFragmentManager()
//                            .beginTransaction()
//                            .replace(R.id.container, myInfoFragment)
//                            .commit();
//                } else {
//                    getSupportFragmentManager()
//                            .beginTransaction()
//                            .replace(R.id.container, mapViewFragment)
//                            .commit();
//                }
//                isMap = !isMap;
//            }
//        });

        walkStartFAB = (FloatingActionButton) findViewById(R.id.fab_walking);
        walkStartFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mapViewFragment.isWalkOn()) {
                    mapViewFragment.stopLocationUpdates();
                    AddNameDialogFragment dialog = AddNameDialogFragment.newInstance(new AddNameDialogFragment.NameInputListener() {
                        @Override
                        public void onNameInputComplete(String name) {
                            if(name != null) {
                                mapViewFragment.walkStart(name);            //걸음 시작, 걸음 이름 넘겨줌
                                mapViewFragment.changeWalkState();          //걸음 상태 true
                                walkStartFAB.setImageResource(R.drawable.ic_stop);
                                Toast.makeText(getApplicationContext(), "걸음 시작", Toast.LENGTH_SHORT).show();
                                    MenuItem item = (MenuItem) findViewById(R.id.action_location_refresh);
//                                item.setVisible(false);
                            }else{
                                Toast.makeText(getApplicationContext(), "걸음이름은 꼭 입렵해주세야 합니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    dialog.show(getFragmentManager(), "addDialog");
                }
                else{
                    Toast.makeText(getApplicationContext(), "걸음 종료", Toast.LENGTH_SHORT).show();
                    mapViewFragment.changeWalkState();
                    mapViewFragment.walkEnd();
                    walkStartFAB.setImageResource(R.drawable.ic_play);
                }
            }
        });

        setLastUpdateDate();

        // distance 테이블에 레코드가 없을 때 이동거리가 0인 레코드를 하나 만들어줌.
        if (!mDBManager.distanceExists()) mDBManager.insertDistance();
        if (!mDBManager.inventoryExists()) mDBManager.insertInventory();
    //mapViewFragment.stopLocationUpdates();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == R.id.action_location_refresh){
            if(!isGPSButton){
                mapViewFragment.startLocationUpdates();
                isGPSButton = true;
                item.setIcon(R.drawable.ic_gps_fixed_white_24dp);
            }else{
                mapViewFragment.stopLocationUpdates();
                isGPSButton = false;
                item.setIcon(R.drawable.ic_gps_not_fixed_white_24dp);
            }
        }

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            getFragmentManager()
//                    .beginTransaction()
//                    .setCustomAnimations(
//                            R.animator.card_flip_right_in, R.animator.card_flip_right_out,
//                            R.animator.card_flip_left_in, R.animator.card_flip_left_out)
//                    .replace(R.id.container, new TestFragment())
//                    .addToBackStack(null)
//                    .commit();
//            return false;
//        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_record) {
            hideWalkStartFAB();
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new MapRecordFrgment())
                    .addToBackStack(null)
                    .commit();
        }else if(id == R.id.nav_home){
            showWalkStartFAB();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setLastUpdateDate() {
        String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());

        if (mDBManager.dateExists()) {
            String lastUpdateDate = mDBManager.selectDate();
            // 최근 업데이트 날짜가 오늘이 아닌 경우
            if (!currentDate.equals(lastUpdateDate)) mDBManager.updateDate(currentDate);
        } else {
            mDBManager.insertDate(currentDate);
        }
    }
    public void showWalkStartFAB(){
        walkStartFAB.show();
    }
    public void hideWalkStartFAB(){
        walkStartFAB.hide();
    }
}