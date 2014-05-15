package com.example.motionauth.Registration;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.motionauth.*;
import com.example.motionauth.Lowpass.Fourier;
import com.example.motionauth.Lowpass.MovingAverage;
import com.example.motionauth.Utility.Enum;

import java.io.*;
import java.util.Arrays;


/**
 * モーションを新規登録する
 *
 * @author Kensuke Kousaka
 */
public class RegistMotion extends Activity implements SensorEventListener {
    private static final String TAG = RegistMotion.class.getSimpleName();

    private MovingAverage mMovingAverage = new MovingAverage();
    private Fourier mFourier = new Fourier();
    private Formatter mFormatter = new Formatter();
    private Calc mCalc = new Calc();
    private Enum mEnum = new Enum();

    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;
    private Sensor mGyroscopeSensor;

    // モーションの生データ
    private float vAccel[];
    private float vGyro[];

    private boolean btnStatus = false;

    private static final int TIMEOUT_MESSAGE = 1;

    // データを取得する間隔
    private static final int INTERVAL = 30;

    // データ取得カウント用
    private int accelCount = 0;
    private int gyroCount = 0;
    private int getCount = 0;

    private float accel_tmp[][][] = new float[3][3][100];
    private float gyro_tmp[][][] = new float[3][3][100];

    private double accel[][][] = new double[3][3][100];
    private double gyro[][][] = new double[3][3][100];


    // 移動平均後のデータを格納する配列
    private double moveAverageDistance[][][] = new double[3][3][100];
    private double moveAverageAngle[][][] = new double[3][3][100];

    private double aveMoveAverageDistance[][] = new double[3][100];
    private double aveMoveAverageAngle[][] = new double[3][100];

    private TextView secondTv;
    private TextView countSecondTv;
    private Button getMotionBtn;


