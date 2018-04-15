package org.quuux.gdax.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.quuux.feller.Log;
import org.quuux.gdax.R;
import org.quuux.gdax.net.API;

public class SigninFragment extends BaseGDAXFragment {

    private static final String TAG = Log.buildTag(SigninFragment.class);

    WebView mWebView;

    public SigninFragment() {}

    public static SigninFragment newInstance() {
        SigninFragment frag = new SigninFragment();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_signin, container, false);
        mWebView = v.findViewById(R.id.webview);

        WebViewClient client = new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(TAG, "url: %s / title: %s", url, view.getTitle());

                if (url.endsWith(view.getTitle())) {
                    String code = view.getTitle();
                    Log.d(TAG, "code = %s", code);
                }
            }
        };
        mWebView.setWebViewClient(client);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        mWebView.loadUrl(API.getInstance().getOAuthUrl());
        return v;
    }
}
