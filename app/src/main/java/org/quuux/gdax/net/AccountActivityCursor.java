package org.quuux.gdax.net;

import org.quuux.gdax.model.Account;
import org.quuux.gdax.model.AccountActivity;


public class AccountActivityCursor extends Cursor<AccountActivity> {

    private final Account account;

    public AccountActivityCursor(Account account) {
        this.account = account;
    }

    @Override
    public Class<AccountActivity[]> getPageClass() {
        return AccountActivity[].class;
    }

    @Override
    public String getEndpoint() {
        return API.getInstance().accountLedgerEndpoint(account);
    }
}
