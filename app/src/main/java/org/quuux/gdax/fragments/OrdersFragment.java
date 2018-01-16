package org.quuux.gdax.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.quuux.gdax.Datastore;
import org.quuux.gdax.R;
import org.quuux.gdax.Util;
import org.quuux.gdax.model.Order;
import org.quuux.gdax.view.CursorAdapter;


public class OrdersFragment extends CursorFragment {

    private Datastore.OrdersCursor mCursor;
    private OrdersAdapter mAdapter;

    public OrdersFragment() {}

    public static OrdersFragment newInstance() {
        OrdersFragment fragment = new OrdersFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        if (getArguments() != null) {
        }

        setLayoutResource(R.layout.fragment_orders);
        setHeaderResource(R.layout.orders_header);

        mCursor = new Datastore.OrdersCursor();
        setCursor(mCursor);

        mAdapter = new OrdersAdapter(getContext(), mCursor);
        setAdapter(mAdapter);

        //setItemClickListener(clickListener);

        super.onCreate(savedInstanceState);
    }

    static class OrderTag {
        TextView side, price, size, date, status;
    }

    class OrdersAdapter extends CursorAdapter<Order> {

        public OrdersAdapter(final Context context, final Datastore.Cursor<Order> cursor) {
            super(context, cursor);
        }

        @Override
        public void bindView(final int position, final View view, final Order order) {
            OrderTag tag = (OrderTag) view.getTag();
            tag.side.setText(order.side.name());
            tag.price.setText(order.price != null ? order.price.toPlainString() : "");
            tag.size.setText(order.size != null ? order.size.toPlainString() : "");
            tag.date.setText(Util.dateFormat(order.created_at));
            tag.status.setText(order.status.name());
        }

        @Override
        public View newView(final int position, final ViewGroup parent) {
            View v = getLayoutInflater().inflate(R.layout.order_item, parent, false);
            OrderTag tag = new OrderTag();
            tag.side = v.findViewById(R.id.side);
            tag.price = v.findViewById(R.id.price);
            tag.size = v.findViewById(R.id.size);
            tag.date = v.findViewById(R.id.date);
            tag.status = v.findViewById(R.id.status);
            v.setTag(tag);
            return v;
        }
    }

}
