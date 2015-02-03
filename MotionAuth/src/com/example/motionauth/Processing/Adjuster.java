package com.example.motionauth.Processing;

/**
 * データの時間的長さを揃える．
 * 調整する際は，最も長いものに合わせる．
 *
 * @author Kensuke Kousaka
 */
public class Adjuster {
    public double[][][][] adjust (double[][][] accel, double[][][] gyro) {
        int first = accel[0][0].length;
        int second = accel[1][0].length;
        int third = accel[2][0].length;

        int maxTime = 0;
        int maxLength = 0;

        if (first >= second && first >= third) {
            maxTime = 0;
            maxLength = first;
        } else if (second >= first && second >= third) {
            maxTime = 1;
            maxLength = second;
        } else if (third >= first && third >= second) {
            maxTime = 2;
            maxLength = third;
        }

        double[][][][] result = new double[2][3][3][maxLength];

        for (int kind = 0; kind < 2; kind++) {
            if (kind == 0) {
                for (int time = 0; time < 3; time++) {
                    if (time == maxTime) {
                        for (int axis = 0; axis < 3; axis++) {
                            for (int length = 0; length < maxLength; length++) {
                                result[kind][time][axis][length] = accel[time][axis][length];
                            }
                        }
                    } else {
                        // 1回目が5，2回目が3，diffが2
                        int diff = maxLength - accel[time][0].length;

                        for (int axis = 0; axis < 3; axis++) {
                            for (int length = 0; length < maxLength - diff; length++) {
                                result[kind][time][axis][length] = accel[time][axis][length];
                            }
                            for (int length = diff; length < maxLength; length++) {
                                result[kind][time][axis][length] = 0;
                            }
                        }
                    }
                }
            } else {
                for (int time = 0; time < 3; time++) {
                    if (time == maxTime) {
                        for (int axis = 0; axis < 3; axis++) {
                            for (int length = 0; length < maxLength; length++) {
                                result[kind][time][axis][length] = gyro[time][axis][length];
                            }
                        }
                    } else {
                        int diff = maxLength - gyro[time][0].length;

                        for (int axis = 0; axis < 3; axis++) {
                            for (int length = 0; length < maxLength - diff; length++) {
                                result[kind][time][axis][length] = gyro[time][axis][length];
                            }
                            for (int length = diff; length < maxLength; length++) {
                                result[kind][time][axis][length] = 0;
                            }
                        }
                    }
                }
            }
        }

        return result;
    }
}
