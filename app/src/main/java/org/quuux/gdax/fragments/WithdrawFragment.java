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

public class WithdrawFragment extends BaseGDAXFragment {

    ViewPager mPager;
    PagerAdapter mAdapter;

    public WithdrawFragment() {}

    public static WithdrawFragment newInstance() {
        WithdrawFragment fragment = new WithdrawFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getTitle() {
        return R.string.withdraw;
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
        View v =  inflater.inflate(R.layout.fragment_withdraw, container, false);

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
            switch (position) {
                case 0:
                    frag = BankAccountWithdrawFragment.newInstance();
                    break;
                case 1:
                    frag = CoinbaseWithdrawFragment.newInstance();
                    break;
            }

            return frag;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(final int position) {
            int resId = 0;
            switch(position) {
                case 0:
                    resId = R.string.to_bank_account;
                    break;
                case 1:
                    resId = R.string.to_coinbase;
                    break;
            }
            return resId != 0 ? getString(resId) : null;

        }
    }

}
