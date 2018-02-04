package org.quuux.gdax.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.quuux.gdax.R;

public class PlaceOrderFragment extends BaseGDAXFragment {

    ViewPager mPager;
    PagerAdapter mAdapter;

    public PlaceOrderFragment() {
    }

    public static PlaceOrderFragment newInstance() {
        PlaceOrderFragment fragment = new PlaceOrderFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public boolean needsProductSelector() {
        return true;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_place_order, container, false);

        mPager = v.findViewById(R.id.pager);

        mAdapter = new PagerAdapter(getFragmentManager());
        mPager.setAdapter(mAdapter);

        return v;
    }

    class PagerAdapter extends FragmentStatePagerAdapter {

        public PagerAdapter(final FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(final int position) {
            Fragment frag = null;
            if (position == 0)
                frag = PlaceMarketOrderFragment.newInstance();
            else if(position == 1)
                frag = PlaceLimitOrderFragment.newInstance();
            else if (position == 2)
                frag = PlaceStopOrderFragment.newInstance();
            return frag;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(final int position) {
            int resId = 0;
            if (position == 0)
                resId = R.string.market;
            else if(position == 1)
                resId = R.string.limit;
            else if (position == 2)
                resId = R.string.stop;
            return resId != 0 ? getString(resId) : null;

        }
    }

}
