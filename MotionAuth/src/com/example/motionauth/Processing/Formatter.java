package com.example.motionauth.Processing;

import android.util.Log;
import com.example.motionauth.Utility.LogUtil;

import java.util.Locale;


/**
 * データの桁揃えを行う．対応データはfloat及びdouble．返り値はdouble型．
 *
 * @author Kensuke Kousaka
 */
public class Formatter {

	/**
	 * float型の２次元数値データを小数点以下２桁に揃え，doubleに変換する
	 *
	 * @param inputVal float型の２次元配列データ
	 * @return 小数点以下２桁に揃え，double型に変換した２次元数値データ
	 */
	public double[][] floatToDoubleFormatter (float[][] inputVal) {
		LogUtil.log(Log.INFO);

		double[][] returnVal = new double[inputVal.length][inputVal[0].length];

		for (int i = 0; i < inputVal.length; i++) {
			for (int j = 0; j < inputVal[i].length; j++) {
				String format = String.format(Locale.getDefault(), "%.2f", inputVal[i][j]);
				returnVal[i][j] = Double.valueOf(format);
			}
		}

		return returnVal;
	}


	/**
	 * float型の３次元数値データを小数点以下２桁に揃え，doubleに変換する
	 *
	 * @param inputVal float型の3次元配列データ
	 * @return 小数点以下２桁に揃え，double型に変換した３次元数値データ
	 */
	public double[][][] floatToDoubleFormatter (float[][][] inputVal) {
		LogUtil.log(Log.INFO);

		double[][][] returnVal = new double[inputVal.length][inputVal[0].length][inputVal[0][0].length];

		for (int i = 0; i < inputVal.length; i++) {
			for (int j = 0; j < inputVal[i].length; j++) {
				for (int k = 0; k < inputVal[i][j].length; k++) {
					String format = String.format(Locale.getDefault(), "%.2f", inputVal[i][j][k]);
					returnVal[i][j][k] = Double.valueOf(format);
				}
			}
		}

		return returnVal;
	}


	/**
	 * double型の二次元数値データを小数点以下二桁に揃える
	 *
	 * @param inputVal double型の二次元配列データ
	 * @return 小数点以下二桁に揃えたdouble型二次元数値データ
	 */
	public double[][] doubleToDoubleFormatter (double[][] inputVal) {
		LogUtil.log(Log.INFO);

		double[][] returnVal = new double[inputVal.length][inputVal[0].length];

		for (int i = 0; i < inputVal.length; i++) {
			for (int j = 0; j < inputVal[i].length; j++) {
				String format = String.format(Locale.getDefault(), "%.2f", inputVal[i][j]);
				returnVal[i][j] = Double.valueOf(format);
			}
		}

		return returnVal;
	}


	/**
	 * double型の３次元数値データを小数点以下２桁に揃える
	 *
	 * @param inputVal double型の３次元配列データ
	 * @return 小数点以下２桁に揃えたdouble型３次元数値データ
	 */
	public double[][][] doubleToDoubleFormatter (double[][][] inputVal) {
		LogUtil.log(Log.INFO);

		double[][][] returnVal = new double[inputVal.length][inputVal[0].length][inputVal[0][0].length];

		for (int i = 0; i < inputVal.length; i++) {
			for (int j = 0; j < inputVal[i].length; j++) {
				for (int k = 0; k < inputVal[i][j].length; k++) {
					String format = String.format(Locale.getDefault(), "%.2f", inputVal[i][j][k]);
					returnVal[i][j][k] = Double.valueOf(format);
				}
			}
		}

		return returnVal;
	}
}
