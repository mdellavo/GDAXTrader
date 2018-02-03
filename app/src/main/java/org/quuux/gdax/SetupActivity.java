package org.quuux.gdax;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import org.quuux.feller.Log;
import org.quuux.gdax.net.API;

import java.util.Arrays;

public class SetupActivity extends AppCompatActivity {

    private static final String TAG = Log.buildTag(SetupActivity.class);

    EditText apiKeyField, apiSecretField, apiPassphraseField;
    EditText[] fields;

    ImageButton scanApiKey, scanApiSecret, scanApiPassphrase;
    ImageButton[] buttons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        apiKeyField = findViewById(R.id.api_key);
        apiSecretField = findViewById(R.id.api_secret);
        apiPassphraseField = findViewById(R.id.api_passphrase);
        fields = new EditText[] {apiKeyField, apiSecretField, apiPassphraseField};
        Settings settings = Settings.get(this);
        apiKeyField.setText(settings.getApiKey());
        apiSecretField.setText(settings.getApiSecret());
        apiPassphraseField.setText(settings.getApiPassphrase());

        scanApiKey = findViewById(R.id.scan_api_key);
        scanApiSecret = findViewById(R.id.scan_api_secret);
        scanApiPassphrase = findViewById(R.id.scan_api_passphrase);

        buttons = new ImageButton[] {scanApiKey, scanApiSecret, scanApiPassphrase};

        for (int i=0; i<fields.length; i++) {
            setupQRButton(buttons[i], fields[i]);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            String contents = data.getStringExtra("SCAN_RESULT");
            fields[requestCode].setText(contents);
        }
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

    private void setupQRButton(final ImageButton b, final EditText field) {
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                int requestCode = Arrays.asList(fields).indexOf(field);
                launchQR(requestCode);
            }
        });
    }

    private void launchQR(int requestCode) {
        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
            startActivity(marketIntent);
        }
    }
}
