package net.trileg.motionauth.Utility;

import android.util.Log;

/**
 * Join String type array to String, or separate String to String type array.
 *
 * @author Kensuke Kosaka
 */
public class ConvertArrayAndString {

	/**
	 * Join String type 2-array data to String by special character.
	 *
	 * @param input String type 2-array data.
	 * @return Joined String data.
	 */
	public String arrayToString(String[][] input) {
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

		String[] split = join.split("'");
		// a,a,a   b,b,b   c,c,c

		for (String i : split) {
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
	 * Separate String data to String type 2-array data by special character.
	 *
	 * @param input String data.
	 * @return Separated String type 2-array data.
	 */
	public String[][] stringToArray(String input) {
		LogUtil.log(Log.INFO);
		String[] splitDimension = input.split("'");
		String[][] result = new String[3][100];

		for (int i = 0; i < splitDimension.length; i++) result[i] = splitDimension[i].split(",");

		return result;
	}
}
