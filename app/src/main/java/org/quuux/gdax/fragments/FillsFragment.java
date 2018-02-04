package org.quuux.gdax.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.quuux.gdax.R;
import org.quuux.gdax.Util;
import org.quuux.gdax.events.ProductSelected;
import org.quuux.gdax.model.Fill;
import org.quuux.gdax.net.Cursor;
import org.quuux.gdax.net.FillsCursor;
import org.quuux.gdax.view.CursorAdapter;

public class FillsFragment extends CursorFragment {

    private FillsCursor mCursor;
    private FillsAdapter mAdapter;

    public FillsFragment() {}

    public static FillsFragment newInstance() {
        FillsFragment fragment = new FillsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getTitle() {
        return R.string.fills;
    }

    @Override
    public boolean needsProductSelector() {
        return true;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        if (getArguments() != null) {
        }

        setLayoutResource(R.layout.fragment_fills);
        setHeaderResource(R.layout.fills_header);

        mCursor = new FillsCursor();
        setCursor(mCursor);

        mAdapter = new FillsAdapter(getContext(), mCursor);
        setAdapter(mAdapter);

        setItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                Fill fill = (Fill) getList().getItemAtPosition(position);
                showFill(fill);
            }
        });

        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    private void showFill(final Fill fill) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(fill.side.name());

        final View v = getLayoutInflater().inflate(R.layout.fill_details, null);

        TextView id = v.findViewById(R.id.id);
        id.setText(fill.order_id);

        TextView product = v.findViewById(R.id.product);
        product.setText(fill.product_id);

        TextView size = v.findViewById(R.id.size);
        size.setText(Util.longFormat(fill.size));

        TextView price = v.findViewById(R.id.price);
        price.setText(Util.longFormat(fill.price));

        TextView fee = v.findViewById(R.id.fee);
        fee.setText(Util.longFormat(fill.fee));

        TextView created_at = v.findViewById(R.id.created_at);
        created_at.setText(fill.created_at.toString());

        builder.setView(v);
        final AlertDialog dialog = builder.create();

        dialog.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProductSelected(final ProductSelected event) {
        mCursor.reset();
    }


    static class FillTag {
        TextView side, size, price, fee, date;
    }

    class FillsAdapter extends CursorAdapter<Fill> {

        public FillsAdapter(final Context context, final Cursor<Fill> cursor) {
            super(context, cursor);
        }

        @Override
        public void bindView(final int position, final View view, final Fill fill) {
            FillTag tag = (FillTag) view.getTag();
            tag.side.setText(fill.side.name());
            tag.price.setText(fill.price != null ? Util.currencyFormat(fill.price) : "");
            tag.size.setText(fill.size != null ? Util.shortFormat(fill.size) : "");
            tag.fee.setText(fill.fee != null ? Util.currencyFormat(fill.fee) : "");
            tag.date.setText(Util.dateFormat(fill.created_at));
        }

        @Override
        public View newView(final int position, final ViewGroup parent) {
            View v = getLayoutInflater().inflate(R.layout.fill_item, parent, false);
            FillTag tag = new FillTag();
            tag.side = v.findViewById(R.id.side);
            tag.price = v.findViewById(R.id.price);
            tag.size = v.findViewById(R.id.size);
            tag.fee = v.findViewById(R.id.fee);
            tag.date = v.findViewById(R.id.date);
            v.setTag(tag);
            return v;
        }
    }
}
