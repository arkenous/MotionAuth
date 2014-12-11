package com.example.motionauth.Authentication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.motionauth.Lowpass.Fourier;
import com.example.motionauth.Processing.Amplifier;
import com.example.motionauth.Processing.Calc;
import com.example.motionauth.Processing.Correlation;
import com.example.motionauth.Processing.Formatter;
import com.example.motionauth.R;
import com.example.motionauth.Utility.Enum;
import com.example.motionauth.Utility.LogUtil;
import com.example.motionauth.Utility.ManageData;

import java.util.ArrayList;

/**
 * ユーザ認証を行う
 *
 * @author Kensuke Kousaka
 */
public class AuthMotion extends Activity implements SensorEventListener, Runnable {
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

	private Fourier     mFourier     = new Fourier();
	private Formatter   mFormatter   = new Formatter();
	private Calc        mCalc        = new Calc();
	private Correlation mCorrelation = new Correlation();
	private Amplifier   mAmplifier   = new Amplifier();

	private int dataCount    = 0;
	private int prepareCount = 0;

	private boolean isGetMotionBtnClickable = true;

	private double ampValue = 0.0;

	// モーションの生データ
	private float[] vAccel;
	private float[] vGyro;

	private float[][] accelFloat = new float[3][100];
	private float[][] gyroFloat  = new float[3][100];

	private double[][] distance = new double[3][100];
	private double[][] angle    = new double[3][100];

	private double[][] registed_ave_distance = new double[3][100];
	private double[][] registed_ave_angle    = new double[3][100];

	private boolean resultCorrelation = false;

	private ProgressDialog progressDialog;


	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LogUtil.log(Log.INFO);

		setContentView(R.layout.activity_auth_motion);

