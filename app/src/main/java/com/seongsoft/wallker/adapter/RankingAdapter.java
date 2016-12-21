package com.seongsoft.wallker.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.seongsoft.wallker.R;
import com.seongsoft.wallker.beans.Ranking;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BeINone on 2016-12-10.
 */

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.RankingViewHolder> {

    private Context mContext;
    private List<Ranking> mRankings;

    public RankingAdapter(ArrayList<Ranking> rankings, Context context) {
        mContext = context;
        mRankings = new ArrayList<>();
        mRankings.addAll(rankings);
    }

    @Override
    public RankingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_ranking, parent, false);
        return new RankingViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RankingViewHolder holder, int position) {
        Ranking ranking = mRankings.get(position);

        holder.mRankingTV.setText(String.valueOf(ranking.getRanking()));
        switch (ranking.getRanking()) {
            case 1:
                ((View) holder.mRankingTV.getParent())
                        .setBackgroundColor(ContextCompat.getColor(mContext, R.color.gold));
                break;
            case 2:
                ((View) holder.mRankingTV.getParent())
                        .setBackgroundColor(ContextCompat.getColor(mContext, R.color.silver));
                break;
            case 3:
                ((View) holder.mRankingTV.getParent())
                        .setBackgroundColor(ContextCompat.getColor(mContext, R.color.bronze));
                break;
            default:
                break;
        }
        holder.mIDTV.setText(ranking.getId());
        holder.mNumZonesTV.setText(String.valueOf(ranking.getNumZones()));
    }

    @Override
    public int getItemCount() {
        return mRankings.size();
    }

    public class RankingViewHolder extends RecyclerView.ViewHolder {

        public TextView mRankingTV;
        public TextView mIDTV;
        public TextView mNumZonesTV;

        public RankingViewHolder(View itemView) {
            super(itemView);
            mRankingTV = (TextView) itemView.findViewById(R.id.tv_ranking);
            mIDTV = (TextView) itemView.findViewById(R.id.tv_id);
            mNumZonesTV = (TextView) itemView.findViewById(R.id.tv_num_zones);
        }

    }

}