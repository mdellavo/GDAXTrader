package org.quuux.gdax.fragments;


import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.quuux.gdax.Datastore;
import org.quuux.gdax.R;
import org.quuux.gdax.model.Product;
import org.quuux.gdax.view.CandleView;


public class CandlesFragment extends BaseGDAXFragment {

    CandleView mCandles;

    public CandlesFragment() {
    }

    public static CandlesFragment newInstance() {
        CandlesFragment fragment = new CandlesFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_candles, container, false);

        mCandles = v.findViewById(R.id.chart);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        load();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCandlesLoaded(Datastore.Candles candles) {
        mCandles.update(candles);
    }

    private void load() {
        Datastore ds = Datastore.getInstance();
        Product product = ds.getSelectedProduct();
        if (product != null) {
            ds.loadCandles(product);
        }
    }

}
