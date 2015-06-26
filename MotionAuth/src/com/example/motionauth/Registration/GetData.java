package com.example.motionauth.Registration;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.TextView;

public class GetData extends Handler implements Runnable, SensorEventListener {
	private static final int PREPARATION = 1;
	private static final int GET_MOTION = 0;
	private static final int VIBRATOR_SHORT = 25;
	private static final int VIBRATOR_NORMAL = 50;
	private static final int VIBRATOR_LONG = 100;
	private static final int PREPARATION_INTERVAL = 1000;
	private static final int GET_INTERVAL = 30;

	private Button mGetMotion;
	private TextView mSecond;
	private TextView mCount;
	private Vibrator mVibrator;
	private SensorManager mSensorManager;
	private Sensor mAccelerometerSensor;
	private Sensor mGyroscopeSensor;
	private Registration mRegistration;

	private int countdown = 4;
	private int countData = 0;
	private int countTime = 0;
	private float[] mOriginAcceleration = new float[3];
	private float[] mOriginGyro = new float[3];
	private float[][][] mAcceleration = new float[3][3][100];
	private float[][][] mGyro = new float[3][3][100];


	public GetData(Registration registration, Button getMotion, TextView second, TextView count, Vibrator vibrator,
	               Context context) {
		mRegistration = registration;
		mGetMotion = getMotion;
		mSecond = second;
		mCount = count;
		mVibrator = vibrator;

		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mGyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
	}


	@Override
	public void run() {
		get(PREPARATION);
	}


	@Override
	public void dispatchMessage(@NonNull Message msg) {
		switch (msg.what) {
			case 0:
				mSecond.setText(String.valueOf(countdown));
				mVibrator.vibrate(VIBRATOR_SHORT);
				break;
			case 1:
				mSecond.setText("Start");
				mVibrator.vibrate(VIBRATOR_LONG);
				mGetMotion.setText("Getting data");
				break;
			case 2:
				mSecond.setText("3");
				mVibrator.vibrate(VIBRATOR_NORMAL);
				break;
			case 3:
				mSecond.setText("2");
				mVibrator.vibrate(VIBRATOR_NORMAL);
				break;
			case 4:
				mSecond.setText("1");
				mVibrator.vibrate(VIBRATOR_NORMAL);
				break;
			case 5:
				mVibrator.vibrate(VIBRATOR_LONG);
				mCount.setText("回");
				mGetMotion.setText("モーションデータ取得");
				break;
			case 6:
				mSecond.setText("2");
				mGetMotion.setClickable(true);
				break;
			case 7:
				mSecond.setText("1");
				mGetMotion.setClickable(true);
				break;
			case 8:
				mRegistration.finishGetMotion(mAcceleration, mGyro);
				break;
			case 10:
				mGetMotion.setClickable(true);
				mSecond.setText("3");
				mGetMotion.setText("モーションデータ取得");
				break;
		}
	}


	private void get(int status) {
		switch (status) {
			case PREPARATION:
				countdown--;
				switch (countdown) {
					case 0:
						super.sendEmptyMessage(1);
						get(GET_MOTION);
						break;
					default:
						super.sendEmptyMessage(0);
						try {
							Thread.sleep(PREPARATION_INTERVAL);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						get(PREPARATION);
						break;
				}
				break;
			case GET_MOTION:
				if (countData < 100) {
					for (int i = 0; i < 3; i++) {
						mAcceleration[countTime][i][countData] = mOriginAcceleration[i];
						mGyro[countTime][i][countData] = mOriginGyro[i];
					}

					countData++;

					switch (countData) {
						case 1:
							super.sendEmptyMessage(2);
							break;
						case 33:
							super.sendEmptyMessage(3);
							break;
						case 66:
							super.sendEmptyMessage(4);
							break;
					}
					try {
						Thread.sleep(GET_INTERVAL);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					get(GET_MOTION);
				} else {
					countTime++;
					countData = 0;
					countdown = 4;
					super.sendEmptyMessage(5);

					switch (countTime) {
						case 1:
							sendEmptyMessage(6);
							break;
						case 2:
							sendEmptyMessage(7);
							break;
						case 3:
							sendEmptyMessage(8);
							break;
					}
				}
				break;
		}
	}


	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) mOriginAcceleration = event.values.clone();
		if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) mOriginGyro = event.values.clone();
	}


	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}


	public void registrationSensor() {
		mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
		mSensorManager.registerListener(this, mGyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
	}


	public void unRegistrationSensor() {
		mSensorManager.unregisterListener(this);
	}


	public void reset() {
		countdown = 4;
		countData = 0;
		countTime = 0;
		sendEmptyMessage(10);
	}
}
