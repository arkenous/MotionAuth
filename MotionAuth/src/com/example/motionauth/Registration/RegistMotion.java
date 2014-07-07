package com.example.motionauth.Registration;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import com.example.motionauth.Lowpass.Fourier;
import com.example.motionauth.Processing.*;
import com.example.motionauth.R;
import com.example.motionauth.Utility.Enum;
import com.example.motionauth.Utility.WriteData;


/**
 * モーションを新規登録する
 *
 * @author Kensuke Kousaka
 */
public class RegistMotion extends Activity implements SensorEventListener, Runnable {
    private static final String TAG = RegistMotion.class.getSimpleName();

    private static final int VIBRATOR_SHORT  = 40;
    private static final int VIBRATOR_NORMAL = 50;
    private static final int VIBRATOR_LONG   = 80;

    private static final int PREPARATION = 1;
    private static final int GET_MOTION  = 2;

    private static final int PREPARATION_INTERVAL = 1000;
    private static final int GET_MOTION_INTERVAL  = 30;

    private static final int FINISH = 5;

    private SensorManager mSensorManager;
    private Sensor        mAccelerometerSensor;
    private Sensor        mGyroscopeSensor;

    private Vibrator mVibrator;

    private TextView secondTv;
    private TextView countSecondTv;
    private Button   getMotionBtn;

    private Fourier     mFourier     = new Fourier();
    private Formatter   mFormatter   = new Formatter();
    private Calc        mCalc        = new Calc();
    private Amplifier   mAmplifier   = new Amplifier();
    private WriteData   mWriteData   = new WriteData();
    private Correlation mCorrelation = new Correlation();

    // データ取得カウント用
    private int accelCount = 0;
    private int gyroCount  = 0;
    private int getCount   = 0;

    private int prepareCount = 0;

    private boolean btnStatus = false;

    private boolean isAmplified = false;

    // モーションの生データ
    private float[] vAccel;
    private float[] vGyro;

    private float[][][] accelFloat = new float[3][3][100];
    private float[][][] gyroFloat  = new float[3][3][100];

    // 移動平均後のデータを格納する配列
    private double[][][] distance = new double[3][3][100];
    private double[][][] angle    = new double[3][3][100];

    private double[][] averageDistance = new double[3][100];
    private double[][] averageAngle    = new double[3][100];


    // 計算処理のスレッド化に関する変数
    private boolean resultCalc   = false;
    private boolean resultSoukan = false;

    private ProgressDialog progressDialog;
    private Thread         thread;


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v(TAG, "--- onCreate ---");

