package net.trileg.motionauth.Processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

import static android.util.Log.DEBUG;
import static android.util.Log.INFO;
import static net.trileg.motionauth.Utility.Enum.*;
import static net.trileg.motionauth.Utility.LogUtil.log;


/**
 * Correct time gap of each data.
 *
 * @author Kensuke Kosaka
 */
public class CorrectDeviation {

  /**
   * Correct time gap of each data.
   *
   * @param vector Vector data (combined acceleration and gyroscope).
   * @param mode   Criterion for correction.
   * @return Corrected data.
   */
  public double[][][] correctDeviation(double[][][] vector, MODE mode) {
    log(INFO);

    // ずらしたデータを格納する配列
    double[][][] corrected = new double[vector.length][NUM_AXIS][vector[0][0].length];

    //region 試行回ごとの代表値の出ている時間を抽出
    double[][] representativeValue = new double[vector.length][NUM_AXIS];
    int[][] representativeTime = new int[vector.length][NUM_AXIS];

    for (int time = 0; time < vector.length; time++)
      for (int axis = 0; axis < NUM_AXIS; axis++)
        representativeValue[time][axis] = vector[time][axis][0];

    switch (mode) {
      case MAX:
        log(DEBUG, "MAX");
        for (int time = 0; time < vector.length; time++) {
          for (int axis = 0; axis < NUM_AXIS; axis++) {
            for (int item = 0; item < vector[time][axis].length; item++) {
              if (representativeValue[time][axis] < vector[time][axis][item]) {
                representativeValue[time][axis] = vector[time][axis][item];
                representativeTime[time][axis] = item;
              }
            }
          }
        }
        break;
      case MIN:
        log(DEBUG, "MIN");
        for (int time = 0; time < vector.length; time++) {
          for (int axis = 0; axis < NUM_AXIS; axis++) {
            for (int item = 0; item < vector[time][axis].length; item++) {
              if (representativeValue[time][axis] > vector[time][axis][item]) {
                representativeValue[time][axis] = vector[time][axis][item];
                representativeTime[time][axis] = item;
              }
            }
          }
        }
        break;
      case MEDIAN:
        log(DEBUG, "MEDIAN");
        // キーが自動ソートされるTreeMapを用いる．データと順番を紐付けしたものを作成し，中央値の初期の順番の値を取り出す．
        for (int time = 0; time < vector.length; time++) {
          for (int axis = 0; axis < NUM_AXIS; axis++) {
            TreeMap<Double, Integer> treeMap = new TreeMap<>();

            for (int item = 0; item < vector[time][axis].length; item++)
              treeMap.put(vector[time][axis][item], item);

            int loopCount = 0;
            for (Integer initCount : treeMap.values()) {
              if (loopCount == vector[time][axis].length / 2)
                representativeTime[time][axis] = initCount;
              loopCount++;
            }
          }
        }
        break;
    }
    //endregion

    //region 1回目のデータの代表値が出た場所とそれ以降のデータの代表値が出た場所の差を取り，その差だけデータをずらす（ずらしてはみ出たデータは空いたところに入れる）
    int lagData[][] = new int[vector.length - 1][NUM_AXIS];

    // どれだけずれているかを計算する
    for (int axis = 0; axis < NUM_AXIS; axis++) {
      for (int time = 0; time < lagData.length; time++) {
        lagData[time][axis] = representativeTime[0][axis] - representativeTime[time + 1][axis];
        log(DEBUG, "lagData["+time+"]["+axis+"]: " + lagData[time][axis]);
      }
    }

    // 1回目のデータに関しては基準となるデータなのでそのまま入れる
    for (int axis = 0; axis < NUM_AXIS; axis++)
      System.arraycopy(vector[0][axis], 0, corrected[0][axis], 0, vector[0][axis].length);

    // 実際にデータをずらしていく（ずらすのは，1回目を除くデータ）
    for (int time = 1; time < vector.length; time++) {
      for (int axis = 0; axis < NUM_AXIS; axis++) {
        ArrayList<Double> vectorTmp = new ArrayList<>();

        for (int item = 0; item < vector[time][axis].length; item++)
          vectorTmp.add(vector[time][axis][item]);
        Collections.rotate(vectorTmp, lagData[time - 1][axis]);
        for (int item = 0; item < vector[time][axis].length; item++)
          corrected[time][axis][item] = vectorTmp.get(item);
      }
    }
    //endregion

    return corrected;
  }
}
