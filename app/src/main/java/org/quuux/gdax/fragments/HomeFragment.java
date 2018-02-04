package org.quuux.gdax.fragments;


import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.quuux.gdax.R;

public class HomeFragment extends BaseGDAXFragment {

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

    private boolean shouldShowWelcomeCard() {
        return true;
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
        }
    }

    final static int CARD_HOME = 0;
    final static int CARD_ACTIVITY = 1;

    class HomeAdapter extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {

            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            ViewHolder rv = null;
            switch (viewType) {
                case CARD_HOME:
                    rv = new WelcomeCard(inflater.inflate(R.layout.card_welcome, parent, false));
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
            int num = 0;

            if (shouldShowWelcomeCard())
                num += 1;

            return num;
        }

        @Override
        public long getItemId(final int position) {
            return position;
        }

        @Override
        public int getItemViewType(final int position) {
            return position;
        }
    }

}
