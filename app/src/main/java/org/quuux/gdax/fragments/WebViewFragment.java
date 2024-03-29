package org.quuux.gdax.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.quuux.gdax.R;

public class WebViewFragment extends BaseGDAXFragment {

    WebView mWebView;

    public WebViewFragment() {}

    public static WebViewFragment newInstance(int title, String url) {
        WebViewFragment frag = new WebViewFragment();
        Bundle args = new Bundle();
        args.putInt("title", title);
        args.putString("url", url);
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
        View v =  inflater.inflate(R.layout.fragment_webview, container, false);
        mWebView = v.findViewById(R.id.webview);

        WebViewClient client = new WebViewClient();
        mWebView.setWebViewClient(client);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        mWebView.loadUrl(getUrl());
        return v;
    }

    private String getUrl() {
        return getArguments().getString("url");
    }


    @Override
    public int getTitle() {
        return getArguments().getInt("title");
    }

}
