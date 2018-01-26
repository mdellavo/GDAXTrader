package org.quuux.gdax.fragments;

import android.os.Bundle;

import org.quuux.gdax.Datastore;
import org.quuux.gdax.model.Order;
import org.quuux.gdax.model.Product;

import java.math.BigDecimal;

public class PlaceMarketOrderFragment extends OrderFragment {

    public PlaceMarketOrderFragment() {
    }

    public static PlaceMarketOrderFragment newInstance() {
        PlaceMarketOrderFragment fragment = new PlaceMarketOrderFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public void onCommit() {
        Order.Side side = getSide();
        BigDecimal amount = getAmount();
        if (amount == null)
            return;

        Product product = Datastore.getInstance().getSelectedProduct();
        Order order = Order.newMarketOrder(product, side, amount);
        submitOrder(order);
    }

}
