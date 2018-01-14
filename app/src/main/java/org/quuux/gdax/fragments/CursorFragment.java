package org.quuux.gdax.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.quuux.gdax.Datastore;
import org.quuux.gdax.R;
import org.quuux.gdax.view.CursorAdapter;

public abstract class CursorFragment extends Fragment {
    private Datastore.Cursor<?> mCursor;
    private CursorAdapter<?> mAdapter;
    private ListView mList;
    private int mLayoutResource;
    private int mHeaderResource = 0;
    private AdapterView.OnItemClickListener mItemClickListener;

    public ListView getList() {
        return mList;
    }

    public void setCursor(Datastore.Cursor cursor) {
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

        mCursor.load();
        mAdapter.register();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.unregister();
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

        mList.setEmptyView(v.findViewById(R.id.empty));

        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(mItemClickListener);
        return v;
    }
}
