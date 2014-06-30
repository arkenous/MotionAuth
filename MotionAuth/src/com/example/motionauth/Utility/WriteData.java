package com.example.motionauth.Utility;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.*;


/**
 * データをSDカードに書き込む
 *
 * @author Kensuke Kousaka
 */
public class WriteData {
    private static final String TAG = WriteData.class.getSimpleName();

    /**
     * Float型の三次元配列データをアウトプットする．保存先は，SDカードディレクトリ/folderName/userName/fileName+回数+次元
     *
     * @param folderName 保存するフォルダ名
     * @param dataName   保存するデータ名
     * @param userName   保存するユーザ名
     * @param data       保存するfloat型の3次元配列データ
     * @param context    実行するアクティビティのコンテキスト
     */
    public void writeFloatThreeArrayData (String folderName, String dataName, String userName, float[][][] data, Context context) {
        Log.v(TAG, "--- writeFloatThreeArrayData ---");

        // SDカードのマウント確認
        String status = Environment.getExternalStorageState();
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
            // マウントされていない場合
            Toast.makeText(context, "SDカードがマウントされていません", Toast.LENGTH_SHORT).show();
        }

        // SDカードのフォルダパスの取得
        String SD_PATH = Environment.getExternalStorageDirectory().getPath();

        // SDカードにフォルダを作成
        String FOLDER_PATH = SD_PATH + File.separator + "MotionAuth" + File.separator + folderName + File.separator + userName;

        File file = new File(FOLDER_PATH);

