package org.quuux.gdax;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import org.quuux.feller.Log;

public class SetupActivity extends AppCompatActivity {

    private static final String TAG = Log.buildTag(SetupActivity.class);

    EditText apiKeyField, apiSecretField, apiPassphraseField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        apiKeyField = findViewById(R.id.api_key);
        apiSecretField = findViewById(R.id.api_secret);
        apiPassphraseField = findViewById(R.id.api_passphrase);

        Settings settings = Settings.get(this);
        apiKeyField.setText(settings.getApiKey());
        apiSecretField.setText(settings.getApiSecret());
        apiPassphraseField.setText(settings.getApiPassphrase());
    }

    private String validate(EditText field) {
        final String value = field.getText().toString().trim();
        final boolean isValid = !TextUtils.isEmpty(value);
        field.setError(!isValid ? getString(R.string.not_empty) : null);
        return isValid ? value : null;
    }

    public void onSave(View view) {
        final String apiKey = validate(apiKeyField);
        final String apiSecret = validate(apiSecretField);
        final String apiPassphrase = validate(apiPassphraseField);

        if (apiKey != null && apiSecret != null && apiPassphrase != null) {
            saveKey(apiKey, apiSecret, apiPassphrase);
            API api = API.getInstance();

            api.setApiKey(apiKey, apiSecret, apiPassphrase);
            finish();
        }
    }

    private void saveKey(final String apiKey, final String apiSecret, final String apiPassphrase) {
        Settings.get(this).setApiKey(apiKey, apiSecret, apiPassphrase);
    }
}
