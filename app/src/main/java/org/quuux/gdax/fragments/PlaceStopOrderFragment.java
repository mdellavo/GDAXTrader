package org.quuux.gdax.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.quuux.gdax.Datastore;
import org.quuux.gdax.R;
import org.quuux.gdax.Util;
import org.quuux.gdax.model.Order;
import org.quuux.gdax.model.Product;
import org.quuux.gdax.model.Side;

import java.math.BigDecimal;


public class PlaceStopOrderFragment extends BasePlaceOrderFragment {

    private EditText mPriceText;

    public PlaceStopOrderFragment() {
    }

    public static PlaceStopOrderFragment newInstance() {
        PlaceStopOrderFragment fragment = new PlaceStopOrderFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mPriceText = v.findViewById(R.id.price);
        mPriceText.setVisibility(View.VISIBLE);
        v.findViewById(R.id.label_price).setVisibility(View.VISIBLE);
        return v;
    }

    public BigDecimal getPrice() {
        return Util.cleanDecimalInput(mPriceText, true);
    }

    public void onCommit() {
        Side side = getSide();
        BigDecimal amount = getAmount();
        BigDecimal price = getPrice();
        if (amount == null || price == null)
            return;

        Product product = Datastore.getInstance().getSelectedProduct();
        Order order = Order.newStopOrder(product, side, amount, price);
        submitOrder(order);
    }
}
