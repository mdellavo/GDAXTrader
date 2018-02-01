package org.quuux.gdax.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import org.quuux.gdax.Datastore;
import org.quuux.gdax.R;
import org.quuux.gdax.Util;
import org.quuux.gdax.events.APIError;
import org.quuux.gdax.model.Deposit;
import org.quuux.gdax.model.PaymentMethod;
import org.quuux.gdax.net.API;
import org.quuux.gdax.net.Cursor;
import org.quuux.gdax.net.PaymentMethodsCursor;
import org.quuux.gdax.view.PaymentMethodAdapter;

import java.math.BigDecimal;


public class BankAccountDepositFragment extends Fragment {

    Spinner mPaymentMethod;
    EditText mAmount;
    PaymentMethodAdapter mAdapter;
    PaymentMethodsCursor mCursor = Datastore.getInstance().getPaymentMethodsCursor();
    Button mCommit;

    public BankAccountDepositFragment() {
    }

    public static BankAccountDepositFragment newInstance() {
        BankAccountDepositFragment fragment = new BankAccountDepositFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        mAdapter = new PaymentMethodAdapter(getContext(), mCursor);
        mAdapter.register();

        if (mCursor.getState() == Cursor.State.init)
            mCursor.load();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.unregister();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_bank_account_deposit, container, false);

        mPaymentMethod = v.findViewById(R.id.payment_method);
        mPaymentMethod.setAdapter(mAdapter);

        mAmount = v.findViewById(R.id.amount);

        mCommit = v.findViewById(R.id.commit);
        mCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onCommit();
            }
        });

        return v;
    }

    private void onCommit() {
        PaymentMethod paymentMethod = getPaymentMethod();
        BigDecimal amount = getAmount();
        if (paymentMethod == null || amount == null)
            return;

        Deposit deposit = Deposit.newPaymentMethod(paymentMethod, amount);
        API.getInstance().deposit(deposit, new API.ResponseListener<Deposit>() {
            @Override
            public void onSuccess(final Deposit result) {

            }

            @Override
            public void onError(final APIError error) {

            }
        });
    }

    public PaymentMethod getPaymentMethod() {
        PaymentMethod rv = (PaymentMethod) mPaymentMethod.getSelectedItem();
        if (rv == null) {

        }
        return rv;
    }

    public BigDecimal getAmount() {
        return Util.cleanDecimalInput(mAmount, true);
    }

}
