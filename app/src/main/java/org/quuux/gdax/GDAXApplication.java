package org.quuux.gdax;

import android.app.Application;

public class GDAXApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Settings settings = Settings.get(this);
        if (settings.hasApiKey()) {
            API.getInstance().setApiKey(settings.getApiKey(), settings.getApiSecret(), settings.getApiPassphrase());
            Datastore.getInstance().load();
        }
    }
}
