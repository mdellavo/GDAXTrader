package org.quuux.gdax.fragments;

import android.support.v4.app.Fragment;


public class BaseGDAXFragment extends Fragment {

    public boolean needsProductSelector() {
        return false;
    }

    public int getTitle() {
        return 0;
    }

}