        try {
            if (!file.exists()) {
                // フォルダがない場合
                file.mkdirs();
            }
        } catch (Exception e) {
            Toast.makeText(context, "makeFolderException", Toast.LENGTH_SHORT).show();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        try {
            // 1ファイルでave_distance_y_1@みたいな感じでやる
            String dimension = null;

            for (int i = 0; i < data.length; i++) {
                // X,Y,Zループ
                for (int j = 0; j < data[i].length; j++) {
                    if (j == 0) {
                        dimension = "x";
                    } else if (j == 1) {
                        dimension = "y";
                    } else if (j == 2) {
                        dimension = "z";
                    }

                    // ファイルパス
                    String filePath = FOLDER_PATH + File.separator + dataName + String.valueOf(i) + dimension;
                    file = new File(filePath);

                    // ファイルを追記モードで書き込む
                    FileOutputStream fos = new FileOutputStream(file, true);
                    OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                    BufferedWriter bw = new BufferedWriter(osw);

                    for (int k = 0; k < data[i][j].length; k++) {
                        bw.write(dataName + "_" + dimension + "_" + String.valueOf(i + 1) + "@" + data[i][j][k] + "\n");
                    }
                    bw.close();
                }
            }
        } catch (Exception e) {
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Double型の1次元配列データをアウトプットする． 保存先は，SDカードディレクトリ/folderName/userName/fileName
     *
     * @param folderName 保存するフォルダ名
     * @param dataName   保存するデータ名
     * @param userName   保存するユーザ名
     * @param data       保存するdouble型の1次元配列データ
     * @param context    実行するアクティビティのコンテキスト
     * @return 保存に成功したらtrue，失敗したらfalseを返す
     */
    public boolean writeDoubleOneArrayData (String folderName, String dataName, String userName, double[] data, Context context) {
        Log.v(TAG, "--- writeDoubleOneArrayData ---");

        // SDカードのマウント確認
        String status = Environment.getExternalStorageState();
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
            // マウントされていない場合
            Toast.makeText(context, "SDカードがマウントされていません", Toast.LENGTH_SHORT).show();
            return false;
        }

        // SDカードのフォルダパスの取得
        String SD_PATH = Environment.getExternalStorageDirectory().getPath();

        // SDカードにフォルダを作成
        String FOLDER_PATH = SD_PATH + File.separator + "MotionAuth" + File.separator + folderName + File.separator + userName;

        File file = new File(FOLDER_PATH);

        try {
            if (!file.exists()) {
                // フォルダがない場合
                file.mkdirs();
            }
        } catch (Exception e) {
            Toast.makeText(context, "makeFolderException", Toast.LENGTH_SHORT).show();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return false;
        }

        try {
            // ファイルパス
            String filePath = FOLDER_PATH + File.separator + dataName;
            file = new File(filePath);

            // ファイルを追記モードで書き込む
            FileOutputStream fos = new FileOutputStream(file, true);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter bw = new BufferedWriter(osw);

            for (int i = 0; i < data.length; i++) {
                bw.write(dataName + "@" + data[i] + "\n");
            }
            bw.close();
        } catch (Exception e) {
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    /**
     * Double型の２次元配列データをアウトプットする． 保存先は，SDカードディレクトリ/folderName/userName/fileName+次元
     *
     * @param folderName 保存するフォルダ名
     * @param dataName   保存するデータ名
     * @param userName   保存するユーザ名
     * @param data       保存するdouble型の２次元配列データ
     * @param context    実行するアクティビティのコンテキスト
     * @return 保存に成功したらtrue，失敗したらfalseを返す
     */
    public boolean writeDoubleTwoArrayData (String folderName, String dataName, String userName, double[][] data, Context context) {
        Log.v(TAG, "--- writeDoubleTwoArrayData ---");
        // SDカードのマウント確認
        String status = Environment.getExternalStorageState();
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
            // マウントされていない場合
            Toast.makeText(context, "SDカードがマウントされていません", Toast.LENGTH_SHORT).show();
            return false;
        }

        // SDカードのフォルダパスの取得
        String SD_PATH = Environment.getExternalStorageDirectory().getPath();

        // SDカードにフォルダを作成
        String FOLDER_PATH = SD_PATH + File.separator + "MotionAuth" + File.separator + folderName + File.separator + userName;

        File file = new File(FOLDER_PATH);

        try {
            if (!file.exists()) {
                // フォルダがない場合
                file.mkdirs();
            }
        } catch (Exception e) {
            Toast.makeText(context, "makeFolderException", Toast.LENGTH_SHORT).show();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return false;
        }

        try {
            String dimension = null;

            // X,Y,Zループ
            for (int i = 0; i < data.length; i++) {
                if (i == 0) {
                    dimension = "x";
                } else if (i == 1) {
                    dimension = "y";
                } else if (i == 2) {
                    dimension = "z";
                }

                // ファイルパス
                String filePath = FOLDER_PATH + File.separator + dataName + String.valueOf(i) + dimension;
                file = new File(filePath);

                // ファイルを追記モードで書き込む
                FileOutputStream fos = new FileOutputStream(file, true);
                OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                BufferedWriter bw = new BufferedWriter(osw);

                for (int j = 0; j < data[i].length; j++) {
                    bw.write(dataName + "_" + dimension + "@" + data[i][j] + "\n");
                }
                bw.close();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    /**
     * Double型の三次元配列データをアウトプットする． 保存先は，SDカードディレクトリ/folderName/userName/fileName+回数+次元
     *
     * @param folderName 保存するフォルダ名
     * @param dataName   保存するデータ名
     * @param userName   保存するユーザ名
     * @param data       保存するdouble型の３次元配列データ
     * @param context    実行するアクティビティのコンテキスト
     */
    public void writeDoubleThreeArrayData (String folderName, String dataName, String userName, double[][][] data, Context context) {
        Log.v(TAG, "--- writeDoubleThreeArrayData ---");

        // SDカードのマウント確認
        String status = Environment.getExternalStorageState();
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
            // マウントされていない場合
            Toast.makeText(context, "SDカードがマウントされていません", Toast.LENGTH_SHORT).show();
        }

        // SDカードのフォルダパスの取得
        String SD_PATH = Environment.getExternalStorageDirectory().getPath();

        // SDカードにフォルダを作成
        String FOLDER_PATH = SD_PATH + File.separator + "MotionAuth" + File.separator + folderName + File.separator + userName;

        File file = new File(FOLDER_PATH);

        try {
            if (!file.exists()) {
                // フォルダがない場合
                file.mkdirs();
            }
        } catch (Exception e) {
            Toast.makeText(context, "makeFolderException", Toast.LENGTH_SHORT).show();
        }

        try {
            String dimension = null;

            for (int i = 0; i < data.length; i++) {
                // X,Y,Zループ
                for (int j = 0; j < data[i].length; j++) {
                    if (j == 0) {
                        dimension = "x";
                    } else if (j == 1) {
                        dimension = "y";
                    } else if (j == 2) {
                        dimension = "z";
                    }

                    // ファイルパス
                    String filePath = FOLDER_PATH + File.separator + dataName + String.valueOf(i) + dimension;
                    file = new File(filePath);

                    // ファイルを追記モードで書き込む
                    FileOutputStream fos = new FileOutputStream(file, false);
                    OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                    BufferedWriter bw = new BufferedWriter(osw);

                    for (int k = 0; k < data[0][0].length; k++) {
                        //bw.write(dataName + "_" + dimension + "_" + String.valueOf(i + 1) + "@" + data[i][j][k] + "\n");
                        bw.write(data[i][j][k] + "\n");
                        bw.flush();
                    }
                    bw.close();
                    fos.close();
                }
            }
        } catch (Exception e) {
            Toast.makeText(context, "ErrorWhileWriting", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Double型の２次元配列データをアウトプットする． 保存先は，SDカードディレクトリ/folderName/userName/fileName+次元
     *
     * @param folderName 保存するフォルダ名
     * @param dataName   保存するデータ名
     * @param userName   保存するユーザ名
     * @param data       保存するdouble型の２次元配列データ
     * @param context    実行するアクティビティのコンテキスト
     */
    public void writeRData (String folderName, String dataName, String userName, double[][] data, Context context) {
        Log.v(TAG, "--- writeRData ---");

        // SDカードのマウント確認
        String status = Environment.getExternalStorageState();
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
            // マウントされていない場合
            Toast.makeText(context, "SDカードがマウントされていません", Toast.LENGTH_SHORT).show();
        }

        // SDカードのフォルダパスの取得
        String SD_PATH = Environment.getExternalStorageDirectory().getPath();

        // SDカードにフォルダを作成
        String FOLDER_PATH = SD_PATH + File.separator + "MotionAuth" + File.separator + folderName + File.separator + userName;

        File file = new File(FOLDER_PATH);

        try {
            if (!file.exists()) {
                // フォルダがない場合
                file.mkdirs();
            }
        } catch (Exception e) {
            Toast.makeText(context, "makeFolderException", Toast.LENGTH_SHORT).show();
        }

        try {
            String dimension = null;

            for (int i = 0; i < data.length; i++) {

                // X,Y,Zループ
                for (int j = 0; j < data[i].length; j++) {
                    if (j == 0) {
                        dimension = "x";
                    } else if (j == 1) {
                        dimension = "y";
                    } else if (j == 2) {
                        dimension = "z";
                    }

                    // ファイルパス
                    String filePath = FOLDER_PATH + File.separator + dataName + String.valueOf(i) + dimension;
                    file = new File(filePath);

                    // ファイルを追記モードで書き込む
                    FileOutputStream fos = new FileOutputStream(file, true);
                    OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                    BufferedWriter bw = new BufferedWriter(osw);

                    bw.write(dataName + "_" + dimension + "_" + String.valueOf(i + 1) + "@" + data[i][j] + "\n");
                    bw.close();
                }
            }
        } catch (Exception e) {
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
        }
    }


    public boolean writeRegistedData (String folderName, String userName, double[][] averageDistance, double[][] averageAngle, boolean isAmplify, Context context) {
        Log.v(TAG, "--- writeRegistedData ---");

        try {
            Log.d(TAG, "--- writeRegistedData ---");
            String filePath = Environment.getExternalStorageDirectory() + File.separator + "MotionAuth" + File.separator + folderName + File.separator + userName;
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            FileOutputStream fos;

            fos = new FileOutputStream(file, false);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter bw = new BufferedWriter(osw);

            Log.d(TAG, "*** Preparing is finished ***");

            if (isAmplify) {
                for (int i = 0; i < 100; i++) {
                    bw.write("ave_distance_x@" + averageDistance[0][i] + ":" + "true" + "\n");
                    bw.flush();
                }
                for (int i = 0; i < 100; i++) {
                    bw.write("ave_distance_y@" + averageDistance[1][i] + ":" + "true" + "\n");
                    bw.flush();
                }
                for (int i = 0; i < 100; i++) {
                    bw.write("ave_distance_z@" + averageDistance[2][i] + ":" + "true" + "\n");
                    bw.flush();
                }
                for (int i = 0; i < 100; i++) {
                    bw.write("ave_angle_x@" + averageAngle[0][i] + ":" + "true" + "\n");
                    bw.flush();
                }
                for (int i = 0; i < 100; i++) {
                    bw.write("ave_angle_y@" + averageAngle[1][i] + ":" + "true" + "\n");
                    bw.flush();
                }
                for (int i = 0; i < 100; i++) {
                    bw.write("ave_angle_z@" + averageAngle[2][i] + ":" + "true" + "\n");
                    bw.flush();
                }
            }
            else {
                for (int i = 0; i < 100; i++) {
                    bw.write("ave_distance_x@" + averageDistance[0][i] + ":" + "false" + "\n");
                    bw.flush();
                }
                for (int i = 0; i < 100; i++) {
                    bw.write("ave_distance_y@" + averageDistance[1][i] + ":" + "false" + "\n");
                    bw.flush();
                }
                for (int i = 0; i < 100; i++) {
                    bw.write("ave_distance_z@" + averageDistance[2][i] + ":" + "false" + "\n");
                    bw.flush();
                }
                for (int i = 0; i < 100; i++) {
                    bw.write("ave_angle_x@" + averageAngle[0][i] + ":" + "false" + "\n");
                    bw.flush();
                }
                for (int i = 0; i < 100; i++) {
                    bw.write("ave_angle_y@" + averageAngle[1][i] + ":" + "false" + "\n");
                    bw.flush();
                }
                for (int i = 0; i < 100; i++) {
                    bw.write("ave_angle_z@" + averageAngle[2][i] + ":" + "false" + "\n");
                    bw.flush();
                }
            }

            bw.close();
            fos.close();

            return true;
        } catch (IOException e) {
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
