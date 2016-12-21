package com.seongsoft.wallker.fragment;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.seongsoft.wallker.R;
import com.seongsoft.wallker.adapter.RankingAdapter;
import com.seongsoft.wallker.beans.Ranking;
import com.seongsoft.wallker.constants.HttpConst;
import com.seongsoft.wallker.manager.JSONManager;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by BeINone on 2016-11-27.
 */

public class NumZonesFragment extends Fragment {

    private RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_num_zones, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerview_num_zones);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        new HttpNumZonesLoadRankingTask().execute();

        return v;
    }

    private class HttpNumZonesLoadRankingTask extends AsyncTask<Void, Void, ArrayList<Ranking>> {

        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage("랭킹 불러오는 중...");
            mProgressDialog.show();
        }

        @Override
        protected ArrayList<Ranking> doInBackground(Void... params) {
            HttpURLConnection conn = null;
            List<Ranking> rankings = null;
            try {
                conn = (HttpURLConnection) new URL(HttpConst.SERVER_URL + "/loadranking/numzones/index.jsp")
                        .openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(false);

                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String rankingJArrayString = reader.readLine();
                rankings = new ArrayList<>();
                rankings.addAll(
                        JSONManager.parseRankingJSONArray(new JSONArray(rankingJArrayString)));
                is.close();
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn != null) conn.disconnect();
            }

            return (ArrayList<Ranking>) rankings;
        }

        @Override
        protected void onPostExecute(ArrayList<Ranking> rankings) {
            mProgressDialog.dismiss();
            if (rankings == null) {
                Toast.makeText(getContext(), "랭킹을 불러오지 못했습니다.", Toast.LENGTH_SHORT)
                        .show();
            } else {
                mRecyclerView.setAdapter(new RankingAdapter(rankings, getContext()));
            }
        }

    }

}
