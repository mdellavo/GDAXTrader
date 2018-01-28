package org.quuux.gdax.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.quuux.gdax.model.Side;
import org.quuux.gdax.net.API;
import org.quuux.gdax.Datastore;
import org.quuux.gdax.R;
import org.quuux.gdax.Util;
import org.quuux.gdax.events.APIError;
import org.quuux.gdax.events.ProductSelected;
import org.quuux.gdax.model.Order;
import org.quuux.gdax.model.Product;
import org.quuux.gdax.model.Tick;

import java.math.BigDecimal;

abstract public class BasePlaceOrderFragment extends Fragment {

    public interface Listener {
        void showOrders();
    }

    protected RadioGroup mSide;
    protected EditText mAmountText;
    protected TextView mTotalText;
    protected Tick mTick;
    protected TextView mCurrentPrice;
    protected Listener mListener;

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        mListener = (Listener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_new_order, container, false);

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
        mCurrentPrice = v.findViewById(R.id.current_price);

        return v;
    }

    protected abstract void onCommit();

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

    private void updateTicker() {
        Datastore.getInstance().getTicker(Datastore.getInstance().getSelectedProduct());
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProductSelected(final ProductSelected event) {
        updateTicker();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTicker(final Tick tick) {
        mTick = tick;
        Product product = Datastore.getInstance().getSelectedProduct();
        mCurrentPrice.setText(getString(R.string.current_price, product.base_currency, Util.currencyFormat(mTick.price), product.quote_currency));
        updateTotal();
    }


    public void updateTotal() {
        BigDecimal amount = cleanDecimalInput(mAmountText, false);

        if (amount == null)
            return;

        if (mTick == null)
            return;

        BigDecimal value = amount.divide(mTick.price, 10, BigDecimal.ROUND_HALF_DOWN);
        Product product = Datastore.getInstance().getSelectedProduct();
        mTotalText.setText(getString(R.string.order_total, product.base_currency, Util.longFormat(value)));
    }

    public BigDecimal cleanDecimalInput(EditText v, boolean validate) {
        String value = v.getText().toString();
        if (TextUtils.isEmpty(value)) {
            if (validate)
                v.setError(getString(R.string.error_amount_not_valid));
            return null;
        }

        BigDecimal amount = null;

        try {
            amount = new BigDecimal(value);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                if (validate)
                    v.setError(getString(R.string.error_amount_not_greater_than_zero));
                amount = null;
            }
        } catch (NumberFormatException e) {
            if (validate)
                v.setError(getString(R.string.error_amount_not_valid));
        }

        return amount;
    }

    public BigDecimal getAmount() {
        return cleanDecimalInput(mAmountText, true);
    }

    public Side getSide() {
        return mSide.getCheckedRadioButtonId() == R.id.buy ? Side.buy : Side.sell;
    }

    public void submitOrder(Order order) {
        API.getInstance().placeOrder(order, new API.ResponseListener<Order>() {
            @Override
            public void onSuccess(final Order result) {
                mListener.showOrders();
            }

            @Override
            public void onError(final APIError error) {

            }
        });
    }

}
