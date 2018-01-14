package org.quuux.gdax.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.quuux.gdax.R;

public class PlaceMarketOrderFragment extends Fragment {

    public PlaceMarketOrderFragment() {
    }

    public static PlaceMarketOrderFragment newInstance() {
        PlaceMarketOrderFragment fragment = new PlaceMarketOrderFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_place_market_order, container, false);

        return v;
    }
}
