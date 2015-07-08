package net.trileg.motionauth.Processing;

import android.util.Log;
import net.trileg.motionauth.Utility.LogUtil;

/**
 * Calculate distance or angle from acceleration or gyroscope.
 *
 * @author Kensuke Kosaka
 */
public class Calc {

	/**
	 * Convert acceleration data to distance data.
	 *
	 * @param inputVal 3-array acceleration data for target of conversion.
	 * @param t        time
	 * @return After converted 3-array distance data.
	 */
	public double[][][] accelToDistance(double[][][] inputVal, double t) {
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
	 * Convert gyroscope data to angle data.
	 *
	 * @param inputVal 3-array gyroscope data for target of conversion.
	 * @param t        time
	 * @return After converted 3-array angle data.
	 */
	public double[][][] gyroToAngle(double[][][] inputVal, double t) {
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
	 * Convert acceleration data to distance data.
	 *
	 * @param inputVal 2-array acceleration data for target of conversion.
	 * @param t        time
	 * @return After converted 2-array distance data.
	 */
	public double[][] accelToDistance(double[][] inputVal, double t) {
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
	 * Convert gyroscope data to angle data.
	 *
	 * @param inputVal 2-array gyroscope data for target of conversion.
	 * @param t        time
	 * @return After converted 2-array angle data.
	 */
	public double[][] gyroToAngle(double[][] inputVal, double t) {
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
