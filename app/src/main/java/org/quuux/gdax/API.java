package org.quuux.gdax;

import android.net.Uri;

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
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;



@SuppressWarnings("WeakerAccess")
public class API {

    private static final String TAG = "API";

    private static final String CLIENT_ID = "595cdd1b0acc22a78debbf6f9a2a928e2e25482f74132dbd410f549046a98ba9";
    private static final String CLIENT_SECRET = "cac3c43f727379cd7aeee0958d1c007561b6c52ff55ddd62d2b461db1cb57b74";

    private static final String COINBASE_OATH_URL = "https://www.coinbase.com/oauth/authorize";
    private static final String REDIRECT_URI = "gdax-quuux://coinbase-oauth";

    private static final String COINBASE_API_URL = "https://api.coinbase.com";
    private static final String COINBASE_TOKEN_URL = COINBASE_API_URL + "/oauth/token";
    private static final String COINBASE_TOKEN_REVOKE_URL = COINBASE_API_URL + "/oauth/revoke";

    private static final String PRODUCT_ID = "BTC-USD";

    private static final String GDAX_FEED_URL = "wss://ws-feed.gdax.com";
    private static final String GDAX_API_URL = "https://api.gdax.com";
    private static final String GDAX_ORDER_BOOK_SNAPSHOT_URL = GDAX_API_URL + "/products/BTC-USD/book?level=3";

    static class OrderBookSnapshot {
        long sequence;
        List<Order> bids;
        List<Order> asks;
    }

    static class TokenResponse {
        String access_token;
        String token_type;
        int expires_in;
        String refresh_token;
        String scope;
        Date created = new Date();
        public Date getExpires() {
            return new Date(created.getTime() + (expires_in * 1000));
        }
    }

    interface FeedListener {
        void onMessage(FeedMessage message);
        void onSnapshot(OrderBookSnapshot snapshot);
        void onError(final Throwable t, final Response response);
        void onClosed(final int code, final String reason);
    }

    interface TokenListener {
        void onError(Throwable t);
        void onToken(TokenResponse token);
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

    public String getOAuthUrl() {
        return Uri.parse(COINBASE_OATH_URL).buildUpon()
                .appendQueryParameter("client_id", CLIENT_ID)
                .appendQueryParameter("redirect_uri", REDIRECT_URI)
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("scope", "")
                .build().toString();
    }

    private void doToken(final RequestBody body, final TokenListener listener) {
        final Request req = new Request.Builder().url(COINBASE_TOKEN_URL).post(body).build();
        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                listener.onError(e);
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                TokenResponse token = gson.fromJson(response.body().charStream(), TokenResponse.class);
                listener.onToken(token);
            }
        });
    }

    public void getToken(String code, TokenListener listener) {
        RequestBody body = new FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("client_id", CLIENT_ID)
                .add("client_secret", CLIENT_SECRET)
                .add("redirect_uri", REDIRECT_URI)
                .build();
        doToken(body, listener);
    }

    public void refreshToken(String token, TokenListener listener) {
        RequestBody body = new FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("refresh_token", token)
                .add("client_id", CLIENT_ID)
                .add("client_secret", CLIENT_SECRET)
                .add("redirect_uri", REDIRECT_URI)
                .build();
        doToken(body, listener);
    }

    public void revokeToken(String token) {
        RequestBody body = new FormBody.Builder()
                .add("token", token)
                .build();
        final Request req = new Request.Builder().url(COINBASE_TOKEN_REVOKE_URL).post(body).build();
        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
            }
        });
    }

    private List<Order> buildBins(JsonObject obj, String key) {
        JsonArray array = obj.getAsJsonArray(key).getAsJsonArray();
        List<Order> rv = new ArrayList<>();

        for (int i=0; i<array.size(); i++) {
            JsonArray row = array.get(i).getAsJsonArray();

            BigDecimal price = row.get(0).getAsBigDecimal();
            BigDecimal size = row.get(1).getAsBigDecimal();
            String order_id = row.get(2).getAsString();
            Order order = new Order(order_id, price, size);
            rv.add(order);
        }

        return rv;
    }

    public OrderBookSnapshot getOrderBookSnapshot() {
        final Request req = new Request.Builder().url(GDAX_ORDER_BOOK_SNAPSHOT_URL).build();
        OrderBookSnapshot rv = null;
        try {
            Log.d(TAG, "fetching %s...", GDAX_ORDER_BOOK_SNAPSHOT_URL);
            Response response = client.newCall(req).execute();
            JsonParser parser = new JsonParser();
            JsonObject obj = parser.parse(response.body().charStream()).getAsJsonObject();

            rv = new OrderBookSnapshot();
            rv.sequence = obj.get("sequence").getAsLong();
            rv.asks = buildBins(obj, "asks");
            rv.bids = buildBins(obj, "bids");
        } catch (IOException e) {
            Log.e(TAG, "error populating order book: %s", e);
        }
        return rv;
    }

    public void disconnect() {
        if (isConnected()) {
            feedSocket.close(1000, null);
            feedSocket = null;
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
                listener.onClosed(code, reason);
            }

            @Override
            public void onFailure(final WebSocket webSocket, final Throwable t, final Response response) {
                super.onFailure(webSocket, t, response);
                Log.d(TAG, "websocket error: %s", t);
                listener.onError(t, response);
            }
        });
    }

}
