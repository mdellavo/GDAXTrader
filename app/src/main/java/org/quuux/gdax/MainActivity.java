package org.quuux.gdax;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import org.quuux.gdax.model.FeedMessage;
import org.quuux.gdax.model.OrderBook;
import org.quuux.gdax.view.DepthView;
import org.quuux.gdax.view.OrderBookAdapter;

public class MainActivity extends AppCompatActivity {

    private OrderBookService service;
    private boolean bound;

    private ListView orders;
    private DepthView depth;
    private OrderBookAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        orders = (ListView) findViewById(R.id.orders);
        depth = (DepthView)findViewById(R.id.depth);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final Intent intent = new Intent(this, OrderBookService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bound) {
            service.shutdown();
            this.service = null;
            unbindService(connection);
        }
    }

    private void connect(OrderBookService service) {
        this.service = service;
        this.service.connectToFeed(listener);
        OrderBook orderBook = MainActivity.this.service.getOrderBook();
        adapter = new OrderBookAdapter(MainActivity.this, orderBook);
        depth.setOrderBook(orderBook);
    }

    private void updateOrderBook() {
        adapter.update();
        if (orders.getAdapter() == null && adapter.getCount() > 0) {
            orders.setAdapter(adapter);
        }
        orders.setSelection(adapter.getMarkerPosition() - 5);
        depth.update();
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            OrderBookService.LocalBinder binder = (OrderBookService.LocalBinder) service;
            connect(binder.getService());
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    final API.FeedListener listener = new API.FeedListener() {
        @Override
        public void onMessage(final FeedMessage message) {
            updateOrderBook();
        }

        @Override
        public void onSnapshot(final API.OrderBookSnapshot snapshot) {
            updateOrderBook();
        }
    };
}
