package org.quuux.gdax;


import org.greenrobot.eventbus.EventBus;
import org.quuux.feller.Log;
import org.quuux.gdax.events.APIError;
import org.quuux.gdax.events.ProductSelected;
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
import java.util.Map;
import java.util.Objects;

public class Datastore {
    private static Datastore instance;

    private static final String TAG = Log.buildTag(Datastore.class);

    public static class Candles {
        public final Product product;
        public final int granularity;
        public final Date start;
        public final Date end;
        public final Date loaded = new Date();
        public final float[][] candles;

        public Candles(Product product, int granularity, Date start, Date end, float[][] candles) {
            this.product = product;
            this.granularity = granularity;
            this.start = start;
            this.end = end;
            this.candles = candles;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Candles candles = (Candles) o;
            return granularity == candles.granularity &&
                    Objects.equals(product, candles.product) &&
                    Objects.equals(start, candles.start) &&
                    Objects.equals(end, candles.end);
        }

        @Override
        public int hashCode() {
            return Objects.hash(product, granularity, start, end);
        }
    }

    private static final int CACHE_AGE = 60 * 1000;

    // Mem Caches
    private Map<Product, Tick> tickers = new HashMap<>();
    private Map<Product, ProductStat> stats = new HashMap<>();

    private Map<Candles, float[][]> candles = new HashMap<>();

    // Cursors
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
        Cursor[] cursors = new Cursor[]{mAccounts, mProducts};
        for (int i = 0; i < cursors.length; i++) {
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
        if (t != null && valid(t.time, CACHE_AGE)) {
            EventBus.getDefault().post(t);
            return;
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
        if (s != null && valid(s.created_at, CACHE_AGE)) {
            EventBus.getDefault().post(s);
            return;
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

    public CoinbaseAccountsCursor getCoinbaseAccounts() {
        return mCoinbaseAccounts;
    }

    public void loadCandles(final Product product, final int granularity, final Date start, final Date end) {

        API.getInstance().getCandles(product, granularity, start, end, new API.ResponseListener<float[][]>() {
            @Override
            public void onSuccess(final float[][] result) {
                Candles c = new Candles(product, granularity, start, end, result);
                EventBus.getDefault().post(c);
            }

            @Override
            public void onError(final APIError error) {

            }
        });
    }

    public void loadCandles(final Product product) {
        loadCandles(product, API.ONE_DAY, null, null);
    }


    public void loadRecentCandles(final Product product, int granularity, int lookback) {
        Date end = new Date();
        Date start = Util.addDays(end, -lookback);
        loadCandles(product, granularity, start, end);
    }
}
