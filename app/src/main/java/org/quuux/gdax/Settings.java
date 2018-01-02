package org.quuux.gdax;


import android.content.Context;
import android.content.SharedPreferences;

import org.quuux.feller.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Settings {

    private static final String NAME = "gdax";
    private static final String TAG = Log.buildTag(Settings.class);
    SharedPreferences prefs;

    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US);

    Settings(Context context) {
        prefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static Settings get(Context context) {
        return new Settings(context);
    }

    public String getAccessToken() {
        String token = prefs.getString("access_token", null);
        return token != null ? GDAXKeyStore.getInstance().decrypt(token) : null;
    }

    public String getRefreshToken() {
        String token = prefs.getString("refresh_token", null);
        return token != null ? GDAXKeyStore.getInstance().decrypt(token) : null;
    }

    public Date getExpired() {
        Date rv = null;
        String expired = prefs.getString("expired", null);
        if (expired != null) {
            try {
                rv = df.parse(expired);
            } catch (ParseException e) {
                Log.e(TAG, "error parsing expired (%s): %s", expired, e);
            }
        }
        return rv;
    }

    public boolean hasAccessToken() {
        return getAccessToken() != null;
    }

    public boolean areTokensValid() {
        Date expired = getExpired();
        return expired != null && expired.after(new Date());
    }

    public boolean hasValidTokens() {
        return hasAccessToken() && areTokensValid();
    }

    public void setTokens(String access_token, final String refresh_token, Date expires) {
        GDAXKeyStore keystore = GDAXKeyStore.getInstance();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("access_token", keystore.encrypt(access_token));
        editor.putString("refresh_token", keystore.encrypt(refresh_token));
        editor.putString("expires", df.format(expires));
        editor.apply();
    }

    public void clearTokens() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("access_token");
        editor.remove("refresh_token");
        editor.remove("expires");
        editor.apply();
    }

    public String getApiKey() {
        String token = prefs.getString("api_key", null);
        return token != null ? GDAXKeyStore.getInstance().decrypt(token) : null;
    }

    public String getApiSecret() {
        String token = prefs.getString("api_secret", null);
        return token != null ? GDAXKeyStore.getInstance().decrypt(token) : null;
    }

    public String getApiPassphrase() {
        String token = prefs.getString("api_passphrase", null);
        return token != null ? GDAXKeyStore.getInstance().decrypt(token) : null;
    }

    public boolean hasApiKey() {
        return this.prefs.contains("api_key") && this.prefs.contains("api_secret") && this.prefs.contains("api_passphrase");
    }

    public void setApiKey(String key, final String secret, String passphrase) {

        GDAXKeyStore keystore = GDAXKeyStore.getInstance();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("api_key", keystore.encrypt(key));
        editor.putString("api_secret", keystore.encrypt(secret));
        editor.putString("api_passphrase", keystore.encrypt(passphrase));
        editor.apply();
    }

    public void clearApiKey() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("api_key");
        editor.remove("api_secret");
        editor.remove("api_passphrase");
        editor.apply();
    }

}
