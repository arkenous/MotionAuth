package com.example.motionauth.Handler;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.TextView;
import com.example.motionauth.Registration.RegistMotion;
import com.example.motionauth.Registration.RegistNameInput;
import com.example.motionauth.Utility.LogUtil;
import com.example.motionauth.Utility.ManageData;

public class Regist extends Handler implements SensorEventListener{
	private static final int PREPARATION = 1;
	private static final int GETMOTION = 2;
	private static final int FINISH = 5;
	private static final int VIBRATOR_SHORT = 25;
	private static final int VIBRATOR_NORMAL = 50;
	private static final int VIBRATOR_LONG = 100;
	private static final int PREPARATION_INTERVAL = 1000;
	private static final int GETMOTION_INTERVAL = 30;

	private ManageData mManageData = new ManageData();

	private Button mGetMotion;
	private TextView mSecond;
	private TextView mCount;
	private Vibrator mVibrator;

	private int prepareCount = 0;
	private int dataCount = 0;
	private int getCount = 0;

	private float[] mOriginAccel;
	private float[] mOriginGyro;
	private float[][][] mAccel = new float[3][3][100];
	private float[][][] mGyro = new float[3][3][100];

	private RegistMotion mRegistMotion;
	private Context mContext;

	private Regist mRegist;

	private SensorManager mSensorManager;
	private Sensor mAccelerometerSensor;
	private Sensor mGyroscopeSensor;


	public Regist(RegistMotion registMotion, Button getMotion, TextView second, TextView count, Vibrator vibrator,
	              Context context) {
		LogUtil.log(Log.INFO);
		mRegistMotion = registMotion;
		mGetMotion = getMotion;
		mSecond = second;
		mCount = count;
		mVibrator = vibrator;
		mContext = context;
		mRegist = this;

		mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
		mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mGyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
	}

	@Override
	public void dispatchMessage(@NonNull Message msg) {
		LogUtil.log(Log.INFO);

		if (msg.what == PREPARATION && !mGetMotion.isClickable()) {
			switch (prepareCount) {
				case 0:
					mSecond.setText("3");
					mVibrator.vibrate(VIBRATOR_SHORT);
					this.sendEmptyMessageDelayed(PREPARATION, PREPARATION_INTERVAL);
					break;
				case 1:
					mSecond.setText("2");
					mVibrator.vibrate(VIBRATOR_SHORT);
					this.sendEmptyMessageDelayed(PREPARATION, PREPARATION_INTERVAL);
					break;
				case 2:
					mSecond.setText("1");
					mVibrator.vibrate(VIBRATOR_SHORT);
					this.sendEmptyMessageDelayed(PREPARATION, PREPARATION_INTERVAL);
					break;
				case 3:
					mSecond.setText("Start");
					mVibrator.vibrate(VIBRATOR_LONG);

					this.sendEmptyMessage(GETMOTION);
					mGetMotion.setText("Correcting");
					break;
			}

			prepareCount++;
		} else if (msg.what == GETMOTION && !mGetMotion.isClickable()) {
			if (dataCount < 100 && getCount >= 0 && getCount < 3) {
				for (int i = 0; i < 3; i++) {
					mAccel[getCount][i][dataCount] = mOriginAccel[i];
					mGyro[getCount][i][dataCount] = mOriginGyro[i];
				}

				dataCount++;

				switch (dataCount) {
					case 1:
						mSecond.setText("3");
						mVibrator.vibrate(VIBRATOR_NORMAL);
						break;
					case 33:
						mSecond.setText("2");
						mVibrator.vibrate(VIBRATOR_NORMAL);
						break;
					case 66:
						mSecond.setText("1");
						mVibrator.vibrate(VIBRATOR_NORMAL);
						break;
				}
				this.sendEmptyMessageDelayed(GETMOTION, GETMOTION_INTERVAL);
			} else if (dataCount >= 100 && getCount >= 0 && getCount < 4) {
				mVibrator.vibrate(VIBRATOR_LONG);
				mGetMotion.setClickable(true);
				getCount++;
				mCount.setText("回");
				mGetMotion.setText("モーションデータ取得");

				dataCount = 0;
				prepareCount = 0;

				switch (getCount) {
					case 1:
						mSecond.setText("2");
						mGetMotion.setClickable(true);
						break;
					case 2:
						mSecond.setText("1");
						mGetMotion.setClickable(true);
						break;
					case 3:
						mRegistMotion.finishGetMotion(mAccel, mGyro);
						break;
				}
			}
		} else if (msg.what == FINISH) {
			boolean mResultCalc = msg.getData().getBoolean("resultCalc");
			boolean mResultCorrelation = msg.getData().getBoolean("resultCorrelation");
			double[][] averageDistance = new double[3][100];
			for(int i = 0; i < averageDistance.length; i++) {
				switch (i) {
					case 0:
						for (int j = 0; j < averageDistance[i].length; j++)
							averageDistance[i][j] = msg.getData().getDoubleArray("DistanceX")[j];
						break;
					case 1:
						for (int j = 0; j < averageDistance[i].length; j++)
							averageDistance[i][j] = msg.getData().getDoubleArray("DistanceY")[j];
						break;
					case 2:
						for (int j = 0; j < averageDistance[i].length; j++)
							averageDistance[i][j] = msg.getData().getDoubleArray("DistanceZ")[j];
						break;
				}
			}
			double[][] averageAngle = new double[3][100];
			for(int i = 0; i < averageAngle.length; i++) {
				switch (i) {
					case 0:
						for (int j = 0; j < averageAngle[i].length; j++)
							averageAngle[i][j] = msg.getData().getDoubleArray("AngleX")[j];
						break;
					case 1:
						for (int j = 0; j < averageAngle[i].length; j++)
							averageAngle[i][j] = msg.getData().getDoubleArray("AngleY")[j];
						break;
					case 2:
						for (int j = 0; j < averageAngle[i].length; j++)
							averageAngle[i][j] = msg.getData().getDoubleArray("AngleZ")[j];
						break;
				}
			}
			double ampValue = msg.getData().getDouble("ampValue");

			LogUtil.log(Log.DEBUG, "resultCalc: " + mResultCalc);
			LogUtil.log(Log.DEBUG, "resultCorrelation: " + mResultCorrelation);
			if (!mResultCalc || !mResultCorrelation) {
				AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
				alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						return keyCode == KeyEvent.KEYCODE_BACK;
					}
				});

				alert.setCancelable(false);

				alert.setTitle("登録失敗");
				alert.setMessage("登録に失敗しました．やり直して下さい");

				alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mRegist.reset();
					}
				});

				alert.show();
			} else {
				// 3回のモーションの平均値をファイルに書き出す
				mManageData.writeRegistedData(RegistNameInput.name, averageDistance, averageAngle, ampValue, mContext);

				AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
				alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						return keyCode == KeyEvent.KEYCODE_BACK;
					}
				});

				alert.setCancelable(false);

				alert.setTitle("登録完了");
				alert.setMessage("登録が完了しました．\nスタート画面に戻ります");

				alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mRegistMotion.finishRegist();
					}
				});

				alert.show();

			}
		}
	}

	public void reset() {
		mGetMotion.setClickable(true);
		dataCount = 0;
		getCount = 0;
		mSecond.setText("3");
		mGetMotion.setText("モーションデータ取得");
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) mOriginAccel = event.values.clone();
		if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) mOriginGyro = event.values.clone();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void registSensor() {
		mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
		mSensorManager.registerListener(this, mGyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
	}

	public void unregistSensor() {
		mSensorManager.unregisterListener(this);
	}
}
