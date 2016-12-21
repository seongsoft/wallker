package com.seongsoft.wallker.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.seongsoft.wallker.R;
import com.seongsoft.wallker.fragment.NumFlagsInZoneFragment;
import com.seongsoft.wallker.fragment.NumZonesFragment;

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
        mViewPager = (ViewPager) v.findViewById(R.id.viewpager_ranking);
        RankingPagerAdapter adapter = new RankingPagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(adapter);

        mTabLayout = (TabLayout) v.findViewById(R.id.tabs_ranking);
        mTabLayout.setupWithViewPager(mViewPager);

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

}