		authMotion();
	}

	/**
	 * 認証画面にイベントリスナ等を設定する
	 */
	private void authMotion () {
		LogUtil.log(Log.INFO);

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

		getMotionBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick (View v) {
				LogUtil.log(Log.DEBUG, "Click get motion button");
				if (isGetMotionBtnClickable) {
					isGetMotionBtnClickable = false;

					// ボタンをクリックできないようにする
					v.setClickable(false);

					getMotionBtn.setText("インターバル中");
					countSecondTv.setText("秒");

					// timeHandler呼び出し
					timeHandler.sendEmptyMessage(PREPARATION);
				}
			}
		});
	}


	/**
	 * 一定時間ごとにモーションデータを取得するハンドラ
	 * 計算処理や相関係数計算関数もここから呼び出す
	 */
	Handler timeHandler = new Handler() {
		@Override
		public void dispatchMessage (Message msg) {
			LogUtil.log(Log.VERBOSE);

			if (msg.what == PREPARATION && !isGetMotionBtnClickable) {
				switch (prepareCount) {
					case 0:
						secondTv.setText("3");
						mVibrator.vibrate(VIBRATOR_SHORT);

						// 第二引数で指定したミリ秒分遅延させてから，第一引数のメッセージを添えてtimeHandlerを呼び出す
						timeHandler.sendEmptyMessageDelayed(PREPARATION, PREPARATION_INTERVAL);
						break;
					case 1:
						secondTv.setText("2");
						mVibrator.vibrate(VIBRATOR_SHORT);
						timeHandler.sendEmptyMessageDelayed(PREPARATION, PREPARATION_INTERVAL);
						break;
					case 2:
						secondTv.setText("1");
						mVibrator.vibrate(VIBRATOR_SHORT);
						timeHandler.sendEmptyMessageDelayed(PREPARATION, PREPARATION_INTERVAL);
						break;
					case 3:
						secondTv.setText("START");
						mVibrator.vibrate(VIBRATOR_LONG);

						// GET_MOTIONメッセージを添えて，timeHandlerを呼び出す
						timeHandler.sendEmptyMessage(GET_MOTION);
						getMotionBtn.setText("取得中");
						break;
				}

				prepareCount++;
			}
			else if (msg.what == GET_MOTION && !isGetMotionBtnClickable) {
				LogUtil.log(Log.VERBOSE, "Get motion");

				if (dataCount < 100) {
					LogUtil.log(Log.VERBOSE, "Getting motion data");
					// 取得した値を，0.03秒ごとに配列に入れる
					for (int i = 0; i < 3; i++) {
						accelFloat[i][dataCount] = vAccel[i];
						gyroFloat[i][dataCount] = vGyro[i];
					}

					dataCount++;

					switch (dataCount) {
						case 1:
							secondTv.setText("3");
							mVibrator.vibrate(VIBRATOR_NORMAL);
							break;
						case 33:
							secondTv.setText("2");
							mVibrator.vibrate(VIBRATOR_NORMAL);
							break;
						case 66:
							secondTv.setText("1");
							mVibrator.vibrate(VIBRATOR_NORMAL);
							break;
					}

					timeHandler.sendEmptyMessageDelayed(GET_MOTION, GET_MOTION_INTERVAL);
				}
				else if (dataCount >= 100) {
					LogUtil.log(Log.VERBOSE, "Complete getting motion data");

					finishGetMotion();
				}
			}
			else {
				super.dispatchMessage(msg);
			}
		}
	};


	@Override
	public void onSensorChanged (SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) vAccel = event.values.clone();
		if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) vGyro = event.values.clone();
	}

	private void finishGetMotion () {
		getMotionBtn.setText("認証処理中");

		prepareCount = 0;

		LogUtil.log(Log.DEBUG, "Start initialize ProgressDialog");

		progressDialog = new ProgressDialog(this);
		progressDialog.setTitle("計算処理中");
		progressDialog.setMessage("しばらくお待ちください");
		progressDialog.setIndeterminate(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setCancelable(false);

		LogUtil.log(Log.DEBUG, "Finish initialize ProgressDialog");

		progressDialog.show();

		// スレッドを作り，開始する（runメソッドに飛ぶ）．表面ではプログレスダイアログがくるくる
		Thread thread = new Thread(this);
		thread.start();
	}

	@Override
	public void run () {
		LogUtil.log(Log.DEBUG, "Thread start");

		readRegistedData();
		calc();
		resultCorrelation = measureCorrelation();

		progressDialog.dismiss();
		progressDialog = null;
		resultHandler.sendEmptyMessage(FINISH);

		LogUtil.log(Log.DEBUG, "Thread finish");
	}

	/**
	 * RegistMotionにて登録したモーションの平均値データを読み込む
	 */
	private void readRegistedData () {
		LogUtil.log(Log.INFO);

		ManageData mManageData = new ManageData();
		ArrayList<double[][]> readDataList = mManageData.readRegistedData(AuthMotion.this, AuthNameInput.name);

		registed_ave_distance = readDataList.get(0);
		registed_ave_angle = readDataList.get(1);

		SharedPreferences preferences = AuthMotion.this.getApplicationContext().getSharedPreferences("MotionAuth", Context.MODE_PRIVATE);
		String registedAmplify = preferences.getString(AuthNameInput.name + "amplify", "");

		if ("".equals(registedAmplify)) throw new RuntimeException();

		ampValue = Double.valueOf(registedAmplify);
	}

	/**
	 * データ加工・計算処理を行う
	 */
	private void calc () {
		LogUtil.log(Log.INFO);
		// 原データの桁揃え
		double[][] accel = mFormatter.floatToDoubleFormatter(accelFloat);
		double[][] gyro = mFormatter.floatToDoubleFormatter(gyroFloat);

//		if (isAmplify) {
			LogUtil.log(Log.DEBUG, "Amplify on");
		accel = mAmplifier.Amplify(accel, ampValue);
		gyro = mAmplifier.Amplify(gyro, ampValue);
//		}

		// フーリエ変換を用いたローパス処理
		accel = mFourier.LowpassFilter(accel, "accel");
		gyro = mFourier.LowpassFilter(gyro, "gyro");

		distance = mCalc.accelToDistance(accel, 0.03);
		angle = mCalc.gyroToAngle(gyro, 0.03);

		distance = mFormatter.doubleToDoubleFormatter(distance);
		angle = mFormatter.doubleToDoubleFormatter(angle);
	}


	/**
	 * 認証処理終了後に呼び出されるハンドラ
	 * 認証に成功すればスタート画面に戻り，そうでなければ認証やり直しの処理を行う
	 */
	private Handler resultHandler = new Handler() {
		public void handleMessage (Message msg) {
			if (msg.what == FINISH) {
				if (!resultCorrelation) {
					LogUtil.log(Log.INFO, "False authentication");
					AlertDialog.Builder alert = new AlertDialog.Builder(AuthMotion.this);
					alert.setTitle("認証失敗です");
					alert.setMessage("認証に失敗しました");
					alert.setCancelable(false);
					alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick (DialogInterface dialog, int which) {
							isGetMotionBtnClickable = true;
							getMotionBtn.setClickable(true);
							// データ取得関係の変数を初期化
							dataCount = 0;
							secondTv.setText("3");
							getMotionBtn.setText("モーションデータ取得");
						}
					});
					alert.show();
				}
				else {
					LogUtil.log(Log.INFO, "Success authentication");
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


	private boolean measureCorrelation () {
		LogUtil.log(Log.INFO);
		Enum.MEASURE measure = mCorrelation.measureCorrelation(distance, angle, registed_ave_distance, registed_ave_angle);

		LogUtil.log(Log.DEBUG, "measure: " + measure);

		return measure == Enum.MEASURE.CORRECT;
	}

	@Override
	public void onAccuracyChanged (Sensor sensor, int accuracy) {
		LogUtil.log(Log.INFO);
	}


	@Override
	protected void onResume () {
		super.onResume();
		LogUtil.log(Log.INFO);

		mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
		mSensorManager.registerListener(this, mGyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
	}


	@Override
	protected void onPause () {
		super.onPause();
		LogUtil.log(Log.INFO);

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
		LogUtil.log(Log.INFO);
		Intent intent = new Intent();

		intent.setClassName(pkgName, actName);

		if (flg) intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

		startActivityForResult(intent, 0);
		finish();
	}
}
