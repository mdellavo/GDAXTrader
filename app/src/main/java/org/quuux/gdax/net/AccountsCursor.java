package org.quuux.gdax.net;


import org.quuux.gdax.model.Account;

public class AccountsCursor extends Cursor<Account> {
    @Override
    public Class<Account[]> getPageClass() {
        return Account[].class;
    }

    @Override
    public String getEndpoint() {
        return API.GDAX_ACCOUNTS_ENDPOINT;
    }
}
