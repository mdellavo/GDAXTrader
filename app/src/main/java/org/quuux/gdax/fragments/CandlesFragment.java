package org.quuux.gdax.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import static org.quuux.gdax.net.API.FIFTEEN_MINUTES;
import static org.quuux.gdax.net.API.FIVE_MINUTES;
import static org.quuux.gdax.net.API.ONE_DAY;
import static org.quuux.gdax.net.API.ONE_HOUR;
import static org.quuux.gdax.net.API.ONE_MINUTE;
import static org.quuux.gdax.net.API.SIX_HOURS;


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
        setHasOptionsMenu(true);
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
            onCandlesLoaded(mCandles);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        load(ONE_DAY);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_candles, menu);

        MenuItem checked = menu.findItem(R.id.one_day);

        if (mCandles != null) {
            switch (mCandles.granularity) {
                case ONE_MINUTE:
                    checked = menu.findItem(R.id.one_minute);
                    break;

                case FIVE_MINUTES:
                    checked = menu.findItem(R.id.five_minutes);
                    break;

                case FIFTEEN_MINUTES:
                    checked = menu.findItem(R.id.fifteen_minutes);
                    break;

                case ONE_HOUR:
                    checked = menu.findItem(R.id.one_hour);
                    break;

                case SIX_HOURS:
                    checked = menu.findItem(R.id.six_hours);
                    break;
            }
        }

        checked.setChecked(true);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        boolean rv;
        switch (item.getItemId()) {

            case R.id.one_minute:
            case R.id.five_minutes:
            case R.id.fifteen_minutes:
            case R.id.one_hour:
            case R.id.six_hours:
            case R.id.one_day:
                if (!item.isChecked()) {
                    item.setChecked(true);
                }
                updateGranularity(item.getItemId());
                rv = true;
                break;

            default:
                rv = super.onOptionsItemSelected(item);
        }

        return rv;
    }

    private int mapGranularity(final int id) {
        switch (id) {
            case R.id.one_minute:
                return ONE_MINUTE;
            case R.id.five_minutes:
                return FIVE_MINUTES;
            case R.id.fifteen_minutes:
                return FIFTEEN_MINUTES;
            case R.id.one_hour:
                return ONE_HOUR;
            case R.id.six_hours:
                return SIX_HOURS;
            case R.id.one_day:
            default:
                return ONE_DAY;
        }
    }
    private void updateGranularity(final int id) {
        int granularity = mapGranularity(id);
        load(granularity);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProductSelected(final ProductSelected event) {
        load(ONE_DAY);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCandlesLoaded(Datastore.Candles candles) {
        mChart.update(candles);
        mChart.setVisibleXRangeMaximum(75);
        mChart.moveViewToX(mChart.getData().getXMax() - 30);
        mCandles = candles;
        getActivity().invalidateOptionsMenu();
    }

    private void load(final int granularity) {
        Datastore ds = Datastore.getInstance();
        Product product = ds.getSelectedProduct();
        if (product != null) {
            ds.loadCandles(product, granularity, null, null);
        }
    }

}
