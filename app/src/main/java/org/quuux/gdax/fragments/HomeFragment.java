package org.quuux.gdax.fragments;


import android.content.Context;
import android.os.Bundle;
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
import org.quuux.gdax.Datastore;
import org.quuux.gdax.R;
import org.quuux.gdax.Settings;
import org.quuux.gdax.Util;
import org.quuux.gdax.events.CursorUpdated;
import org.quuux.gdax.events.ProductSelected;
import org.quuux.gdax.model.Product;
import org.quuux.gdax.model.ProductStat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends BaseGDAXFragment {

    Listener mListener;

    public interface Listener {
        void showSetup();
    }

    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    HomeAdapter mAdapter;

    public HomeFragment() {
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
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
        mRecyclerView = v.findViewById(R.id.recycler_view);

        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new HomeAdapter();
        mRecyclerView.setAdapter(mAdapter);

        return v;
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
            mAdapter.notifyDataSetChanged();
            loadStats();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProductSelected(final ProductSelected event) {
        loadStats();
    }

    private void loadStats() {
        Datastore ds = Datastore.getInstance();
        ds.loadStats(ds.getSelectedProduct());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStatsUpdated(ProductStat stat) {
        mAdapter.notifyDataSetChanged();
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
            ProductStat stat = Datastore.getInstance().getProductStat(p);
            open.setText(stat != null ? Util.currencyFormat(stat.open) : "-");
            low.setText(stat != null ? Util.currencyFormat(stat.low) : "-");
            high.setText(stat != null ? Util.currencyFormat(stat.high) : "-");
            volume.setText(stat != null ? Util.intFormat(stat.volume) : "-");
            last.setText(stat != null ? Util.currencyFormat(stat.last) : "-");

            change.setText(stat != null ? Util.percentageFormat(stat.last.subtract(stat.open).divide(stat.open, BigDecimal.ROUND_HALF_EVEN)) : "-");

        }
    }

    final static int CARD_TYPE_WELCOME = 0;
    final static int CARD_TYPE_ACTIVITY = 1;

    class HomeAdapter extends RecyclerView.Adapter<ViewHolder> {

        List<Integer> mCards = new ArrayList<>();

        public HomeAdapter() {
            if (shouldShowWelcomeCard())
                mCards.add(CARD_TYPE_WELCOME);

            mCards.add(CARD_TYPE_ACTIVITY);
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
