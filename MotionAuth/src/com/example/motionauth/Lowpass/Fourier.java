package com.example.motionauth.Lowpass;

import android.util.Log;
import com.example.motionauth.Authentication.AuthNameInput;
import com.example.motionauth.Registration.RegistNameInput;
import com.example.motionauth.Utility.LogUtil;
import com.example.motionauth.Utility.ManageData;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

/**
 * フーリエ変換を用いたローパスフィルタ
 * フーリエ変換にはjtransformsライブラリを使用
 *
 * @author Kensuke Kousaka
 * @see <a href="https://sites.google.com/site/piotrwendykier/software/jtransforms">https://sites.google.com/site/piotrwendykier/software/jtransforms</a>
 */
public class Fourier {
	private ManageData mManageData = new ManageData();

	/**
	 * double型三次元配列の入力データに対し，フーリエ変換を用いてローパスフィルタリングを行ってデータの平滑化を行う
	 *
	 * @param data     データ平滑化を行うdouble型三次元配列データ
	 * @param dataName アウトプット用，データ種別
	 * @return フーリエ変換によるローパスフィルタリングにより滑らかになったdouble型三次元配列データ
	 */
	public double[][][] LowpassFilter (double[][][] data, String dataName) {
		LogUtil.log(Log.INFO);

		DoubleFFT_1D realfft = new DoubleFFT_1D(data[0][0].length);

		// フーリエ変換（ForwardDFT）の実行
		for (double[][] i : data) {
			for (double[] j : i) {
				realfft.realForward(j);
			}
		}


		// 実数部，虚数部それぞれを入れる配列
		double[][][] real = new double[data.length][data[0].length][data[0][0].length];
		double[][][] imaginary = new double[data.length][data[0].length][data[0][0].length];

		int countReal = 0;
		int countImaginary = 0;

		// 実数部と虚数部に分解
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				for (int k = 0; k < data[i][j].length; k++) {
					if (k % 2 == 0) {
						real[i][j][countReal] = data[i][j][k];
						countReal++;
						if (countReal == 99) countReal = 0;
					}
					else {
						imaginary[i][j][countImaginary] = data[i][j][k];
						countImaginary++;
						if (countImaginary == 99) countImaginary = 0;
					}
				}
			}
		}

		mManageData.writeDoubleThreeArrayData("ResultFFT", "rFFT" + dataName, RegistNameInput.name, real);
		mManageData.writeDoubleThreeArrayData("ResultFFT", "iFFT" + dataName, RegistNameInput.name, imaginary);

		// パワースペクトルを求めるために，実数部（k），虚数部（k + 1）それぞれを二乗して加算し，平方根を取り，絶対値を求める
		double[][][] power = new double[data.length][data[0].length][data[0][0].length / 2];

		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				for (int k = 0; k < data[i][j].length / 2; k++) {
					power[i][j][k] = Math.sqrt(Math.pow(real[i][j][k], 2) + Math.pow(imaginary[i][j][k], 2));
				}
			}
		}

		mManageData.writeDoubleThreeArrayData("ResultFFT", "powerFFT" + dataName, RegistNameInput.name, power);

		// ローパスフィルタ処理
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				for (int k = 0; k < data[i][j].length; k++) {
					if (k > 30) data[i][j][k] = 0;
				}
			}
		}

		for (double[][] i : data) {
			for (double[] j : i) {
				realfft.realInverse(j, true);
			}
		}

		mManageData.writeDoubleThreeArrayData("AfterFFT", dataName, RegistNameInput.name, data);

		return data;
	}


	/**
	 * double型二次元配列の入力データに対し，フーリエ変換を用いてローパスフィルタリングを行ってデータの平滑化を行う
	 *
	 * @param data     データ平滑化を行うdouble型三次元配列データ
	 * @param dataName アウトプット用，データ種別
	 * @return フーリエ変換によるローパスフィルタリングにより滑らかになったdouble型三次元配列データ
	 */
	public double[][] LowpassFilter (double[][] data, String dataName) {
		LogUtil.log(Log.INFO);

		DoubleFFT_1D realfft = new DoubleFFT_1D(data[0].length);

		// フーリエ変換（ForwardDFT）の実行
		for (double[] i : data) realfft.realForward(i);

		// 実数部，虚数部それぞれを入れる配列
		double[][] real = new double[data.length][data[0].length];
		double[][] imaginary = new double[data.length][data[0].length];

		int countReal = 0;
		int countImaginary = 0;

		// 実数部と虚数部に分解
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				if (j % 2 == 0) {
					real[i][countReal] = data[i][j];
					countReal++;
					if (countReal == 99) countReal = 0;
				}
				else {
					imaginary[i][countImaginary] = data[i][j];
					countImaginary++;
					if (countImaginary == 99) countImaginary = 0;
				}

			}
		}

		mManageData.writeDoubleTwoArrayData("ResultFFT", "rFFT" + dataName, AuthNameInput.name, real);
		mManageData.writeDoubleTwoArrayData("ResultFFT", "iFFT" + dataName, AuthNameInput.name, imaginary);

		// パワースペクトルを求めるために，実数部（k），虚数部（k + 1）それぞれを二乗して加算し，平方根を取り，絶対値を求める
		double[][] power = new double[data.length][data[0].length / 2];

		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length / 2; j++) {
				power[i][j] = Math.sqrt(Math.pow(real[i][j], 2) + Math.pow(imaginary[i][j], 2));
			}
		}

		mManageData.writeDoubleTwoArrayData("ResultFFT", "powerFFT" + dataName, AuthNameInput.name, power);

		// ローパスフィルタ処理
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				if (j > 30) data[i][j] = 0;
			}
		}

		// 逆フーリエ変換（InverseDFT）
		for (double[] i : data) realfft.realInverse(i, true);

		mManageData.writeDoubleTwoArrayData("AfterFFT", dataName, AuthNameInput.name, data);

		return data;
	}
}
