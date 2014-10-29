package com.example.motionauth.Utility;

import android.util.Log;

/**
 * Created by ken on 14/10/29.
 */
public class ConvertArrayAndString {
	private static final String TAG = ConvertArrayAndString.class.getSimpleName();


	public String arrayToString (String[][] input) {
		Log.v(TAG, "--- arrayToString ---");
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

		Log.e(TAG, "result : " + result);

		return result;
	}


	public String[][] stringToArray (String input) {
		Log.i(TAG, "--- stringToArray ---");
		Log.i(TAG, "input : " + input);
		String[] splitDimention = input.split("'");
		String[][] result = new String[3][100];

		for (int i = 0; i < splitDimention.length; i++) {
			result[i] = splitDimention[i].split(",");
		}

		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < result[i].length; j++) {
				Log.e(TAG, "result : " + result[i][j]);
			}
		}

		return result;
	}
}
