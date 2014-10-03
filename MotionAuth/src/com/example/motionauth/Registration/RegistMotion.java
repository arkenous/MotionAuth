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
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
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

    private static final int VIBRATOR_SHORT  = 25;
    private static final int VIBRATOR_NORMAL = 50;
    private static final int VIBRATOR_LONG   = 100;

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

    private Fourier          mFourier          = new Fourier();
    private Formatter        mFormatter        = new Formatter();
    private Calc             mCalc             = new Calc();
    private Amplifier        mAmplifier        = new Amplifier();
    private WriteData        mWriteData        = new WriteData();
    private Correlation      mCorrelation      = new Correlation();
    private CorrectDeviation mCorrectDeviation = new CorrectDeviation();

    private int accelCount   = 0;
    private int gyroCount    = 0;
    private int getCount     = 0;
    private int prepareCount = 0;

    private boolean isGetMotionBtnClickable = true;

    private boolean isAmplified = false;

    // モーションの生データ
    private float[] vAccel;
    private float[] vGyro;

    private float[][][] accelFloat = new float[3][3][100];
    private float[][][] gyroFloat  = new float[3][3][100];

    private double[][][] distance        = new double[3][3][100];
    private double[][][] angle           = new double[3][3][100];
    private double[][]   averageDistance = new double[3][100];
    private double[][]   averageAngle    = new double[3][100];

    private boolean resultCalc        = false;
    private boolean resultCorrelation = false;

    private ProgressDialog progressDialog;
    private double checkRangeValue = 2.0;

    private boolean isMenuClickable = true;


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v(TAG, "--- onCreate ---");

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
                if (isGetMotionBtnClickable) {
                    isGetMotionBtnClickable = false;

                    // ボタンをクリックできないようにする
                    v.setClickable(false);

                    isMenuClickable = false;

                    getMotionBtn.setText("インターバル中");
                    countSecondTv.setText("秒");

                    // timeHandler呼び出し
                    timeHandler.sendEmptyMessage(PREPARATION);
                }
            }
        });
    }


    /**
     * 一定時間ごとにモーションデータを取得し配列に格納するハンドラ
     * 計算処理や相関係数取得関数の呼び出しもここで行う
     */
    private Handler timeHandler = new Handler() {
        @Override
        public void dispatchMessage (Message msg) {
            Log.i(TAG, "--- dispatchMessage ---");

            if (msg.what == PREPARATION && !isGetMotionBtnClickable) {
                if (prepareCount == 0) {
                    secondTv.setText("3");
                    mVibrator.vibrate(VIBRATOR_SHORT);

                    // 第二引数で指定したミリ秒分遅延させてから，第一引数のメッセージを添えてtimeHandlerを呼び出す
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

                    // GET_MOTIONメッセージを添えて，timeHandlerを呼び出す
                    timeHandler.sendEmptyMessage(GET_MOTION);
                    getMotionBtn.setText("取得中");
                }

                prepareCount++;
            }
            else if (msg.what == GET_MOTION && !isGetMotionBtnClickable) {
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

                    timeHandler.sendEmptyMessageDelayed(GET_MOTION, GET_MOTION_INTERVAL);
                }
                else if (accelCount >= 100 && gyroCount >= 100 && getCount >= 0 && getCount < 4) {
                    // 取得完了
                    mVibrator.vibrate(VIBRATOR_LONG);
                    isGetMotionBtnClickable = true;
                    isMenuClickable = true;
                    getCount++;
                    countSecondTv.setText("回");
                    getMotionBtn.setText("モーションデータ取得");

                    accelCount = 0;
                    gyroCount = 0;

                    prepareCount = 0;

                    if (getCount == 1) {
                        secondTv.setText("2");

                        getMotionBtn.setClickable(true);
                    }
                    if (getCount == 2) {
                        secondTv.setText("1");

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


    @Override
    public void onSensorChanged (SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            vAccel = event.values.clone();
        }
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            vGyro = event.values.clone();
        }
    }

    private void finishGetMotion () {
        if (getMotionBtn.isClickable()) {
            getMotionBtn.setClickable(false);
        }
        isMenuClickable = false;
        secondTv.setText("0");
        getMotionBtn.setText("データ処理中");

        Log.i(TAG, "Start Initialize ProgressDialog");

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("計算処理中");
        progressDialog.setMessage("しばらくお待ちください");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        Log.i(TAG, "Finish Initialize ProgressDialog");

        progressDialog.show();

        // スレッドを作り，開始する（runメソッドに飛ぶ）．表面ではプログレスダイアログがくるくる
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run () {
        Log.i(TAG, "Thread Running");
        mWriteData.writeFloatThreeArrayData("RegistRawData", "rawAccelo", RegistNameInput.name, accelFloat);
        mWriteData.writeFloatThreeArrayData("RegistRawData", "rawGyro", RegistNameInput.name, gyroFloat);

        resultCalc = calc();
        resultCorrelation = measureCorrelation();

        progressDialog.dismiss();
        progressDialog = null;
        resultHandler.sendEmptyMessage(FINISH);
        Log.i(TAG, "Thread Finish");
    }

    /**
     * データ加工，計算処理を行う
     */
    private boolean calc () {
        Log.v(TAG, "--- calc ---");
        // データの桁揃え
        double[][][] accel_double = mFormatter.floatToDoubleFormatter(accelFloat);
        double[][][] gyro_double = mFormatter.floatToDoubleFormatter(gyroFloat);

        mWriteData.writeDoubleThreeArrayData("BeforeFFT", "accel", RegistNameInput.name, accel_double);
        mWriteData.writeDoubleThreeArrayData("BeforeFFT", "gyro", RegistNameInput.name, gyro_double);

        if (mAmplifier.CheckValueRange(accel_double, checkRangeValue) || mAmplifier.CheckValueRange(gyro_double, checkRangeValue)) {
            accel_double = mAmplifier.Amplify(accel_double);
            gyro_double = mAmplifier.Amplify(gyro_double);
            isAmplified = true;
        }

        // フーリエ変換によるローパスフィルタ
        accel_double = mFourier.LowpassFilter(accel_double, "accel");
        gyro_double = mFourier.LowpassFilter(gyro_double, "gyro");

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
        Enum.MEASURE measure = mCorrelation.measureCorrelation(distance, angle, averageDistance, averageAngle);

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
            distance = mCorrectDeviation.correctDeviation(distance);
            angle = mCorrectDeviation.correctDeviation(angle);
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

        mWriteData.writeDoubleThreeArrayData("AfterCalcData", "afterFormatDistance", RegistNameInput.name, distance);
        mWriteData.writeDoubleThreeArrayData("AfterCalcData", "afterFormatAngle", RegistNameInput.name, angle);

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
    private boolean measureCorrelation () {
        Log.v(TAG, "--- measureCorrelation ---");
        Enum.MEASURE measure = mCorrelation.measureCorrelation(distance, angle, averageDistance, averageAngle);

        Log.d(TAG, "measure = " + measure);

        return measure == Enum.MEASURE.CORRECT || measure == Enum.MEASURE.PERFECT;
    }


    /**
     * 計算，モーション照合処理終了後に呼ばれるハンドラ
     * 同一のモーションであると確認されたら登録を行い，そうでなければ取り直しの処理を行う
     */
    private Handler resultHandler = new Handler() {
        public void handleMessage (Message msg) {
            if (msg.what == FINISH) {
                if (!resultCalc || !resultCorrelation) {
                    // もう一度モーションを取り直す処理
                    // ボタンのstatusをenableにして押せるようにする
                    AlertDialog.Builder alert = new AlertDialog.Builder(RegistMotion.this);
                    alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event) {
                            return keyCode == KeyEvent.KEYCODE_BACK;
                        }
                    });

                    alert.setCancelable(false);

                    alert.setTitle("登録失敗");
                    alert.setMessage("登録に失敗しました．やり直して下さい");

                    alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick (DialogInterface dialog, int which) {
                            resetValue();
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
                            return keyCode == KeyEvent.KEYCODE_BACK;
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


    /**
     * モーション取得に関する変数群を初期化する
     */
    private void resetValue () {
        getMotionBtn.setClickable(true);
        // データ取得関係の変数を初期化
        accelCount = 0;
        gyroCount = 0;
        getCount = 0;
        secondTv.setText("3");
        getMotionBtn.setText("モーションデータ取得");
    }


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
        getMenuInflater().inflate(R.menu.regist_motion, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        switch (item.getItemId()) {
            case R.id.change_range_value:
                if (isMenuClickable) {
                    LayoutInflater inflater = LayoutInflater.from(RegistMotion.this);
                    View seekView = inflater.inflate(R.layout.seekdialog, (ViewGroup) findViewById(R.id.dialog_root));
                    SeekBar seekBar = (SeekBar) seekView.findViewById(R.id.seekbar);
                    final TextView seekText = (TextView) seekView.findViewById(R.id.seektext);
                    seekText.setText("現在の値は" + checkRangeValue + "です");
                    seekBar.setMax(30);

                    seekBar.setProgress(16);
                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged (SeekBar seekBar, int progress, boolean fromUser) {
                            checkRangeValue = (seekBar.getProgress() + 10) / 10.0;
                            seekText.setText("現在の値は" + checkRangeValue + "です");
                        }

                        @Override
                        public void onStartTrackingTouch (SeekBar seekBar) {
                        }

                        @Override
                        public void onStopTrackingTouch (SeekBar seekBar) {
                            checkRangeValue = (seekBar.getProgress() + 10) / 10.0;
                            seekText.setText("現在の値は" + checkRangeValue + "です");
                        }
                    });

                    AlertDialog.Builder dialog = new AlertDialog.Builder(RegistMotion.this);
                    dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event) {
                            return keyCode == KeyEvent.KEYCODE_BACK;
                        }
                    });
                    dialog.setTitle("増幅器の閾値調整");
                    dialog.setMessage("増幅器にかけるかどうかを判断する閾値を調整できます．\n" +
                            "2.5を中心に，値が小さければ登録・認証が難しくなり，大きければ易しくなります．");
                    dialog.setView(seekView);
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick (DialogInterface dialog, int which) {

                        }
                    });
                    dialog.show();
                }
                return true;

            case R.id.reset:
                if (isMenuClickable) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(RegistMotion.this);
                    alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event) {
                            return keyCode == KeyEvent.KEYCODE_BACK;
                        }
                    });

                    alert.setCancelable(false);

                    alert.setTitle("データ取得リセット");
                    alert.setMessage("本当にデータ取得をやり直しますか？");

                    alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick (DialogInterface dialog, int which) {
                        }
                    });

                    alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick (DialogInterface dialog, int which) {
                            resetValue();
                        }
                    });

                    alert.show();
                }
                return true;
        }
        return false;
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
