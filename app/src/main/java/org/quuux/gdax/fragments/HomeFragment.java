package org.quuux.gdax.fragments;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.quuux.gdax.Datastore;
import org.quuux.gdax.R;
import org.quuux.gdax.Settings;
import org.quuux.gdax.Util;
import org.quuux.gdax.events.CursorUpdated;
import org.quuux.gdax.events.ProductSelected;
import org.quuux.gdax.model.Product;
import org.quuux.gdax.model.ProductStat;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class HomeFragment extends BaseGDAXFragment {

    SwipeRefreshLayout mSwipeRefresh;
    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    HomeAdapter mAdapter;
    Listener mListener;
    Datastore.Candles mCandles;
    CandleData mCandleData;
    ProductStat mStats;
    boolean mStatsRefreshing;
    boolean mCandlesRefreshing;

    public interface Listener {
        void showSetup();
    }

    public HomeFragment() {
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getTitle() {
        return R.string.home;
    }

    @Override
    public boolean needsProductSelector() {
        return true;
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        mListener = (Listener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        mSwipeRefresh = v.findViewById(R.id.swiperefresh);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        mRecyclerView = v.findViewById(R.id.recycler_view);

        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new HomeAdapter();
        mRecyclerView.setAdapter(mAdapter);

        return v;
    }

    private void refresh() {
        load();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCursorUpdated(CursorUpdated event) {
        if (Datastore.getInstance().getProducts() == event.cursor) {
            load();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProductSelected(final ProductSelected event) {
        load();
    }

    private void load() {
        mSwipeRefresh.setRefreshing(true);
        mStatsRefreshing = mCandlesRefreshing = true;
        Datastore ds = Datastore.getInstance();
        Product product = ds.getSelectedProduct();
        if (product != null) {
            ds.loadStats(product);
            ds.loadCandles(product);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStatsUpdated(ProductStat stat) {
        mStatsRefreshing = false;
        mStats = stat;
        mAdapter.notifyDataSetChanged();
        checkRefreshing();
    }

    private void checkRefreshing() {
        mSwipeRefresh.setRefreshing(mStatsRefreshing || mCandlesRefreshing);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCandlesLoaded(Datastore.Candles candles) {
        mCandlesRefreshing = false;

        mCandles = candles;


        Arrays.sort(candles.candles, new Comparator<float[]>() {
            @Override
            public int compare(final float[] a, final float[] b) {
                return Float.compare(a[0], b[0]);
            }
        });

        List<CandleEntry> values = new ArrayList<>();

        int lookback = 48;

        for (int i=candles.candles.length-lookback; i<candles.candles.length; i++) {
            float time, low, high, open, close, volume;
            time = candles.candles[i][0];
            low = candles.candles[i][1];
            high = candles.candles[i][2];
            open = candles.candles[i][3];
            close = candles.candles[i][4];
            volume = candles.candles[i][5];

            CandleEntry entry = new CandleEntry(i, high, low, open, close);
            values.add(entry);
        }

        CandleDataSet set = new CandleDataSet(values, "activity");
        set.setDecreasingColor(Color.RED);
        set.setDecreasingPaintStyle(Paint.Style.FILL);
        set.setIncreasingColor(Color.rgb(122, 242, 84));
        set.setIncreasingPaintStyle(Paint.Style.STROKE);
        set.setDrawValues(false);

        mCandleData = new CandleData(set);
        mAdapter.notifyDataSetChanged();
        checkRefreshing();
    }

    private boolean shouldShowWelcomeCard() {
        return !Settings.get(getContext()).hasApiKey();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(final View itemView) {
            super(itemView);
        }

        void bind() {

        }
    }

    class WelcomeCard extends ViewHolder {
        public WelcomeCard(final View itemView) {
            super(itemView);
            Button button = itemView.findViewById(R.id.setup);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    mListener.showSetup();
                }
            });
        }
    }

    class ActivityCard extends ViewHolder {

        TextView open, high, low, volume, last, change;

        public ActivityCard(final View itemView) {
            super(itemView);
            open = itemView.findViewById(R.id.open);
            high = itemView.findViewById(R.id.high);
            low = itemView.findViewById(R.id.low);
            volume = itemView.findViewById(R.id.volume);
            last = itemView.findViewById(R.id.last);
            change = itemView.findViewById(R.id.change);
        }

        @Override
        void bind() {
            Product p = Datastore.getInstance().getSelectedProduct();
            if (p == null)
                return;
            open.setText(mStats != null ? Util.currencyFormat(mStats.open) : "-");
            low.setText(mStats != null ? Util.currencyFormat(mStats.low) : "-");
            high.setText(mStats != null ? Util.currencyFormat(mStats.high) : "-");
            volume.setText(mStats != null ? Util.intFormat(mStats.volume) : "-");
            last.setText(mStats != null ? Util.currencyFormat(mStats.last) : "-");

            change.setText(mStats != null ? Util.percentageFormat(mStats.last.subtract(mStats.open).divide(mStats.open, BigDecimal.ROUND_HALF_EVEN)) : "-");

        }
    }

    class CandlesCard extends ViewHolder {
        CandleStickChart chart;

        public CandlesCard(final View itemView) {
            super(itemView);
            chart = itemView.findViewById(R.id.chart);
            chart.setDrawGridBackground(false);
            chart.setDragEnabled(false);
            chart.setTouchEnabled(false);
            chart.getAxisRight().setEnabled(false);
            chart.getLegend().setEnabled(false);
            chart.setDrawBorders(false);
            chart.getDescription().setEnabled(false);

            XAxis xAxis = chart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setDrawAxisLine(false);
            xAxis.setLabelRotationAngle(-45);
            xAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(final float value, final AxisBase axis) {
                    float time = mCandles.candles[(int)value][0];
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM d ha");
                    return sdf.format(new Date((long) time * 1000));
                }
            });
        }

        @Override
        void bind() {
            super.bind();
            if (mCandleData != null) {
                chart.setData(mCandleData);
                chart.invalidate();
            }
        }
    }

    final static int CARD_TYPE_WELCOME = 0;
    final static int CARD_TYPE_ACTIVITY = 1;
    final static int CARD_TYPE_CANDLES= 2;
    final static int CARD_TYPE_WHATS_NEW = 3;

    class HomeAdapter extends RecyclerView.Adapter<ViewHolder> {

        List<Integer> mCards = new ArrayList<>();

        public HomeAdapter() {
            if (shouldShowWelcomeCard())
                mCards.add(CARD_TYPE_WELCOME);

            mCards.add(CARD_TYPE_ACTIVITY);
            mCards.add(CARD_TYPE_CANDLES);
        }

        @Override
        public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {

            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            ViewHolder rv = null;
            switch (viewType) {
                case CARD_TYPE_WELCOME:
                    rv = new WelcomeCard(inflater.inflate(R.layout.card_welcome, parent, false));
                    break;

                case CARD_TYPE_ACTIVITY:
                    rv = new ActivityCard(inflater.inflate(R.layout.card_activity, parent, false));
                    break;

                case CARD_TYPE_CANDLES:
                    rv = new CandlesCard(inflater.inflate(R.layout.card_candles, parent, false));
                    break;
            }

            return rv;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.bind();
        }

        @Override
        public int getItemCount() {
            return mCards.size();
        }

        @Override
        public long getItemId(final int position) {
            return mCards.get(position).hashCode();
        }

        @Override
        public int getItemViewType(final int position) {
            return mCards.get(position);
        }
    }
}
