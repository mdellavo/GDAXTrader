package org.quuux.gdax;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.quuux.feller.Log;
import org.quuux.gdax.model.FeedMessage;
import org.quuux.gdax.model.Order;
import org.quuux.gdax.model.SubscribeMessage;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;



public class API {

    private static final String TAG = "API";

    private static final String PRODUCT_ID = "BTC-USD";

    private static final String GDAX_FEED_URL = "wss://ws-feed.gdax.com";
    private static final String GDAX_API_URL = "https://api.gdax.com";
    private static final String GDAX_ORDER_BOOK_SNAPSHOT_URL = GDAX_API_URL + "/products/BTC-USD/book?level=3";

    static class OrderBookSnapshot {
        long sequence;
        List<Order> bids;
        List<Order> asks;
    }


    interface FeedListener {
        void onMessage(FeedMessage message);
        void onSnapshot(OrderBookSnapshot snapshot);
    }

    private static API instance;

    private WebSocket feedSocket;
    private Gson gson = new Gson();
    private OkHttpClient client;

    private API() {
        client = getClient();
    }

    private OkHttpClient getClient() {
        return new OkHttpClient.Builder().build();
    }

    public static API getInstance() {
        if (instance == null) {
            instance = new API();
        }

        return instance;
    }

    public boolean isConnected() {
        return feedSocket != null;
    }

    private List<Order> buildBins(JsonObject obj, String key) {
        JsonArray array = obj.getAsJsonArray(key).getAsJsonArray();
        List<Order> rv = new ArrayList<>();

        for (int i=0; i<array.size(); i++) {
            JsonArray row = array.get(i).getAsJsonArray();

            BigDecimal price = row.get(0).getAsBigDecimal();
            BigDecimal size = row.get(1).getAsBigDecimal();
            String order_id = row.get(2).getAsString();
            Order order = new Order(order_id, size, price);
            rv.add(order);
        }

        return rv;
    }

    public OrderBookSnapshot getOrderBookSnapshot() {
        final Request req = new Request.Builder().url(GDAX_ORDER_BOOK_SNAPSHOT_URL).build();
        Response response = null;
        try {
            response = client.newCall(req).execute();
            JsonParser parser = new JsonParser();
            JsonObject obj = parser.parse(response.body().charStream()).getAsJsonObject();

            OrderBookSnapshot snapshot = new OrderBookSnapshot();
            snapshot.sequence = obj.get("sequence").getAsLong();
            snapshot.asks = buildBins(obj, "asks");
            snapshot.bids = buildBins(obj, "bids");

        } catch (IOException e) {
            Log.e(TAG, "error populating order book: %s", e);
        }
        return null;
    }

    public void qdisconnect() {
        if (isConnected()) {
            feedSocket.close(1000, null);
        }
    }

    public void connectToFeed(final FeedListener listener) {
        if (isConnected()) {
            return;
        }

        Log.d(TAG, "connecting to %s...", GDAX_FEED_URL);

        final Boolean[] snapshotLoaded = new Boolean[1];
        snapshotLoaded[0] = false;
        final List<FeedMessage> queue = new ArrayList<>();

        final Request req = new Request.Builder().url(GDAX_FEED_URL).build();
        feedSocket  = client.newWebSocket(req, new WebSocketListener() {
            @Override
            public void onOpen(final WebSocket webSocket, final Response response) {
                super.onOpen(webSocket, response);
                Log.d(TAG, "connected!");

                final SubscribeMessage message = new SubscribeMessage(PRODUCT_ID);
                webSocket.send(gson.toJson(message));

                OrderBookSnapshot snapshot = getOrderBookSnapshot();
                if (snapshot != null)
                    listener.onSnapshot(snapshot);

                for (FeedMessage queued : queue) {
                    listener.onMessage(queued);
                }
                queue.clear();

                snapshotLoaded[0] = true;
            }

            @Override
            public void onMessage(final WebSocket webSocket, final String text) {
                //Log.d(TAG, "message: %s", text);
                final FeedMessage message = gson.fromJson(text, FeedMessage.class);

                if (snapshotLoaded[0])
                    listener.onMessage(message);
                else
                    queue.add(message);
            }

            @Override
            public void onClosing(final WebSocket webSocket, final int code, final String reason) {
                super.onClosing(webSocket, code, reason);
                Log.d(TAG, "closing...");
            }

            @Override
            public void onClosed(final WebSocket webSocket, final int code, final String reason) {
                super.onClosed(webSocket, code, reason);
                Log.d(TAG, "closed");
            }

            @Override
            public void onFailure(final WebSocket webSocket, final Throwable t, final Response response) {
                super.onFailure(webSocket, t, response);
                Log.d(TAG, "websocket error: %s", t);
            }
        });
    }

}
