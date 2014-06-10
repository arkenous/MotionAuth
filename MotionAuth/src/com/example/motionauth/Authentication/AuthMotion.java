package com.example.motionauth.Authentication;

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
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.motionauth.Calc;
import com.example.motionauth.Correlation;
import com.example.motionauth.Formatter;
import com.example.motionauth.Lowpass.Fourier;
import com.example.motionauth.R;
import com.example.motionauth.Utility.Enum;

import java.io.*;

/**
 * ユーザ認証を行う
 *
 * @author Kensuke Kousaka
 */
public class AuthMotion extends Activity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;
    private Sensor mGyroscopeSensor;

    private Fourier mFourier = new Fourier();
    private Formatter mFormatter = new Formatter();
    private Calc mCalc = new Calc();
    private Correlation mCorrelation = new Correlation();

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

    private float accel_tmp[][] = new float[3][100];
    private float gyro_tmp[][] = new float[3][100];

    private double accel[][] = new double[3][100];
    private double gyro[][] = new double[3][100];

    // 移動平均後のデータを格納する配列
    private double distance[][] = new double[3][100];
    private double angle[][] = new double[3][100];

    // RegistMotionにて登録された平均データ
    private double registed_ave_distance[][] = new double[3][100];
    private double registed_ave_angle[][] = new double[3][100];

    TextView secondTv;
    TextView countSecondTv;
    Button getMotionBtn;


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // タイトルバーの非表示
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_auth_motion);

        authMotion();
    }


    /**
     * 認証画面にイベントリスナ等を設定する
     */
    private void authMotion () {
        // センササービス，各種センサを取得する
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        TextView nameTv = (TextView) findViewById(R.id.textView1);
        secondTv = (TextView) findViewById(R.id.secondTextView);
        countSecondTv = (TextView) findViewById(R.id.textView4);
        getMotionBtn = (Button) findViewById(R.id.button1);

        nameTv.setText(AuthNameInput.name + "さん読んでね！");

        getMotionBtn.setOnClickListener(new OnClickListener() {
            public void onClick (View v) {
                if (!btnStatus) {
                    // ボタンを押したら，statusをfalseにして押せないようにする
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

    Handler timeHandler = new Handler() {
        @Override
        public void dispatchMessage (Message msg) {
            if (msg.what == TIMEOUT_MESSAGE && btnStatus) {
                if (accelCount < 100 && gyroCount < 100) {
                    // 取得した値を，0.03秒ごとに配列に入れる
                    for (int i = 0; i < 3; i++) {
                        accel_tmp[i][accelCount] = vAccel[i];
                    }

                    for (int i = 0; i < 3; i++) {
                        gyro_tmp[i][gyroCount] = vGyro[i];
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

                    timeHandler.sendEmptyMessageDelayed(TIMEOUT_MESSAGE, INTERVAL);
                }
                else if (accelCount >= 100 && gyroCount >= 100) {
                    // 取得完了
                    btnStatus = false;
                    getMotionBtn.setText("モーションデータ取得完了");

                    readRegistedData();
                    calc();

                    if (!soukan()) {
                        Toast.makeText(AuthMotion.this, "認証失敗です", Toast.LENGTH_SHORT).show();
                    }

                    Toast.makeText(AuthMotion.this, "認証成功です", Toast.LENGTH_SHORT).show();
                    moveActivity("com.example.motionauth", "com.example.motionauth.Start", true);
                }
            }
            else {
                super.dispatchMessage(msg);
            }
        }
    };


    /**
     * データ加工・計算処理を行う
     */
    //TODO データのズレを，登録された平均値データと比較して修正する
    private void calc () {
        // 原データの桁揃え
        accel = mFormatter.floatToDoubleFormatter(accel_tmp);
        gyro = mFormatter.floatToDoubleFormatter(gyro_tmp);

        // フーリエ変換を用いたローパス処理
        accel = mFourier.retValLowpassFilter(accel, "accel", this);
        gyro = mFourier.retValLowpassFilter(gyro, "gyro", this);

        distance = mCalc.accelToDistance(accel, 0.03);
        angle = mCalc.gyroToAngle(gyro, 0.03);

        distance = mFormatter.doubleToDoubleFormatter(distance);
        angle = mFormatter.doubleToDoubleFormatter(angle);
    }


    private void readRegistedData() {
        int readCount = 0;

        try {
            String filePath = Environment.getExternalStorageDirectory() + File.separator + "MotionAuth" + File.separator + "MotionAuth" + File.separator + AuthNameInput.name;
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);

            String beforeSplitData;
            String[] afterSplitData;

            while ((beforeSplitData = br.readLine()) != null) {
                afterSplitData = beforeSplitData.split("@");

                if (afterSplitData[0].equals("ave_distance_x")) {
                    registed_ave_distance[0][readCount] = Float.valueOf(afterSplitData[1]);
                    if (readCount == 99) {
                        readCount = 0;
                    }
                    else {
                        readCount++;
                    }
                }

                if (afterSplitData[0].equals("ave_distance_y")) {
                    registed_ave_distance[1][readCount] = Float.valueOf(afterSplitData[1]);
                    if (readCount == 99) {
                        readCount = 0;
                    }
                    else {
                        readCount++;
                    }
                }

                if (afterSplitData[0].equals("ave_distance_z")) {
                    registed_ave_distance[2][readCount] = Float.valueOf(afterSplitData[1]);
                    if (readCount == 99) {
                        readCount = 0;
                    }
                    else {
                        readCount++;
                    }
                }

                if (afterSplitData[0].equals("ave_angle_x")) {
                    registed_ave_angle[0][readCount] = Float.valueOf(afterSplitData[1]);
                    if (readCount == 99) {
                        readCount = 0;
                    }
                    else {
                        readCount++;
                    }
                }

                if (afterSplitData[0].equals("ave_angle_y")) {
                    registed_ave_angle[1][readCount] = Float.valueOf(afterSplitData[1]);
                    if (readCount == 99) {
                        readCount = 0;
                    }
                    else {
                        readCount++;
                    }
                }

                if (afterSplitData[0].equals("ave_angle_z")) {
                    registed_ave_angle[2][readCount] = Float.valueOf(afterSplitData[1]);
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


    private boolean soukan () {
        Enum.MEASURE measure = mCorrelation.measureCorrelation(this, distance, angle, registed_ave_distance, registed_ave_angle);

        return measure == Enum.MEASURE.CORRECT;
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


    /**
     * アクティビティを移動する
     *
     * @param pkgName 移動先のパッケージ名
     * @param actName 移動先のアクティビティ名
     * @param flg     戻るキーを押した際にこのアクティビティを表示させるかどうか
     */
    private void moveActivity (String pkgName, String actName, boolean flg) {
        Intent intent = new Intent();

        intent.setClassName(pkgName, actName);

        if (flg) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        startActivityForResult(intent, 0);
    }


    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.auth_motion, menu);
        return true;
    }
}
