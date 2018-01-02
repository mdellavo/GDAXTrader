package org.quuux.gdax;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import org.quuux.feller.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Enumeration;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class GDAXKeyStore {

    private static final String TAG = Log.buildTag(GDAXKeyStore.class);

    private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
    private static final String SECRET_KEY_ALIAS = "secret-key";
    private static final String CIPHER_TRANSFORM = "AES/CBC/PKCS7Padding";
    private static final int IV_SIZE = 128;
    private static final int KEY_SIZE = 128;

    private static final int BASE64_FLAGS = Base64.NO_WRAP | Base64.URL_SAFE;
    private static final String SEP = "/";

    private static GDAXKeyStore instance;
    private final KeyStore keystore;

    protected GDAXKeyStore(KeyStore keystore) {
        this.keystore = keystore;
    }

    public SecretKey getSecretKey() {
        SecretKey rv = null;
        try {
            rv = ((KeyStore.SecretKeyEntry) keystore.getEntry(SECRET_KEY_ALIAS, null)).getSecretKey();
        } catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException  e) {
            Log.e(TAG, "error getting secret key: %s", e);
        }
        return rv;
    }

    public String encrypt(String s) {
        String rv = null;
        try {
            Cipher cipher = getEncryptionCipher(getSecretKey());

            byte[] iv = cipher.getIV();
            byte[] ciphertext = cipher.doFinal(s.getBytes("UTF-8"));

            rv = b64encode(iv) + SEP + b64encode(ciphertext);

        } catch (NoSuchProviderException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException| BadPaddingException | IllegalBlockSizeException e) {
            Log.e(TAG, "error encrypting: %s", e);
        }
        return rv;
    }

    public String decrypt(String s) {
        String[] parts = s.split(SEP);
        if (parts.length != 2)
            return null;

        String rv = null;
        try {
            byte[] iv = b64decode(parts[0]);
            Cipher cipher = getDecryptionCipher(getSecretKey(), iv);

            byte[] ciphertext = b64decode(parts[1]);
            byte[] cleartext = cipher.doFinal(ciphertext);
            rv = new String(cleartext, "UTF-8");
        } catch (NoSuchProviderException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | UnsupportedEncodingException e) {
            Log.e(TAG, "error decrypting: %s", e);
        }
        return rv;
    }

    public KeyStore.SecretKeyEntry ensureSecretKey() {
        KeyStore.SecretKeyEntry entry = null;
        try {
            entry = (KeyStore.SecretKeyEntry) keystore.getEntry(SECRET_KEY_ALIAS, null);
            if (entry == null) {
                entry = generateSecretKey();
            }
        } catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
            Log.e(TAG, "error creating secret key: %s", e);
        }
        return entry;
    }

    public KeyGenerator getKeyGenerator() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyGenerator kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES);
        final KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(SECRET_KEY_ALIAS,KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setKeySize(KEY_SIZE)
                .setRandomizedEncryptionRequired(true)
                .build();
        kg.init(spec);
        return kg;
    }

    public KeyStore.SecretKeyEntry generateSecretKey() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        return new KeyStore.SecretKeyEntry(getKeyGenerator().generateKey());
    }

    public static KeyStore getKeyStore() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore rv = KeyStore.getInstance(KEYSTORE_PROVIDER);
        rv.load(null);
        return rv;
    }

    public String b64encode(byte[] data) {
        return Base64.encodeToString(data, BASE64_FLAGS);
    }

    public byte[] b64decode(String data) {
        return Base64.decode(data, BASE64_FLAGS);
    }

    public Cipher getCipher() throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException {
        return Cipher.getInstance(CIPHER_TRANSFORM);
    }

    private Cipher getEncryptionCipher(SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
        final Cipher cipher = getCipher();
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher;
    }

    public Cipher getDecryptionCipher(SecretKey key, byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchProviderException {
        final Cipher cipher = getCipher();
        IvParameterSpec spec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        return cipher;
    }

    public void dumpKeys() {
        try {
            for(Enumeration<String> aliases = keystore.aliases(); aliases.hasMoreElements(); ) {
                Log.d(TAG, "alias: %s", aliases.nextElement());
            }
        } catch (KeyStoreException e) {
            Log.e(TAG, "error dumping keys", e);
        }
    }

    public static GDAXKeyStore getInstance() {
        if (instance == null) {
            try {
                instance = new GDAXKeyStore(getKeyStore());
                instance.ensureSecretKey();
            } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
                Log.e(TAG, "error creating keystore: %s", e);
            }
        }
        return instance;
    }
}
