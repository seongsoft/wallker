package com.seongsoft.wallker.fragment;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.seongsoft.wallker.R;
import com.seongsoft.wallker.beans.User;
import com.seongsoft.wallker.beans.Walking;
import com.seongsoft.wallker.manager.DatabaseManager;
import com.seongsoft.wallker.manager.TreasureManager;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by dsm_025 on 2016-10-15.
 */

public class MapRecordFrgment extends Fragment implements
        View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
        {
    private MapView mMapView;
    private GoogleMap mMap;
    private LinearLayout mBottomBar;
    private DatabaseManager mDBManager;
    private FloatingActionButton mShowDateRecordFAB;
    private ImageView mLeftButton;
    private ImageView mRightButton;
    private ArrayList<String> dateList;
    private int currentCnt;
    private ArrayList<Walking> walkList;
    private GoogleApiClient mGoogleApiClient;


    private Context mContext;

    private CardView mNumFlagsRecordCV;
    private CardView mTimeRecordCV;
    private CardView mDistanceRecordCV;
    private CardView mStepRecordCV;
    private CardView mSpeedRecordCV;

    private TextView mNumFlagsRecordTV;
    private TextView mTimeRecordTV;
    private TextView mDistanceRecordTV;
    private TextView mStepRecordTV;
    private TextView mSpeedRecordTV;
    private TextView mWalkingNameTV;
    private TextView mDateTV;

    private String selectedData;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_walk_record, container, false);
        mDBManager = new DatabaseManager(v.getContext());
        mContext = v.getContext();
        mMapView = (MapView) v.findViewById(R.id.map_view);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();    // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(v.getContext());
        } catch (Exception e) {
            e.printStackTrace();
            return v;
        }
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                setCV();
            }
        });

        mShowDateRecordFAB = (FloatingActionButton)v.findViewById(R.id.fab_show_date_record);
        mLeftButton = (ImageView)v.findViewById(R.id.left_button);
        mRightButton = (ImageView)v.findViewById(R.id.right_button);

        mLeftButton.setOnClickListener(this);
        mRightButton.setOnClickListener(this);
        mShowDateRecordFAB.setOnClickListener(this);

        mNumFlagsRecordCV = (CardView) v.findViewById(R.id.cv_num_flags_record);
        mTimeRecordCV = (CardView) v.findViewById(R.id.cv_time_record);
        mDistanceRecordCV = (CardView) v.findViewById(R.id.cv_distance_record);
        mStepRecordCV = (CardView) v.findViewById(R.id.cv_step_record);
        mSpeedRecordCV = (CardView) v.findViewById(R.id.cv_speed_record);

        mNumFlagsRecordTV = (TextView) v.findViewById(R.id.tv_num_flags_record);
        mTimeRecordTV = (TextView)v.findViewById(R.id.tv_time_record);
        mDistanceRecordTV = (TextView)v.findViewById(R.id.tv_distance_record);
        mStepRecordTV = (TextView)v.findViewById(R.id.tv_step_record);
        mSpeedRecordTV = (TextView)v.findViewById(R.id.tv_speed_record);

        mWalkingNameTV = (TextView)v.findViewById(R.id.tv_walking_name);
        mDateTV = (TextView)v.findViewById(R.id.tv_date);

        dateList = mDBManager.selectAllDate();
        walkList = mDBManager.selectDayWalking(dateList.get(dateList.size()-1));          //가장 최근의 데이터들 가지고 온다.
        currentCnt = 0;
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();


    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public void setCV(){
        mWalkingNameTV.setText(walkList.get(currentCnt).getWlak_name());
        String[] dateArr = extractDate();
        mDateTV.setText(dateArr[0] + "년 " +dateArr[1] + "월 " + dateArr[2] + "일 " + dateArr[3] + "시 " + dateArr[4] + "분");

        mNumFlagsRecordTV.setText(Integer.toString(walkList.get(currentCnt).getNumflag()));
        mTimeRecordTV.setText(Integer.toString(walkList.get(currentCnt).getTime()));
        mDistanceRecordTV.setText(Double.toString(walkList.get(currentCnt).getDistance()));
        mStepRecordTV.setText(Integer.toString(walkList.get(currentCnt).getStep()));
        mSpeedRecordTV.setText(Double.toString(walkList.get(currentCnt).getSppedAverage()));
        setLocation();
    }
    public void setLocation(){
        ArrayList<LatLng> lines = walkList.get(currentCnt).getLines();
        com.google.android.gms.maps.model.LatLng[] pathPoints = new com.google.android.gms.maps.model.LatLng[lines.size()];
        pathPoints = lines.toArray(pathPoints);
        mMap.addPolyline(new PolylineOptions().add(pathPoints)
                .color(R.color.colorPrimary));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pathPoints[pathPoints.length-1], 16));
        mMap.addMarker(new MarkerOptions()
        .position(new LatLng(pathPoints[0].latitude, pathPoints[0].longitude))
        .title("출발지").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(pathPoints[pathPoints.length-1].latitude, pathPoints[pathPoints.length-1].longitude))
                .title("목적지").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab_show_date_record:
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(
                        getActivity());
                alertBuilder.setTitle("항목중에 하나를 선택하세요");

                final ArrayAdapter<String> adapter = addToAdapter();

                alertBuilder.setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertBuilder.setAdapter(adapter,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                selectedData = adapter.getItem(id);
                            }
                        });
                alertBuilder.show();
                setWalkingDataByDate();
//                setCV();
                break;
            case R.id.left_button :
                if(isNextDataExist(0)){
                    mMap.clear();
                    currentCnt--;
                    setCV();
                }else{
                    printToast("데이터가 더 이상 존재 하지 않습니다.");
                }
                break;
            case R.id.right_button :
                if(isNextDataExist(1)) {
                    mMap.clear();
                    currentCnt++;
                    setCV();
                }else{
                    printToast("데이터가 더 이상 존재 하지 않습니다.");
                }
                break;
        }
    }
    public boolean isNextDataExist(int state){
        if(state == 1){     //오른쪽 버튼을 클릭했을 때
            return (currentCnt == walkList.size() - 1)? false:true;
        }else{
            return (currentCnt == 0)? false:true;
        }
    }
    public void printToast(String message){
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }
    public String[] extractDate(){
        return walkList.get(currentCnt).getDate().split("-");
    }
    public ArrayAdapter addToAdapter(){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getActivity(), android.R.layout.select_dialog_singlechoice);
            for(int i = 0; i < dateList.size(); i++){
            adapter.add(dateList.get(i));
        }
        return adapter;
    }
    public void setWalkingDataByDate(){
        walkList = mDBManager.selectDayWalking(selectedData);
        currentCnt = 0;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


}