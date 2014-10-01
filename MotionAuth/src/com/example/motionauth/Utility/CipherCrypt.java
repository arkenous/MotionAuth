package com.example.motionauth.Utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;


/**
 * 暗号化に関係する処理
 *
 * @author Kensuke Kousaka
 */
public class CipherCrypt {
    private static final String TAG                = CipherCrypt.class.getSimpleName();
    private static final int    ENCRYPT_KEY_LENGTH = 128;
    private static final String PREF_KEY           = "MotionAuth";
    private static final String CIPHER_KEY         = "CipherCrypt";
    private static final String CIPHER_IV          = "CipherIv";

    private final Key             key;
    private final IvParameterSpec iv;


    public CipherCrypt (Context context) {
        Log.v(TAG, "--- CipherCrypt ---");

        Context mContext = context.getApplicationContext();

        // SharedPreferencesを取得する
        SharedPreferences preferences = mContext.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Preferenceから暗号化キーを取得（値が保存されていなければ，空文字を返す）
        String keyStr = preferences.getString(CIPHER_KEY, "");

        if ("".equals(keyStr)) {
            Log.i(TAG, "Coundn't get cipher key from preferences");
            // Preferenceから取得できなかった場合
            // 暗号化キーを生成し，保存する

            // 暗号化キーを生成
            key = generateKey();

            // 生成したキーを保存
            String base64Key = Base64.encodeToString(key.getEncoded(), Base64.URL_SAFE | Base64.NO_WRAP);

            editor.putString(CIPHER_KEY, base64Key).apply();
        }
        else {
            Log.i(TAG, "Get cipher key from preferences");
            // Preferenceから取得できた場合
            // 暗号化キーを復元
            byte[] byteKey = Base64.decode(keyStr, Base64.URL_SAFE | Base64.NO_WRAP);
            key = new SecretKeySpec(byteKey, "AES");
        }


        // PreferenceからIVを取得（値が保存されていなければ，空文字を返す）
        String ivStr = preferences.getString(CIPHER_IV, "");

        if ("".equals(ivStr)) {
            Log.i(TAG, "Coundn't get iv from preferences");
            // Preferenceから取得できなかった場合
            // Ivを生成し，保存する

            // Ivを生成
            byte[] byteIv = generateIv();
            iv = new IvParameterSpec(byteIv);

            // 生成したIvを保存
            String base64Iv = Base64.encodeToString(byteIv, Base64.URL_SAFE | Base64.NO_WRAP);

            editor.putString(CIPHER_IV, base64Iv).apply();
        }
        else {
            Log.i(TAG, "Get iv from preferences");
            // Preferenceから取得できた場合
            // Ivを復元
            byte[] byteIv = Base64.decode(ivStr, Base64.URL_SAFE | Base64.NO_WRAP);
            iv = new IvParameterSpec(byteIv);
        }
    }


    /**
     * 暗号化・復号化に使用する秘密鍵を生成する
     *
     * @return 秘密鍵
     */
    private Key generateKey () {
        Log.v(TAG, "--- generateKey ---");
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

            generator.init(ENCRYPT_KEY_LENGTH, random);

            return generator.generateKey();
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 暗号化・復号化に使用するIvを生成する
     *
     * @return byte配列型のIv
     */
    private byte[] generateIv () {
        Log.v(TAG, "--- generateIv ---");
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            return cipher.getIV();
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
        catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * 入力されたString型二次元配列データを暗号化したものを返す
     *
     * @param input String型二次元配列データ
     * @return 暗号化されたString型二次元配列データ
     */
    public String[][] encrypt (String[][] input) {
        Log.v(TAG, "--- encrypt ---");
        if (input == null) {
            Log.w(TAG, "Input data is NULL");
            return null;
        }

        String[][] encrypted = new String[input.length][input[0].length];

        try {
            // 暗号化アルゴリズムにAESを，動作モードにCBCを，パディングにPKCS5を用いる
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);

            for (int i = 0; i < input.length; i++) {
                for (int j = 0; j < input[i].length; j++) {
                    byte[] result = cipher.doFinal(input[i][j].getBytes());
                    encrypted[i][j] = Base64.encodeToString(result, Base64.URL_SAFE | Base64.NO_WRAP);
                }
            }

            return encrypted;
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
        catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        catch (BadPaddingException e) {
            throw new RuntimeException(e);
        }
        catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
        catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 入力された暗号化済みString型二次元配列データを復号化したものを返す
     *
     * @param input 暗号化されたString型二次元配列データ
     * @return 復号化されたString型二次元配列データ
     */
    public String[][] decrypt (String[][] input) {
        Log.v(TAG, "--- decrypt ---");
        if (input == null) {
            Log.w(TAG, "Input data is NULL");
            return null;
        }

        String[][] decrypted = new String[input.length][input[0].length];

        try {
            // 復号化を行う
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, iv);

            for (int i = 0; i < input.length; i++) {
                for (int j = 0; j < input[i].length; j++) {
                    byte[] result = cipher.doFinal(Base64.decode(input[i][j], Base64.URL_SAFE | Base64.NO_WRAP));
                    decrypted[i][j] = new String(result);
                }
            }

            return decrypted;
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
        catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        catch (BadPaddingException e) {
            throw new RuntimeException(e);
        }
        catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
        catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }
}
