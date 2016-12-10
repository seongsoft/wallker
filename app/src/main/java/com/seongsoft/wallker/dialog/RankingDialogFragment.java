package com.seongsoft.wallker.dialog;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.seongsoft.wallker.R;
import com.seongsoft.wallker.beans.Ranking;
import com.seongsoft.wallker.constants.HttpConst;
import com.seongsoft.wallker.fragment.NumFlagsInZoneFragment;
import com.seongsoft.wallker.fragment.NumZonesFragment;
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
 * Created by BeINone on 2016-11-21.
 */

public class RankingDialogFragment extends DialogFragment {

//    private RecyclerView mRecyclerView;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_ranking, container, false);

//        mRecyclerView = (RecyclerView) v.findViewById(R.id.rv_ranking);
//        mRecyclerView.setHasFixedSize(true);
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mTabLayout = (TabLayout) v.findViewById(R.id.tabs_ranking);
        mViewPager = (ViewPager) v.findViewById(R.id.viewpager_ranking);

        RankingPagerAdapter adapter = new RankingPagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(adapter);

        new HttpLoadRankingTask().execute();

        return v;
    }

    private class RankingPagerAdapter extends FragmentPagerAdapter {

        private static final String TAB1 = "구역";
        private static final String TAB2 = "깃발";

        public RankingPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: return new NumZonesFragment();
                case 1: return new NumFlagsInZoneFragment();
                default: return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return TAB1;
                case 1: return TAB2;
                default: return null;
            }
        }

    }

    private class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.RankingItemVH> {

        private List<Ranking> mRankings;

        public RankingAdapter(ArrayList<Ranking> rankings) {
            mRankings = new ArrayList<>();
            mRankings.addAll(rankings);
        }

        @Override
        public RankingItemVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getContext()).inflate(R.layout.item_ranking, parent, false);
            return new RankingItemVH(v);
        }

        @Override
        public void onBindViewHolder(RankingItemVH holder, int position) {
            Ranking ranking = mRankings.get(position);

            holder.mRankingTV.setText(String.valueOf(ranking.getRanking()));
            holder.mIDTV.setText(ranking.getId());
            holder.mNumZonesTV.setText(String.valueOf(ranking.getNumZones()));
        }

        @Override
        public int getItemCount() {
            return mRankings.size();
        }

        public class RankingItemVH extends RecyclerView.ViewHolder {

            public TextView mRankingTV;
            public TextView mIDTV;
            public TextView mNumZonesTV;

            public RankingItemVH(View itemView) {
                super(itemView);
                mRankingTV = (TextView) itemView.findViewById(R.id.tv_ranking);
                mIDTV = (TextView) itemView.findViewById(R.id.tv_id);
                mNumZonesTV = (TextView) itemView.findViewById(R.id.tv_num_zones);
            }

        }

    }

    private class HttpLoadRankingTask extends AsyncTask<Void, Void, ArrayList<Ranking>> {

        private static final String MSG_SUCCEED = "깃발을 꽂았습니다.";
        private static final String MSG_FAILED = "한 발 늦었습니다.";
        private static final String MSG_ERROR = "깃발을 꽂지 못했습니다.";

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
                conn = (HttpURLConnection) new URL(HttpConst.SERVER_URL + "/loadranking/index.jsp")
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
//                mRecyclerView.setAdapter(new RankingAdapter(rankings));
            }
        }

    }

}
