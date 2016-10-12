package com.seongsoft.wallker;

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
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private DatabaseManager mDBManager;
    private AlertDialog mDialog;

    private boolean isMap = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        deleteDatabase("wallker.db");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mDBManager = new DatabaseManager(this);

        final MapViewFragment mapViewFragment = new MapViewFragment();
        final MyInfoFragment myInfoFragment = new MyInfoFragment();

        getSupportFragmentManager().beginTransaction().add(R.id.container, mapViewFragment)
                .commit();

        FloatingActionButton switchFAB = (FloatingActionButton) findViewById(R.id.fab_switch);
        switchFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMap) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.container, myInfoFragment)
                            .commit();
                } else {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.container, mapViewFragment)
                            .commit();
                }
                isMap = !isMap;
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_walking);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mapViewFragment.isWalkOn()) {
                    Toast.makeText(getApplicationContext(), "걸음 시작", Toast.LENGTH_SHORT).show();
//                    LayoutInflater inflater = getLayoutInflater();
//                    final View dialogView = inflater.inflate(R.layout.dialog_walking_name, null);
//                    AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
//                    builder.setTitle("걸음명");
//                    builder.setView(dialogView);
//                    builder.setPositiveButton("Complite", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            EditText editText = (EditText)dialogView.findViewById(R.id.dialog_edit);
//                        }
//                    });
//                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                        //Dialog에 "Cancel"이라는 타이틀의 버튼을 설정
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            // TODO Auto-generated method stub
//                            //멤버 정보의 입력을 취소하고 Dialog를 종료하는 작업
//                            //취소하였기에 특별한 작업은 없고 '취소'했다는 메세지만 Toast로 출력
//                            Toast.makeText(MainActivity.this, "멤버 추가를 취소합니다", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                    AlertDialog dialog = builder.create();

                    mapViewFragment.walkStart("beinone");
                }
                else{
                    Toast.makeText(getApplicationContext(), "걸음 종료", Toast.LENGTH_SHORT).show();

                }
                mapViewFragment.changeWalkState();
            }
        });

        setLastUpdateDate();

        // distance 테이블에 레코드가 없을 때 이동거리가 0인 레코드를 하나 만들어줌.
        if (!mDBManager.distanceExists()) mDBManager.insertDistance();
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

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

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

}
