package org.quuux.gdax;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.quuux.feller.Log;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = Log.buildTag(MainActivity.class);
    private DrawerLayout mDrawerLayout;
    private NavigationView mDrawerList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (NavigationView) findViewById(R.id.navigation);
        mDrawerLayout.openDrawer(mDrawerList, false);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        boolean rv;
        switch (item.getItemId()) {
            case R.id.signin:
                launchSignIn();
                rv = true;
                break;

            default:
                rv = super.onOptionsItemSelected(item);
                break;
        }

        return rv;
    }

    private void launchSignIn() {
        startActivity(new Intent(this, SignInActivity.class));
    }

//    private void connect(OrderBookService service) {
//        this.service = service;
//        this.service.connectToFeed(listener);
//        OrderBook orderBook = MainActivity.this.service.getOrderBook();
//        adapter = new OrderBookAdapter(MainActivity.this, orderBook);
//
//        depth.setOrderBook(orderBook);
//    }
//
//    private void updateOrderBook() {
//
//        long t1 = System.currentTimeMillis();
//        adapter.update();
//        long t2 = System.currentTimeMillis();
//
//        Log.d(TAG, "updating adapter took %sms", t2 - t1);
//
//        if (orders.getAdapter() == null && adapter.getCount() > 0) {
//            orders.setAdapter(adapter);
//            orders.setSelection(adapter.getMarkerPosition() - 5);
//        }
//
//        long t3 = System.currentTimeMillis();
//        depth.update();
//        long t4 = System.currentTimeMillis();
//        Log.d(TAG, "updating view took %sms", t4 - t3);
//    }
//
//    private ServiceConnection connection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName className, IBinder service) {
//            OrderBookService.LocalBinder binder = (OrderBookService.LocalBinder) service;
//            connect(binder.getService());
//            bound = true;
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName arg0) {
//            bound = false;
//        }
//    };
//
//    final API.FeedListener listener = new API.FeedListener() {
//        @Override
//        public void onMessage(final FeedMessage message) {
//        }
//
//        @Override
//        public void onSnapshot(final API.OrderBookSnapshot snapshot) {
//            handler.post(updater);
//        }
//
//        @Override
//        public void onError(final Throwable t, final Response response) {
//
//        }
//
//        @Override
//        public void onClosed(final int code, final String reason) {
//
//        }
//    };
//
//    private Runnable updater = new Runnable() {
//        @Override
//        public void run() {
//            updateOrderBook();
//            handler.postDelayed(updater, 5000);
//        }
//    };
}
