package com.example.motionauth.Lowpass;

import android.util.Log;
import com.example.motionauth.Authentication.AuthNameInput;
import com.example.motionauth.Registration.RegistNameInput;
import com.example.motionauth.Utility.WriteData;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

import java.util.Arrays;

/**
 * フーリエ変換を用いたローパスフィルタ
 * フーリエ変換にはjtransformsライブラリを使用
 *
 * @author Kensuke Kousaka
 * @see <a href="https://sites.google.com/site/piotrwendykier/software/jtransforms">https://sites.google.com/site/piotrwendykier/software/jtransforms</a>
 */
public class Fourier {
    private static final String TAG = Fourier.class.getSimpleName();

    private WriteData mWriteData = new WriteData();

    /**
     * double型三次元配列の入力データに対し，フーリエ変換を用いてローパスフィルタリングを行ってデータの平滑化を行う
     *
     * @param data     データ平滑化を行うdouble型三次元配列データ
     * @param dataName アウトプット用，データ種別
     * @return フーリエ変換によるローパスフィルタリングにより滑らかになったdouble型三次元配列データ
     */
    public double[][][] LowpassFilter (double[][][] data, String dataName) {
        Log.v(TAG, "--- LowpassFilter ---");

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
                        if (countReal == 99) {
                            countReal = 0;
                        }
                    }
                    else {
                        imaginary[i][j][countImaginary] = data[i][j][k];
                        countImaginary++;
                        if (countImaginary == 99) {
                            countImaginary = 0;
                        }
                    }
                }
            }
        }

        mWriteData.writeDoubleThreeArrayData("ResultFFT", "rFFT" + dataName, RegistNameInput.name, real);
        mWriteData.writeDoubleThreeArrayData("ResultFFT", "iFFT" + dataName, RegistNameInput.name, imaginary);

        // パワースペクトルを求めるために，実数部（k），虚数部（k + 1）それぞれを二乗して加算し，平方根を取り，絶対値を求める
        double[][][] power = new double[data.length][data[0].length][data[0][0].length / 2];

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                for (int k = 0; k < data[i][j].length / 2; k++) {
                    power[i][j][k] = Math.sqrt(Math.pow(real[i][j][k], 2) + Math.pow(imaginary[i][j][k], 2));
                }
            }
        }

        mWriteData.writeDoubleThreeArrayData("ResultFFT", "powerFFT" + dataName, RegistNameInput.name, power);

        // For Test Purpose
        // ローパスの閾値を複数パターン試すために，元データを複数の配列にディープコピーする
        double[][][] testData1 = new double[data.length][data[0].length][data[0][0].length];
        double[][][] testData2 = new double[data.length][data[0].length][data[0][0].length];
        double[][][] testData3 = new double[data.length][data[0].length][data[0][0].length];
        double[][][] testData4 = new double[data.length][data[0].length][data[0][0].length];
        double[][][] testData5 = new double[data.length][data[0].length][data[0][0].length];
        double[][][] testData6 = new double[data.length][data[0].length][data[0][0].length];
        double[][][] testData7 = new double[data.length][data[0].length][data[0][0].length];

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                testData1[i][j] = Arrays.copyOf(data[i][j], data[i][j].length);
                testData2[i][j] = Arrays.copyOf(data[i][j], data[i][j].length);
                testData3[i][j] = Arrays.copyOf(data[i][j], data[i][j].length);
                testData4[i][j] = Arrays.copyOf(data[i][j], data[i][j].length);
                testData5[i][j] = Arrays.copyOf(data[i][j], data[i][j].length);
                testData6[i][j] = Arrays.copyOf(data[i][j], data[i][j].length);
                testData7[i][j] = Arrays.copyOf(data[i][j], data[i][j].length);
            }
        }


        // ローパスフィルタ処理
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                for (int k = 0; k < data[i][j].length; k++) {
                    if (k > 10) {
                        testData1[i][j][k] = 0;
                    }
                    else if (k > 20) {
                        testData2[i][j][k] = 0;
                    }
                    else if (k > 30) {
                        data[i][j][k] = 0;
                    }
                    else if (k > 40) {
                        testData3[i][j][k] = 0;
                    }
                    else if (k > 50) {
                        testData4[i][j][k] = 0;
                    }
                    else if (k > 60) {
                        testData5[i][j][k] = 0;
                    }
                    else if (k > 70) {
                        testData6[i][j][k] = 0;
                    }
                    else if (k > 80) {
                        testData7[i][j][k] = 0;
                    }
                }
            }
        }

        // 逆フーリエ変換（InverseDFT）
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                realfft.realInverse(data[i][j], true);

                realfft.realInverse(testData1[i][j], true);
                realfft.realInverse(testData2[i][j], true);
                realfft.realInverse(testData3[i][j], true);
                realfft.realInverse(testData4[i][j], true);
                realfft.realInverse(testData5[i][j], true);
                realfft.realInverse(testData6[i][j], true);
                realfft.realInverse(testData7[i][j], true);

            }
        }

        mWriteData.writeDoubleThreeArrayData("AfterFFT", dataName, RegistNameInput.name, data);

        mWriteData.writeDoubleThreeArrayData("AfterFFT", dataName + "testData1", RegistNameInput.name, testData1);
        mWriteData.writeDoubleThreeArrayData("AfterFFT", dataName + "testData2", RegistNameInput.name, testData2);
        mWriteData.writeDoubleThreeArrayData("AfterFFT", dataName + "testData3", RegistNameInput.name, testData3);
        mWriteData.writeDoubleThreeArrayData("AfterFFT", dataName + "testData4", RegistNameInput.name, testData4);
        mWriteData.writeDoubleThreeArrayData("AfterFFT", dataName + "testData5", RegistNameInput.name, testData5);
        mWriteData.writeDoubleThreeArrayData("AfterFFT", dataName + "testData6", RegistNameInput.name, testData6);
        mWriteData.writeDoubleThreeArrayData("AfterFFT", dataName + "testData7", RegistNameInput.name, testData7);


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
        Log.v(TAG, "--- LowpassFilter ---");

        DoubleFFT_1D realfft = new DoubleFFT_1D(data[0].length);

        // フーリエ変換（ForwardDFT）の実行
        for (double[] i : data) {
            realfft.realForward(i);
        }

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
                    if (countReal == 99) {
                        countReal = 0;
                    }
                }
                else {
                    imaginary[i][countImaginary] = data[i][j];
                    countImaginary++;
                    if (countImaginary == 99) {
                        countImaginary = 0;
                    }
                }

            }
        }

        mWriteData.writeDoubleTwoArrayData("ResultFFT", "rFFT" + dataName, AuthNameInput.name, real);
        mWriteData.writeDoubleTwoArrayData("ResultFFT", "iFFT" + dataName, AuthNameInput.name, imaginary);

        // パワースペクトルを求めるために，実数部（k），虚数部（k + 1）それぞれを二乗して加算し，平方根を取り，絶対値を求める
        double[][] power = new double[data.length][data[0].length / 2];

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length / 2; j++) {
                power[i][j] = Math.sqrt(Math.pow(real[i][j], 2) + Math.pow(imaginary[i][j], 2));
            }
        }

        mWriteData.writeDoubleTwoArrayData("ResultFFT", "powerFFT" + dataName, AuthNameInput.name, power);

        // ローパスフィルタ処理
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                if (j > 30) {
                    data[i][j] = 0;
                }
            }
        }

        // 逆フーリエ変換（InverseDFT）
        for (double[] i : data) {
            realfft.realInverse(i, true);
        }

        mWriteData.writeDoubleTwoArrayData("AfterFFT", dataName, AuthNameInput.name, data);

        return data;
    }
}
