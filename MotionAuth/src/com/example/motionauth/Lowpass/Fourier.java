package com.example.motionauth.Lowpass;

import android.content.Context;
import android.util.Log;
import com.example.motionauth.Registration.RegistNameInput;
import com.example.motionauth.WriteData;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_3D;

import java.util.Arrays;

/**
 * フーリエ変換を用いたローパスフィルタ
 * フーリエ変換にはjtransformsライブラリを使用
 *
 * @author Kensuke Kousaka
 * @see <a href="https://sites.google.com/site/piotrwendykier/software/jtransforms">https://sites.google.com/site/piotrwendykier/software/jtransforms</a>
 */
public class Fourier {
    private WriteData mWriteData = new WriteData();

    /**
     * 3次元入力データに対し，フーリエ変換を用いてローパスフィルタリングを行ってデータの平滑化を行う
     * @param data データ平滑化を行うdouble型3次元配列データ
     * @param name アウトプット用，データ種別
     * @param context Toast表示用
     */
    public void LowpassFilter (double[][][] data, String name, Context context) {
        DoubleFFT_1D realfft = new DoubleFFT_1D(data[0][0].length);

//        mWriteData.writeDoubleThreeArrayData("BeforeFFT", name, RegistNameInput.name, data, context);

        // フーリエ変換（ForwardDFT）の実行
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                realfft.realForward(data[i][j]);
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
                    } else {
                        imaginary[i][j][countImaginary] = data[i][j][k];
                        countImaginary++;
                        if (countImaginary == 99) {
                            countImaginary = 0;
                        }
                    }
                }
            }
        }

        mWriteData.writeDoubleThreeArrayData("ResultFFT", "rFFT" + name, RegistNameInput.name, real, context);
        mWriteData.writeDoubleThreeArrayData("ResultFFT", "iFFT" + name, RegistNameInput.name, imaginary, context);

        // パワースペクトルを求めるために，実数部（k），虚数部（k + 1）それぞれを2乗して加算し，平方根を取り，絶対値を求める
        double[][][] power = new double[data.length][data[0].length][data[0][0].length / 2];

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                for (int k = 0; k < data[0][0].length / 2; k++) {
                    power[i][j][k] = Math.sqrt(Math.pow(real[i][j][k], 2) + Math.pow(imaginary[i][j][k], 2));
                }
            }
        }

        mWriteData.writeDoubleThreeArrayData("ResultFFT", "powerFFT" + name, RegistNameInput.name, power, context);

        // ローパスフィルタ処理
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                for (int k = 0; k < data[i][j].length; k++) {
                    if (k > 30) {
                        data[i][j][k] = 0;
                    }
                }
            }
        }

        // 逆フーリエ変換（InverseDFT）
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                realfft.realInverse(data[i][j], true);
            }
        }

        mWriteData.writeDoubleThreeArrayData("AfterFFT", name, RegistNameInput.name, data, context);
    }


    public double[][][] retValLowpassFilter (double[][][] data, String dataName, Context context) {
        DoubleFFT_1D realfft = new DoubleFFT_1D(data[0][0].length);

        // フーリエ変換（ForwardDFT）の実行
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                realfft.realForward(data[i][j]);
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

        mWriteData.writeDoubleThreeArrayData("ResultFFT", "rFFT" + dataName, RegistNameInput.name, real, context);
        mWriteData.writeDoubleThreeArrayData("ResultFFT", "iFFT" + dataName, RegistNameInput.name, imaginary, context);

        // パワースペクトルを求めるために，実数部（k），虚数部（k + 1）それぞれを二乗して加算し，平方根を取り，絶対値を求める
        double[][][] power = new double[data.length][data[0].length][data[0][0].length / 2];

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                for (int k = 0; k < data[i][j].length / 2; k++) {
                    power[i][j][k] = Math.sqrt(Math.pow(real[i][j][k], 2) + Math.pow(imaginary[i][j][k], 2));
                }
            }
        }

        mWriteData.writeDoubleThreeArrayData("ResultFFT", "powerFFT" + dataName, RegistNameInput.name, power, context);

        // ローパスフィルタ処理
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                for (int k = 0; k < data[i][j].length; k++) {
                    if (k > 30) {
                        data[i][j][k] = 0;
                    }
                }
            }
        }

        // 逆フーリエ変換（InverseDFT）
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                realfft.realInverse(data[i][j], true);
            }
        }

        mWriteData.writeDoubleThreeArrayData("AfterFFT", dataName, RegistNameInput.name, data, context);

        return data;
    }
}
