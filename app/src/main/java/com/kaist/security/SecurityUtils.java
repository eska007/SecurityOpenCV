package com.kaist.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by user on 2016-08-03.
 */
public class SecurityUtils {
    private final static int JELLY_BEAN_4_2 = 17;
    private final static String HEX = "0123456789ABCDEF";
    static String TAG = "[SM]";
    private Context mContext;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    static byte[] default_text = new byte[]{
            '0', '0', '0', '0', '0', '0', '0', '0',
            '0', '0', '0', '0', '0', '0', '0', '0'};
    static byte[] default_key = new byte[]{'0'};


    //SingleTone 처리
    public SecurityUtils(Context context) {
        mContext = context;
    }

    private String getPrivateKey() {
        SharedPreferences prefs = mContext.getSharedPreferences("SM", Context.MODE_PRIVATE);
        String key = prefs.getString("regist_key", "");
        Log.d(TAG, "Regist private key: " + key);

        return key;
    }

    public static String encrypt(String key, String plainText, int type) throws Exception {
        byte[] rawKey = key.getBytes();
        byte[] rawText = plainText.getBytes();//StandardCharsets.US_ASCII);

        for (int i = 0; rawKey.length > i; i++) {
            Log.d(TAG, "rawKey[" + i +"] = " + rawKey[i]);
        }

        for (int i=0; rawText.length > i; i++) {
            Log.d(TAG, "rawText[" + i +"] = " + rawText[i]);
        }

        //byte[] result = encrypt(getRawKey(rawKey), rawText);
        byte[] result = encrypt(getRawKey(rawKey, type), rawText);
        return toHex(result);
    }

    public static String decrypt(String key, String encrypted) throws Exception {
        byte[] rawKey = getRawKey(key.getBytes());
        byte[] enc = toByte(encrypted);
        byte[] result = decrypt(rawKey, enc);
        return new String(result);
    }

    private static byte[] getRawKey(byte[] seed) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        //SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        SecureRandom sr;
        if (Build.VERSION.SDK_INT >= JELLY_BEAN_4_2) {
            sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
        } else {
            sr = SecureRandom.getInstance("SHA1PRNG");
        }

        sr.setSeed(seed);
        kgen.init(128, sr); // 192 and 256 bits may not be available
        SecretKey skey = kgen.generateKey();
        byte[] raw = skey.getEncoded();
        return raw;
    }

    private static byte[] getRawKey(byte[] seed, int type) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        //SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        SecureRandom sr;
        if (Build.VERSION.SDK_INT >= JELLY_BEAN_4_2) {
            sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
        } else {
            sr = SecureRandom.getInstance("SHA1PRNG");
        }

        sr.setSeed(seed);
        kgen.init(type, sr); // 192 and 256 bits may not be available
        SecretKey skey = kgen.generateKey();
        byte[] raw = skey.getEncoded();
        return raw;
    }

    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);

        return decrypted;
    }

    public static String toHex(String txt) {
        return toHex(txt.getBytes());
    }

    public static String fromHex(String hex) {
        return new String(toByte(hex));
    }

    public static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
        return result;
    }

    public static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }

    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }

}
