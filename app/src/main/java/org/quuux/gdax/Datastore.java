package org.quuux.gdax;


import org.greenrobot.eventbus.EventBus;
import org.quuux.feller.Log;
import org.quuux.gdax.events.APIError;
import org.quuux.gdax.events.AccountsLoadError;
import org.quuux.gdax.events.AccountsUpdated;
import org.quuux.gdax.events.ProductSelected;
import org.quuux.gdax.events.ProductsLoadError;
import org.quuux.gdax.events.ProductsLoaded;
import org.quuux.gdax.model.Account;
import org.quuux.gdax.model.AccountActivity;
import org.quuux.gdax.model.Order;
import org.quuux.gdax.model.Product;
import org.quuux.gdax.model.Tick;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Datastore {

    private static final String TAG = Log.buildTag(Datastore.class);
    private static Datastore instance;

    private List<Product> products = new ArrayList<>();
    private List<Account> accounts = new ArrayList<>();
    private Map<Product, Tick> tickers = new HashMap<>();

    private String selectedProductId = "BTC-USD";

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
                EventBus.getDefault().post(new AccountsLoadError(error));
            }
        });
    }

    public List<Product> getProducts() {
        return products;
    }

    public void loadProducts() {
        API.getInstance().getProducts(new API.ResponseListener<Product[]>() {
            @Override
            public void onSuccess(final Product[] result) {
                products.clear();
                Collections.addAll(products, result);
                EventBus.getDefault().post(new ProductsLoaded());
            }

            @Override
            public void onError(final APIError error) {
                EventBus.getDefault().post(new ProductsLoadError(error));
            }
        });
    }

    public void load() {
        loadAccounts();
        loadProducts();
    }

    public Product getProduct(String id) {
        for (Product p : products)
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

    public static class OrdersCursor extends Cursor<Order> {
        public enum StatusFilter {all, open, closed}

        StatusFilter status = StatusFilter.all;

        public StatusFilter getStatusFilter() {
            return status;
        }

        public void setStatusFilter(StatusFilter status) {
            if (this.status != status) {
                this.status = status;
                reset();
            }
        }

        @Override
        public Class<Order[]> getPageClass() {
            return Order[].class;
        }

        public String[] expandStatuses() {

            String rv[];
            switch (status) {
                case open:
                    rv = new String[] {
                            Order.Status.pending.name(),
                            Order.Status.open.name(),
                            Order.Status.active.name(),
                    };
                    break;

                case closed:
                    rv = new String[] {
                            Order.Status.done.name(),
                    };
                    break;

                default:
                    rv = new String[] {"all"};
                    break;

            }
            return rv;
        }

        @Override
        String getEndpoint() {
            StringBuilder sb = new StringBuilder(API.GDAX_ORDERS_ENDPOINT);

            try {
                String productId = Datastore.getInstance().getSelectedProduct().id;
                sb.append(String.format("?product_id=%s", URLEncoder.encode(productId, "UTF-8")));

                for (String status : expandStatuses())
                    sb.append(String.format("&status=%s", URLEncoder.encode(status, "UTF-8")));

            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "error building orders endpoint: %s", e);
            }
            return sb.toString();
        }

    }
}
