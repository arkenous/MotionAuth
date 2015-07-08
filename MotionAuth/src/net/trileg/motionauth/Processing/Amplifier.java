package net.trileg.motionauth.Processing;

import android.util.Log;
import net.trileg.motionauth.Utility.LogUtil;

/**
 * Amplify data.
 *
 * @author Kensuke Kosaka
 */
public class Amplifier {
	private boolean isRangeCheck = false;


	/**
	 * Check weather data is less than threshold.
	 *
	 * @param data Double type 3-array data to check.
	 * @return true if data is less than threshold even once during the entire number of trials, otherwise false.
	 */
	public boolean CheckValueRange(double[][][] data, double checkRangeValue) {
		LogUtil.log(Log.INFO);

		LogUtil.log(Log.DEBUG, "checkRangeValue" + checkRangeValue);

		double[][] max = new double[data.length][data[0].length];
		double[][] min = new double[data.length][data[0].length];

		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				max[i][j] = 0;
				min[i][j] = 0;
			}
		}

		double range;
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				for (int k = 0; k < data[i][j].length; k++) {
					if (data[i][j][k] > max[i][j]) {
						max[i][j] = data[i][j][k];
					} else if (data[i][j][k] < min[i][j]) {
						min[i][j] = data[i][j][k];
					}
				}
			}
		}

		for (int i = 0; i < max.length; i++) {
			for (int j = 0; j < max[i].length; j++) {
				range = max[i][j] - min[i][j];
				LogUtil.log(Log.DEBUG, "range = " + range);
				if (range < checkRangeValue) isRangeCheck = true;
			}
		}

		return isRangeCheck;
	}


	/**
	 * Amplify data.
	 *
	 * @param data     Double type 3-array data to amplify.
	 * @param ampValue How much amplify data.
	 * @return Amplified double type 3-array data
	 */
	public double[][][] Amplify(double[][][] data, double ampValue) {
		LogUtil.log(Log.INFO);

		if (ampValue != 0.0) {
			for (int i = 0; i < data.length; i++) {
				for (int j = 0; j < data[i].length; j++) {
					for (int k = 0; k < data[i][j].length; k++) {
						data[i][j][k] *= ampValue;
					}
				}
			}
		}
		return data;
	}


	/**
	 * Amplify data.
	 *
	 * @param data     Double type 2-array data to amplify.
	 * @param ampValue How much amplify data.
	 * @return Amplified double type 2-array data
	 */
	public double[][] Amplify(double[][] data, double ampValue) {
		LogUtil.log(Log.INFO);

		if (ampValue != 0.0) {
			for (int i = 0; i < data.length; i++) {
				for (int j = 0; j < data[i].length; j++) {
					data[i][j] *= ampValue;
				}
			}
		}

		return data;
	}
}
