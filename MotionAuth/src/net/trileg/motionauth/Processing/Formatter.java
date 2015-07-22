package net.trileg.motionauth.Processing;

import android.util.Log;
import net.trileg.motionauth.Utility.LogUtil;

import java.util.Locale;


/**
 * Round data length.
 *
 * @author Kensuke Kosaka
 */
public class Formatter {

	/**
	 * Round float type 2-array data to two digits after the decimal point, and convert it to double type.
	 *
	 * @param inputVal Float type 2-array data.
	 * @return Rounded and Converted double type 2-array data.
	 */
	public double[][] floatToDoubleFormatter(float[][] inputVal) {
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
	 * Round float type 3-array data to two digits after the decimal point, and convert it to double type.
	 *
	 * @param inputVal Float type 3-array data.
	 * @return Rounded and Converted double type 3-array data.
	 */
	public double[][][] floatToDoubleFormatter(float[][][] inputVal) {
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
	 * Round double type 2-array data to two digits after the decimal point.
	 *
	 * @param inputVal Double type 2-array data.
	 * @return Rounded double type 2-array data.
	 */
	public double[][] doubleToDoubleFormatter(double[][] inputVal) {
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
	 * Round double type 3-array data to two digits after the decimal point.
	 *
	 * @param inputVal Double type 3-array data.
	 * @return Rounded double type 3-array data.
	 */
	public double[][][] doubleToDoubleFormatter(double[][][] inputVal) {
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
