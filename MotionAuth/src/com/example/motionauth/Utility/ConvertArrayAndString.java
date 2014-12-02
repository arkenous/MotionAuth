package com.example.motionauth.Utility;

import android.util.Log;

/**
 * 文字列型配列データの要素を連結したり，分離して配列に戻すクラス
 *
 * @author Kensuke Kousaka
 */
public class ConvertArrayAndString {

	/**
	 * 受け取った配列データを，特定の文字を用いて連結する
	 *
	 * @param input 処理するString型二次元配列データ
	 * @return 連結したString型データ
	 */
	public String arrayToString (String[][] input) {
		LogUtil.log(Log.INFO);
		String join = "", result = "";

		// aaa   bbb   ccc
		for (String[] i : input) {
			for (String j : i) {
				join += j + ",";
			}
			join += "'";
		}
		// a,a,a,'b,b,b,'c,c,c,'

		String[] splited = join.split("'");
		// a,a,a   b,b,b   c,c,c

		for (String i : splited) {
			if (i.endsWith(",")) {
				int last = i.lastIndexOf(",");
				i = i.substring(0, last);
				// a,a,a
				result += i + "'";
			}
		}

		// a,a,a'b,b,b'c,c,c'

		if (result.endsWith("'")) {
			int last = result.lastIndexOf("'");
			result = result.substring(0, last);
		}
		// a,a,a'b,b,b'c,c,c

		return result;
	}


	/**
	 * 受け取ったString型データを特定文字列で分割して配列データにする
	 *
	 * @param input 処理するString型データ
	 * @return 分割したString型二次元配列データ
	 */
	public String[][] stringToArray (String input) {
		LogUtil.log(Log.INFO);
		String[] splitDimention = input.split("'");
		String[][] result = new String[3][100];

		for (int i = 0; i < splitDimention.length; i++) result[i] = splitDimention[i].split(",");

		return result;
	}
}
