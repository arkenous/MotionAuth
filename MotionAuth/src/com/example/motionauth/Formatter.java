package com.example.motionauth;

import java.util.Locale;


/**
 * データの桁揃えを行う．対応データはfloat及びdouble．返り値はdouble型．
 *
 * @author Kensuke Kousaka
 */
public class Formatter {
    /**
     * float型の数値データを小数点以下2桁に揃え，doubleに変換する
     *
     * @param inputVal float型の数値データ
     * @return 小数点以下2桁に揃え，double型に変換した数値データ
     */
    public static double floatToDoubleFormatter (float inputVal) {
        double returnVal;

        String afterFormat = String.format(Locale.getDefault(), "%.2f", inputVal);

        returnVal = Double.valueOf(afterFormat);

        return returnVal;
    }


    /**
     * float型の２次元数値データを小数点以下２桁に揃え，doubleに変換する
     *
     * @param inputVal float型の２次元配列データ
     * @return 小数点以下２桁に揃え，double型に変換した２次元数値データ
     */
    public double[][] floatToDoubleFormatter (float[][] inputVal) {
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
     * float型の３次元数値データを小数点以下２桁に揃え，doubleに変換する
     *
     * @param inputVal float型の3次元配列データ
     * @return 小数点以下２桁に揃え，double型に変換した３次元数値データ
     */
    public double[][][] floatToDoubleFormatter (float[][][] inputVal) {
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
     * double型の数値データを小数点以下2桁に揃える
     *
     * @param inputVal double型の数値データ
     * @return 小数点以下2桁に揃えたdouble型数値データ
     */
    public static double doubleToDoubleFormatter (double inputVal) {
        double returnVal;

        String afterFormat = String.format(Locale.getDefault(), "%.2f", inputVal);

        returnVal = Double.valueOf(afterFormat);

        return returnVal;
    }


    public double[][] doubleToDoubleFormatter (double[][] inputVal) {
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
     * double型の３次元数値データを小数点以下２桁に揃える
     *
     * @param inputVal double型の３次元配列データ
     * @return 小数点以下２桁に揃えたdouble型３次元数値データ
     */
    public double[][][] doubleToDoubleFormatter (double[][][] inputVal) {
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
