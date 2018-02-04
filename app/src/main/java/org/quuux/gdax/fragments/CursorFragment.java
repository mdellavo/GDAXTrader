package org.quuux.gdax.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.quuux.gdax.net.Cursor;
import org.quuux.gdax.R;
import org.quuux.gdax.events.CursorUpdated;
import org.quuux.gdax.view.CursorAdapter;

public abstract class CursorFragment extends BaseGDAXFragment {
    private Cursor<?> mCursor;
    private CursorAdapter<?> mAdapter;
    private ListView mList;
    private int mLayoutResource;
    private int mHeaderResource = 0;
    private AdapterView.OnItemClickListener mItemClickListener;
    private View mEmptyView;
    private SwipeRefreshLayout mSwipeRefresh;

    public ListView getList() {
        return mList;
    }

    public void setCursor(Cursor cursor) {
        mCursor = cursor;
    }

    public void setAdapter(CursorAdapter adapter) {
        mAdapter = adapter;
    }

    public void setLayoutResource(final int res) {
        this.mLayoutResource = res;
    }

    public void setHeaderResource(final int mHeaderResource) {
        this.mHeaderResource = mHeaderResource;
    }

    public void setItemClickListener(final AdapterView.OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter.register();
        EventBus.getDefault().register(this);
        mCursor.load();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.unregister();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(mLayoutResource, container, false);
        mList = v.findViewById(R.id.list);

        if (mHeaderResource != 0) {
            View headerView = inflater.inflate(mHeaderResource, null);
            mList.addHeaderView(headerView, null, false);
        }

        mEmptyView = v.findViewById(R.id.empty);

        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(mItemClickListener);

        mSwipeRefresh = v.findViewById(R.id.swiperefresh);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCursor.reset();
            }
        });
        mSwipeRefresh.setRefreshing(true);

        return v;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCursorUpdated(CursorUpdated event) {
        if (event.cursor != mCursor)
            return;

        switch (mCursor.getState()) {
            case init:
                break;

            case loading:
                if (mSwipeRefresh != null)
                    mSwipeRefresh.setRefreshing(true);
                if (mEmptyView != null)
                    mEmptyView.setVisibility(View.GONE);
                break;

            case loaded:
                mSwipeRefresh.setRefreshing(false);
                if (mCursor.getItems().size() == 0) {
                    mEmptyView.setVisibility(View.VISIBLE);
                }
                break;

            case error:
                mSwipeRefresh.setRefreshing(false);
                break;
        }
    }
}
