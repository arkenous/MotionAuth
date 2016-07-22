package net.trileg.motionauth.Processing;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;

import static android.content.Context.MODE_PRIVATE;
import static android.util.Base64.*;
import static android.util.Log.*;
import static javax.crypto.Cipher.*;
import static net.trileg.motionauth.Utility.Enum.NUM_AXIS;
import static net.trileg.motionauth.Utility.LogUtil.log;


/**
 * Encrypt or Decrypt data.
 *
 * @author Kensuke Kosaka
 */
public class CipherCrypt {
  private static final int ENCRYPT_KEY_LENGTH = 128;
  private static final String PREF_KEY = "Cipher";
  private static final String CIPHER_KEY = "CipherCrypt";
  private static final String CIPHER_IV = "CipherIv";

  private final Key key;
  private final IvParameterSpec iv;


  /**
   * Prepare Secret key and IV (Initialization Vector) using Encrypt and Decrypt data.
   *
   * @param context Context use to get Application unique SharedPreferences.
   */
  public CipherCrypt(Context context) {
    log(INFO);

    Context mContext = context.getApplicationContext();

    // SharedPreferencesを取得する
    SharedPreferences preferences = mContext.getSharedPreferences(PREF_KEY, MODE_PRIVATE);
    SharedPreferences.Editor editor = preferences.edit();

    // PreferenceからSecret Keyを取得（値が保存されていなければ，空文字を返す）
    String keyStr = preferences.getString(CIPHER_KEY, "");

    if ("".equals(keyStr)) {
      log(DEBUG, "Couldn't get cipher key from preferences");
      // Preferenceから取得できなかった場合
      // Secret Keyを生成し，保存する

      // Secret Keyを生成
      key = generateKey();

      // 生成したSecret Keyを保存
      String base64Key = Base64.encodeToString(key.getEncoded(), URL_SAFE | NO_WRAP);

      editor.putString(CIPHER_KEY, base64Key).apply();
    } else {
      log(DEBUG, "Get cipher key from preferences");
      // Preferenceから取得できた場合
      // Secret Keyを復元
      byte[] byteKey = Base64.decode(keyStr, URL_SAFE | NO_WRAP);
      key = new SecretKeySpec(byteKey, "AES");
    }


    // PreferenceからIVを取得（値が保存されていなければ，空文字を返す）
    String ivStr = preferences.getString(CIPHER_IV, "");

    if ("".equals(ivStr)) {
      log(DEBUG, "Couldn't get iv from preferences");
      // Preferenceから取得できなかった場合
      // IVを生成し，保存する

      // IVを生成
      byte[] byteIv = generateIv();
      iv = new IvParameterSpec(byteIv);

      // 生成したIVを保存
      String base64Iv = Base64.encodeToString(byteIv, URL_SAFE | NO_WRAP);

      editor.putString(CIPHER_IV, base64Iv).apply();
    } else {
      log(DEBUG, "Get iv from preferences");
      // Preferenceから取得できた場合
      // IVを復元
      byte[] byteIv = Base64.decode(ivStr, URL_SAFE | NO_WRAP);
      iv = new IvParameterSpec(byteIv);
    }
  }


  /**
   * Generate Secret key using Encrypt and Decrypt.
   *
   * @return Secret Key
   */
  private Key generateKey() {
    log(INFO);

    try {
      KeyGenerator generator = KeyGenerator.getInstance("AES");
      SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

      generator.init(ENCRYPT_KEY_LENGTH, random);

      return generator.generateKey();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }


  /**
   * Generate IV (Initialization Vector) using Encrypt and Decrypt.
   *
   * @return byte-array IV.
   */
  private byte[] generateIv() {
    log(INFO);

    try {
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(ENCRYPT_MODE, key);

      return cipher.getIV();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    } catch (NoSuchPaddingException e) {
      throw new RuntimeException(e);
    } catch (InvalidKeyException e) {
      throw new RuntimeException(e);
    }
  }


  /**
   * Encrypt String type 2-array data.
   *
   * @param input String type 2-array data.
   * @return Encrypted String type 2-array data.
   */
  public String[][] encrypt(String[][] input) {
    log(INFO);

    if (input == null) {
      log(WARN, "Input data is NULL");
      return null;
    }

    String[][] encrypted = new String[NUM_AXIS][input[0].length];

    try {
      // 暗号化アルゴリズムにAESを，動作モードにCBCを，パディングにPKCS5を用いる
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(ENCRYPT_MODE, key, iv);

      for (int axis = 0; axis < NUM_AXIS; axis++) {
        for (int item = 0; item < input[axis].length; item++) {
          byte[] result = cipher.doFinal(input[axis][item].getBytes());
          encrypted[axis][item] = Base64.encodeToString(result, URL_SAFE | NO_WRAP);
        }
      }

      return encrypted;
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    } catch (NoSuchPaddingException e) {
      throw new RuntimeException(e);
    } catch (InvalidKeyException e) {
      throw new RuntimeException(e);
    } catch (BadPaddingException e) {
      throw new RuntimeException(e);
    } catch (IllegalBlockSizeException e) {
      throw new RuntimeException(e);
    } catch (InvalidAlgorithmParameterException e) {
      throw new RuntimeException(e);
    }
  }


  /**
   * Decrypt String type 2-array data.
   *
   * @param input String type encrypted 2-array data
   * @return Decrypted String type 2-array data.
   */
  public String[][] decrypt(String[][] input) {
    log(INFO);

    if (input == null) {
      log(WARN, "Input data is NULL");
      return null;
    }

    String[][] decrypted = new String[input.length][input[0].length];

    try {
      // 復号を行う
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(DECRYPT_MODE, key, iv);

      for (int axis = 0; axis < NUM_AXIS; axis++) {
        for (int item = 0; item < input[axis].length; item++) {
          byte[] result = cipher.doFinal(Base64.decode(input[axis][item], URL_SAFE | NO_WRAP));
          decrypted[axis][item] = new String(result);
          log(VERBOSE, "Decrypted : " + decrypted[axis][item]);
        }
      }

      return decrypted;
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    } catch (NoSuchPaddingException e) {
      throw new RuntimeException(e);
    } catch (InvalidKeyException e) {
      throw new RuntimeException(e);
    } catch (BadPaddingException e) {
      throw new RuntimeException(e);
    } catch (IllegalBlockSizeException e) {
      throw new RuntimeException(e);
    } catch (InvalidAlgorithmParameterException e) {
      throw new RuntimeException(e);
    }
  }
}
