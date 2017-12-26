package org.quuux.gdax;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import org.quuux.feller.Log;
import org.quuux.gdax.model.FeedMessage;
import org.quuux.gdax.model.Order;
import org.quuux.gdax.model.OrderBook;

import java.math.BigDecimal;

import okhttp3.Response;


public class OrderBookService extends Service {

    private static final String TAG = "OrderBookService";

    private Handler handler;
    private OrderBook orderBook;

    public OrderBookService() {
        handler = new Handler();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    public class LocalBinder extends Binder {
        public OrderBookService getService() {
            return OrderBookService.this;
        }
    }

    public OrderBook getOrderBook() {
        return orderBook;
    }

    public void connectToFeed(final API.FeedListener listener) {
        orderBook = new OrderBook();

        API.getInstance().connectToFeed(new API.FeedListener() {
            @Override
            public void onMessage(final FeedMessage message) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        processFeedMessage(message);
                        listener.onMessage(message);
                    }
                });
            }

            @Override
            public void onSnapshot(final API.OrderBookSnapshot snapshot) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        processSnapshot(snapshot);
                        listener.onSnapshot(snapshot);
                    }
                });
            }

            @Override
            public void onError(final Throwable t, final Response response) {
                listener.onError(t, response);
            }

            @Override
            public void onClosed(final int code, final String reason) {
                listener.onClosed(code, reason);
            }
        });
    }

    public void shutdown() {
        API.getInstance().disconnect();
    }

    private void processFeedMessage(FeedMessage message) {
        if (message.sequence < orderBook.getSequence()) {
            Log.d(TAG, "dropping message sequence=%s", message.sequence);
            return;
        }

        orderBook.setSequence(message.sequence);

        if (message.type.equals("received") && message.price != null) {
            Order order = new Order(message.order_id, new BigDecimal(message.price), new BigDecimal(message.size));
            orderBook.addOrder(order);
        } else if (message.type.equals("done")) {
            orderBook.removeOrder(message.order_id);
        } else if (message.type.equals("open")) {
            orderBook.incDepth(OrderBook.Side.valueOf(message.side), new BigDecimal(message.price), new BigDecimal(message.remaining_size));
        } else if (message.type.equals("match")) {
            orderBook.decDepth(OrderBook.Side.valueOf(message.side), new BigDecimal(message.price), new BigDecimal(message.size));
        }
    }

    private void processSnapshot(final API.OrderBookSnapshot snapshot) {
        Log.d(TAG, "processing snapshot...");

        for (Order order : snapshot.bids) {
            orderBook.addOrder(order);
            orderBook.incDepth(OrderBook.Side.buy, order.price, order.size);
        }

        for (Order order : snapshot.asks) {
            orderBook.addOrder(order);
            orderBook.incDepth(OrderBook.Side.sell, order.price, order.size);
        }

        orderBook.setSequence(snapshot.sequence);

        Log.d(TAG, "loaded snapshot %s buys, %s sells", orderBook.numPrices(OrderBook.Side.buy), orderBook.numPrices(OrderBook.Side.sell));
    }
}
