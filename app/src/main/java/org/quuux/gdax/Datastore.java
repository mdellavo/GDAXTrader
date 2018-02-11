package org.quuux.gdax;


import android.util.Pair;

import org.greenrobot.eventbus.EventBus;
import org.quuux.feller.Log;
import org.quuux.gdax.events.APIError;
import org.quuux.gdax.events.ProductSelected;
import org.quuux.gdax.model.Account;
import org.quuux.gdax.model.Product;
import org.quuux.gdax.model.ProductStat;
import org.quuux.gdax.model.Tick;
import org.quuux.gdax.net.API;
import org.quuux.gdax.net.AccountsCursor;
import org.quuux.gdax.net.CoinbaseAccountsCursor;
import org.quuux.gdax.net.Cursor;
import org.quuux.gdax.net.PaymentMethodsCursor;
import org.quuux.gdax.net.ProductsCursor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Datastore {
    private static Datastore instance;

    private static final String TAG = Log.buildTag(Datastore.class);
    private static final int TICKER_CACHE_AGE = 60 * 1000;
    private static final int STATS_CACHE_AGE = 60 * 1000;

    private Map<Product, Tick> tickers = new HashMap<>();
    private Map<Product, ProductStat> stats = new HashMap<>();
    private ProductsCursor mProducts = new ProductsCursor();
    private AccountsCursor mAccounts = new AccountsCursor();
    private PaymentMethodsCursor mPaymentMethods = new PaymentMethodsCursor();
    private CoinbaseAccountsCursor mCoinbaseAccounts = new CoinbaseAccountsCursor();

    private String selectedProductId = "BTC-USD";

    protected Datastore() {
    }

    public static Datastore getInstance() {
        if (instance == null)
            instance = new Datastore();
        return instance;
    }

    public AccountsCursor getAccounts() {
        return mAccounts;
    }
    public ProductsCursor getProducts() {
        return mProducts;
    }

    public void load() {
        Cursor[] cursors = new Cursor[] {mAccounts, mProducts};
        for (int i=0; i<cursors.length; i++) {
            if (cursors[i].getState() == Cursor.State.init)
                cursors[i].load();
        }
    }

    public Product getProduct(String id) {
        for (Product p : getProducts().getItems())
            if (p.id.equals(id))
                return p;
        return null;
    }

    private boolean valid(Date t, int cacheTimeMs) {
        Date now = new Date();
        return (now.getTime() - t.getTime()) < cacheTimeMs;
    }

    public void loadTicker(final Product product) {

        Tick t = tickers.get(product);
        if (t != null) {
            if (valid(t.time, TICKER_CACHE_AGE)) {
                EventBus.getDefault().post(t);
                return;
            }
        }
        if (tickers.containsKey(product))
            return;
        tickers.put(product, null);
        API.getInstance().getTicker(product, new API.ResponseListener<Tick>() {
            @Override
            public void onSuccess(final Tick result) {
                tickers.put(product, result);
                EventBus.getDefault().post(result);
            }

            @Override
            public void onError(final APIError error) {

            }
        });
    }

    public ProductStat getProductStat(Product product) {
        return stats.get(product);
    }

    public void loadStats(final Product product) {

        ProductStat s = stats.get(product);
        if (s != null) {
            if (valid(s.created_at, STATS_CACHE_AGE)) {
                EventBus.getDefault().post(s);
                return;
            }
        }
        if (stats.containsKey(product))
            return;
        stats.put(product, null);
        API.getInstance().getStats(product, new API.ResponseListener<ProductStat>() {
            @Override
            public void onSuccess(final ProductStat result) {
                stats.put(product, result);
                EventBus.getDefault().post(result);
            }

            @Override
            public void onError(final APIError error) {

            }
        });
    }

    public Product getSelectedProduct() {
        return getProduct(selectedProductId);
    }

    public void setSelectedProduct(Product product) {
        Log.d(TAG, "selected product %s", product.getName());
        selectedProductId = product.id;
        EventBus.getDefault().post(new ProductSelected());
    }

    public PaymentMethodsCursor getPaymentMethods() {
        return mPaymentMethods;
    }
    public CoinbaseAccountsCursor getCoinbaseAccounts() { return mCoinbaseAccounts; }

}
