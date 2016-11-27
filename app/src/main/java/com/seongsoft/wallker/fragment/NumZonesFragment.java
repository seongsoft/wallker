package com.seongsoft.wallker.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.seongsoft.wallker.R;
import com.seongsoft.wallker.beans.Ranking;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BeINone on 2016-11-27.
 */

public class NumZonesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_num_zones, container, false);

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recyclerview_num_zones);
        recyclerView.setAdapter();
    }

    private class NumZonesRecyclerViewAdapter extends RecyclerView.Adapter {

        private List<Ranking> mRankings;

        public NumZonesRecyclerViewAdapter() {
            mRankings = new ArrayList<>();
        }

        public NumZonesRecyclerViewAdapter(ArrayList<Ranking> rankings) {
            this();
            mRankings.addAll(rankings);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public int getItemCount() {
            return 0;
        }

    }

}
