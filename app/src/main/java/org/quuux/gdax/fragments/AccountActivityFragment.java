package org.quuux.gdax.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.quuux.gdax.Datastore;
import org.quuux.gdax.R;
import org.quuux.gdax.Util;
import org.quuux.gdax.model.Account;
import org.quuux.gdax.model.AccountActivity;
import org.quuux.gdax.view.CursorAdapter;

public class AccountActivityFragment extends Fragment {
    private static final String ARG_ACCOUNT = "account";

    private Account mAccount;
    private Datastore.AccountActivityCursor mCursor;
    private CursorAdapter<AccountActivity> mAdapter;
    private ListView mList;

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

        mCursor = new Datastore.AccountActivityCursor(mAccount);
        mCursor.load();

        mAdapter = new AccountActivityAdapter(getContext(), mCursor);
        mAdapter.register();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.unregister();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_account_activity, container, false);
        mList = v.findViewById(R.id.list);

        View headerView = inflater.inflate(R.layout.account_activity_header, null);
        mList.addHeaderView(headerView, null, false);

        mList.setEmptyView(v.findViewById(R.id.empty));

        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                AccountActivity activity = (AccountActivity) mList.getItemAtPosition(position);
                show(activity);
            }
        });
        return v;
    }

    private void show(final AccountActivity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(activity.type);

        final View v = getLayoutInflater().inflate(R.layout.account_activity_details, null);

        TextView id = v.findViewById(R.id.id);
        id.setText(activity.id);

        TextView created_at = v.findViewById(R.id.created_at);
        created_at.setText(activity.created_at.toString());

        TextView amount = v.findViewById(R.id.amount);
        amount.setText(activity.amount.toPlainString());

        TextView balance = v.findViewById(R.id.balance);
        balance.setText(activity.balance.toPlainString());

        builder.setView(v);

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    static class AccountActivityViewTag {
        TextView type, date, amount, balance;
    }

    class AccountActivityAdapter extends CursorAdapter<AccountActivity> {

        public AccountActivityAdapter(final Context context, final Datastore.Cursor<AccountActivity> cursor) {
            super(context, cursor);
        }

        @Override
        public void bindView(final int position, final View view, final AccountActivity item) {
            AccountActivityViewTag tag = (AccountActivityViewTag) view.getTag();
            tag.type.setText(item.type);
            tag.date.setText(Util.dateFormat(item.created_at));
            tag.amount.setText(Util.longDecimalFormat(item.amount));
            tag.balance.setText(Util.longDecimalFormat(item.balance));
        }

        @Override
        public View newView(final int position, final ViewGroup parent) {
            View v = getLayoutInflater().inflate(R.layout.account_activity_item, parent, false);
            AccountActivityViewTag tag = new AccountActivityViewTag();
            tag.type = v.findViewById(R.id.type);
            tag.date = v.findViewById(R.id.date);
            tag.amount = v.findViewById(R.id.amount);
            tag.balance = v.findViewById(R.id.balance);
            v.setTag(tag);
            return v;
        }
    }

}