package org.quuux.gdax.fragments;



import android.os.Bundle;

import org.quuux.gdax.Datastore;
import org.quuux.gdax.model.CoinbaseAccount;
import org.quuux.gdax.model.Withdraw;
import org.quuux.gdax.net.CoinbaseAccountsCursor;
import org.quuux.gdax.view.CoinbaseAccountAdapter;
import org.quuux.gdax.view.CursorAdapter;

import java.math.BigDecimal;


public class CoinbaseWithdrawFragment extends BaseWithdrawFragment<CoinbaseAccount> {
    public static CoinbaseWithdrawFragment newInstance() {
        CoinbaseWithdrawFragment fragment = new CoinbaseWithdrawFragment();
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
    protected Withdraw getWithdraw(final CoinbaseAccount source, final BigDecimal amount) {
        return Withdraw.newCoinbase(source, amount);
    }
}
