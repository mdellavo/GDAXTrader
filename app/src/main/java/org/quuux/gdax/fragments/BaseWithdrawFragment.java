package org.quuux.gdax.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
    EditText mAmount, mCode;
    Button mCommit;

    Handler mHandler = new Handler(Looper.getMainLooper());
    Button mGetCode;

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

        mGetCode = v.findViewById(R.id.get_code);
        mGetCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                getCode();            }
        });

        mCode = v.findViewById(R.id.two_fa);

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

        String code = getTwoFactorCode();

        Withdraw withdraw = getWithdraw(source, amount);

        API.getInstance().withdraw(withdraw, code, new API.ResponseListener<Withdraw>() {
            @Override
            public void onSuccess(final Withdraw result) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onWithdrawSuccess();
                    }
                });
            }

            @Override
            public void onError(final APIError error) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onWithdrawError();
                    }
                });
            }
        });
    }

    private String getTwoFactorCode() {
        return mCode.getText().toString();
    }

    protected abstract Withdraw getWithdraw(final T source, final BigDecimal amount);

    public T getSource() {
        T rv = (T) mSource.getSelectedItem();
        return rv;
    }

    public BigDecimal getAmount() {
        return Util.cleanDecimalInput(mAmount, true);
    }

    public void onWithdrawError() {
        mCommit.setEnabled(true);
    }

    public void onWithdrawSuccess() {
        mCommit.setEnabled(true);
        mCommit.setText(null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.withdraw_success_title);
        builder.setMessage(R.string.withdraw_success_message);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void getCode() {
        mGetCode.setEnabled(false);

        API.getInstance().getTwoFactorCode(new API.ResponseListener<API.TwoFactorResponse>() {
            @Override
            public void onSuccess(final API.TwoFactorResponse result) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onTwoFactorSuccess();
                    }
                });
            }

            @Override
            public void onError(final APIError error) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onTwoFactorFailure();
                    }
                });
            }
        });
    }

    private void onTwoFactorSuccess() {
        mGetCode.setEnabled(true);
    }

    private void onTwoFactorFailure() {
        mGetCode.setEnabled(true);
    }
}
