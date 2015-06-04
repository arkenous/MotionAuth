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
import android.os.Vibrator;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import com.example.motionauth.Handler.Regist;
import com.example.motionauth.Lowpass.Fourier;
import com.example.motionauth.Processing.*;
import com.example.motionauth.R;
import com.example.motionauth.Utility.Enum;
import com.example.motionauth.Utility.LogUtil;
import com.example.motionauth.Utility.ManageData;


/**
 * モーションを新規登録する
 *
 * @author Kensuke Kousaka
 */
public class RegistMotion extends Activity implements SensorEventListener, Runnable {
	private static final int PREPARATION = 1;

	private static final int FINISH = 5;

	private SensorManager mSensorManager;
	private Sensor mAccelerometerSensor;
	private Sensor mGyroscopeSensor;

	private TextView secondTv;
	private TextView countSecondTv;
	private Button getMotionBtn;

	private Fourier mFourier = new Fourier();
	private Formatter mFormatter = new Formatter();
	private Calc mCalc = new Calc();
	private Amplifier mAmplifier = new Amplifier();
	private ManageData mManageData = new ManageData();
	private Correlation mCorrelation = new Correlation();
	private CorrectDeviation mCorrectDeviation = new CorrectDeviation();
	//	private Adjuster mAdjuster = new Adjuster();
	private Regist mRegist;

	// モーションの生データ
	private float[] vAccel = new float[3];
	private float[] vGyro = new float[3];

	private float[][][] accelFloat = new float[3][3][100];
	private float[][][] gyroFloat = new float[3][3][100];

	private double[][][] distance = new double[3][3][100];
	private double[][][] angle = new double[3][3][100];
	private double[][] averageDistance = new double[3][100];
	private double[][] averageAngle = new double[3][100];

	private boolean resultCalc = false;
	private boolean resultCorrelation = false;

	private ProgressDialog progressDialog;
	private double checkRangeValue = 2.0;
	private double ampValue = 2.0;

	private boolean isMenuClickable = true;

	private RegistMotion mRegistMotion;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtil.log(Log.INFO);

