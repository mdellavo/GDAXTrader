package org.quuux.gdax.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.quuux.gdax.Datastore;
import org.quuux.gdax.R;
import org.quuux.gdax.events.ProductSelected;
import org.quuux.gdax.model.Product;
import org.quuux.gdax.view.CandleView;


public class CandlesFragment extends BaseGDAXFragment {

    CandleView mChart;
    Datastore.Candles mCandles;

    public CandlesFragment() {
    }

    public static CandlesFragment newInstance() {
        CandlesFragment fragment = new CandlesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getTitle() {
        return R.string.candles;
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
        View v = inflater.inflate(R.layout.fragment_candles, container, false);

        mChart = v.findViewById(R.id.chart);
        mChart.setDragEnabled(true);
        mChart.setTouchEnabled(true);
        mChart.setPinchZoom(true);
        mChart.setDoubleTapToZoomEnabled(true);
        if (mCandles != null)
            mChart.update(mCandles);
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
    public void onProductSelected(final ProductSelected event) {
        load();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCandlesLoaded(Datastore.Candles candles) {
        mChart.update(candles);
    }

    private void load() {
        Datastore ds = Datastore.getInstance();
        Product product = ds.getSelectedProduct();
        if (product != null) {
            ds.loadCandles(product);
        }
    }

}
