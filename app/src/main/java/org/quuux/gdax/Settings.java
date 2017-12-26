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
        return prefs.getString("access_token", null);
    }

    public String getRefreshToken() {
        return prefs.getString("refresh_token", null);
    }

    public boolean hasAccessToken() {
        return getAccessToken() != null;
    }

    public boolean areTokensValid() {
        String expired = prefs.getString("expired", null);
        if (expired != null) {
            try {
                return df.parse(expired).after(new Date());
            } catch (ParseException e) {
                Log.e(TAG, "error parsing expired (%s): %s", expired, e);
            }
        }

        return false;
    }

    public boolean hasValidTokens() {
        return hasAccessToken() && areTokensValid();
    }

    public void setTokens(String access_token, final String refresh_token, Date expires) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("access_token", access_token);
        editor.putString("refresh_token", refresh_token);
        editor.putString("expires", df.format(expires));
        editor.apply();
    }

    public void clearTokens() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("access_token", null);
        editor.putString("refresh_token", null);
        editor.putString("expires", null);
        editor.apply();
    }
}