        // タイトルバーの非表示
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_regist_motion);

        registMotion();
    }


    /**
     * モーション登録画面にイベントリスナ等を設定する
     */
    private void registMotion () {
        Log.v(TAG, "--- registMotion ---");

        // センササービス，各種センサを取得する
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        TextView nameTv = (TextView) findViewById(R.id.textView2);
        secondTv = (TextView) findViewById(R.id.secondTextView);
        countSecondTv = (TextView) findViewById(R.id.textView4);
        getMotionBtn = (Button) findViewById(R.id.button1);

        nameTv.setText(RegistNameInput.name + "さん読んでね！");

        getMotionBtn.setOnClickListener(new OnClickListener() {
            public void onClick (View v) {
                Log.i(TAG, "Click Get Motion Button");
                if (!btnStatus) {
                    // ボタンを押したら，statusをfalseにして押せないようにする
                    btnStatus = true;

                    // ボタンをクリックできないようにする
                    v.setClickable(false);

                    getMotionBtn.setText("インターバル中");
                    countSecondTv.setText("秒");
                    timeHandler.sendEmptyMessage(PREPARATION);
                }
            }
        });

        Log.e(TAG, "aaaaa");
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


    /**
     * 一定時間ごとにモーションデータを取得し配列に格納するハンドラ
     * 計算処理や相関係数取得関数の呼び出しもここで行う
     */
    private Handler timeHandler = new Handler() {
        @Override
        public void dispatchMessage (Message msg) {
            Log.i(TAG, "--- dispatchMessage ---");

            if (msg.what == PREPARATION && btnStatus) {
                if (prepareCount == 0) {
                    secondTv.setText("3");
                    mVibrator.vibrate(VIBRATOR_SHORT);
                    timeHandler.sendEmptyMessageDelayed(PREPARATION, PREPARATION_INTERVAL);

                }
                else if (prepareCount == 1) {
                    secondTv.setText("2");
                    mVibrator.vibrate(VIBRATOR_SHORT);
                    timeHandler.sendEmptyMessageDelayed(PREPARATION, PREPARATION_INTERVAL);
                }
                else if (prepareCount == 2) {
                    secondTv.setText("1");
                    mVibrator.vibrate(VIBRATOR_SHORT);
                    timeHandler.sendEmptyMessageDelayed(PREPARATION, PREPARATION_INTERVAL);
                }
                else if (prepareCount == 3) {
                    secondTv.setText("START");
                    mVibrator.vibrate(VIBRATOR_LONG);
                    timeHandler.sendEmptyMessage(GET_MOTION);
                    getMotionBtn.setText("取得中");
                }

                prepareCount++;
            }
            else if (msg.what == GET_MOTION && btnStatus) {
                if (accelCount < 100 && gyroCount < 100 && getCount >= 0 && getCount < 3) {
                    // 取得した値を，0.03秒ごとに配列に入れる
                    for (int i = 0; i < 3; i++) {
                        accelFloat[getCount][i][accelCount] = vAccel[i];
                    }

                    for (int i = 0; i < 3; i++) {
                        gyroFloat[getCount][i][gyroCount] = vGyro[i];
                    }

                    accelCount++;
                    gyroCount++;

                    if (accelCount == 1) {
                        secondTv.setText("3");
                        mVibrator.vibrate(VIBRATOR_NORMAL);
                    }

                    if (accelCount == 33) {
                        secondTv.setText("2");
                        mVibrator.vibrate(VIBRATOR_NORMAL);
                    }
                    if (accelCount == 66) {
                        secondTv.setText("1");
                        mVibrator.vibrate(VIBRATOR_NORMAL);
                    }

                    // INTERVALで指定したミリ秒後に再度timeHandler（これ自身）を呼び出す
                    timeHandler.sendEmptyMessageDelayed(GET_MOTION, GET_MOTION_INTERVAL);
                }
                else if (accelCount >= 100 && gyroCount >= 100 && getCount >= 0 && getCount < 4) {
                    // 取得完了
                    mVibrator.vibrate(VIBRATOR_LONG);
                    btnStatus = false;
                    getCount++;
                    countSecondTv.setText("回");
                    getMotionBtn.setText("モーションデータ取得");

                    accelCount = 0;
                    gyroCount = 0;

                    prepareCount = 0;

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
                        finishGetMotion();
                    }
                }
                else {
                    super.dispatchMessage(msg);
                }
            }
        }
    };


    private void finishGetMotion () {
        // 全データ取得完了（3回分の加速度，ジャイロを取得完了）
        // ボタンのstatusをdisableにして押せないようにする
        if (getMotionBtn.isClickable()) {
            getMotionBtn.setClickable(false);
        }
        secondTv.setText("0");
        getMotionBtn.setText("データ処理中");

        Log.d(TAG, "writeRawData");

        Log.e(TAG, "progressInitStart");
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("計算処理中");
        progressDialog.setMessage("しばらくお待ちください");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        Log.e(TAG, "progressInitFinish");

        progressDialog.show();
        thread = new Thread(this);
        thread.start();
    }


    @Override
    public void run () {
        Log.e(TAG, "Thread Start");

        mWriteData.writeFloatThreeArrayData("RegistRawData", "rawAccelo", RegistNameInput.name, accelFloat, RegistMotion.this);
        mWriteData.writeFloatThreeArrayData("RegistRawData", "rawGyro", RegistNameInput.name, gyroFloat, RegistMotion.this);

        resultCalc = calc();
        resultSoukan = soukan();

        Log.e(TAG, "Task Finished");

        progressDialog.dismiss();
        progressDialog = null;
        resultHander.sendEmptyMessage(FINISH);
    }


    /**
     * データ加工，計算処理を行う
     */
    private boolean calc () {
        Log.v(TAG, "--- calc ---");
        // データ加工，計算処理
        // データの桁揃え
        double[][][] accel_double = mFormatter.floatToDoubleFormatter(accelFloat);
        double[][][] gyro_double = mFormatter.floatToDoubleFormatter(gyroFloat);

        mWriteData.writeDoubleThreeArrayData("BeforeFFT", "accel", RegistNameInput.name, accel_double, this);
        mWriteData.writeDoubleThreeArrayData("BeforeFFT", "gyro", RegistNameInput.name, gyro_double, this);

        if (mAmplifier.CheckValueRange(accel_double) || mAmplifier.CheckValueRange(gyro_double)) {
            accel_double = mAmplifier.Amplify(accel_double);
            gyro_double = mAmplifier.Amplify(gyro_double);
            isAmplified = true;
        }

        // フーリエ変換によるローパスフィルタ
        accel_double = mFourier.LowpassFilter(accel_double, "accel", this);
        gyro_double = mFourier.LowpassFilter(gyro_double, "gyro", this);

        Log.d(TAG, "*** finishFourier ***");

        distance = mCalc.accelToDistance(accel_double, 0.03);
        angle = mCalc.gyroToAngle(gyro_double, 0.03);

        distance = mFormatter.doubleToDoubleFormatter(distance);
        angle = mFormatter.doubleToDoubleFormatter(angle);

        Log.d(TAG, "*** afterWriteData ***");

        // measureCorrelation用の平均値データを作成
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 100; j++) {
                averageDistance[i][j] = (distance[0][i][j] + distance[1][i][j] + distance[2][i][j]) / 3;
                averageAngle[i][j] = (angle[0][i][j] + angle[1][i][j] + angle[2][i][j]) / 3;
            }
        }

        //region 同一のモーションであるかの確認をし，必要に応じてズレ修正を行う
        Enum.MEASURE measure = mCorrelation.measureCorrelation(this, distance, angle, averageDistance, averageAngle);

        Log.d(TAG, "*** afterMeasureCorrelation");
        Log.d(TAG, "measure = " + String.valueOf(measure));

        if (Enum.MEASURE.BAD == measure) {
            // 相関係数が0.4以下
            Log.i(TAG, "measure: BAD");
            return false;
        }
        else if (Enum.MEASURE.INCORRECT == measure) {
            // 相関係数が0.4よりも高く，0.8以下の場合
            // ズレ修正を行う
            Log.i(TAG, "measure: INCORRECT");
            distance = CorrectDeviation.correctDeviation(distance);
            angle = CorrectDeviation.correctDeviation(angle);
        }
        else if (Enum.MEASURE.PERFECT == measure || Enum.MEASURE.CORRECT == measure) {
            Log.i(TAG, "measure: CORRECT or PERFECT");
            // PERFECTなら，何もしない
        }
        else {
            // なにかがおかしい
            Log.e(TAG, "measure: Error");
            return false;
        }
        //endregion

        mWriteData.writeDoubleThreeArrayData("AfterCalcData", "afterFormatDistance", RegistNameInput.name, distance, RegistMotion.this);
        mWriteData.writeDoubleThreeArrayData("AfterCalcData", "afterFormatAngle", RegistNameInput.name, angle, RegistMotion.this);

        // ズレ修正後の平均値データを出す
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 100; j++) {
                averageDistance[i][j] = (distance[0][i][j] + distance[1][i][j] + distance[2][i][j]) / 3;
                averageAngle[i][j] = (angle[0][i][j] + angle[1][i][j] + angle[2][i][j]) / 3;
            }
        }

        Log.d(TAG, "*** return ***");
        return true;
    }


    /**
     * 相関係数を導出し，ユーザが入力した3回のモーションの類似性を確認する
     */
    private boolean soukan () {
        Log.v(TAG, "--- soukan ---");
        Enum.MEASURE measure = mCorrelation.measureCorrelation(this, distance, angle, averageDistance, averageAngle);

        Log.d(TAG, "measure = " + measure);

        return measure == Enum.MEASURE.CORRECT || measure == Enum.MEASURE.PERFECT;
    }

    private Handler resultHander = new Handler() {
        public void handleMessage (Message msg) {
            if (msg.what == FINISH) {
                if (!resultCalc || !resultSoukan) {
                    // もう一度モーションを取り直す処理
                    // ボタンのstatusをenableにして押せるようにする
                    AlertDialog.Builder alert = new AlertDialog.Builder(RegistMotion.this);
                    alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                return true;
                            }
                            return false;
                        }
                    });

                    alert.setCancelable(false);

                    alert.setTitle("登録失敗");
                    alert.setMessage("登録に失敗しました．やり直して下さい");

                    alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick (DialogInterface dialog, int which) {
                            getMotionBtn.setClickable(true);
                            // データ取得関係の変数を初期化
                            accelCount = 0;
                            gyroCount = 0;
                            getCount = 0;
                            secondTv.setText("3");
                            getMotionBtn.setText("モーションデータ取得");
                        }
                    });

                    alert.show();
                }
                else {
                    // 3回のモーションの平均値をファイルに書き出す
                    mWriteData.writeRegistedData("MotionAuth", RegistNameInput.name, averageDistance, averageAngle, isAmplified, RegistMotion.this);

                    AlertDialog.Builder alert = new AlertDialog.Builder(RegistMotion.this);
                    alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                return true;
                            }
                            return false;
                        }
                    });

                    alert.setCancelable(false);

                    alert.setTitle("登録完了");
                    alert.setMessage("登録が完了しました．\nスタート画面に戻ります");

                    alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick (DialogInterface dialog, int which) {
                            finishRegist();
                        }
                    });

                    alert.show();
                }
            }
        }
    };


    @Override
    public void onAccuracyChanged (Sensor sensor, int accuracy) {
        Log.v(TAG, "--- onAccuracyChanged ---");
    }


    @Override
    protected void onResume () {
        super.onResume();

        Log.v(TAG, "--- onResume ---");

        mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mGyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
    }


    @Override
    protected void onPause () {
        super.onPause();

        Log.v(TAG, "--- onPause ---");

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
        Log.v(TAG, "--- finishRegist ---");
        Intent intent = new Intent();

        intent.setClassName("com.example.motionauth", "com.example.motionauth.Start");

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivityForResult(intent, 0);
        finish();
    }
}
