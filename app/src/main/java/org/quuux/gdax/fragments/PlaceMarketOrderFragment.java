package org.quuux.gdax.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;

import org.quuux.gdax.API;
import org.quuux.gdax.Datastore;
import org.quuux.gdax.R;
import org.quuux.gdax.events.APIError;
import org.quuux.gdax.model.Order;
import org.quuux.gdax.view.ProductAdapater;

import java.math.BigDecimal;

public class PlaceMarketOrderFragment extends Fragment {

    private RadioGroup mSide;
    private EditText mAmountText;
    private Spinner mSpinner;
    private ProductAdapater mSpinnerAdapter;

    public PlaceMarketOrderFragment() {
    }

    public static PlaceMarketOrderFragment newInstance() {
        PlaceMarketOrderFragment fragment = new PlaceMarketOrderFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_place_market_order, container, false);

        mSpinner = v.findViewById(R.id.product);

        mSpinnerAdapter = new ProductAdapater(getContext(), Datastore.getInstance().getProducts());
        mSpinner.setAdapter(mSpinnerAdapter);

        Button button = v.findViewById(R.id.commit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onCommit();
            }
        });

        mAmountText = v.findViewById(R.id.amount);
        mSide = v.findViewById(R.id.side);

        return v;
    }

    private BigDecimal getAmount() {
        return new BigDecimal(mAmountText.getText().toString());
    }

    private Order.Side getSide() {
        return mSide.getCheckedRadioButtonId() == R.id.buy ? Order.Side.buy : Order.Side.sell;
    }

    private void onCommit() {
        Order.Side side = getSide();

        BigDecimal amount;
        try {
            amount = getAmount();
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                mAmountText.setError(getString(R.string.error_amount_not_greater_than_zero));
                return;
            }
        } catch (NumberFormatException e) {
            mAmountText.setError(getString(R.string.error_amount_not_valid));
            return;
        }

        API.getInstance().placeOrder(Order.newMarketOrder("BTC-USD", side, amount), new API.ResponseListener<Order>() {
            @Override
            public void onSuccess(final Order result) {

            }

            @Override
            public void onError(final APIError error) {

            }
        });

    }
}
