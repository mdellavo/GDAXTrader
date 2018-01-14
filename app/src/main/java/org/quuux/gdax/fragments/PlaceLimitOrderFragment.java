package org.quuux.gdax.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.quuux.gdax.R;

public class PlaceLimitOrderFragment extends Fragment {

    public PlaceLimitOrderFragment() {
    }

    public static PlaceLimitOrderFragment newInstance() {
        PlaceLimitOrderFragment fragment = new PlaceLimitOrderFragment();
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
        View v =  inflater.inflate(R.layout.fragment_place_limit_order, container, false);

        return v;
    }
}
