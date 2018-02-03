package org.quuux.gdax.fragments;


import android.os.Bundle;

import org.quuux.gdax.Datastore;
import org.quuux.gdax.model.PaymentMethod;
import org.quuux.gdax.model.Withdraw;
import org.quuux.gdax.net.PaymentMethodsCursor;
import org.quuux.gdax.view.CursorAdapter;
import org.quuux.gdax.view.PaymentMethodAdapter;

import java.math.BigDecimal;

public class BankAccountWithdrawFragment extends BaseWithdrawFragment<PaymentMethod> {
    public static BankAccountWithdrawFragment newInstance() {
        BankAccountWithdrawFragment fragment = new BankAccountWithdrawFragment();
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
    protected Withdraw getWithdraw(final PaymentMethod source, final BigDecimal amount) {
        return Withdraw.newPaymentMethod(source, amount);
    }
}