    private WriteData mWriteData = new WriteData();
    private Correlation mCorrelation = new Correlation();


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // タイトルバーの非表示
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_regist_motion);

        registMotion();
    }


    /**
     * モーション登録画面にイベントリスナ等を設定する
     */
    private void registMotion () {
        // センササービス，各種センサを取得する
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        TextView nameTv = (TextView) findViewById(R.id.textView2);
        secondTv = (TextView) findViewById(R.id.secondTextView);
        countSecondTv = (TextView) findViewById(R.id.textView4);
        getMotionBtn = (Button) findViewById(R.id.button1);

        nameTv.setText(RegistNameInput.name + "さん読んでね！");

        getMotionBtn.setOnClickListener(new OnClickListener() {
            public void onClick (View v) {
                if (!btnStatus) {
                    // ボタンを押したら，statusをdisableにして押せないようにする
                    btnStatus = true;

                    // ボタンをクリックできないようにする
                    v.setClickable(false);

                    getMotionBtn.setText("取得中");
                    countSecondTv.setText("秒");
                    timeHandler.sendEmptyMessage(TIMEOUT_MESSAGE);
                }
            }
        });
    }


    @Override
    public void onSensorChanged (SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            vAccel = event.values.clone();
        }
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            vGyro = event.values.clone();
        }
    }


    private Handler timeHandler = new Handler() {
        @Override
        public void dispatchMessage (Message msg) {
            Log.d(TAG, "dispatchMessageIn");

            if (msg.what == TIMEOUT_MESSAGE && btnStatus) {
                Log.d(TAG, "ifIn");
                if (accelCount < 100 && gyroCount < 100 && getCount >= 0 && getCount < 3) {
                    // 取得した値を，0.03秒ごとに配列に入れる
                    for (int i = 0; i < 3; i++) {
                        accel_tmp[getCount][i][accelCount] = vAccel[i];
                    }

                    for (int i = 0; i < 3; i++) {
                        gyro_tmp[getCount][i][gyroCount] = vGyro[i];
                    }

                    accelCount++;
                    gyroCount++;

                    if (accelCount == 1) {
                        secondTv.setText("3");
                    }

                    if (accelCount == 33) {
                        secondTv.setText("2");
                    }
                    if (accelCount == 66) {
                        secondTv.setText("1");
                    }

                    // INTERVALで指定したミリ秒後に再度timeHandler（これ自身）を呼び出す
                    timeHandler.sendEmptyMessageDelayed(TIMEOUT_MESSAGE, INTERVAL);
                }
                else if (accelCount >= 100 && gyroCount >= 100 && getCount >= 0 && getCount < 4) {
                    // 取得完了
                    btnStatus = false;
                    getCount++;
                    countSecondTv.setText("回");
                    getMotionBtn.setText("モーションデータ取得");

                    accelCount = 0;
                    gyroCount = 0;

                    // TODO 画面に番号を表示するのではなく，音声で出力させる
                    // 取り終わったら，ボタンのstatusをenableにして押せるようにする
                    if (getCount == 1) {
                        secondTv.setText("2");

                        // ボタンを押せるようにする
                        getMotionBtn.setClickable(true);
                    }
                    if (getCount == 2) {
                        secondTv.setText("1");

                        // ボタンを押せるようにする
                        getMotionBtn.setClickable(true);
                    }

                    if (getCount == 3) {
                        // 全データ取得完了（3回分の加速度，ジャイロを取得完了）
                        // ボタンのstatusをdisableにして押せないようにする
                        if (getMotionBtn.isClickable()) {
                            getMotionBtn.setClickable(false);
                        }
                        secondTv.setText("0");

                        // 生データをアウトプット
                        mWriteData.writeFloatThreeArrayData("RegistRawData", "rawAccelo", RegistNameInput.name, accel_tmp, RegistMotion.this);
                        mWriteData.writeFloatThreeArrayData("RegistRawData", "rawGyro", RegistNameInput.name, gyro_tmp, RegistMotion.this);

// 実験用にコメントアウト
                        // 実験用
                        calc();
/*
                        if (!calc() || !soukan()) {
                            // もう一度モーションを取り直す処理
                            // ボタンのstatusをenableにして押せるようにする
                            getMotionBtn.setClickable(true);
                            // データ取得関係の変数を初期化
                            accelCount = 0;
                            gyroCount = 0;
                            getCount = 0;
                        }
                        else {
                            getMotionBtn.setText("認証登録中");
                            Toast.makeText(RegistMotion.this, "モーションを登録中です", Toast.LENGTH_SHORT).show();

                            // 3回のモーションの平均値をファイルに書き出す
                            mWriteData.writeDoubleTwoArrayData("MotionAuth", "ave_distance", RegistNameInput.name, aveMoveAverageDistance, RegistMotion.this);
                            mWriteData.writeDoubleTwoArrayData("MotionAuth", "ave_angle", RegistNameInput.name, aveMoveAverageAngle, RegistMotion.this);
                        }
*/
// コメントアウト終わり

                    }
                }
                else {
                    Log.d(TAG, "elseIn");
                    super.dispatchMessage(msg);
                }
            }
        }
    };


    /**
     * データ加工，計算処理を行う
     */
    private boolean calc () {
        Log.d(TAG, "calc");

        // データ加工，計算処理
        // データの桁揃え
        accel = mFormatter.floatToDoubleFormatter(accel_tmp);
        gyro = mFormatter.floatToDoubleFormatter(gyro_tmp);

        mWriteData.writeDoubleThreeArrayData("BeforeFFT", "accel", RegistNameInput.name, accel, this);
        mWriteData.writeDoubleThreeArrayData("BeforeFFT", "gyro", RegistNameInput.name, gyro, this);

        double[][][] forMAaccel = new double[accel.length][accel[0].length][accel[0][0].length];
        double[][][] forMAgyro = new double[gyro.length][gyro[0].length][gyro[0][0].length];

        for (int i = 0; i < accel.length; i++) {
            for (int j = 0; j < accel[0].length; j++) {
                forMAaccel[i][j] = Arrays.copyOf(accel[i][j], accel[i][j].length);
                forMAgyro[i][j] = Arrays.copyOf(gyro[i][j], gyro[i][j].length);
            }
        }


        // ローパス処理
//        accel = mMovingAverage.LowpassFilter(accel);
        mMovingAverage.LowpassFilter(forMAaccel, "accel", this);
//        gyro = mMovingAverage.LowpassFilter(gyro);
        mMovingAverage.LowpassFilter(forMAgyro, "gyro", this);

        // 現状は，FFTにより得られた諸データをアウトプットするだけ
        mFourier.LowpassFilter(accel, "accel", this);
        mFourier.LowpassFilter(gyro, "gyro", this);

// 実験用にコメントアウト
/*
        moveAverageDistance = mCalc.accelToDistance(accel, 0.03);
        moveAverageAngle = mCalc.gyroToAngle(gyro, 0.03);

        moveAverageDistance = mFormatter.doubleToDoubleFormatter(moveAverageDistance);
        moveAverageAngle = mFormatter.doubleToDoubleFormatter(moveAverageAngle);

        mWriteData.writeDoubleThreeArrayData("FormatRawData", "formatAccelo", RegistNameInput.name, accel, RegistMotion.this);
        mWriteData.writeDoubleThreeArrayData("FormatRawData", "formatGyro", RegistNameInput.name, gyro, RegistMotion.this);

        // measureCorrelation用の平均値データを作成
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 100; j++) {
                aveMoveAverageDistance[i][j] = (moveAverageDistance[0][i][j] + moveAverageDistance[1][i][j] + moveAverageDistance[2][i][j]) / 3;
                aveMoveAverageAngle[i][j] = (moveAverageAngle[0][i][j] + moveAverageAngle[1][i][j] + moveAverageAngle[2][i][j]) / 3;
            }
        }


        //region 同一のモーションであるかの確認をし，必要に応じてズレ修正を行う
        Enum.MEASURE measure = mCorrelation.measureCorrelation(this, moveAverageDistance, moveAverageAngle, aveMoveAverageDistance, aveMoveAverageAngle);

        if (Enum.MEASURE.BAD == measure) {
            // 相関係数が0.4以下
            Toast.makeText(RegistMotion.this, "同一モーションですか？", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (Enum.MEASURE.INCORRECT == measure || Enum.MEASURE.CORRECT == measure) {
            // 相関係数が0.4よりも高く，0.8以下の場合
            // ズレ修正を行う
            moveAverageDistance = CorrectDeviation.correctDeviation(moveAverageDistance);
            moveAverageAngle = CorrectDeviation.correctDeviation(moveAverageAngle);
        }
        else {
            // なにかがおかしい
            Toast.makeText(RegistMotion.this, "Error", Toast.LENGTH_LONG).show();
            return false;
        }
        //endregion


        // ズレ修正後の平均値データを出す
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 100; j++) {
                aveMoveAverageDistance[i][j] = (moveAverageDistance[0][i][j] + moveAverageDistance[1][i][j] + moveAverageDistance[2][i][j]) / 3;

                aveMoveAverageAngle[i][j] = (moveAverageAngle[0][i][j] + moveAverageAngle[1][i][j] + moveAverageAngle[2][i][j]) / 3;
            }
        }
*/
// コメントアウト終わり
        return true;
    }


    /**
     * 相関係数を導出し，ユーザが入力した3回のモーションの類似性を確認する
     */
    private boolean soukan () {
        Log.d(TAG, "soukan");

        Enum.MEASURE measure = mCorrelation.measureCorrelation(this, moveAverageDistance, moveAverageAngle, aveMoveAverageDistance, aveMoveAverageAngle);

        if (measure == Enum.MEASURE.CORRECT || measure == Enum.MEASURE.PERFECT) {
            return true;
//            getMotionBtn.setText("認証登録中");
//            Toast.makeText(this, "モーションを登録中です", Toast.LENGTH_SHORT).show();
//
//            // 3回のモーションの平均値をファイルに書き出す
////            writeData();
//            mWriteData.writeDoubleTwoArrayData("MotionAuth", "ave_distance", RegistNameInput.name, aveMoveAverageDistance, this);
//            mWriteData.writeDoubleTwoArrayData("MotionAuth", "ave_angle", RegistNameInput.name, aveMoveAverageAngle, this);
        }
        return false;
    }


    /**
     * モーションデータの平均値をSDカードの指定したディレクトリの出力するメソッド
     */
    // TODO WriteDataに任せる（ここに書かない）
    private void writeData () {
        try {
            String filePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "MotionAuth" + File.separator + "MotionAuth" + File.separator + RegistNameInput.name;
            File file = new File(filePath);
            file.getParentFile().mkdir();
            FileOutputStream fos;

            fos = new FileOutputStream(file, false);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter bw = new BufferedWriter(osw);

            // TODO "@"を他の記号で代用できないか調べる
            for (int i = 0; i < 100; i++) {
                bw.write("ave_distance_x@" + aveMoveAverageDistance[0][i] + "\n");
                bw.flush();
            }

            for (int i = 0; i < 100; i++) {
                bw.write("ave_distance_y@" + aveMoveAverageDistance[1][i] + "\n");
                bw.flush();
            }

            for (int i = 0; i < 100; i++) {
                bw.write("ave_distance_z@" + aveMoveAverageDistance[2][i] + "\n");
                bw.flush();
            }

            for (int i = 0; i < 100; i++) {
                bw.write("ave_angle_x@" + aveMoveAverageAngle[0][i] + "\n");
                bw.flush();
            }

            for (int i = 0; i < 100; i++) {
                bw.write("ave_angle_y@" + aveMoveAverageAngle[1][i] + "\n");
                bw.flush();
            }

            for (int i = 0; i < 100; i++) {
                bw.write("ave_angle_z@" + aveMoveAverageAngle[2][i] + "\n");
                bw.flush();
            }

            bw.close();
            fos.close();

            Toast.makeText(this, "finish", Toast.LENGTH_LONG).show();

            finishRegist();
        }
        catch (IOException e) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onAccuracyChanged (Sensor sensor, int accuracy) {

    }


    @Override
    protected void onResume () {
        super.onResume();

        mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mGyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
    }


    @Override
    protected void onPause () {
        super.onPause();

        mSensorManager.unregisterListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.regist_motion, menu);
        return true;
    }


    /**
     * スタート画面に移動するメソッド
     */
    private void finishRegist () {
        Intent intent = new Intent();

        intent.setClassName("com.example.motionauth", "com.example.motionauth.Start");

        startActivityForResult(intent, 0);
        finish();
    }
}
