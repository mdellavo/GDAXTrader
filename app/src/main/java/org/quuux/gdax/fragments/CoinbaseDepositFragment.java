package org.quuux.gdax.fragments;


import android.os.Bundle;

import org.quuux.gdax.Datastore;
import org.quuux.gdax.model.CoinbaseAccount;
import org.quuux.gdax.model.Deposit;
import org.quuux.gdax.net.CoinbaseAccountsCursor;
import org.quuux.gdax.view.CoinbaseAccountAdapter;
import org.quuux.gdax.view.CursorAdapter;

import java.math.BigDecimal;

public class CoinbaseDepositFragment extends BaseDepositFragment<CoinbaseAccount> {

    public static CoinbaseDepositFragment newInstance() {
        CoinbaseDepositFragment fragment = new CoinbaseDepositFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected CursorAdapter<CoinbaseAccount> getAdapter() {
        CoinbaseAccountsCursor cursor = Datastore.getInstance().getCoinbaseAccounts();
        CoinbaseAccountAdapter adapter = new CoinbaseAccountAdapter(getContext(), cursor);
        return adapter;
    }

    @Override
    protected Deposit getDeposit(final CoinbaseAccount source, final BigDecimal amount) {
        return Deposit.newCoinbase(source, amount);
    }
}
