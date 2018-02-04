package org.quuux.gdax.fragments;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import org.quuux.gdax.R;
import org.quuux.gdax.Settings;
import org.quuux.gdax.net.API;

import java.util.Arrays;

public class SetupFragment extends BaseGDAXFragment {

    EditText apiKeyField, apiSecretField, apiPassphraseField;
    EditText[] fields;

    ImageButton scanApiKey, scanApiSecret, scanApiPassphrase;
    ImageButton[] buttons;

    Button saveButton;

    public SetupFragment() {
    }

    public static SetupFragment newInstance() {
        SetupFragment fragment = new SetupFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_setup, container, false);

        apiKeyField = v.findViewById(R.id.api_key);
        apiSecretField = v.findViewById(R.id.api_secret);
        apiPassphraseField = v.findViewById(R.id.api_passphrase);
        fields = new EditText[] {apiKeyField, apiSecretField, apiPassphraseField};
        Settings settings = Settings.get(getContext());
        apiKeyField.setText(settings.getApiKey());
        apiSecretField.setText(settings.getApiSecret());
        apiPassphraseField.setText(settings.getApiPassphrase());

        scanApiKey = v.findViewById(R.id.scan_api_key);
        scanApiSecret = v.findViewById(R.id.scan_api_secret);
        scanApiPassphrase = v.findViewById(R.id.scan_api_passphrase);

        buttons = new ImageButton[] {scanApiKey, scanApiSecret, scanApiPassphrase};

        for (int i=0; i<fields.length; i++) {
            setupQRButton(buttons[i], fields[i]);
        }

        saveButton = v.findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onSave();
            }
        });

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
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

    public void onSave() {
        final String apiKey = validate(apiKeyField);
        final String apiSecret = validate(apiSecretField);
        final String apiPassphrase = validate(apiPassphraseField);

        if (apiKey != null && apiSecret != null && apiPassphrase != null) {
            saveKey(apiKey, apiSecret, apiPassphrase);
            API.getInstance().setApiKey(apiKey, apiSecret, apiPassphrase);
        }
    }

    private void saveKey(final String apiKey, final String apiSecret, final String apiPassphrase) {
        Settings.get(getContext()).setApiKey(apiKey, apiSecret, apiPassphrase);
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
