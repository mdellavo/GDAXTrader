package org.quuux.gdax.fragments;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.quuux.feller.Log;
import org.quuux.gdax.BuildConfig;
import org.quuux.gdax.Datastore;
import org.quuux.gdax.R;
import org.quuux.gdax.Settings;
import org.quuux.gdax.Util;
import org.quuux.gdax.events.CursorUpdated;
import org.quuux.gdax.events.ProductSelected;
import org.quuux.gdax.events.PurchaseUpdate;
import org.quuux.gdax.model.Product;
import org.quuux.gdax.model.ProductStat;
import org.quuux.gdax.net.API;
import org.quuux.gdax.view.CandleView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends BaseGDAXFragment {

    private static final String TAG = Log.buildTag(HomeFragment.class);
    SwipeRefreshLayout mSwipeRefresh;
    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    HomeAdapter mAdapter;
    Listener mListener;
    Datastore.Candles mCandles;
    ProductStat mStats;
    boolean mStatsRefreshing;
    boolean mCandlesRefreshing;
    CandleView mChart;


    public interface Listener {
        void showSetup();
        boolean isUnlocked();
        void purchaseUnlock();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPurchaseUpdate(PurchaseUpdate event) {
        mAdapter.update();
    }

    private void load() {
        Datastore ds = Datastore.getInstance();
        Product product = ds.getSelectedProduct();
        if (product != null) {
            if (!mStatsRefreshing) {
                ds.loadStats(product);
                mStatsRefreshing = true;
            }
            if (!mCandlesRefreshing) {
                ds.loadRecentCandles(product, API.ONE_HOUR, 1);
                mCandlesRefreshing = true;
            }
        }

        checkRefreshing();
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
        if (mChart != null) {
            mChart.update(candles);
            mChart.setVisibleXRange(24, 24);
            mChart.moveViewToX(24);
        }
        checkRefreshing();
    }

    private boolean shouldShowWelcomeCard() {
        return !Settings.get(getContext()).hasApiKey();
    }

    private boolean shouldShowNagCard() {
        return !mListener.isUnlocked();
    }

    private boolean shouldShowWhatsNew() {

        int installedVersionCode = -1;

        Context context = getContext();
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            installedVersionCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "unable to get version code: %s", e);
        }

        int lastVersionCode = Settings.get(context).getLastVersionCode();
        boolean rv = lastVersionCode < installedVersionCode;
        if (rv)
            Settings.get(getContext()).setLastVersionCode(installedVersionCode);

        return rv || BuildConfig.DEBUG;
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

            if (mStats != null) {
                BigDecimal percentage = mStats.last.subtract(mStats.open).divide(mStats.open, BigDecimal.ROUND_HALF_EVEN);
                change.setTextColor(percentage.compareTo(BigDecimal.ZERO) < 0 ? Color.RED : Color.GREEN);
                change.setText(Util.percentageFormat(percentage));
            }
        }
    }

    class CandlesCard extends ViewHolder {

        public CandlesCard(final View itemView) {
            super(itemView);
            mChart = itemView.findViewById(R.id.chart);
            mChart.setDragEnabled(false);
            mChart.setTouchEnabled(false);

        }

        @Override
        void bind() {
            if (mCandles != null)
                mChart.update(mCandles);
        }
    }

    class NagCard extends ViewHolder {
        public NagCard(final View itemView) {
            super(itemView);

            Button button = itemView.findViewById(R.id.unlock);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.purchaseUnlock();
                }
            });
        }
    }

    class WhatsNewCard extends ViewHolder {
        public WhatsNewCard(final View itemView) {
            super(itemView);


        }
    }

    enum CardType{WELCOME, NAG, ACTIVITY, CANDLES, WHATS_NEW};

    class HomeAdapter extends RecyclerView.Adapter<ViewHolder> {

        List<CardType> mCards = new ArrayList<>();

        public HomeAdapter() {
            update();
        }

        public void update() {
            mCards.clear();
            if (shouldShowWelcomeCard())
                mCards.add(CardType.WELCOME);

            if (shouldShowNagCard()) {
                mCards.add(CardType.NAG);
            }

            if (shouldShowWhatsNew()) {
                mCards.add(CardType.WHATS_NEW);
            }

            mCards.add(CardType.ACTIVITY);
            mCards.add(CardType.CANDLES);
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {

            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            ViewHolder rv = null;
            switch (CardType.values()[viewType]) {
                case WELCOME:
                    rv = new WelcomeCard(inflater.inflate(R.layout.card_welcome, parent, false));
                    break;

                case NAG:
                    rv = new NagCard(inflater.inflate(R.layout.card_nag, parent, false));
                    break;

                case WHATS_NEW:
                    rv = new WhatsNewCard(inflater.inflate(R.layout.card_whats_new, parent, false));
                    break;

                case ACTIVITY:
                    rv = new ActivityCard(inflater.inflate(R.layout.card_activity, parent, false));
                    break;

                case CANDLES:
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
            return mCards.get(position).ordinal();
        }
    }
}
