package org.quuux.gdax;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.quuux.feller.Log;

import java.util.Date;


public class SignInActivity  extends AppCompatActivity {

    private static final String TAG = Log.buildTag(SignInActivity.class);
    private static final String SCHEME = "gdax-quuux";

    WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        webView = (WebView) findViewById(R.id.webview);

        WebViewClient client = new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                return checkComplete(Uri.parse(url));
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, final WebResourceRequest request) {
                return checkComplete(request.getUrl());
            }
        };
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(client);

        webView.loadUrl(API.getInstance().getOAuthUrl());
    }

    private boolean checkComplete(final Uri url) {
        Log.d(TAG, "url: %s", url);
        if (url.getScheme().equals(SCHEME)) {
            String code = url.getQueryParameter("code");
            getToken(code);
            return true;
        }

        return false;
    }

    private void getToken(String code) {
        Log.d(TAG, "code: %s", code);

        API.getInstance().getToken(code, new API.TokenListener() {

            @Override
            public void onError(final Throwable t) {

            }

            @Override
            public void onToken(final API.TokenResponse token) {
                Log.d(TAG, "token: %s", token.access_token);
                saveToken(token.access_token, token.refresh_token, token.getExpires());
            }
        });
    }

    private void saveToken(final String access_token, final String refresh_token, final Date expires) {
        Settings.get(this).setTokens(access_token, refresh_token, expires);
        finish();
    }
}
