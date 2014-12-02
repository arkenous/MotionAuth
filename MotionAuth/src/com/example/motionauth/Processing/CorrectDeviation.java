package com.example.motionauth.Processing;

import android.util.Log;
import com.example.motionauth.Utility.Enum;
import com.example.motionauth.Utility.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;


/**
 * データの時間的なズレを修正する
 *
 * @author Kensuke Kousaka
 */
public class CorrectDeviation {

	/**
	 * 取得回数ごとのデータのズレを時間的なズレを修正する
	 *
	 * @param data 修正するdouble型の3次元配列データ
	 * @return newData ズレ修正後のdouble型の3次元配列データ
	 */
	public double[][][] correctDeviation (double[][][] data, Enum.MODE mode) {
		LogUtil.log(Log.INFO);

		double[][][] newData = new double[3][3][100];

		// 試行回ごとの代表値の出ている時間を抽出
		// 変数は．桁揃え，計算後のdistance, angleを利用

		// 回数・XYZを配列で
		double value[][] = new double[3][3];

		// 代表値の出ている時間，回数，XYZ
		int count[][] = new int[3][3];

		// とりあえず，変数にXYZそれぞれの一個目の値を放り込む
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				value[i][j] = data[i][j][0];
			}
		}


		// 代表値が出ている場所を取得する
		switch (mode) {
			case MAX:
				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 3; j++) {
						for (int k = 0; k < 100; k++) {
							if (value[i][j] < data[i][j][k]) {
								value[i][j] = data[i][j][k];
								count[i][j] = k;
							}
						}
					}
				}
				break;
			case MIN:
				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 3; j++) {
						for (int k = 0; k < 100; k++) {
							if (value[i][j] > data[i][j][k]) {
								value[i][j] = data[i][j][k];
								count[i][j] = k;
							}
						}
					}
				}
				break;
			case MEDIAN:
				// キーが自動ソートされるTreeMapを用いる．データと順番を紐付けしたものを作成し，中央値の初期の順番の値を取り出す．
				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 3; j++) {
						TreeMap<Double, Integer> treeMap = new TreeMap<>();

						for (int k = 0; k < 100; k++) {
							treeMap.put(data[i][j][k], k);
						}

						int loopCount = 0;
						for (Integer initCount : treeMap.values()) {
							if (loopCount == 49) {
								count[i][j] = initCount;
							}

							loopCount++;
						}
					}
				}
				break;
		}
		// 1回目のデータの代表値が出た場所と，2回目・3回目のデータの代表値が出た場所の差をとる
		// とったら，その差だけデータをずらす（ずらしてはみ出たデータは空いたとこに入れる）

		// sample
		// 一回目：50，二回目：30 → 右に20ずらす 一回目-二回目=+20
		// 一回目：30，二回目：50 → 左に20ずらす 一回目-二回目=-20

		// ずらす移動量を計算（XYZそれぞれ）
		int lagData[][] = new int[2][3];


		ArrayList<ArrayList<ArrayList<Double>>> tmpData = new ArrayList<>();

		ArrayList<ArrayList<Double>> tmpData1 = new ArrayList<>();
		ArrayList<Double> tmpData1X = new ArrayList<>();
		ArrayList<Double> tmpData1Y = new ArrayList<>();
		ArrayList<Double> tmpData1Z = new ArrayList<>();

		tmpData1.add(tmpData1X);
		tmpData1.add(tmpData1Y);
		tmpData1.add(tmpData1Z);

		ArrayList<ArrayList<Double>> tmpData2 = new ArrayList<>();
		ArrayList<Double> tmpData2X = new ArrayList<>();
		ArrayList<Double> tmpData2Y = new ArrayList<>();
		ArrayList<Double> tmpData2Z = new ArrayList<>();

		tmpData2.add(tmpData2X);
		tmpData2.add(tmpData2Y);
		tmpData2.add(tmpData2Z);

		tmpData.add(tmpData1);
		tmpData.add(tmpData2);

		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 3; j++) {
				for (int k = 0; k < 100; k++) {
					tmpData.get(i).get(j).add(data[i][j][k]);
				}
			}
		}


		// どれだけズレているかを計算する
		for (int i = 0; i < 3; i++) {
			lagData[0][i] = count[0][i] - count[1][i];
			LogUtil.log(Log.DEBUG, "lagData[0]" + "[" + i + "]" + ": " + lagData[0][i]);

			lagData[1][i] = count[0][i] - count[2][i];
			LogUtil.log(Log.DEBUG, "lagData[1]" + "[" + i + "]" + ": " + lagData[1][i]);
		}


		// 実際にリストの要素をずらしていく

		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 3; j++) {
				Collections.rotate(tmpData.get(i).get(j), lagData[i][j]);
			}
		}


		// これでずらせたはずだ．成果はリストに入っているはずなので，ずらした後のデータを専用の配列に入れよう
		// 1回目のデータに関しては基準となるデータなのでそのまま入れる
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 100; j++) {
				newData[0][i][j] = data[0][i][j];
			}
		}

		// ずらしたデータを入れる
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 3; j++) {
				for (int k = 0; k < 100; k++) {
					newData[i + 1][j][k] = tmpData.get(i).get(j).get(k);
				}
			}
		}

		return newData;
	}
}
