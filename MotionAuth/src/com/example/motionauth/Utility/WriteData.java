package com.example.motionauth.Utility;

import android.os.Environment;
import android.util.Log;

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
     */
    public void writeFloatThreeArrayData (String folderName, String dataName, String userName, float[][][] data) {
        Log.v(TAG, "--- writeFloatThreeArrayData ---");

        // SDカードのマウント確認
        String status = Environment.getExternalStorageState();
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
            // マウントされていない場合
            Log.e(TAG, "SDCard not mounted");

        }

        // SDカードのフォルダパスの取得
        String SD_PATH = Environment.getExternalStorageDirectory().getPath();

        // SDカードにフォルダを作成
        String FOLDER_PATH = SD_PATH + File.separator + "MotionAuth" + File.separator + folderName + File.separator + userName;

        File file = new File(FOLDER_PATH);

        try {
            if (!file.exists()) {
                // フォルダがない場合
                if (!file.mkdirs()) {
                    Log.e(TAG, "Make Folder Error");
                }
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Make Folder Exception");
        }

        try {
            // 1ファイルでave_distance_y_1@みたいな感じでやる
            String dimension = null;

            for (int i = 0; i < data.length; i++) {
                // X,Y,Zループ
                for (int j = 0; j < data[i].length; j++) {
                    if (j == 0) {
                        dimension = "x";
                    }
                    else if (j == 1) {
                        dimension = "y";
                    }
                    else if (j == 2) {
                        dimension = "z";
                    }

                    // ファイルパス
                    String filePath = FOLDER_PATH + File.separator + dataName + String.valueOf(i) + dimension;
                    file = new File(filePath);

                    // ファイルを追記モードで書き込む
                    FileOutputStream fos = new FileOutputStream(file, false);
                    OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                    BufferedWriter bw = new BufferedWriter(osw);

                    for (int k = 0; k < data[i][j].length; k++) {
                        bw.write(dataName + "_" + dimension + "_" + String.valueOf(i + 1) + "@" + data[i][j][k] + "\n");
                    }
                    bw.close();
                    osw.close();
                    fos.close();
                }
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Error");
        }
    }


    /**
     * Double型の２次元配列データをアウトプットする． 保存先は，SDカードディレクトリ/folderName/userName/fileName+次元
     *
     * @param folderName 保存するフォルダ名
     * @param dataName   保存するデータ名
     * @param userName   保存するユーザ名
     * @param data       保存するdouble型の２次元配列データ
     * @return 保存に成功したらtrue，失敗したらfalseを返す
     */
    public boolean writeDoubleTwoArrayData (String folderName, String dataName, String userName, double[][] data) {
        Log.v(TAG, "--- writeDoubleTwoArrayData ---");
        // SDカードのマウント確認
        String status = Environment.getExternalStorageState();
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
            // マウントされていない場合
            Log.e(TAG, "SDCard not mounted");
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
                if (!file.mkdirs()) {
                    Log.e(TAG, "Make Folder Error");
                }
            }
        }
        catch (Exception e) {
            Log.e(TAG, "MakeFolderException");
            e.printStackTrace();
            return false;
        }

        try {
            String dimension = null;

            // X,Y,Zループ
            for (int i = 0; i < data.length; i++) {
                if (i == 0) {
                    dimension = "x";
                }
                else if (i == 1) {
                    dimension = "y";
                }
                else if (i == 2) {
                    dimension = "z";
                }

                // ファイルパス
                String filePath = FOLDER_PATH + File.separator + dataName + String.valueOf(i) + dimension;
                file = new File(filePath);

                // ファイルを追記モードで書き込む
                FileOutputStream fos = new FileOutputStream(file, false);
                OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                BufferedWriter bw = new BufferedWriter(osw);

                for (int j = 0; j < data[i].length; j++) {
                    bw.write(dataName + "_" + dimension + "@" + data[i][j] + "\n");
                }
                bw.close();
                osw.close();
                fos.close();
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Error");
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
     */
    public void writeDoubleThreeArrayData (String folderName, String dataName, String userName, double[][][] data) {
        Log.v(TAG, "--- writeDoubleThreeArrayData ---");

        // SDカードのマウント確認
        String status = Environment.getExternalStorageState();
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
            // マウントされていない場合
            Log.e(TAG, "SDCard not mounted");
        }

        // SDカードのフォルダパスの取得
        String SD_PATH = Environment.getExternalStorageDirectory().getPath();

        // SDカードにフォルダを作成
        String FOLDER_PATH = SD_PATH + File.separator + "MotionAuth" + File.separator + folderName + File.separator + userName;

        File file = new File(FOLDER_PATH);

        try {
            if (!file.exists()) {
                // フォルダがない場合
                if (!file.mkdirs()) {
                    Log.e(TAG, "Make Folder Error");
                }
            }
        }
        catch (Exception e) {
            Log.e(TAG, "MakeFolderException");
        }

        try {
            String dimension = null;

            for (int i = 0; i < data.length; i++) {
                // X,Y,Zループ
                for (int j = 0; j < data[i].length; j++) {
                    if (j == 0) {
                        dimension = "x";
                    }
                    else if (j == 1) {
                        dimension = "y";
                    }
                    else if (j == 2) {
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
                    osw.close();
                    fos.close();
                }
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Error");
        }
    }


    /**
     * Double型の２次元配列データをアウトプットする． 保存先は，SDカードディレクトリ/folderName/userName/fileName+次元
     *
     * @param folderName 保存するフォルダ名
     * @param dataName   保存するデータ名
     * @param userName   保存するユーザ名
     * @param data       保存するdouble型の２次元配列データ
     */
    public void writeRData (String folderName, String dataName, String userName, double[][] data) {
        Log.v(TAG, "--- writeRData ---");

        // SDカードのマウント確認
        String status = Environment.getExternalStorageState();
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
            // マウントされていない場合
            Log.e(TAG, "SDCard not mounted");
        }

        // SDカードのフォルダパスの取得
        String SD_PATH = Environment.getExternalStorageDirectory().getPath();

        // SDカードにフォルダを作成
        String FOLDER_PATH = SD_PATH + File.separator + "MotionAuth" + File.separator + folderName + File.separator + userName;

        File file = new File(FOLDER_PATH);

        try {
            if (!file.exists()) {
                // フォルダがない場合
                if (!file.mkdirs()) {
                    Log.e(TAG, "Make Folder Error");
                }
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Make Folder Exception");
        }

        try {
            String dimension = null;

            for (int i = 0; i < data.length; i++) {

                // X,Y,Zループ
                for (int j = 0; j < data[i].length; j++) {
                    if (j == 0) {
                        dimension = "x";
                    }
                    else if (j == 1) {
                        dimension = "y";
                    }
                    else if (j == 2) {
                        dimension = "z";
                    }

                    // ファイルパス
                    String filePath = FOLDER_PATH + File.separator + dataName + String.valueOf(i) + dimension;
                    file = new File(filePath);

                    // ファイルを追記モードで書き込む
                    FileOutputStream fos = new FileOutputStream(file, false);
                    OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                    BufferedWriter bw = new BufferedWriter(osw);

                    bw.write(dataName + "_" + dimension + "_" + String.valueOf(i + 1) + "@" + data[i][j] + "\n");
                    bw.close();
                    osw.close();
                    fos.close();
                }
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Error");
        }
    }


    /**
     * Double型の２次元配列データをアウトプットする． 保存先は，SDカードディレクトリ/MotionAuth/folderName/userName
     *
     * @param folderName 保存するフォルダ名
     * @param userName   保存するユーザ名
     * @param R_accel    保存する1次元double型配列の加速度Rデータ
     * @param R_gyro     保存する1次元double型配列の角速度Rデータ
     */
    public void writeRData (String folderName, String userName, double[] R_accel, double[] R_gyro) {
        Log.v(TAG, "--- writeRData ---");

        // SDカードのマウント確認
        String status = Environment.getExternalStorageState();
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
            // マウントされていない場合
            Log.e(TAG, "SDCard not mounted");
        }

        // SDカードのフォルダパスの取得
        String SD_PATH = Environment.getExternalStorageDirectory().getPath();

        // SDカードにフォルダを作成
        String FOLDER_PATH = SD_PATH + File.separator + "MotionAuth" + File.separator + folderName;

        File file = new File(FOLDER_PATH);

        try {
            if (!file.exists()) {
                // フォルダがない場合
                if (!file.mkdirs()) {
                    Log.e(TAG, "Make Folder Error");
                }
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Make Folder Exception");
        }

        try {
            // ファイルパス
            String filePath = FOLDER_PATH + File.separator + userName;
            file = new File(filePath);

            // ファイルを追記モードで書き込む
            FileOutputStream fos = new FileOutputStream(file, false);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter bw = new BufferedWriter(osw);

            for (int i = 0; i < 2; i++) {
                if (i == 0) {
                    for (int j = 0; j < 3; j++) {
                        if (j == 0) {
                            bw.write("R_accel_x@" + R_accel[j] + "\n");
                            bw.flush();
                        }
                        if (j == 1) {
                            bw.write("R_accel_y@" + R_accel[j] + "\n");
                            bw.flush();
                        }
                        if (j == 2) {
                            bw.write("R_accel_z@" + R_accel[j] + "\n");
                            bw.flush();
                        }
                    }
                }
                else if (i == 1) {
                    for (int j = 0; j < 3; j++) {
                        if (j == 0) {
                            bw.write("R_gyro_x@" + R_gyro[j] + "\n");
                            bw.flush();
                        }
                        if (j == 1) {
                            bw.write("R_gyro_y@" + R_gyro[j] + "\n");
                            bw.flush();
                        }
                        if (j == 2) {
                            bw.write("R_gyro_z@" + R_gyro[j] + "\n");
                            bw.flush();
                        }
                    }
                }
            }
            bw.close();
            osw.close();
            fos.close();
        }
        catch (Exception e) {
            Log.e(TAG, "Error");
        }
    }


    /**
     * RegistMotionより渡された，認証のキーとなるデータをアウトプットする
     *
     * @param folderName      保存するフォルダ名
     * @param userName        保存するユーザ名
     * @param averageDistance 保存する距離データ
     * @param averageAngle    保存する角度データ
     * @param isAmplify       データ増幅フラグ
     * @return 保存できたらtrue，失敗したらfalseを返す
     */
    //TODO データ保存時に暗号化処理を行う
    // 受け取ったデータをCipherクラスに渡し，暗号化されたデータを保存する
    public boolean writeRegistedData (String folderName, String userName, double[][] averageDistance, double[][] averageAngle, boolean isAmplify) {
        Log.v(TAG, "--- writeRegistedData ---");

        try {
            Log.d(TAG, "--- writeRegistedData ---");
            String filePath = Environment.getExternalStorageDirectory() + File.separator + "MotionAuth" + File.separator + folderName + File.separator + userName;
            File file = new File(filePath);
            if (!file.getParentFile().mkdirs()) {
                Log.e(TAG, "Make Folder Error");
            }
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
            osw.close();
            fos.close();

            return true;
        }
        catch (IOException e) {
            Log.e(TAG, "Error");
            return false;
        }
    }
}
