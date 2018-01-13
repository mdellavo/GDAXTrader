package org.quuux.gdax;


import org.greenrobot.eventbus.EventBus;
import org.quuux.gdax.events.APIError;
import org.quuux.gdax.events.AccountsLoadError;
import org.quuux.gdax.events.AccountsUpdated;
import org.quuux.gdax.events.PageLoadError;
import org.quuux.gdax.events.PageLoaded;
import org.quuux.gdax.model.Account;
import org.quuux.gdax.model.AccountActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    public static abstract class Cursor<T> implements API.PaginatedResponseListener<T[]> {
        String before, after;
        List<T> items = new ArrayList<>();

        public List<T> getItems() {
            return items;
        }

        abstract public Class<T[]> getPageClass();

        public void load() {
            API api = API.getInstance();
            api.loadPage(getEndpoint(), this, getPageClass());
        }

        abstract String getEndpoint();

        @Override
        public void onSuccess(final T[] result, final String before, final String after) {
            Collections.addAll(items, result);
            this.before = before;
            this.after = after;
            EventBus.getDefault().post(new PageLoaded(this));
        }

        @Override
        public void onError(final APIError error) {
            EventBus.getDefault().post(new PageLoadError(this, error));
        }
    }

    public static class AccountActivityCursor extends Cursor<AccountActivity> {

        private final Account account;

        public AccountActivityCursor(Account account) {
            this.account = account;
        }

        @Override
        public Class<AccountActivity[]> getPageClass() {
            return AccountActivity[].class;
        }

        @Override
        String getEndpoint() {
            return API.getInstance().accountLedgerEndpoint(account);
        }
    }
}
