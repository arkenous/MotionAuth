package com.example.motionauth.Authentication;

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
import android.os.*;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import com.example.motionauth.Lowpass.Fourier;
import com.example.motionauth.Processing.Amplifier;
import com.example.motionauth.Processing.Calc;
import com.example.motionauth.Processing.Correlation;
import com.example.motionauth.Processing.Formatter;
import com.example.motionauth.R;
import com.example.motionauth.Utility.Enum;

import java.io.*;

/**
 * ユーザ認証を行う
 *
 * @author Kensuke Kousaka
 */
public class AuthMotion extends Activity implements SensorEventListener, Runnable {
    private static final String TAG = AuthMotion.class.getSimpleName();

    private static final int VIBRATOR_SHORT  = 40;
    private static final int VIBRATOR_NORMAL = 50;
    private static final int VIBRATOR_LONG   = 60;

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
    private Correlation mCorrelation = new Correlation();
    private Amplifier   mAmplifier   = new Amplifier();

    // データ取得カウント用
    private int accelCount = 0;
    private int gyroCount  = 0;

    private int prepareCount = 0;

    private boolean btnStatus = false;

    private boolean isAmplity = false;

    // モーションの生データ
    private float[] vAccel;
    private float[] vGyro;

    private float[][] accelFloat = new float[3][100];
    private float[][] gyroFloat  = new float[3][100];

    // 移動平均後のデータを格納する配列
    private double[][] distance = new double[3][100];
    private double[][] angle    = new double[3][100];

    // RegistMotionにて登録された平均データ
    private double[][] registed_ave_distance = new double[3][100];
    private double[][] registed_ave_angle    = new double[3][100];

    // 計算処理のスレッドに関する変数
    private boolean resultSoukan = false;
    private ProgressDialog progressDialog;
    private Thread         thread;


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v(TAG, "--- onCreate ---");

