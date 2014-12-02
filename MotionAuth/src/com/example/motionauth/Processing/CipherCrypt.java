package com.example.motionauth.Processing;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import com.example.motionauth.Utility.LogUtil;

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
	private static final int    ENCRYPT_KEY_LENGTH = 128;
	private static final String PREF_KEY           = "Cipher";
	private static final String CIPHER_KEY         = "CipherCrypt";
	private static final String CIPHER_IV          = "CipherIv";

	private final Key key;
	private final IvParameterSpec iv;


	/**
	 * 暗号化・復号に必要なSecret Key，IV（Initialization Vector）の準備を行う
	 *
	 * @param context アプリケーション固有のSharedPreferencesを取得する際に用いるContext
	 */
	public CipherCrypt (Context context) {
		LogUtil.log(Log.INFO);

		Context mContext = context.getApplicationContext();

		// SharedPreferencesを取得する
		SharedPreferences preferences = mContext.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();

		// PreferenceからSecret Keyを取得（値が保存されていなければ，空文字を返す）
		String keyStr = preferences.getString(CIPHER_KEY, "");

		if ("".equals(keyStr)) {
			LogUtil.log(Log.DEBUG, "Couldn't get cipher key from preferences");
			// Preferenceから取得できなかった場合
			// Secret Keyを生成し，保存する

			// Secret Keyを生成
			key = generateKey();

			// 生成したSecret Keyを保存
			String base64Key = Base64.encodeToString(key.getEncoded(), Base64.URL_SAFE | Base64.NO_WRAP);

			editor.putString(CIPHER_KEY, base64Key).apply();
		}
		else {
			LogUtil.log(Log.DEBUG, "Get cipher key from preferences");
			// Preferenceから取得できた場合
			// Secret Keyを復元
			byte[] byteKey = Base64.decode(keyStr, Base64.URL_SAFE | Base64.NO_WRAP);
			key = new SecretKeySpec(byteKey, "AES");
		}


		// PreferenceからIVを取得（値が保存されていなければ，空文字を返す）
		String ivStr = preferences.getString(CIPHER_IV, "");

		if ("".equals(ivStr)) {
			LogUtil.log(Log.DEBUG, "Couldn't get iv from preferences");
			// Preferenceから取得できなかった場合
			// IVを生成し，保存する

			// IVを生成
			byte[] byteIv = generateIv();
			iv = new IvParameterSpec(byteIv);

			// 生成したIVを保存
			String base64Iv = Base64.encodeToString(byteIv, Base64.URL_SAFE | Base64.NO_WRAP);

			editor.putString(CIPHER_IV, base64Iv).apply();
		}
		else {
			LogUtil.log(Log.DEBUG, "Get iv from preferences");
			// Preferenceから取得できた場合
			// IVを復元
			byte[] byteIv = Base64.decode(ivStr, Base64.URL_SAFE | Base64.NO_WRAP);
			iv = new IvParameterSpec(byteIv);
		}
	}


	/**
	 * 暗号化・復号に使用するSecret Keyを生成する
	 *
	 * @return Secret Key
	 */
	private Key generateKey () {
		LogUtil.log(Log.INFO);
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
	 * 暗号化・復号に使用するIVを生成する
	 *
	 * @return byte配列型のIV
	 */
	private byte[] generateIv () {
		LogUtil.log(Log.INFO);
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
		LogUtil.log(Log.INFO);
		if (input == null) {
			LogUtil.log(Log.WARN, "Input data is NULL");
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
	 * 入力された暗号化済みString型二次元配列データを復号したものを返す
	 *
	 * @param input 暗号化されたString型二次元配列データ
	 * @return 復号されたString型二次元配列データ
	 */
	public String[][] decrypt (String[][] input) {
		LogUtil.log(Log.INFO);
		if (input == null) {
			LogUtil.log(Log.WARN, "Input data is NULL");
			return null;
		}

		String[][] decrypted = new String[input.length][input[0].length];

		try {
			// 復号を行う
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, key, iv);

			for (int i = 0; i < input.length; i++) {
				for (int j = 0; j < input[i].length; j++) {
					byte[] result = cipher.doFinal(Base64.decode(input[i][j], Base64.URL_SAFE | Base64.NO_WRAP));
					decrypted[i][j] = new String(result);
					LogUtil.log(Log.VERBOSE, "Decrypted : " + decrypted[i][j]);
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


	/**
	 * 入力された暗号化済みString型一次元配列データを復号したものを返す
	 *
	 * @param input 暗号化されたString型一次元配列データ
	 * @return 復号されたString型一次元配列データ
	 */
	public String[] decrypt (String[] input) {
		LogUtil.log(Log.INFO);
		if (input == null) {
			LogUtil.log(Log.DEBUG, "Input data is NULL");
			return null;
		}

		String[] decrypted = new String[input.length];

		try {
			// 復号を行う
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, key, iv);

			for (int i = 0; i < input.length; i++) {
				byte[] result = cipher.doFinal(Base64.decode(input[i], Base64.URL_SAFE | Base64.NO_WRAP));
				decrypted[i] = new String(result);
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
