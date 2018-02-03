package org.quuux.gdax.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import org.quuux.gdax.R;
import org.quuux.gdax.Util;
import org.quuux.gdax.events.APIError;
import org.quuux.gdax.model.Withdraw;
import org.quuux.gdax.net.API;
import org.quuux.gdax.net.Cursor;
import org.quuux.gdax.view.CursorAdapter;

import java.math.BigDecimal;

abstract public class BaseWithdrawFragment<T> extends Fragment {
    CursorAdapter<T> mAdapter;
    Spinner mSource;
    EditText mAmount;
    Button mCommit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        mAdapter = getAdapter();
        mAdapter.register();

        if (mAdapter.cursor.getState() == Cursor.State.init)
            mAdapter.cursor.load();
    }

    protected abstract CursorAdapter<T> getAdapter();

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.unregister();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_new_withdraw, container, false);

        mSource = v.findViewById(R.id.to);
        mSource.setAdapter(mAdapter);

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


    public void onCommit() {
        T source = getSource();
        BigDecimal amount = getAmount();
        if (source == null || amount == null)
            return;

        Withdraw withdraw = getWithdraw(source, amount);

        API.getInstance().withdraw(withdraw, new API.ResponseListener<Withdraw>() {
            @Override
            public void onSuccess(final Withdraw result) {

            }

            @Override
            public void onError(final APIError error) {

            }
        });
    }

    protected abstract Withdraw getWithdraw(final T source, final BigDecimal amount);

    public T getSource() {
        T rv = (T) mSource.getSelectedItem();
        return rv;
    }

    public BigDecimal getAmount() {
        return Util.cleanDecimalInput(mAmount, true);
    }

}
