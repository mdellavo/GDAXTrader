package org.quuux.gdax.net;

import org.quuux.feller.Log;
import org.quuux.gdax.Datastore;
import org.quuux.gdax.model.Order;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class OrdersCursor extends Cursor<Order> {
    private static final String TAG = Log.buildTag(OrdersCursor.class);

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
                rv = new String[]{
                        Order.Status.pending.name(),
                        Order.Status.open.name(),
                        Order.Status.active.name(),
                };
                break;

            case closed:
                rv = new String[]{
                        Order.Status.done.name(),
                };
                break;

            default:
                rv = new String[]{"all"};
                break;

        }
        return rv;
    }

    @Override
    public String getEndpoint() {
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
