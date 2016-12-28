package org.quuux.gdax;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import org.quuux.gdax.view.OrderBookAdapter;

public class MainActivity extends AppCompatActivity {

    private static final long TICK = 1000;
    private OrderBookService service;
    private boolean bound;

    private Handler handler = new Handler();
    private ListView orders;
    private OrderBookAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        orders = (ListView) findViewById(R.id.orders);
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

        handler.removeCallbacks(updater);
    }

    private void connect(OrderBookService service) {
        this.service = service;
        this.service.connectToFeed();
        adapter = new OrderBookAdapter(MainActivity.this, MainActivity.this.service.getOrderBook());
        handler.postDelayed(updater, TICK);

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

    Runnable updater = new Runnable() {
        @Override
        public void run() {
            if (adapter != null) {
                adapter.update();
                orders.smoothScrollToPosition(adapter.getMarkerPosition());
                if (orders.getAdapter() == null && adapter.getCount() > 0) {
                    orders.setAdapter(adapter);
                }
            }
            handler.postDelayed(this, TICK);
        }
    };
}
