package com.example.motionauth.Lowpass;

/**
 * 移動平均を用いたローパスフィルタ
 *
 * @author Kensuke Kousaka
 */
public class MovingAverage {
    /**
     * Double型3次元配列データを受け取り，移動平均を用いて平滑化し，
     * 処理したDouble型3次元配列データを返す
     *
     * @param data 平滑化を行うDouble型3次元配列データ
     * @return 平滑化を行ったDouble型3次元配列データ
     */
    public double[][][] LowpassFilter (double[][][] data) {
        double outputData = 0.0;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; i++) {
                for (int k = 0; k < 100; k++) {
                    outputData = outputData * 0.9 + data[i][j][k] * 0.1;
                    data[i][j][k] = outputData;

                    if (k == 99) {
                        outputData = 0.0;
                    }
                }
            }
        }
        return data;
    }

    public double[][] LowpassFilter (double[][] data) {
        double outputData = 0.0;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 100; j++) {
                outputData = outputData * 0.9 + data[i][j] * 0.1;
                data[i][j] = outputData;

                if (j == 99) {
                    outputData = 0.0;
                }
            }
        }
        return data;
    }
}
