package org.quuux.gdax.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;

import org.quuux.gdax.R;


public class BankAccountDepositFragment extends Fragment {

    Spinner mPaymentMethod;
    EditText mAmount;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_bank_account_deposit, container, false);

        mPaymentMethod = v.findViewById(R.id.payment_method);
        mAmount = v.findViewById(R.id.amount);

        return v;
    }
}
