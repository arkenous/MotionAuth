package net.trileg.motionauth.Utility;

import static android.util.Log.INFO;
import static net.trileg.motionauth.Utility.LogUtil.log;

/**
 * Join String type array to String, or separate String to String type array.
 *
 * @author Kensuke Kosaka
 */
class ConvertArrayAndString {

  /**
   * Join String type 2-array data to String by special character.
   *
   * @param input String type 2-array data.
   * @return Joined String data.
   */
  String arrayToString(String[][] input) {
    log(INFO);
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
  String[][] stringToArray(String input) {
    log(INFO);
    String[] splitDimension = input.split("'");
    String[] dimenX = splitDimension[0].split(",");
    String[] dimenY = splitDimension[1].split(",");
    String[] dimenZ = splitDimension[2].split(",");
    String[][] result = new String[3][dimenX.length];
    result[0] = dimenX;
    result[1] = dimenY;
    result[2] = dimenZ;

    return result;
  }
}
