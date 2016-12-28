package org.quuux.gdax;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import org.quuux.gdax.model.FeedMessage;
import org.quuux.gdax.model.Order;
import org.quuux.gdax.model.OrderBook;

import java.math.BigDecimal;


public class OrderBookService extends Service {

    private static final String TAG = "OrderBookService";

    private OrderBook orderBook;

    public OrderBookService() {
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

    public void connectToFeed() {
        orderBook = new OrderBook();
        API.getInstance().connectToFeed(feedListener);
    }

    public void shutdown() {
        API.getInstance().disconnect();
    }

    private API.FeedListener feedListener = new API.FeedListener() {
        @Override
        public void onMessage(final FeedMessage message) {
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

        @Override
        public void onSnapshot(final API.OrderBookSnapshot snapshot) {
            for (Order order : snapshot.asks) {
                orderBook.addOrder(order);
                orderBook.incDepth(OrderBook.Side.sell, order.price, order.size);
            }

            for (Order order : snapshot.bids) {
                orderBook.addOrder(order);
                orderBook.incDepth(OrderBook.Side.buy, order.price, order.size);
            }
        }
    };
}
