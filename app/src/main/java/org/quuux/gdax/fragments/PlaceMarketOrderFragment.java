package org.quuux.gdax.fragments;

import android.os.Bundle;

import org.quuux.gdax.Datastore;
import org.quuux.gdax.model.Order;
import org.quuux.gdax.model.Product;
import org.quuux.gdax.model.Side;

import java.math.BigDecimal;

public class PlaceMarketOrderFragment extends BasePlaceOrderFragment {

    public PlaceMarketOrderFragment() {
    }

    public static PlaceMarketOrderFragment newInstance() {
        PlaceMarketOrderFragment fragment = new PlaceMarketOrderFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public void onCommit() {
        Side side = getSide();
        BigDecimal amount = getAmount();
        if (amount == null)
            return;

        Product product = Datastore.getInstance().getSelectedProduct();
        Order order = Order.newMarketOrder(product, side, amount);
        submitOrder(order);
    }

}
