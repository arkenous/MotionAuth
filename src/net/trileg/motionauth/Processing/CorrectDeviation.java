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
   * @param linearDistance Double type 3-array linear distance data.
   * @param angle          Double type 3-array angle data.
   * @param mode           Criterion for correction.
   * @param target         Which data to set standard.
   * @return newData Corrected double type 4-array data which include distance and angle data.
   */
  public double[][][][] correctDeviation(double[][][] linearDistance, double[][][] angle, MODE mode, TARGET target) {
    log(INFO);

    // ずらしたデータを格納する配列
    double[][][][] newData = new double[2][NUM_TIME][NUM_AXIS][linearDistance[0][0].length];

    // 試行回ごとの代表値の出ている時間を抽出
    // 変数は，桁揃え，計算後のdistance，angleを利用する

    // 回数・XYZを配列で
    double tmpValue[][] = new double[NUM_TIME][NUM_AXIS];

    // 代表値の出ている時間，回数，XYZ
    int count[][] = new int[NUM_TIME][NUM_AXIS];

    // 変数に3回分XYZそれぞれの1個目の値を放り込む
    switch (target) {
      case LINEAR_DISTANCE:
        log(DEBUG, "LINEAR_DISTANCE");
        for (int time = 0; time < NUM_TIME; time++) {
          for (int axis = 0; axis < NUM_AXIS; axis++) {
            tmpValue[time][axis] = linearDistance[time][axis][0];
          }
        }
        break;
      case ANGLE:
        log(DEBUG, "ANGLE");
        for (int time = 0; time < NUM_TIME; time++) {
          for (int axis = 0; axis < NUM_AXIS; axis++) {
            tmpValue[time][axis] = angle[time][axis][0];
          }
        }
        break;
    }

    // 代表値が出ている場所を取得する
    switch (target) {
      case LINEAR_DISTANCE:
        log(DEBUG, "LINEAR_DISTANCE");
        switch (mode) {
          case MAX:
            log(DEBUG, "MAX");
            for (int time = 0; time < NUM_TIME; time++) {
              for (int axis = 0; axis < NUM_AXIS; axis++) {
                for (int item = 0; item < linearDistance[time][axis].length; item++) {
                  if (tmpValue[time][axis] < linearDistance[time][axis][item]) {
                    tmpValue[time][axis] = linearDistance[time][axis][item];
                    count[time][axis] = item;
                  }
                }
              }
            }
            break;
          case MIN:
            log(DEBUG, "MIN");
            for (int time = 0; time < NUM_TIME; time++) {
              for (int axis = 0; axis < NUM_AXIS; axis++) {
                for (int item = 0; item < linearDistance[time][axis].length; item++) {
                  if (tmpValue[time][axis] > linearDistance[time][axis][item]) {
                    tmpValue[time][axis] = linearDistance[time][axis][item];
                    count[time][axis] = item;
                  }
                }
              }
            }
            break;
          case MEDIAN:
            log(DEBUG, "MEDIAN");
            // キーが自動ソートされるTreeMapを用いる．データと順番を紐付けしたものを作成し，中央値の初期の順番の値を取り出す．
            for (int time = 0; time < NUM_TIME; time++) {
              for (int axis = 0; axis < NUM_AXIS; axis++) {
                TreeMap<Double, Integer> treeMap = new TreeMap<>();

                for (int item = 0; item < linearDistance[time][axis].length; item++) {
                  treeMap.put(linearDistance[time][axis][item], item);
                }

                int loopCount = 0;
                for (Integer initCount : treeMap.values()) {
                  if (loopCount == linearDistance[time][axis].length / 2) {
                    count[time][axis] = initCount;
                  }

                  loopCount++;
                }
              }
            }
            break;
        }
        break;
      case ANGLE:
        log(DEBUG, "ANGLE");
        switch (mode) {
          case MAX:
            log(DEBUG, "MAX");
            for (int time = 0; time < NUM_TIME; time++) {
              for (int axis = 0; axis < NUM_AXIS; axis++) {
                for (int item = 0; item < angle[time][axis].length; item++) {
                  if (tmpValue[time][axis] < angle[time][axis][item]) {
                    tmpValue[time][axis] = angle[time][axis][item];
                    count[time][axis] = item;
                  }
                }
              }
            }
            break;
          case MIN:
            log(DEBUG, "MIN");
            for (int time = 0; time < NUM_TIME; time++) {
              for (int axis = 0; axis < NUM_AXIS; axis++) {
                for (int item = 0; item < angle[time][axis].length; item++) {
                  if (tmpValue[time][axis] > angle[time][axis][item]) {
                    tmpValue[time][axis] = angle[time][axis][item];
                    count[time][axis] = item;
                  }
                }
              }
            }
            break;
          case MEDIAN:
            log(DEBUG, "MEDIAN");
            // キーが自動ソートされるTreeMapを用いる．データと順番を紐付けしたものを作成し，中央値の初期の順番の値を取り出す．
            for (int time = 0; time < NUM_TIME; time++) {
              for (int axis = 0; axis < NUM_AXIS; axis++) {
                TreeMap<Double, Integer> treeMap = new TreeMap<>();

                for (int item = 0; item < angle[time][axis].length; item++) {
                  treeMap.put(angle[time][axis][item], item);
                }

                int loopCount = 0;
                for (Integer initCount : treeMap.values()) {
                  if (loopCount == angle[time][axis].length / 2) {
                    count[time][axis] = initCount;
                  }

                  loopCount++;
                }
              }
            }
            break;
        }
        break;
    }

    // 1回目のデータの代表値が出た場所と，2回目・3回目のデータの代表値が出た場所の差を取る
    // 取ったら，その差だけデータをずらす（ずらしてはみ出たデータは空いたとこに入れる）

    // ずらす移動量を計算
    int lagData[][] = new int[2][NUM_AXIS];

    // どれだけズレているかを計算する
    for (int axis = 0; axis < NUM_AXIS; axis++) {
      lagData[0][axis] = count[0][axis] - count[1][axis];
      log(DEBUG, "lagData[0]" + "[" + axis + "]" + ": " + lagData[0][axis]);

      lagData[1][axis] = count[0][axis] - count[2][axis];
      log(DEBUG, "lagData[1]" + "[" + axis + "]" + ": " + lagData[1][axis]);
    }

    // 1回目のデータに関しては基準となるデータなのでそのまま入れる
    for (int axis = 0; axis < NUM_AXIS; axis++) {
      for (int item = 0; item < linearDistance[0][axis].length; item++) {
        newData[0][0][axis][item] = linearDistance[0][axis][item];
        newData[1][0][axis][item] = angle[0][axis][item];
      }
    }

    // 実際にデータをずらしていく（ずらすのは，1回目を除くデータ）
    for (int time = 1; time < NUM_TIME; time++) {
      for (int axis = 0; axis < NUM_AXIS; axis++) {
        ArrayList<Double> linearDistanceTemp = new ArrayList<>();
        ArrayList<Double> angleTemp = new ArrayList<>();

        for (int item = 0; item < linearDistance[time][axis].length; item++) {
          linearDistanceTemp.add(linearDistance[time][axis][item]);
          angleTemp.add(angle[time][axis][item]);
        }
        Collections.rotate(linearDistanceTemp, lagData[time - 1][axis]);
        Collections.rotate(angleTemp, lagData[time - 1][axis]);
        for (int item = 0; item < linearDistance[time][axis].length; item++) {
          newData[0][time][axis][item] = linearDistanceTemp.get(item);
          newData[1][time][axis][item] = angleTemp.get(item);
        }
      }
    }

    return newData;
  }
}
