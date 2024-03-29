package org.quuux.gdax.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.quuux.gdax.net.API;
import org.quuux.gdax.net.Cursor;
import org.quuux.gdax.R;
import org.quuux.gdax.Util;
import org.quuux.gdax.events.APIError;
import org.quuux.gdax.events.ProductSelected;
import org.quuux.gdax.model.Order;
import org.quuux.gdax.net.OrdersCursor;
import org.quuux.gdax.view.CursorAdapter;


public class OrdersFragment extends CursorFragment {

    private OrdersCursor mCursor;
    private OrdersAdapter mAdapter;

    public OrdersFragment() {}

    public static OrdersFragment newInstance() {
        OrdersFragment fragment = new OrdersFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getTitle() {
        return R.string.orders;
    }

    @Override
    public boolean needsProductSelector() {
        return true;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        if (getArguments() != null) {
        }

        setLayoutResource(R.layout.fragment_orders);
        setHeaderResource(R.layout.orders_header);

        mCursor = new OrdersCursor();
        setCursor(mCursor);

        mAdapter = new OrdersAdapter(getContext(), mCursor);
        setAdapter(mAdapter);

        setItemClickListener(clickListener);

        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProductSelected(final ProductSelected event) {
        mCursor.reset();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_orders, menu);

        MenuItem checked;
        switch (mCursor.getStatusFilter()) {
            case open:
                checked = menu.findItem(R.id.open);
                break;

            case closed:
                checked = menu.findItem(R.id.closed);
                break;

            default:
                checked = menu.findItem(R.id.all);
                break;
        }

        checked.setChecked(true);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        boolean rv;
        switch (item.getItemId()) {

            case R.id.all:
            case R.id.open:
            case R.id.closed:
                if (!item.isChecked()) {
                    updateStatusFilter(item.getItemId());
                    item.setChecked(true);
                }
                rv = true;
                break;

            default:
                rv = super.onOptionsItemSelected(item);
        }

        return rv;
    }

    private void updateStatusFilter(final int id) {

        OrdersCursor.StatusFilter filter;
        switch (id) {
            case R.id.open:
                filter = OrdersCursor.StatusFilter.open;
                break;
            case R.id.closed:
                filter = OrdersCursor.StatusFilter.closed;
                break;

            default:
                filter = OrdersCursor.StatusFilter.all;
                break;
        }

        mCursor.setStatusFilter(filter);
        mAdapter.notifyDataSetChanged();
    }

    private void showOrder(final Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(order.side.name());

        final View v = getLayoutInflater().inflate(R.layout.order_details, null);

        TextView id = v.findViewById(R.id.id);
        id.setText(order.id);

        TextView product = v.findViewById(R.id.product);
        product.setText(order.product_id);

        TextView status = v.findViewById(R.id.status);
        status.setText(order.status.name());

        TextView created_at = v.findViewById(R.id.created_at);
        created_at.setText(order.created_at.toString());

        TextView type = v.findViewById(R.id.type);
        type.setText(order.type.name());

        TextView size = v.findViewById(R.id.size);
        size.setText(Util.longFormat(order.size));

        TextView price = v.findViewById(R.id.price);
        price.setText(Util.longFormat(order.price));

        builder.setView(v);
        final AlertDialog dialog = builder.create();

        if (order.isCancelable()) {
            Button cancel = v.findViewById(R.id.cancel);
            cancel.setVisibility(View.VISIBLE);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    cancelOrder(order);
                    dialog.dismiss();
                }
            });
        }

        dialog.show();
    }

    private void cancelOrder(final Order order) {
        API.getInstance().cancelOrder(order, new API.ResponseListener<Order>() {
            @Override
            public void onSuccess(final Order result) {

            }

            @Override
            public void onError(final APIError error) {

            }
        });
    }

    static class OrderTag {
        TextView side, price, size, date;
    }

    class OrdersAdapter extends CursorAdapter<Order> {

        public OrdersAdapter(final Context context, final Cursor<Order> cursor) {
            super(context, cursor);
        }

        @Override
        public void bindView(final int position, final View view, final Order order) {
            OrderTag tag = (OrderTag) view.getTag();
            tag.side.setText(order.side.name());
            tag.price.setText(order.price != null ? Util.currencyFormat(order.price) : "");
            tag.size.setText(order.size != null ? Util.shortFormat(order.size) : "");
            tag.date.setText(Util.dateFormat(order.created_at));
        }

        @Override
        public View newView(final int position, final ViewGroup parent) {
            View v = getLayoutInflater().inflate(R.layout.order_item, parent, false);
            OrderTag tag = new OrderTag();
            tag.side = v.findViewById(R.id.side);
            tag.price = v.findViewById(R.id.price);
            tag.size = v.findViewById(R.id.size);
            tag.date = v.findViewById(R.id.date);
            v.setTag(tag);
            return v;
        }
    }

    private AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
            Order order = (Order) getList().getItemAtPosition(position);
            showOrder(order);
        }
    };
}
