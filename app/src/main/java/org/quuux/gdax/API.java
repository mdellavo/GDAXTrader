package org.quuux.gdax;

import android.net.Uri;
import android.util.Base64;

import com.google.gson.Gson;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.greenrobot.eventbus.EventBus;
import org.quuux.feller.Log;
import org.quuux.gdax.events.APIError;
import org.quuux.gdax.model.Account;
import org.quuux.gdax.model.AccountActivity;
import org.quuux.gdax.model.FeedMessage;
import org.quuux.gdax.model.Order;
import org.quuux.gdax.model.OrderBookEntry;
import org.quuux.gdax.model.Product;
import org.quuux.gdax.model.SubscribeMessage;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.Buffer;


@SuppressWarnings("WeakerAccess")
public class API {

    private static final String TAG = Log.buildTag(API.class);

    private static final String USER_AGENT = "GDAX for Android / 1.0";

    private static final String CLIENT_ID = "595cdd1b0acc22a78debbf6f9a2a928e2e25482f74132dbd410f549046a98ba9";
    private static final String CLIENT_SECRET = "cac3c43f727379cd7aeee0958d1c007561b6c52ff55ddd62d2b461db1cb57b74";

    private static final String GDAX_API_URL = "https://api.gdax.com";
    private static final String GDAX_ACCOUNTS_ENDPOINT = "/accounts";
    private static final String GDAX_ACCOUNT_LEDGER_ENDPOINT = "/accounts/%s/ledger";
    private static final String GDAX_ORDERS_ENDPOINT = "/orders";
    private static final String GDAX_PRODUCTS_ENDPOINT = "/products";

    private static final String COINBASE_API_URL = "https://api.coinbase.com";
    private static final String COINBASE_TOKEN_URL = COINBASE_API_URL + "/oauth/token";
    private static final String COINBASE_TOKEN_REVOKE_URL = COINBASE_API_URL + "/oauth/revoke";
    private static final String COINBASE_OATH_URL = "https://www.coinbase.com/oauth/authorize/create_session";

    private static final String REDIRECT_URI = "gdax-quuux://coinbase-oauth";

    private static final String PRODUCT_ID = "BTC-USD";

    private static final String GDAX_FEED_URL = "wss://ws-feed.gdax.com";
    private static final String GDAX_ORDER_BOOK_SNAPSHOT_URL = GDAX_API_URL + "/products/BTC-USD/book?level=3";

    public static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    enum Method {GET, PUT, POST, DELETE}

    static class OrderBookSnapshot {
        long sequence;
        List<OrderBookEntry> bids;
        List<OrderBookEntry> asks;
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

    public interface ResponseListener<T> {
        void onSuccess(T result);
        void onError(APIError error);
    }

    public interface PaginatedResponseListener<T> {
        void onSuccess(T result, String before, String after);
        void onError(APIError error);
    }

    private static API instance;

    private String apiKey;
    private String apiSecretKey;
    private String apiPassphrase;

    private WebSocket feedSocket;
    private Gson gson;
    private OkHttpClient client;

    private API() {
        client = getClient();
        gson = getGson();
    }

    public static API getInstance() {
        if (instance == null) {
            instance = new API();
        }

        return instance;
    }

    private Interceptor interceptor = new Interceptor() {
        @Override
        public Response intercept(final Chain chain) throws IOException {
            String timestamp = String.valueOf(new Date().getTime() / 1000);
            Request originalRequest = chain.request();

            Request.Builder builder = originalRequest.newBuilder()
                    .addHeader("User-Agent", USER_AGENT);

            if (hasAuth()) {
                RequestBody body = originalRequest.body();
                Buffer buffer = new Buffer();
                String bodyString = "";
                if (body != null) {
                    body.writeTo(buffer);
                    bodyString = buffer.readUtf8();
                }
                builder
                        .addHeader("CB-ACCESS-KEY", apiKey)
                        .addHeader("CB-ACCESS-SIGN", sign(apiSecretKey, originalRequest.url().encodedPath(), originalRequest.method(), bodyString, timestamp))
                        .addHeader("CB-ACCESS-TIMESTAMP", timestamp)
                        .addHeader("CB-ACCESS-PASSPHRASE", apiPassphrase);
            }

            Request request = builder.build();

            long t1 = System.nanoTime();
            Response response = chain.proceed(request);
            long t2 = System.nanoTime();

            Log.d(TAG, "%s -> %s (%.1fms)", response.request().url(), response.code(), (t2 - t1) / 1e6d);

            return response;
        }
    };

    private OkHttpClient getClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    private Gson getGson() {
        return new GsonBuilder().create();
    }

    public void setApiKey(String apiKey, String secretKey, String passphase) {
        this.apiKey = apiKey;
        this.apiSecretKey = secretKey;
        this.apiPassphrase = passphase;
    }

    public boolean hasAuth() {
        return this.apiKey != null && this.apiSecretKey != null && this.apiPassphrase != null;
    }

