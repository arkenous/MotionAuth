package com.example.motionauth;

public class Calc {
    /**
     * 加速度データを距離データに変換する
     *
     * @param inputVal 変換対象の，三次元加速度データ
     * @param t        時間
     * @return 変換後の三次元距離データ
     */
    public double[][][] accelToDistance (double[][][] inputVal, double t) {
        double[][][] returnVal = {};

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
        double[][][] returnVal = {};

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
     * @param inputVal 変換対象の，三次元加速度データ
     * @param t        時間
     * @return 変換後の三次元距離データ
     */
    public double[][] accelToDistance (double[][] inputVal, double t) {
        double[][] returnVal = {};

        for (int i = 0; i < inputVal.length; i++) {
            for (int j = 0; j < inputVal[i].length; j++) {
                returnVal[i][j] = (inputVal[i][j] * t * t) / 2 * 1000;
            }
        }

        return returnVal;
    }


    /**
     * 角速度データを角度データに変換する
     * @param inputVal 変換対象の，三次元角速度データ
     * @param t        時間
     * @return 変換後の三次元角度データ
     */
    public double[][] gyroToAngle (double[][] inputVal, double t) {
        double[][] returnVal = {};

        for (int i = 0; i < inputVal.length; i++) {
            for (int j = 0; j < inputVal[i].length; j++) {
                returnVal[i][j] = (inputVal[i][j] * t) * 1000;
            }
        }

        return returnVal;
    }
}
