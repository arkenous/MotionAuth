package net.trileg.motionauth.Lowpass;

import android.util.Log;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
import net.trileg.motionauth.Authentication.InputName;
import net.trileg.motionauth.Utility.LogUtil;
import net.trileg.motionauth.Utility.ManageData;

/**
 * Low pass filter using Fourier transform.
 * Using JTransforms for Fourier transform.
 *
 * @author Kensuke Kosaka
 * @see <a href="https://sites.google.com/site/piotrwendykier/software/jtransforms">https://sites.google.com/site/piotrwendykier/software/jtransforms</a>
 */
public class Fourier {
	private ManageData mManageData = new ManageData();

	/**
	 * Low pass filtering double type 3-array data.
	 *
	 * @param data     Double type 3-array data you want to low pass filtering.
	 * @param dataName Output file name to write SD card.
	 * @return Low pass filtered double type 3-array data.
	 */
	public double[][][] LowpassFilter(double[][][] data, String dataName) {
		LogUtil.log(Log.INFO);

		DoubleFFT_1D realfft = new DoubleFFT_1D(data[0][0].length);

		// Execute forward fourier transform
		for (double[][] i : data) {
			for (double[] j : i) {
				realfft.realForward(j);
			}
		}


		double[][][] real = new double[data.length][data[0].length][data[0][0].length];
		double[][][] imaginary = new double[data.length][data[0].length][data[0][0].length];

		int countReal = 0;
		int countImaginary = 0;

		// Decompose real-part and imaginary-part
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				for (int k = 0; k < data[i][j].length; k++) {
					if (k % 2 == 0) {
						real[i][j][countReal] = data[i][j][k];
						countReal++;
						if (countReal == 99) countReal = 0;
					} else {
						imaginary[i][j][countImaginary] = data[i][j][k];
						countImaginary++;
						if (countImaginary == 99) countImaginary = 0;
					}
				}
			}
		}

		mManageData.writeDoubleThreeArrayData("ResultFFT", "rFFT" + dataName, net.trileg.motionauth.Registration.InputName.name, real);
		mManageData.writeDoubleThreeArrayData("ResultFFT", "iFFT" + dataName, net.trileg.motionauth.Registration.InputName.name, imaginary);

		double[][][] power = new double[data.length][data[0].length][data[0][0].length / 2];

		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				for (int k = 0; k < data[i][j].length / 2; k++) {
					power[i][j][k] = Math.sqrt(Math.pow(real[i][j][k], 2) + Math.pow(imaginary[i][j][k], 2));
				}
			}
		}

		mManageData.writeDoubleThreeArrayData("ResultFFT", "powerFFT" + dataName, net.trileg.motionauth.Registration.InputName.name, power);

		// Low pass filtering
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

		mManageData.writeDoubleThreeArrayData("AfterFFT", dataName, net.trileg.motionauth.Registration.InputName.name, data);

		return data;
	}


	/**
	 * Low pass filtering double type 2-array data.
	 *
	 * @param data     Double type 2-array data you want to low pass filtering.
	 * @param dataName Output file name to write SD card.
	 * @return Low pass filtered double type 2-array data.
	 */
	public double[][] LowpassFilter(double[][] data, String dataName) {
		LogUtil.log(Log.INFO);

		DoubleFFT_1D realfft = new DoubleFFT_1D(data[0].length);

		// Execute forward fourier transform
		for (double[] i : data) realfft.realForward(i);

		double[][] real = new double[data.length][data[0].length];
		double[][] imaginary = new double[data.length][data[0].length];

		int countReal = 0;
		int countImaginary = 0;

		// Decompose real-part and imaginary-part
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				if (j % 2 == 0) {
					real[i][countReal] = data[i][j];
					countReal++;
					if (countReal == 99) countReal = 0;
				} else {
					imaginary[i][countImaginary] = data[i][j];
					countImaginary++;
					if (countImaginary == 99) countImaginary = 0;
				}

			}
		}

		mManageData.writeDoubleTwoArrayData("ResultFFT", "rFFT" + dataName, InputName.userName, real);
		mManageData.writeDoubleTwoArrayData("ResultFFT", "iFFT" + dataName, InputName.userName, imaginary);

		double[][] power = new double[data.length][data[0].length / 2];
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length / 2; j++) {
				power[i][j] = Math.sqrt(Math.pow(real[i][j], 2) + Math.pow(imaginary[i][j], 2));
			}
		}

		mManageData.writeDoubleTwoArrayData("ResultFFT", "powerFFT" + dataName, InputName.userName, power);

		// Low pass filtering
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				if (j > 30) data[i][j] = 0;
			}
		}

		// Execute inverse fourier transform
		for (double[] i : data) realfft.realInverse(i, true);

		mManageData.writeDoubleTwoArrayData("AfterFFT", dataName, InputName.userName, data);

		return data;
	}
}
