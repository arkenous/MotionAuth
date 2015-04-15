package com.example.motionauth.Utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import com.example.motionauth.Processing.CipherCrypt;

import java.io.*;
import java.util.ArrayList;


/**
 * データをSDカードに書き込む
 *
 * @author Kensuke Kousaka
 */
public class ManageData {
    private static final String APP_NAME = "MotionAuth";
    private FileOutputStream fos;
    private OutputStreamWriter osw;
    private BufferedWriter bw;
    
    /**
     * Float型の三次元配列データをアウトプットする．保存先は，SDカードディレクトリ/folderName/userName/fileName+回数+次元
     *
     * @param folderName 保存するフォルダ名
     * @param dataName   保存するデータ名
     * @param userName   保存するユーザ名
     * @param data       保存するfloat型の3次元配列データ
     */
    public void writeFloatThreeArrayData (String folderName, String dataName, String userName, float[][][] data) {
        LogUtil.log (Log.INFO);

        // SDカードのマウント確認
        String status = Environment.getExternalStorageState ();

        // マウントされていない場合
        if (!status.equals (Environment.MEDIA_MOUNTED)) {
            LogUtil.log (Log.ERROR, "SDCard not mounted");
            return;
        }

        // SDカードのフォルダパスの取得
        String SD_PATH = Environment.getExternalStorageDirectory ().getPath ();

        // SDカードにフォルダを作成
        String FOLDER_PATH = SD_PATH + File.separator + APP_NAME + File.separator + folderName + File.separator + userName;

        File file = new File (FOLDER_PATH);

        try {
            if (!file.exists ()) {
                // フォルダがない場合
                if (!file.mkdirs ()) {
                    LogUtil.log (Log.DEBUG, "Make directory error");
                }
            }
        } catch (Exception e) {
            LogUtil.log (Log.ERROR, e.getMessage (), e.getCause ());
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
                    String filePath = FOLDER_PATH + File.separator + dataName + String.valueOf (i) + dimension;
                    file = new File (filePath);

                    // ファイルを追記モードで書き込む
                    fos = new FileOutputStream(file, false);
                    osw = new OutputStreamWriter(fos, "UTF-8");
                    bw = new BufferedWriter(osw);

                    for (int k = 0; k < data[i][j].length; k++) {
                        bw.write (dataName + "_" + dimension + "_" + String.valueOf (i + 1) + "@" + data[i][j][k] + "\n");
                    }
                    bw.close ();
                    osw.close ();
                    fos.close ();
                }
            }
        } catch (Exception e) {
            LogUtil.log (Log.ERROR, e.getMessage (), e.getCause ());
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
        LogUtil.log (Log.INFO);
        // SDカードのマウント確認
        String status = Environment.getExternalStorageState ();
        if (!status.equals (Environment.MEDIA_MOUNTED)) {
            // マウントされていない場合
            LogUtil.log (Log.ERROR, "SDCard not mounted");
            return false;
        }

        // SDカードのフォルダパスの取得
        String SD_PATH = Environment.getExternalStorageDirectory ().getPath ();

        // SDカードにフォルダを作成
        String FOLDER_PATH = SD_PATH + File.separator + APP_NAME + File.separator + folderName + File.separator + userName;

        File file = new File (FOLDER_PATH);

        try {
            if (!file.exists ()) {
                // フォルダがない場合
                if (!file.mkdirs ()) {
                    LogUtil.log (Log.ERROR, "Make directory error");
                }
            }
        } catch (Exception e) {
            LogUtil.log (Log.ERROR, e.getMessage (), e.getCause ());
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
                String filePath = FOLDER_PATH + File.separator + dataName + String.valueOf (i) + dimension;
                file = new File (filePath);

                // ファイルを追記モードで書き込む
                fos = new FileOutputStream(file, false);
                osw = new OutputStreamWriter(fos, "UTF-8");
                bw = new BufferedWriter(osw);

                for (int j = 0; j < data[i].length; j++) {
                    bw.write (dataName + "_" + dimension + "@" + data[i][j] + "\n");
                }
                bw.close ();
                osw.close ();
                fos.close ();
            }
        } catch (Exception e) {
            LogUtil.log (Log.ERROR, e.getMessage (), e.getCause ());
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
        LogUtil.log (Log.INFO);

        // SDカードのマウント確認
        String status = Environment.getExternalStorageState ();
        if (!status.equals (Environment.MEDIA_MOUNTED)) {
            // マウントされていない場合
            LogUtil.log (Log.ERROR, "SDCard not mounted");
        }

        // SDカードのフォルダパスの取得
        String SD_PATH = Environment.getExternalStorageDirectory ().getPath ();

        // SDカードにフォルダを作成
        String FOLDER_PATH = SD_PATH + File.separator + APP_NAME + File.separator + folderName + File.separator + userName;

        File file = new File (FOLDER_PATH);

        try {
            if (!file.exists ()) {
                // フォルダがない場合
                if (!file.mkdirs ()) {
                    LogUtil.log (Log.ERROR, "Make directory Error");
                }
            }
        } catch (Exception e) {
            LogUtil.log (Log.ERROR, e.getMessage (), e.getCause ());
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
                    String filePath = FOLDER_PATH + File.separator + dataName + String.valueOf (i) + dimension;
                    file = new File (filePath);

                    // ファイルを追記モードで書き込む
                    fos = new FileOutputStream(file, false);
                    osw = new OutputStreamWriter(fos, "UTF-8");
                    bw = new BufferedWriter(osw);

                    for (int k = 0; k < data[0][0].length; k++) {
                        //bw.write(dataName + "_" + dimension + "_" + String.valueOf(i + 1) + "@" + data[i][j][k] + "\n");
                        bw.write (data[i][j][k] + "\n");
                        bw.flush ();
                    }
                    bw.close ();
                    osw.close ();
                    fos.close ();
                }
            }
        } catch (Exception e) {
            LogUtil.log (Log.ERROR, e.getMessage (), e.getCause ());
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
        LogUtil.log (Log.INFO);

        // SDカードのマウント確認
        String status = Environment.getExternalStorageState ();
        if (!status.equals (Environment.MEDIA_MOUNTED)) {
            // マウントされていない場合
            LogUtil.log (Log.ERROR, "SDCard not mounted");
        }

        // SDカードのフォルダパスの取得
        String SD_PATH = Environment.getExternalStorageDirectory ().getPath ();

        // SDカードにフォルダを作成
        String FOLDER_PATH = SD_PATH + File.separator + APP_NAME + File.separator + folderName + File.separator + userName;

        File file = new File (FOLDER_PATH);

        try {
            if (!file.exists ()) {
                // フォルダがない場合
                if (!file.mkdirs ()) {
                    LogUtil.log (Log.ERROR, "Make directory error");
                }
            }
        } catch (Exception e) {
            LogUtil.log (Log.ERROR, e.getMessage (), e.getCause ());
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
                    String filePath = FOLDER_PATH + File.separator + dataName + String.valueOf (i) + dimension;
                    file = new File (filePath);

                    // ファイルを追記モードで書き込む
                    fos = new FileOutputStream(file, false);
                    osw = new OutputStreamWriter(fos, "UTF-8");
                    bw = new BufferedWriter(osw);

                    bw.write (dataName + "_" + dimension + "_" + String.valueOf (i + 1) + "@" + data[i][j] + "\n");
                    bw.close ();
                    osw.close ();
                    fos.close ();
                }
            }
        } catch (Exception e) {
            LogUtil.log (Log.ERROR, e.getMessage (), e.getCause ());
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
        LogUtil.log (Log.INFO);

        // SDカードのマウント確認
        String status = Environment.getExternalStorageState ();
        if (!status.equals (Environment.MEDIA_MOUNTED)) {
            // マウントされていない場合
            LogUtil.log (Log.ERROR, "SDCard not mounted");
        }

        // SDカードのフォルダパスの取得
        String SD_PATH = Environment.getExternalStorageDirectory ().getPath ();

        // SDカードにフォルダを作成
        String FOLDER_PATH = SD_PATH + File.separator + APP_NAME + File.separator + folderName;

        File file = new File (FOLDER_PATH);

        try {
            if (!file.exists ()) {
                // フォルダがない場合
                if (!file.mkdirs ()) {
                    LogUtil.log (Log.ERROR, "Make directory error");
                }
            }
        } catch (Exception e) {
            LogUtil.log (Log.ERROR, e.getMessage (), e.getCause ());
        }

        try {
            // ファイルパス
            String filePath = FOLDER_PATH + File.separator + userName;
            file = new File (filePath);

            // ファイルを追記モードで書き込む
            fos = new FileOutputStream(file, false);
            osw = new OutputStreamWriter(fos, "UTF-8");
            bw = new BufferedWriter(osw);

            for (int i = 0; i < 2; i++) {
                if (i == 0) {
                    for (int j = 0; j < 3; j++) {
                        if (j == 0) {
                            bw.write ("R_accel_x@" + R_accel[j] + "\n");
                            bw.flush ();
                        }
                        if (j == 1) {
                            bw.write ("R_accel_y@" + R_accel[j] + "\n");
                            bw.flush ();
                        }
                        if (j == 2) {
                            bw.write ("R_accel_z@" + R_accel[j] + "\n");
                            bw.flush ();
                        }
                    }
                } else if (i == 1) {
                    for (int j = 0; j < 3; j++) {
                        if (j == 0) {
                            bw.write ("R_gyro_x@" + R_gyro[j] + "\n");
                            bw.flush ();
                        }
                        if (j == 1) {
                            bw.write ("R_gyro_y@" + R_gyro[j] + "\n");
                            bw.flush ();
                        }
                        if (j == 2) {
                            bw.write ("R_gyro_z@" + R_gyro[j] + "\n");
                            bw.flush ();
                        }
                    }
                }
            }
            bw.close ();
            osw.close ();
            fos.close ();
        } catch (Exception e) {
            LogUtil.log (Log.ERROR, e.getMessage (), e.getCause ());
        }
    }

    // 実験用．新規登録モードにおける登録データをSDカードに保存する
    public void writeRegistedDataToSd (String folderName, String userName, double[][] averageDistance, double[][] averageAngle) {
        LogUtil.log (Log.INFO);

        // SDカードのマウント確認
        String status = Environment.getExternalStorageState ();
        if (!status.equals (Environment.MEDIA_MOUNTED)) {
            LogUtil.log (Log.ERROR, "SDCard not mounted");
        }

        String SD_PATH = Environment.getExternalStorageDirectory ().getPath ();

        String FOLDER_PATH = SD_PATH + File.separator + APP_NAME + File.separator + folderName;

        File file = new File (FOLDER_PATH);

        try {
            if (!file.exists ()) {
                if (!file.mkdirs ()) {
                    LogUtil.log (Log.ERROR, "Make directory error");
                }
            }
        } catch (Exception e) {
            LogUtil.log (Log.ERROR, e.getMessage (), e.getCause ());
        }

        try {
            String filePath = FOLDER_PATH + File.separator + userName;
            file = new File (filePath);

            fos = new FileOutputStream(file, false);
            osw = new OutputStreamWriter(fos, "UTF-8");
            bw = new BufferedWriter(osw);

            // 距離データ書き込み
            for (int i = 0; i < averageDistance.length; i++) {
                for (int j = 0; j < averageDistance[i].length; j++) {
                    switch (i) {
                        case 0:
                            // dimention x
                            bw.write (String.valueOf (averageDistance[i][j]) + "\n");
                            break;
                        case 1:
                            // dimention y
                            bw.write (String.valueOf (averageDistance[i][j]) + "\n");
                            break;
                        case 2:
                            // dimention z
                            bw.write (String.valueOf (averageDistance[i][j]) + "\n");
                            break;
                    }
                }
            }


            // 角度データ書き込み
            for (int i = 0; i < averageAngle.length; i++) {
                for (int j = 0; j < averageAngle[i].length; j++) {
                    switch (i) {
                        case 0:
                            // dimention x
                            bw.write (String.valueOf (averageAngle[i][j]) + "\n");
                            break;
                        case 1:
                            // dimention y
                            bw.write (String.valueOf (averageAngle[i][j]) + "\n");
                            break;
                        case 2:
                            // dimention z
                            bw.write (String.valueOf (averageAngle[i][j]) + "\n");
                            break;
                    }
                }
            }

            bw.close ();
            osw.close();
            fos.close();
        } catch (Exception e) {
            LogUtil.log (Log.ERROR, e.getMessage (), e.getCause ());
        }
    }


    public void writeRpoint (String folderName, String userName, double data) {
        LogUtil.log (Log.INFO);

        // SDカードのマウント確認
        String status = Environment.getExternalStorageState ();
        if (!status.equals (Environment.MEDIA_MOUNTED)) {
            LogUtil.log (Log.ERROR, "SDCard not mounted");
        }

        String SD_PATH = Environment.getExternalStorageDirectory ().getPath ();

        String FOLDER_PATH = SD_PATH + File.separator + APP_NAME + File.separator + folderName;

        File file = new File (FOLDER_PATH);

        try {
            if (!file.exists ()) {
                if (!file.mkdirs ()) {
                    LogUtil.log (Log.ERROR, "Make directory error");
                }
            }
        } catch (Exception e) {
            LogUtil.log (Log.ERROR, e.getMessage (), e.getCause ());
        }

        try {
            String filePath = FOLDER_PATH + File.separator + userName;
            file = new File (filePath);

            fos = new FileOutputStream(file, false);
            osw = new OutputStreamWriter(fos);
            bw = new BufferedWriter(osw);

            bw.write(String.valueOf(data));

            bw.close();
            osw.close();
            fos.close();
        } catch (Exception e) {
            LogUtil.log (Log.ERROR, e.getMessage (), e.getCause ());
        }
    }


    /**
     * RegistMotionより渡された，認証のキーとなるデータをアウトプットする
     *
     * @param userName        保存するユーザ名
     * @param averageDistance 保存する距離データ
     * @param averageAngle    保存する角度データ
     * @param ampValue        データ増幅値
     * @param context         呼び出し元のコンテキスト
     */
    // 受け取ったデータをCipherクラスに渡し，暗号化されたデータを保存する
    //public void writeRegistedData (String userName, double[][] averageDistance, double[][] averageAngle, boolean isAmplify, Context context) {
    public void writeRegistedData (String userName, double[][] averageDistance, double[][] averageAngle, double ampValue, Context context) {

        LogUtil.log (Log.INFO);

        // 暗号処理を担うオブジェクトを生成
        CipherCrypt mCipherCrypt = new CipherCrypt (context);

        String[][] averageDistanceStr = new String[averageDistance.length][averageDistance[0].length];
        String[][] averageAngleStr = new String[averageAngle.length][averageAngle[0].length];

        // 暗号化処理
        // double型二次元配列で受け取ったデータをString型二次元配列に変換する
        for (int i = 0; i < averageDistance.length; i++) {
            for (int j = 0; j < averageDistance[i].length; j++) {
                averageDistanceStr[i][j] = String.valueOf (averageDistance[i][j]);
                averageAngleStr[i][j] = String.valueOf (averageAngle[i][j]);
            }
        }

        // 暗号化
        String[][] encryptedAvarageDistanceStr = mCipherCrypt.encrypt (averageDistanceStr);
        String[][] encryptedAverageAngleStr = mCipherCrypt.encrypt (averageAngleStr);

        // 配列データを特定文字列を挟んで連結する
        ConvertArrayAndString mConvertArrayAndString = new ConvertArrayAndString ();
        String registDistanceData = mConvertArrayAndString.arrayToString (encryptedAvarageDistanceStr);
        String registAngleData = mConvertArrayAndString.arrayToString (encryptedAverageAngleStr);

        Context mContext = context.getApplicationContext ();
        SharedPreferences userPref = mContext.getSharedPreferences ("UserList", Context.MODE_PRIVATE);
        SharedPreferences.Editor userPrefEditor = userPref.edit ();

        userPrefEditor.putString (userName, "");
        userPrefEditor.apply ();

        SharedPreferences preferences = mContext.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit ();

        editor.putString (userName + "distance", registDistanceData);
        editor.putString (userName + "angle", registAngleData);
        editor.putString (userName + "amplify", String.valueOf (ampValue));
        editor.apply ();
    }


    /**
     * SharedPreferencesに保存されたデータを読み取るクラス
     *
     * @param context  アプリケーション固有のプリファレンスを取得する際に必要となるコンテキスト
     * @param userName 読み取るユーザ名
     * @return 読み取ったdouble型二次元配列データ
     */
    public ArrayList<double[][]> readRegistedData (Context context, String userName) {
        LogUtil.log (Log.INFO);
        Context mContext = context.getApplicationContext ();

        SharedPreferences preferences = mContext.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);

        String registedDistanceData = preferences.getString (userName + "distance", "");
        String registedAngleData = preferences.getString (userName + "angle", "");

        if ("".equals (registedDistanceData)) throw new RuntimeException ();

        ConvertArrayAndString mConvertArrayAndString = new ConvertArrayAndString ();
        CipherCrypt mCipherCrypt = new CipherCrypt (context);

        String[][] decryptedDistance = mCipherCrypt.decrypt (mConvertArrayAndString.stringToArray (registedDistanceData));
        String[][] decryptedAngle = mCipherCrypt.decrypt (mConvertArrayAndString.stringToArray (registedAngleData));

        double[][] distance = new double[3][100], angle = new double[3][100];

        for (int i = 0; i < decryptedDistance.length; i++) {
            for (int j = 0; j < decryptedDistance[i].length; j++) {
                distance[i][j] = Double.valueOf (decryptedDistance[i][j]);
                angle[i][j] = Double.valueOf (decryptedAngle[i][j]);
            }
        }

        ArrayList<double[][]> result = new ArrayList<> ();
        result.add (distance);
        result.add (angle);

        return result;
    }


    /**
     * Float型3次元リストデータをSD_PATH/MotionAuth/dir/user/type+回数+次元で保存する
     *
     * @param dir  任意のディレクトリ名
     * @param user ユーザ名
     * @param type 取得元のセンサ名
     * @param data Float型3次元リストデータ
     */
    public void writeThreeDimenList(String dir, String user, String type, ArrayList<ArrayList<ArrayList<Float>>> data) {
        LogUtil.log(Log.INFO);

        // SDカードのマウント確認

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            LogUtil.log(Log.ERROR, "SD-Card not mounted");
            return;
        }

        // SDカードのディレクトリパスを取得
        String SD_PATH = Environment.getExternalStorageDirectory().getPath();

        // SDカードにディレクトリを作成
        String DIR_PATH = SD_PATH + File.separator + APP_NAME + File.separator + dir + File.separator + user;
        File file = new File(DIR_PATH);
        try {
            if (!file.exists()) {
                // ディレクトリが存在しない場合
                if (!file.mkdirs()) {
                    LogUtil.log(Log.DEBUG, "Make directory Error");
                }
            }
        } catch (Exception e) {
            LogUtil.log(Log.ERROR, e.getMessage(), e.getCause());
        }

        try {
            String dimension;

            for (int time = 0; time < data.size(); time++) {
                for (int dimen = 0; dimen < data.get(time).size(); dimen++) {
                    if (dimen == 0) {
                        dimension = "x";
                    } else if (dimen == 1) {
                        dimension = "y";
                    } else if (dimen == 2) {
                        dimension = "z";
                    } else {
                        dimension = "";
                    }

                    // ファイルパスを指定
                    String filePath = DIR_PATH + File.separator + type + String.valueOf(time) + dimension;
                    file = new File(filePath);

                    // ファイルを書き込む
                    fos = new FileOutputStream(file, false);
                    osw = new OutputStreamWriter(fos, "UTF-8");
                    bw = new BufferedWriter(osw);

                    for (int item = 0; item < data.get(time).get(dimen).size(); item++) {
                        bw.write(type + "_" + dimension + "_" + String.valueOf(time + 1)
                                + "@" + data.get(time).get(dimen).get(item) + "\n");
                    }

                    bw.close();
                    osw.close();
                    fos.close();
                }
            }
        } catch (IOException e) {
            LogUtil.log(Log.ERROR, e.getMessage(), e.getCause());
        }
    }
}
