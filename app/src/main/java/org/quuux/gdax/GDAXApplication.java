package org.quuux.gdax;

import android.app.Application;

import org.joda.money.CurrencyUnit;

import java.util.ArrayList;

public class GDAXApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CurrencyUnit.registerCurrency("BTC", -1, 8, new ArrayList<String>());
    }
}
