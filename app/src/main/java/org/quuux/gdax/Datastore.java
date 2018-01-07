package org.quuux.gdax;


import org.greenrobot.eventbus.EventBus;
import org.quuux.gdax.events.APIError;
import org.quuux.gdax.events.AccountsLoadError;
import org.quuux.gdax.events.AccountsUpdated;
import org.quuux.gdax.model.Account;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Datastore {

    private static Datastore instance;

    private List<Account> accounts = new ArrayList<>();

    protected Datastore() {
    }

    public static Datastore getInstance() {
        if (instance == null)
            instance = new Datastore();
        return instance;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void loadAccounts() {
        API.getInstance().getAccounts(new API.ResponseListener<Account[]>() {
            @Override
            public void onSuccess(final Account[] result) {
                accounts.clear();
                accounts.addAll(Arrays.asList(result));

                EventBus.getDefault().post(new AccountsUpdated());
            }

            @Override
            public void onError(final APIError error) {
                EventBus.getDefault().post(new AccountsLoadError());
            }
        });
    }
}