        // タイトルバーの非表示
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_auth_motion);

        authMotion();
    }


    /**
     * 認証画面にイベントリスナ等を設定する
     */
    private void authMotion () {
        Log.v(TAG, "--- authMotion ---");

        // センササービス，各種センサを取得する
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        TextView nameTv = (TextView) findViewById(R.id.textView1);
        secondTv = (TextView) findViewById(R.id.secondTextView);
        countSecondTv = (TextView) findViewById(R.id.textView4);
        getMotionBtn = (Button) findViewById(R.id.button1);

        nameTv.setText(AuthNameInput.name + "さん読んでね！");

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
    }


    @Override
    public void onSensorChanged (SensorEvent event) {
        Log.v(TAG, "--- onSensorChanged ---");
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            vAccel = event.values.clone();
        }
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            vGyro = event.values.clone();
        }
    }


    /**
     * 一定時間ごとにモーションデータを取得するハンドラ
     * 計算処理や相関係数計算関数もここから呼び出す
     */
    Handler timeHandler = new Handler() {
        @Override
        public void dispatchMessage (Message msg) {
            Log.v(TAG, "--- dispatchMessage ---");

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
                Log.i(TAG, "GET_MOTION");

                if (accelCount < 100 && gyroCount < 100) {
                    Log.i(TAG, "Getting Motion Data");
                    // 取得した値を，0.03秒ごとに配列に入れる
                    for (int i = 0; i < 3; i++) {
                        accelFloat[i][accelCount] = vAccel[i];
                    }

                    for (int i = 0; i < 3; i++) {
                        gyroFloat[i][gyroCount] = vGyro[i];
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

                    timeHandler.sendEmptyMessageDelayed(GET_MOTION, GET_MOTION_INTERVAL);
                }
                else if (accelCount >= 100 && gyroCount >= 100) {
                    Log.i(TAG, "Complete Getting Motion Data");

                    finishGetMotion();
                }
            }
            else {
                super.dispatchMessage(msg);
            }
        }
    };


    private void finishGetMotion () {
        // 取得完了
        btnStatus = false;
        getMotionBtn.setText("認証処理中");

        prepareCount = 0;

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
        readRegistedData();
        calc();

        resultSoukan = soukan();

        Log.e(TAG, "Task Finished");

        progressDialog.dismiss();
        progressDialog = null;
        resultHandler.sendEmptyMessage(FINISH);
    }


    /**
     * RegistMotionにて登録したモーションの平均値データを読み込む
     */
    private void readRegistedData () {
        Log.v(TAG, "--- readRegistedData ---");
        int readCount = 0;

        try {
            String filePath = Environment.getExternalStorageDirectory() + File.separator + "MotionAuth" + File.separator + "MotionAuth" + File.separator + AuthNameInput.name;
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);

            String beforeSplitData;
            String[] checkAmplify;

            while ((beforeSplitData = br.readLine()) != null) {
                checkAmplify = beforeSplitData.split(":");
                if (checkAmplify[1].equals("true")) {
                    isAmplity = true;
                }

                String checkedData = checkAmplify[0];
                String[] checkedSplitData = checkedData.split("@");
                if (checkedSplitData[0].equals("ave_distance_x")) {
                    registed_ave_distance[0][readCount] = Double.valueOf(checkedSplitData[1]);
                    if (readCount == 99) {
                        readCount = 0;
                    }
                    else {
                        readCount++;
                    }
                }

                if (checkedSplitData[0].equals("ave_distance_y")) {
                    registed_ave_distance[1][readCount] = Double.valueOf(checkedSplitData[1]);
                    if (readCount == 99) {
                        readCount = 0;
                    }
                    else {
                        readCount++;
                    }
                }

                if (checkedSplitData[0].equals("ave_distance_z")) {
                    registed_ave_distance[2][readCount] = Double.valueOf(checkedSplitData[1]);
                    if (readCount == 99) {
                        readCount = 0;
                    }
                    else {
                        readCount++;
                    }
                }

                if (checkedSplitData[0].equals("ave_angle_x")) {
                    registed_ave_angle[0][readCount] = Double.valueOf(checkedSplitData[1]);
                    if (readCount == 99) {
                        readCount = 0;
                    }
                    else {
                        readCount++;
                    }
                }

                if (checkedSplitData[0].equals("ave_angle_y")) {
                    registed_ave_angle[1][readCount] = Double.valueOf(checkedSplitData[1]);
                    if (readCount == 99) {
                        readCount = 0;
                    }
                    else {
                        readCount++;
                    }
                }

                if (checkedSplitData[0].equals("ave_angle_z")) {
                    registed_ave_angle[2][readCount] = Double.valueOf(checkedSplitData[1]);
                    if (readCount == 99) {
                        readCount = 0;
                    }
                    else {
                        readCount++;
                    }
                }
            }

            br.close();
            isr.close();
            fis.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * データ加工・計算処理を行う
     */
    private void calc () {
        Log.v(TAG, "--- calc ---");
        // 原データの桁揃え
        double[][] accel = mFormatter.floatToDoubleFormatter(accelFloat);
        double[][] gyro = mFormatter.floatToDoubleFormatter(gyroFloat);

        if (isAmplity) {
            Log.i(TAG, "Amplify");
            accel = mAmplifier.Amplify(accel);
            gyro = mAmplifier.Amplify(gyro);
        }

        // フーリエ変換を用いたローパス処理
        accel = mFourier.LowpassFilter(accel, "accel", this);
        gyro = mFourier.LowpassFilter(gyro, "gyro", this);

        distance = mCalc.accelToDistance(accel, 0.03);
        angle = mCalc.gyroToAngle(gyro, 0.03);

        distance = mFormatter.doubleToDoubleFormatter(distance);
        angle = mFormatter.doubleToDoubleFormatter(angle);
    }


    private boolean soukan () {
        Log.v(TAG, "--- soukan ---");
        Enum.MEASURE measure = mCorrelation.measureCorrelation(this, distance, angle, registed_ave_distance, registed_ave_angle);

        return measure == Enum.MEASURE.CORRECT;
    }


    private Handler resultHandler = new Handler() {
        public void handleMessage (Message msg) {
            if (msg.what == FINISH) {
                if (!resultSoukan) {
                    Log.i(TAG, "False Authentication");
                    AlertDialog.Builder alert = new AlertDialog.Builder(AuthMotion.this);
                    alert.setTitle("認証失敗です");
                    alert.setMessage("認証に失敗しました");
                    alert.setCancelable(false);
                    alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick (DialogInterface dialog, int which) {
                            getMotionBtn.setClickable(true);
                            // データ取得関係の変数を初期化
                            accelCount = 0;
                            gyroCount = 0;
                            secondTv.setText("3");
                            getMotionBtn.setText("モーションデータ取得");
                        }
                    });
                    alert.show();
                }
                else {
                    Log.i(TAG, "Success Authentication");
                    AlertDialog.Builder alert = new AlertDialog.Builder(AuthMotion.this);
                    alert.setTitle("認証成功");
                    alert.setMessage("認証に成功しました．\nスタート画面に戻ります．");
                    alert.setCancelable(false);
                    alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick (DialogInterface dialog, int which) {
                            moveActivity("com.example.motionauth", "com.example.motionauth.Start", true);
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


    /**
     * アクティビティを移動する
     *
     * @param pkgName 移動先のパッケージ名
     * @param actName 移動先のアクティビティ名
     * @param flg     戻るキーを押した際にこのアクティビティを表示させるかどうか
     */
    private void moveActivity (String pkgName, String actName, boolean flg) {
        Log.v(TAG, "--- moveActivity ---");
        Intent intent = new Intent();

        intent.setClassName(pkgName, actName);

        if (flg) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        startActivityForResult(intent, 0);
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.auth_motion, menu);
        return true;
    }
}
