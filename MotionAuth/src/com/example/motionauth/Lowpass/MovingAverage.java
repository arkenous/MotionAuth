package com.example.motionauth.Lowpass;

import android.content.Context;
import com.example.motionauth.Registration.RegistNameInput;
import com.example.motionauth.Utility.WriteData;

/**
 * 移動平均を用いたローパスフィルタ
 *
 * @author Kensuke Kousaka
 */
public class MovingAverage {
    WriteData mWriteData = new WriteData();

    /**
     * Double型3次元配列データを受け取り，移動平均を用いて平滑化し，
     * 処理したDouble型3次元配列データを返す
     *
     * @param data 平滑化を行うDouble型3次元配列データ
     * @return 平滑化を行ったDouble型3次元配列データ
     */
    // 実験用にコメントアウト
//    public double[][][] LowpassFilter (double[][][] data) {
    public void LowpassFilter (double[][][] data, String dataname, Context context) {
        double outputData = 0.0;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 100; k++) {
                    outputData = outputData * 0.9 + data[i][j][k] * 0.1;
                    data[i][j][k] = outputData;

                    if (k == 99) {
                        outputData = 0.0;
                    }
                }
            }
        }

        mWriteData.writeDoubleThreeArrayData("AfterMA", dataname, RegistNameInput.name, data, context);
//        return data;
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
        // 実験用にコメントアウト
        return data;
    }
}
