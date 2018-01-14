package org.quuux.gdax.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.quuux.gdax.API;
import org.quuux.gdax.R;

import java.math.BigDecimal;

public class PlaceMarketOrderFragment extends Fragment {

    EditText mAmountText;

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

        Button button = v.findViewById(R.id.commit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onCommit();
            }
        });

        mAmountText = v.findViewById(R.id.amount);

        return v;
    }

    private BigDecimal getAmount() {
        return new BigDecimal(mAmountText.getText().toString());
    }

    private void onCommit() {
        try {
            BigDecimal amount = getAmount();
            if (amount.compareTo(BigDecimal.ZERO) >= 0) {
                mAmountText.setError(getString(R.string.error_amount_not_greater_than_zero));
                return;
            }
            
            API.getInstance().placeOrder(amount);
        } catch (NumberFormatException e) {
            mAmountText.setError(getString(R.string.error_amount_not_valid));
            return;
        }
    }
}
