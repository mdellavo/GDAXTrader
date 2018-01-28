package org.quuux.gdax.net;

import org.quuux.gdax.model.CoinbaseAccount;

public class CoinbaseAccountsCursor extends Cursor<CoinbaseAccount> {
    @Override
    public Class<CoinbaseAccount[]> getPageClass() {
        return CoinbaseAccount[].class;
    }

    @Override
    public String getEndpoint() {
        return API.GDAX_COINBASE_ACCOUNTS_ENDPOINT;
    }
}
