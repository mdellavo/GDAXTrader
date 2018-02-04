package org.quuux.gdax.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import org.quuux.gdax.R;

public class WebViewFragment extends BaseGDAXFragment {

    WebView mWebView;

    public WebViewFragment() {}

    public static WebViewFragment newInstance(String url) {
        WebViewFragment frag = new WebViewFragment();
        Bundle args = new Bundle();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_webview, container, false);
        mWebView = v.findViewById(R.id.webview);
        mWebView.loadUrl(getUrl());
        return v;
    }

    private String getUrl() {
        return getArguments().getString("url");
    }
}
