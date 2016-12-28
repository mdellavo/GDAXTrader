package org.quuux.gdax.view;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.quuux.gdax.R;
import org.quuux.gdax.Util;
import org.quuux.gdax.model.OrderBook;

import java.math.BigDecimal;
import java.util.ArrayList;

public class OrderBookAdapter extends BaseAdapter {

    private static final String TAG = "OrderBookAdapter";
    private int markerPosition;

    private static class OrderBookEntry {
        OrderBook.Side side;
        BigDecimal price;
        OrderBook.OrderBookBin bin;
    }

    private static OrderBookEntry MARKER = new OrderBookEntry();

    private static class Holder {
        TextView side, price, size;
    }

    private final Context context;
    private final OrderBook orderBook;
    private ArrayList<OrderBookEntry> entries = new ArrayList<>();

    public OrderBookAdapter(Context context, OrderBook orderBook) {
        super();
        this.context = context;
        this.orderBook = orderBook;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getCount() {
        return entries.size();
    }

    @Override
    public OrderBookEntry getItem(int position) {
        return entries.get(position);
    }

    private boolean isMarker(int position) {
        OrderBookEntry entry = getItem(position);
        return entry == MARKER;
    }

    @Override
    public long getItemId(final int i) {
        if (isMarker(i))
            return 0;
        return getItem(i).price.hashCode();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(final int position) {
        return isMarker(position) ? 1 : 0;
    }

    @Override
    public View getView(final int position, final View view, final ViewGroup parent) {
        View v;
        if (isMarker(position)) {
            v = view != null ? view : newMarkerView(parent);
            bindMarkerView(v);
        } else {
            OrderBookEntry item = getItem(position);
            v = view != null ? view : newView(parent);
            bindView(v, item);
        }
        return v;
    }

    private void bindMarkerView(final View view) {

    }

    private View newMarkerView(final ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.order_book_marker, parent, false);
        return v;
    }

    private View newView(final ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.order_book_item, parent, false);

        Holder holder = new Holder();
        holder.side = (TextView) v.findViewById(R.id.side);
        holder.price = (TextView) v.findViewById(R.id.price);
        holder.size = (TextView) v.findViewById(R.id.size);

        v.setTag(holder);

        return v;
    }

    private void bindView(final View view, final OrderBookEntry item) {
        final Holder holder = (Holder) view.getTag();
        holder.side.setText(item.side.name());
        holder.price.setText(Util.currencyFormat(item.bin.price));
        holder.size.setText(Util.decimalFormat(item.bin.size));
    }

    public int getMarkerPosition() {
        return markerPosition;
    }

    public void update() {
        entries.clear();
        buildEntriesForSide(OrderBook.Side.buy);
        markerPosition = entries.size();
        entries.add(MARKER);
        buildEntriesForSide(OrderBook.Side.sell);
        notifyDataSetChanged();
    }

    private void buildEntriesForSide(final OrderBook.Side side) {
        for (BigDecimal price : orderBook.prices(side)) {
            OrderBookEntry entry = new OrderBookEntry();
            entry.price = price;
            entry.side = side;
            entry.bin = orderBook.getBin(side, price);
            entries.add(entry);
        }
    }
}