    public String sign(String secret, String requestPath, String method, String body, String timestamp) {
        String rv = null;
        try {
            String prehash = timestamp + method.toUpperCase() + requestPath + (body != null ? body : "");
            byte[] hmacKey = Base64.decode(secret, 0);
            SecretKeySpec keyspec = new SecretKeySpec(hmacKey, "HmacSHA256");
            Mac sha256 =  Mac.getInstance("HmacSHA256");
            sha256.init(keyspec);
            byte[] hashed = sha256.doFinal(prehash.getBytes());
            rv = Base64.encodeToString(hashed, Base64.NO_WRAP);
        } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException e) {
            Log.d(TAG, "error signing request: %s", e);
        }
        return rv;
    }

   class APICallback<T> implements Callback {
       public final ResponseListener<T> listener;
       public final Class<T> cls;

       public APICallback(final ResponseListener<T> listener, Class<T> cls) {
           this.listener = listener;
           this.cls = cls;
       }

       @Override
        public void onFailure(final Call call, final IOException e) {
           APIError error = new APIError(0, e.getMessage());
           listener.onError(error);
       }

        @Override
        public void onResponse(final Call call, final Response response) throws IOException {
            switch (response.code()) {
                case 200:
                    onSuccess(response);
                    break;

                default:
                    onError(response);
                    break;
            }
        }

       public void onError(final Response response) throws IOException {
           String message;
           ResponseBody body = response.body();
           if (body != null)
               message = body.string();
           else
               message = response.message();
           APIError error = new APIError(response.code(), message);
           listener.onError(error);
           EventBus.getDefault().post(error);
       }

       public T getBody(Response response) throws IOException {
           T result = null;
           ResponseBody body = response.body();
           if (body != null) {
               String bodys = body.string();
               Log.d(TAG, "body: %s", bodys);
               result = gson.fromJson(bodys, cls);
           }
           return result;
       }

       public void onSuccess(Response response) throws IOException {
            listener.onSuccess(getBody(response));
        }
    }

    class PaginatedAPICallback<T> extends APICallback<T> {
        PaginatedResponseListener<T> listener;

        public PaginatedAPICallback(final PaginatedResponseListener<T> listener, final Class<T> cls) {
            super(null, cls);
            this.listener = listener;
        }

        public void onSuccess(Response response) throws IOException {
            String before = response.header("CB-BEFORE");
            String after = response.header("CB-AFTER");
            listener.onSuccess(getBody(response), before, after);
        }
    }

    private String apiUrl(String endpoint) {
        return GDAX_API_URL + endpoint;
    }

    private Request newRequest(Method method, String url, RequestBody body) {
        final Request req = new Request.Builder()
                .method(method.toString(), body)
                .url(url)
                .build();
        return req;
    }

    private <T> void makeRequest(Request request, APICallback<T> callback) {
        client.newCall(request).enqueue(callback);
    }

    public <T> void apiCall(Method method, String endpoint, RequestBody body, ResponseListener<T> listener, Class<T> cls) {
        makeRequest(newRequest(method, apiUrl(endpoint), body), new APICallback<>(listener, cls));
    }

    public <T> void apiCall(Method method, String endpoint, ResponseListener<T> listener, Class<T> cls) {
        makeRequest(newRequest(method, apiUrl(endpoint), null), new APICallback<>(listener, cls));
    }

    public <T> void loadPage(String endpoint, PaginatedResponseListener<T> listener, Class<T> cls) {
        makeRequest(newRequest(Method.GET, apiUrl(endpoint), null), new PaginatedAPICallback<>(listener, cls));
    }

    public void getAccounts(ResponseListener<Account[]> listener) {
        apiCall(Method.GET, GDAX_ACCOUNTS_ENDPOINT, listener, Account[].class);
    }

    public String accountLedgerEndpoint(Account account) {
        return String.format(GDAX_ACCOUNT_LEDGER_ENDPOINT, account.id);
    }

    public void getAccountHistory(Account account, PaginatedResponseListener<AccountActivity[]> listener) {
        loadPage(accountLedgerEndpoint(account), listener, AccountActivity[].class);
    }

    public void placeOrder(final Order order, ResponseListener<Order> listener) {
        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, gson.toJson(order));
        apiCall(Method.POST, GDAX_ORDERS_ENDPOINT, body, listener, Order.class);
    }

    public void getProducts(ResponseListener<Product[]> listener) {
        apiCall(Method.GET, GDAX_PRODUCTS_ENDPOINT, listener, Product[].class);
    }

    // Oauth

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

    // Real time feed

    public boolean isConnected() {
        return feedSocket != null;
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

    private List<OrderBookEntry> buildBins(JsonObject obj, String key) {
        JsonArray array = obj.getAsJsonArray(key).getAsJsonArray();
        List<OrderBookEntry> rv = new ArrayList<>();

        for (int i=0; i<array.size(); i++) {
            JsonArray row = array.get(i).getAsJsonArray();

            BigDecimal price = row.get(0).getAsBigDecimal();
            BigDecimal size = row.get(1).getAsBigDecimal();
            String order_id = row.get(2).getAsString();
            OrderBookEntry order = new OrderBookEntry(order_id, price, size);
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

}
