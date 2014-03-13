package com.example.motionauth;

import java.util.Locale;


/**
 * データの桁揃えを行う．対応データはfloat及びdouble．返り値はdouble型．
 *
 * @author Kensuke Kousaka
 */
public class Formatter
    {
        /**
         * float型の数値データを小数点以下2桁に揃え，doubleに変換する
         *
         * @param tmp float型の数値データ
         * @return 小数点以下2桁に揃え，double型に変換した数値データ
         */
        public static double floatToDoubleFormatter (float tmp)
            {
                String afterFormat = String.format(Locale.getDefault(), "%.2f", tmp);

                Double doubleValue = Double.valueOf(afterFormat);

                return doubleValue;
            }


        /**
         * double型の数値データを小数点以下2桁に揃える
         *
         * @param tmp double型の数値データ
         * @return 小数点以下2桁に揃えたdouble型数値データ
         */
        public static double doubleToDoubleFormatter (double tmp)
            {
                String afterFormat = String.format(Locale.getDefault(), "%.2f", tmp);

                Double doubleValue = Double.valueOf(afterFormat);

                return doubleValue;
            }
    }
