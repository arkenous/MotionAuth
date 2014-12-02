package com.example.motionauth.Processing;

import android.util.Log;
import com.example.motionauth.Utility.LogUtil;

/**
 * 加速度や角速度から，速度や角度を求める
 *
 * @author Kensuke Kousaka
 */
public class Calc {

	/**
	 * 加速度データを距離データに変換する
	 *
	 * @param inputVal 変換対象の，三次元加速度データ
	 * @param t        時間
	 * @return 変換後の三次元距離データ
	 */
	public double[][][] accelToDistance (double[][][] inputVal, double t) {
		LogUtil.log(Log.INFO);

		double[][][] returnVal = new double[inputVal.length][inputVal[0].length][inputVal[0][0].length];

		for (int i = 0; i < inputVal.length; i++) {
			for (int j = 0; j < inputVal[i].length; j++) {
				for (int k = 0; k < inputVal[i][j].length; k++) {
					returnVal[i][j][k] = (inputVal[i][j][k] * t * t) / 2 * 1000;
				}
			}
		}

		return returnVal;
	}


	/**
	 * 角速度データを角度データに変換する
	 *
	 * @param inputVal 変換対象の，三次元角速度データ
	 * @param t        時間
	 * @return 変換後の三次元角度データ
	 */
	public double[][][] gyroToAngle (double[][][] inputVal, double t) {
		LogUtil.log(Log.INFO);

		double[][][] returnVal = new double[inputVal.length][inputVal[0].length][inputVal[0][0].length];

		for (int i = 0; i < inputVal.length; i++) {
			for (int j = 0; j < inputVal[i].length; j++) {
				for (int k = 0; k < inputVal[i][j].length; k++) {
					returnVal[i][j][k] = (inputVal[i][j][k] * t) * 1000;
				}
			}
		}

		return returnVal;
	}


	/**
	 * 加速度データを距離データに変換する
	 *
	 * @param inputVal 変換対象の，三次元加速度データ
	 * @param t        時間
	 * @return 変換後の三次元距離データ
	 */
	public double[][] accelToDistance (double[][] inputVal, double t) {
		LogUtil.log(Log.INFO);

		double[][] returnVal = new double[inputVal.length][inputVal[0].length];

		for (int i = 0; i < inputVal.length; i++) {
			for (int j = 0; j < inputVal[i].length; j++) {
				returnVal[i][j] = (inputVal[i][j] * t * t) / 2 * 1000;
			}
		}

		return returnVal;
	}


	/**
	 * 角速度データを角度データに変換する
	 *
	 * @param inputVal 変換対象の，三次元角速度データ
	 * @param t        時間
	 * @return 変換後の三次元角度データ
	 */
	public double[][] gyroToAngle (double[][] inputVal, double t) {
		LogUtil.log(Log.INFO);

		double[][] returnVal = new double[inputVal.length][inputVal[0].length];

		for (int i = 0; i < inputVal.length; i++) {
			for (int j = 0; j < inputVal[i].length; j++) {
				returnVal[i][j] = (inputVal[i][j] * t) * 1000;
			}
		}

		return returnVal;
	}
}
