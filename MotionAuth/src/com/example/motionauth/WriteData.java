package com.example.motionauth;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;


/**
 * データをSDカードに書き込む
 *
 * @author Kensuke Kousaka
 */
public class WriteData
    {
        /**
         * Float型の三次元配列データをアウトプットする．保存先は，SDカードディレクトリ/folderName/userName/fileName+回数+次元
         *
         * @param folderName 保存するフォルダ名
         * @param fileName   保存するファイル名
         * @param userName   保存するユーザ名
         * @param data       保存するfloat型の3次元配列データ
         * @param context    実行するアクティビティのコンテキスト
         */
        public void writeFloatThreeArrayData (String folderName, String fileName, String userName, float[][][] data, Context context)
            {
                // SDカードのマウント確認
                String status = Environment.getExternalStorageState();
                if (!status.equals(Environment.MEDIA_MOUNTED))
                    {
                        // マウントされていない場合
                        Toast.makeText(context, "SDカードがマウントされていません", Toast.LENGTH_SHORT).show();
                    }

                // SDカードのフォルダパスの取得
                String SD_PATH = Environment.getExternalStorageDirectory().getPath();

                // SDカードにフォルダを作成
                String FOLDER_PATH = SD_PATH + File.separator + folderName + File.separator + userName;

                File file = new File(FOLDER_PATH);

                try
                    {
                        if (!file.exists())
                            {
                                // フォルダがない場合
                                file.mkdirs();
                            }
                        else
                            {
                                // フォルダがある場合
                            }
                    }
                catch (Exception e)
                    {
                        Toast.makeText(context, "makeFolderException", Toast.LENGTH_SHORT).show();
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }

                try
                    {
                        String dimension = null;

                        for (int i = 0; i < 3; i++)
                            {
                                // X,Y,Zループ
                                for (int j = 0; j < 3; j++)
                                    {
                                        if (j == 0)
                                            {
                                                dimension = "X";
                                            }
                                        else if (j == 1)
                                            {
                                                dimension = "Y";
                                            }
                                        else if (j == 2)
                                            {
                                                dimension = "Z";
                                            }

                                        // ファイルパス
                                        String filePath = FOLDER_PATH + File.separator + fileName + String.valueOf(i) + dimension;
                                        file = new File(filePath);
                                        FileOutputStream fos = new FileOutputStream(file, false);
                                        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                                        BufferedWriter bw = new BufferedWriter(osw);

                                        for (int k = 0; k < 100; k++)
                                            {
                                                bw.write(data[i][j][k] + "\n");
                                            }
                                        bw.close();
                                    }
                            }
                    }
                catch (Exception e)
                    {
                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                    }
            }


        /**
         * Double型の三次元配列データをアウトプットする． 保存先は，SDカードディレクトリ/folderName/userName/fileName+回数+次元
         *
         * @param folderName 保存するフォルダ名
         * @param fileName   保存するファイル名
         * @param userName   保存するユーザ名
         * @param data       保存するdouble型の３次元配列データ
         * @param context    実行するアクティビティのコンテキスト
         */
        public void writeDoubleThreeArrayData (String folderName, String fileName, String userName, double[][][] data, Context context)
            {
                // SDカードのマウント確認
                String status = Environment.getExternalStorageState();
                if (!status.equals(Environment.MEDIA_MOUNTED))
                    {
                        // マウントされていない場合
                        Toast.makeText(context, "SDカードがマウントされていません", Toast.LENGTH_SHORT).show();
                    }

                // SDカードのフォルダパスの取得
                String SD_PATH = Environment.getExternalStorageDirectory().getPath();

                // SDカードにフォルダを作成
                String FOLDER_PATH = SD_PATH + File.separator + folderName + File.separator + userName;

                File file = new File(FOLDER_PATH);

                try
                    {
                        if (!file.exists())
                            {
                                // フォルダがない場合
                                file.mkdirs();
                            }
                        else
                            {
                                // フォルダがある場合
                            }
                    }
                catch (Exception e)
                    {
                        Toast.makeText(context, "makeFolderException", Toast.LENGTH_SHORT).show();
                    }

                try
                    {
                        String dimension = null;

                        for (int i = 0; i < 3; i++)
                            {
                                // X,Y,Zループ
                                for (int j = 0; j < 3; j++)
                                    {
                                        if (j == 0)
                                            {
                                                dimension = "X";
                                            }
                                        else if (j == 1)
                                            {
                                                dimension = "Y";
                                            }
                                        else if (j == 2)
                                            {
                                                dimension = "Z";
                                            }

                                        // ファイルパス
                                        String filePath = FOLDER_PATH + File.separator + fileName + String.valueOf(i) + dimension;
                                        file = new File(filePath);
                                        FileOutputStream fos = new FileOutputStream(file, false);
                                        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                                        BufferedWriter bw = new BufferedWriter(osw);

                                        for (int k = 0; k < 100; k++)
                                            {
                                                bw.write(data[i][j][k] + "\n");
                                            }
                                        bw.close();
                                    }
                            }
                    }
                catch (Exception e)
                    {
                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                    }
            }


        /**
         * Double型の２次元配列データをアウトプットする． 保存先は，SDカードディレクトリ/folderName/userName/fileName+次元
         *
         * @param folderName 保存するフォルダ名
         * @param fileName   保存するファイル名
         * @param userName   保存するユーザ名
         * @param data       保存するdouble型の２次元配列データ
         * @param context    実行するアクティビティのコンテキスト
         * @return 保存に成功したらtrue，失敗したらfalseを返す
         */
        public static boolean writeDoubleTwoArrayData (String folderName, String fileName, String userName, double[][] data, Context context)
            {
                // SDカードのマウント確認
                String status = Environment.getExternalStorageState();
                if (!status.equals(Environment.MEDIA_MOUNTED))
                    {
                        // マウントされていない場合
                        Toast.makeText(context, "SDカードがマウントされていません", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                // SDカードのフォルダパスの取得
                String SD_PATH = Environment.getExternalStorageDirectory().getPath();

                // SDカードにフォルダを作成
                String FOLDER_PATH = SD_PATH + File.separator + folderName + File.separator + userName;

                Toast.makeText(context, "FolderPath = " + FOLDER_PATH, Toast.LENGTH_SHORT).show();

                File file = new File(FOLDER_PATH);

                try
                    {
                        if (!file.exists())
                            {
                                // フォルダがない場合
                                Toast.makeText(context, "folderNotExists", Toast.LENGTH_SHORT).show();
                                file.mkdirs();
                            }
                        else
                            {
                                // フォルダがある場合
                                Toast.makeText(context, "folderExists", Toast.LENGTH_SHORT).show();
                            }
                    }
                catch (Exception e)
                    {
                        Toast.makeText(context, "makeFolderException", Toast.LENGTH_SHORT).show();
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                        return false;
                    }

                try
                    {
                        String dimension = null;

                        // X,Y,Zループ
                        for (int i = 0; i < 3; i++)
                            {
                                if (i == 0)
                                    {
                                        dimension = "X";
                                    }
                                else if (i == 1)
                                    {
                                        dimension = "Y";
                                    }
                                else if (i == 2)
                                    {
                                        dimension = "Z";
                                    }

                                // ファイルパス
                                String filePath = FOLDER_PATH + File.separator + fileName + dimension;
                                file = new File(filePath);
                                FileOutputStream fos = new FileOutputStream(file, false);
                                OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                                BufferedWriter bw = new BufferedWriter(osw);

                                for (int j = 0; j < 100; j++)
                                    {
                                        bw.write(data[i][j] + "\n");
                                    }
                                bw.close();
                            }
                    }
                catch (Exception e)
                    {
                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                return true;
            }


        /**
         * Double型の２次元配列データをアウトプットする． 保存先は，SDカードディレクトリ/folderName/userName/fileName+次元
         *
         * @param folderName 保存するフォルダ名
         * @param fileName   保存するファイル名
         * @param userName   保存するユーザ名
         * @param data       保存するdouble型の２次元配列データ
         * @param context    実行するアクティビティのコンテキスト
         */
        public void writeRData (String folderName, String fileName, String userName, double[][] data, Context context)
            {
                // SDカードのマウント確認
                String status = Environment.getExternalStorageState();
                if (!status.equals(Environment.MEDIA_MOUNTED))
                    {
                        // マウントされていない場合
                        Toast.makeText(context, "SDカードがマウントされていません", Toast.LENGTH_SHORT).show();
                    }

                // SDカードのフォルダパスの取得
                String SD_PATH = Environment.getExternalStorageDirectory().getPath();

                // SDカードにフォルダを作成
                String FOLDER_PATH = SD_PATH + File.separator + folderName + File.separator + userName;

                File file = new File(FOLDER_PATH);

                try
                    {
                        if (!file.exists())
                            {
                                // フォルダがない場合
                                file.mkdirs();
                            }
                        else
                            {
                                // フォルダがある場合
                            }
                    }
                catch (Exception e)
                    {
                        Toast.makeText(context, "makeFolderException", Toast.LENGTH_SHORT).show();
                    }

                try
                    {
                        String dimension = null;

                        for (int i = 0; i < 3; i++)
                            {

                                // X,Y,Zループ
                                for (int j = 0; j < 3; j++)
                                    {
                                        if (j == 0)
                                            {
                                                dimension = "X";
                                            }
                                        else if (j == 1)
                                            {
                                                dimension = "Y";
                                            }
                                        else if (j == 2)
                                            {
                                                dimension = "Z";
                                            }

                                        // ファイルパス
                                        String filePath = FOLDER_PATH + File.separator + fileName + String.valueOf(i) + dimension;
                                        file = new File(filePath);
                                        FileOutputStream fos = new FileOutputStream(file, false);
                                        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                                        BufferedWriter bw = new BufferedWriter(osw);

                                        bw.write(data[i][j] + "\n");
                                        bw.close();
                                    }
                            }
                    }
                catch (Exception e)
                    {
                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                    }
            }


        /**
         * Double型の1次元配列データをアウトプットする． 保存先は，SDカードディレクトリ/folderName/userName/fileName
         *
         * @param folderName 保存するフォルダ名
         * @param fileName   保存するファイル名
         * @param userName   保存するユーザ名
         * @param data       保存するdouble型の1次元配列データ
         * @param context    実行するアクティビティのコンテキスト
         * @return 保存に成功したらtrue，失敗したらfalseを返す
         */
        public static boolean writeSingleArrayData (String folderName, String fileName, String userName, double[] data, Context context)
            {
                // SDカードのマウント確認
                String status = Environment.getExternalStorageState();
                if (!status.equals(Environment.MEDIA_MOUNTED))
                    {
                        // マウントされていない場合
                        Toast.makeText(context, "SDカードがマウントされていません", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                // SDカードのフォルダパスの取得
                String SD_PATH = Environment.getExternalStorageDirectory().getPath();

                // SDカードにフォルダを作成
                String FOLDER_PATH = SD_PATH + File.separator + folderName + File.separator + userName;

                Toast.makeText(context, "FolderPath = " + FOLDER_PATH, Toast.LENGTH_SHORT).show();

                File file = new File(FOLDER_PATH);

                try
                    {
                        if (!file.exists())
                            {
                                // フォルダがない場合
                                Toast.makeText(context, "folderNotExists", Toast.LENGTH_SHORT).show();
                                file.mkdirs();
                            }
                        else
                            {
                                // フォルダがある場合
                                Toast.makeText(context, "folderExists", Toast.LENGTH_SHORT).show();
                            }
                    }
                catch (Exception e)
                    {
                        Toast.makeText(context, "makeFolderException", Toast.LENGTH_SHORT).show();
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                        return false;
                    }

                try
                    {
                        // ファイルパス
                        String filePath = FOLDER_PATH + File.separator + fileName;
                        file = new File(filePath);
                        FileOutputStream fos = new FileOutputStream(file, false);
                        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                        BufferedWriter bw = new BufferedWriter(osw);

                        for (int i = 0; i < 100; i++)
                            {
                                bw.write(data[i] + "\n");
                            }
                        bw.close();
                    }
                catch (Exception e)
                    {
                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                return true;
            }
    }
