package org.quuux.gdax;


import org.greenrobot.eventbus.EventBus;
import org.quuux.feller.Log;
import org.quuux.gdax.events.APIError;
import org.quuux.gdax.events.ProductSelected;
import org.quuux.gdax.model.Account;
import org.quuux.gdax.model.CoinbaseAccount;
import org.quuux.gdax.model.PaymentMethod;
import org.quuux.gdax.model.Product;
import org.quuux.gdax.model.Tick;
import org.quuux.gdax.net.API;
import org.quuux.gdax.net.AccountsCursor;
import org.quuux.gdax.net.CoinbaseAccountsCursor;
import org.quuux.gdax.net.Cursor;
import org.quuux.gdax.net.PaymentMethodsCursor;
import org.quuux.gdax.net.ProductsCursor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Datastore {

    private static final String TAG = Log.buildTag(Datastore.class);
    private static Datastore instance;

    private Map<Product, Tick> tickers = new HashMap<>();
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

    public List<Account> getAccounts() {
        return mAccounts.getItems();
    }
    public List<Product> getProducts() {
        return mProducts.getItems();
    }


    public void load() {
        Cursor[] cursors = new Cursor[] {mAccounts, mProducts};
        for (int i=0; i<cursors.length; i++) {
            if (cursors[i].getState() == Cursor.State.init)
                cursors[i].load();
        }
    }

    public Product getProduct(String id) {
        for (Product p : getProducts())
            if (p.id.equals(id))
                return p;
        return null;
    }

    public void getTicker(final Product product) {
        API.getInstance().getTicker(product, new API.ResponseListener<Tick>() {
            @Override
            public void onSuccess(final Tick result) {
                tickers.put(product, result);
                EventBus.getDefault().post(result);
            }

            @Override
            public void onError(final APIError error) {
                EventBus.getDefault().post(error);
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
