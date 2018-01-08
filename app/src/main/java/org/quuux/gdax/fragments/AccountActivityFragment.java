package org.quuux.gdax.fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.quuux.gdax.R;
import org.quuux.gdax.model.Account;

public class AccountActivityFragment extends Fragment {
    private static final String ARG_ACCOUNT = "account";

    private Account mAccount;

    public AccountActivityFragment() {}

    public static AccountActivityFragment newInstance(Account account) {
        AccountActivityFragment fragment = new AccountActivityFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ACCOUNT, account);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAccount = (Account) getArguments().getSerializable(ARG_ACCOUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account_activity, container, false);
    }

}