		setContentView(R.layout.activity_regist_motion);
		mRegistMotion = this;
		registMotion();
	}


	/**
	 * モーション登録画面にイベントリスナ等を設定する
	 */
	public void registMotion() {
		LogUtil.log(Log.INFO);

		// センササービス，各種センサを取得する
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mGyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

		Vibrator mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

		TextView nameTv = (TextView) findViewById(R.id.textView2);
		secondTv = (TextView) findViewById(R.id.secondTextView);
		countSecondTv = (TextView) findViewById(R.id.textView4);
		getMotionBtn = (Button) findViewById(R.id.button1);

		nameTv.setText(RegistNameInput.name + "さん読んでね！");
		mRegist = new Regist(mRegistMotion, getMotionBtn, secondTv, countSecondTv, mVibrator, vAccel, vGyro, accelFloat,
				gyroFloat, resultCalc, resultCorrelation, averageDistance, averageAngle, ampValue, this);


		getMotionBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LogUtil.log(Log.VERBOSE, "Click get motion button");
				if (v.isClickable()) {

					// ボタンをクリックできないようにする
					v.setClickable(false);

					isMenuClickable = false;

					getMotionBtn.setText("インターバル中");
					countSecondTv.setText("秒");

					// timeHandler呼び出し
					mRegist.sendEmptyMessage(PREPARATION);
				}
			}
		});
	}


	@Override
	public void onSensorChanged(SensorEvent event) {
//		LogUtil.log(Log.VERBOSE);
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) vAccel = event.values.clone();
		if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) vGyro = event.values.clone();
	}

	public void finishGetMotion() {
		LogUtil.log(Log.INFO);
		if (getMotionBtn.isClickable()) getMotionBtn.setClickable(false);
		isMenuClickable = false;
		secondTv.setText("0");
		getMotionBtn.setText("データ処理中");

		LogUtil.log(Log.DEBUG, "Start initialize progress dialog");

		progressDialog = new ProgressDialog(this);
		progressDialog.setTitle("計算処理中");
		progressDialog.setMessage("しばらくお待ちください");
		progressDialog.setIndeterminate(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setCancelable(false);

		LogUtil.log(Log.DEBUG, "Finish initialize Progress dialog");

		progressDialog.show();

		// スレッドを作り，開始する（runメソッドに飛ぶ）．表面ではプログレスダイアログがくるくる
		Thread thread = new Thread(this);
		thread.start();
	}

	@Override
	public void run() {
		LogUtil.log(Log.DEBUG, "Thread running");
		mManageData.writeFloatThreeArrayData("RegistRawData", "rawAccelo", RegistNameInput.name, accelFloat);
		mManageData.writeFloatThreeArrayData("RegistRawData", "rawGyro", RegistNameInput.name, gyroFloat);

		resultCalc = calc();
		resultCorrelation = measureCorrelation();

		progressDialog.dismiss();
		progressDialog = null;
		mRegist.sendEmptyMessage(FINISH);
		LogUtil.log(Log.DEBUG, "Thread finished");
	}

	/**
	 * データ加工，計算処理を行う
	 */
	private boolean calc() {
		LogUtil.log(Log.INFO);

		// データの桁揃え
		double[][][] accel_double = mFormatter.floatToDoubleFormatter(accelFloat);
		double[][][] gyro_double = mFormatter.floatToDoubleFormatter(gyroFloat);

		//TODO 回数ごとのデータの時間的長さを揃える

		mManageData.writeDoubleThreeArrayData("BeforeAMP", "accel", RegistNameInput.name, accel_double);
		mManageData.writeDoubleThreeArrayData("BeforeAMP", "gyro", RegistNameInput.name, gyro_double);

		// データの増幅処理
		if (mAmplifier.CheckValueRange(accel_double, checkRangeValue) || mAmplifier.CheckValueRange(gyro_double, checkRangeValue)) {
			accel_double = mAmplifier.Amplify(accel_double, ampValue);
			gyro_double = mAmplifier.Amplify(gyro_double, ampValue);
		}

		mManageData.writeDoubleThreeArrayData("AfterAMP", "accel", RegistNameInput.name, accel_double);
		mManageData.writeDoubleThreeArrayData("AfterAMP", "gyro", RegistNameInput.name, gyro_double);


		// フーリエ変換によるローパスフィルタ
		accel_double = mFourier.LowpassFilter(accel_double, "accel");
		gyro_double = mFourier.LowpassFilter(gyro_double, "gyro");

		mManageData.writeDoubleThreeArrayData("AfterLowpass", "accel", RegistNameInput.name, accel_double);
		mManageData.writeDoubleThreeArrayData("AfterLowpass", "gyro", RegistNameInput.name, gyro_double);

		LogUtil.log(Log.DEBUG, "Finish fourier");

		// 加速度から距離，角速度から角度へ変換
		distance = mCalc.accelToDistance(accel_double, 0.03);
		angle = mCalc.gyroToAngle(gyro_double, 0.03);

		distance = mFormatter.doubleToDoubleFormatter(distance);
		angle = mFormatter.doubleToDoubleFormatter(angle);

		mManageData.writeDoubleThreeArrayData("convertData", "distance", RegistNameInput.name, distance);
		mManageData.writeDoubleThreeArrayData("convertData", "angle", RegistNameInput.name, angle);

		LogUtil.log(Log.DEBUG, "After write data");

		// measureCorrelation用の平均値データを作成
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 100; j++) {
				averageDistance[i][j] = (distance[0][i][j] + distance[1][i][j] + distance[2][i][j]) / 3;
				averageAngle[i][j] = (angle[0][i][j] + angle[1][i][j] + angle[2][i][j]) / 3;
			}
		}

		//region 同一のモーションであるかの確認をし，必要に応じてズレ修正を行う
		Enum.MEASURE measure = mCorrelation.measureCorrelation(distance, angle, averageDistance, averageAngle);

		LogUtil.log(Log.INFO, "After measure correlation");
		LogUtil.log(Log.INFO, "measure = " + String.valueOf(measure));

		if (Enum.MEASURE.BAD == measure) {
			// 相関係数が0.4以下
			return false;
		} else if (Enum.MEASURE.INCORRECT == measure) {
			LogUtil.log(Log.DEBUG, "Deviation");
			// 相関係数が0.4よりも高く，0.6以下の場合
			// ズレ修正を行う
			int time = 0;
			Enum.MODE mode = Enum.MODE.MAX;
			Enum.TARGET target = Enum.TARGET.DISTANCE;

			double[][][] originalDistance = distance;
			double[][][] originalAngle = angle;

			// ズレ修正は基準値を最大値，最小値，中央値の順に置き，さらに距離，角度の順にベースを置く．
			while (true) {
				switch (time) {
					case 0:
						mode = Enum.MODE.MAX;
						target = Enum.TARGET.DISTANCE;
						break;
					case 1:
						mode = Enum.MODE.MAX;
						target = Enum.TARGET.ANGLE;
						break;
					case 2:
						mode = Enum.MODE.MIN;
						target = Enum.TARGET.DISTANCE;
						break;
					case 3:
						mode = Enum.MODE.MIN;
						target = Enum.TARGET.ANGLE;
						break;
					case 4:
						mode = Enum.MODE.MEDIAN;
						target = Enum.TARGET.DISTANCE;
						break;
					case 5:
						mode = Enum.MODE.MEDIAN;
						target = Enum.TARGET.ANGLE;
						break;
				}

				double[][][][] deviatedValue = mCorrectDeviation.correctDeviation(originalDistance, originalAngle, mode, target);

				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 3; j++) {
						for (int k = 0; k < 100; k++) {
							distance[i][j][k] = deviatedValue[0][i][j][k];
							angle[i][j][k] = deviatedValue[1][i][j][k];
						}
					}
				}

				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 100; j++) {
						averageDistance[i][j] = (distance[0][i][j] + distance[1][i][j] + distance[2][i][j]) / 3;
						averageAngle[i][j] = (angle[0][i][j] + angle[1][i][j] + angle[2][i][j]) / 3;
					}
				}

				Enum.MEASURE tmp = mCorrelation.measureCorrelation(distance, angle, averageDistance, averageAngle);

				LogUtil.log(Log.INFO, "MEASURE: " + String.valueOf(tmp));

				mManageData.writeDoubleThreeArrayData("deviatedData" + String.valueOf(mode), "distance", RegistNameInput.name, distance);
				mManageData.writeDoubleThreeArrayData("deviatedData" + String.valueOf(mode), "angle", RegistNameInput.name, angle);


				if (tmp == Enum.MEASURE.PERFECT || tmp == Enum.MEASURE.CORRECT) {
					break;
				} else if (time == 2) {
					// 相関係数が低いまま，アラートなどを出す？
					distance = originalDistance;
					angle = originalAngle;
					break;
				}

				time++;
			}
		} else {
			// なにかがおかしい
			return false;
		}
		//endregion

		mManageData.writeDoubleThreeArrayData("AfterCalcData", "afterFormatDistance", RegistNameInput.name, distance);
		mManageData.writeDoubleThreeArrayData("AfterCalcData", "afterFormatAngle", RegistNameInput.name, angle);

		// ズレ修正後の平均値データを出す
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 100; j++) {
				averageDistance[i][j] = (distance[0][i][j] + distance[1][i][j] + distance[2][i][j]) / 3;
				averageAngle[i][j] = (angle[0][i][j] + angle[1][i][j] + angle[2][i][j]) / 3;
			}
		}

		LogUtil.log(Log.DEBUG, "return");
		return true;
	}

	/**
	 * 相関係数を導出し，ユーザが入力した3回のモーションの類似性を確認する
	 */
	private boolean measureCorrelation() {
		LogUtil.log(Log.INFO);
		Enum.MEASURE measure = mCorrelation.measureCorrelation(distance, angle, averageDistance, averageAngle);

		LogUtil.log(Log.INFO, "measure = " + measure);

		return measure == Enum.MEASURE.CORRECT || measure == Enum.MEASURE.PERFECT;
	}


	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		LogUtil.log(Log.DEBUG);
	}


	@Override
	protected void onResume() {
		super.onResume();

		LogUtil.log(Log.INFO);

		mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
		mSensorManager.registerListener(this, mGyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
	}


	@Override
	protected void onPause() {
		super.onPause();

		LogUtil.log(Log.INFO);

		mSensorManager.unregisterListener(this);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.regist_motion, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.change_range_value:
				if (isMenuClickable) {
					LayoutInflater inflater = LayoutInflater.from(RegistMotion.this);
					View seekView = inflater.inflate(R.layout.seekdialog, (ViewGroup) findViewById(R.id.dialog_root));

					// 閾値調整
					SeekBar thresholdSeekBar = (SeekBar) seekView.findViewById(R.id.threshold);
					final TextView thresholdSeekText = (TextView) seekView.findViewById(R.id.thresholdtext);
					thresholdSeekText.setText("増幅器にかけるかどうかを判断する閾値を調整できます．\n" +
							"2.5を中心に，値が小さければ登録・認証が難しくなり，大きければ易しくなります．\n" +
							"現在の値は" + checkRangeValue + "です．");

					thresholdSeekBar.setMax(30);

					thresholdSeekBar.setProgress(16);
					thresholdSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
						@Override
						public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
							checkRangeValue = (seekBar.getProgress() + 10) / 10.0;
							thresholdSeekText.setText("増幅器にかけるかどうかを判断する閾値を調整できます．\n" +
									"2.5を中心に，値が小さければ登録・認証が難しくなり，大きければ易しくなります．\n" +
									"現在の値は" + checkRangeValue + "です．");
						}

						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {
						}

						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {
							checkRangeValue = (seekBar.getProgress() + 10) / 10.0;
							thresholdSeekText.setText("増幅器にかけるかどうかを判断する閾値を調整できます．\n" +
									"2.5を中心に，値が小さければ登録・認証が難しくなり，大きければ易しくなります．\n" +
									"現在の値は" + checkRangeValue + "です．");
						}
					});

					// 増幅値調整
					SeekBar ampvalSeekBar = (SeekBar) seekView.findViewById(R.id.ampval);
					final TextView ampvalText = (TextView) seekView.findViewById(R.id.ampvaltext);
					ampvalText.setText("増幅器にかける場合に，何倍増幅するかを調整できます．標準は2倍です．\n" +
							"現在の値は" + ampValue + "です．");

					ampvalSeekBar.setMax(10);

					ampvalSeekBar.setProgress(2);
					ampvalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
						@Override
						public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
							ampValue = seekBar.getProgress() * 1.0;
							ampvalText.setText("増幅器にかける場合に，何倍増幅するかを調整できます．標準は2倍です．\n" +
									"現在の値は" + ampValue + "です．");
						}

						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {
						}

						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {
							ampValue = seekBar.getProgress() * 1.0;
							ampvalText.setText("増幅器にかける場合に，何倍増幅するかを調整できます．標準は2倍です．\n" +
									"現在の値は" + ampValue + "です．");
						}
					});

					AlertDialog.Builder dialog = new AlertDialog.Builder(RegistMotion.this);
					dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
						@Override
						public boolean onKey(DialogInterface dialog1, int keyCode, KeyEvent event) {
							return keyCode == KeyEvent.KEYCODE_BACK;
						}
					});
					dialog.setTitle("増幅器設定");
					dialog.setView(seekView);
					dialog.setCancelable(false);
					dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog1, int which) {
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
						public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
							return keyCode == KeyEvent.KEYCODE_BACK;
						}
					});

					alert.setCancelable(false);

					alert.setTitle("データ取得リセット");
					alert.setMessage("本当にデータ取得をやり直しますか？");

					alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});

					alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mRegist.reset();
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
	public void finishRegist() {
		LogUtil.log(Log.INFO);
		Intent intent = new Intent();

		intent.setClassName("com.example.motionauth", "com.example.motionauth.Start");

		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

		startActivityForResult(intent, 0);
		finish();
	}
}
