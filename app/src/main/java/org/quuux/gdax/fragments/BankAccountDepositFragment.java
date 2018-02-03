package org.quuux.gdax.fragments;

import android.os.Bundle;

import org.quuux.gdax.Datastore;
import org.quuux.gdax.model.Deposit;
import org.quuux.gdax.model.PaymentMethod;
import org.quuux.gdax.net.PaymentMethodsCursor;
import org.quuux.gdax.view.CursorAdapter;
import org.quuux.gdax.view.PaymentMethodAdapter;

import java.math.BigDecimal;


public class BankAccountDepositFragment extends BaseDepositFragment<PaymentMethod> {
    public static BankAccountDepositFragment newInstance() {
        BankAccountDepositFragment fragment = new BankAccountDepositFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected CursorAdapter<PaymentMethod> getAdapter() {
        PaymentMethodsCursor cursor = Datastore.getInstance().getPaymentMethods();
        PaymentMethodAdapter adapter = new PaymentMethodAdapter(getContext(), cursor);
        return adapter;
    }

    @Override
    protected Deposit getDeposit(final PaymentMethod source, final BigDecimal amount) {
        return Deposit.newPaymentMethod(source, amount);
    }
}
