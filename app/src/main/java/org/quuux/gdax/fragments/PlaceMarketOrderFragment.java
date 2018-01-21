package org.quuux.gdax.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.quuux.feller.Log;
import org.quuux.gdax.API;
import org.quuux.gdax.Datastore;
import org.quuux.gdax.R;
import org.quuux.gdax.Util;
import org.quuux.gdax.events.APIError;
import org.quuux.gdax.events.ProductSelected;
import org.quuux.gdax.model.Order;
import org.quuux.gdax.model.Product;
import org.quuux.gdax.model.Tick;
import org.quuux.gdax.view.ProductAdapater;

import java.math.BigDecimal;

public class PlaceMarketOrderFragment extends Fragment {

    private static final String TAG = Log.buildTag(PlaceMarketOrderFragment.class);

    private RadioGroup mSide;
    private EditText mAmountText;
    private TextView mTotalText;
    private Tick mTick;
    private TextView mPriceText;

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

    private void updateTicker() {
        Datastore.getInstance().getTicker(Datastore.getInstance().getSelectedProduct());
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
        mAmountText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                updateTotal();
            }

            @Override
            public void afterTextChanged(final Editable s) {

            }
        });
        mSide = v.findViewById(R.id.side);
        mTotalText = v.findViewById(R.id.total);
        mPriceText = v.findViewById(R.id.price);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        updateTicker();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTicker(final Tick tick) {
        mTick = tick;
        Product product = Datastore.getInstance().getSelectedProduct();
        mPriceText.setText(getString(R.string.current_price, product.base_currency, Util.currencyFormat(mTick.price), product.quote_currency));
        updateTotal();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProductSelected(final ProductSelected event) {
        updateTicker();
    }

    private void updateTotal() {
        BigDecimal amount = getAmount();

        if (amount == null)
            return;

        if (mTick == null)
            return;

        BigDecimal value = amount.divide(mTick.price, 10, BigDecimal.ROUND_HALF_DOWN);
        Product product = Datastore.getInstance().getSelectedProduct();
        mTotalText.setText(getString(R.string.order_total, product.base_currency, Util.longFormat(value)));
    }

    private BigDecimal getAmount() {
        String value = mAmountText.getText().toString();
        if (TextUtils.isEmpty(value))
            return null;

        BigDecimal amount = null;

        try {
            amount = new BigDecimal(value);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                mAmountText.setError(getString(R.string.error_amount_not_greater_than_zero));
                amount = null;
            }
        } catch (NumberFormatException e) {
            mAmountText.setError(getString(R.string.error_amount_not_valid));
        }

        return amount;
    }

    private Order.Side getSide() {
        return mSide.getCheckedRadioButtonId() == R.id.buy ? Order.Side.buy : Order.Side.sell;
    }

    private void onCommit() {
        Order.Side side = getSide();
        BigDecimal amount = getAmount();
        if (amount == null)
            return;

        Product product = Datastore.getInstance().getSelectedProduct();
        API.getInstance().placeOrder(Order.newMarketOrder(product, side, amount), new API.ResponseListener<Order>() {
            @Override
            public void onSuccess(final Order result) {

            }

            @Override
            public void onError(final APIError error) {

            }
        });

    }
}
