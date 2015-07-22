package net.trileg.motionauth.Registration;

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
import net.trileg.motionauth.Utility.Enum.STATUS;

import java.util.ArrayList;

/**
 * Collecting data and return it.
 * Data collecting is running on the ExecutorService thread.
 *
 * @author Kensuke Kosaka
 */
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
	private int countTime = 0;

	private STATUS mStatus;

	private float[] mOriginAcceleration = new float[3];
	private float[] mOriginGyro = new float[3];

	private ArrayList<ArrayList<Float>> mAccelerationPerTime = new ArrayList<>();
	private ArrayList<ArrayList<Float>> mGyroPerTime = new ArrayList<>();
	private ArrayList<ArrayList<ArrayList<Float>>> mAcceleration = new ArrayList<>();
	private ArrayList<ArrayList<ArrayList<Float>>> mGyro = new ArrayList<>();


	public GetData(Registration registration, Button getMotion, TextView second, TextView count, Vibrator vibrator,
	               STATUS status, Context context) {
		mRegistration = registration;
		mGetMotion = getMotion;
		mSecond = second;
		mCount = count;
		mVibrator = vibrator;
		mStatus = status;

		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mGyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
	}


	public void changeStatus(STATUS status) {
		mStatus = status;
	}


	@Override
	public void run() {
		mAccelerationPerTime.clear();
		mGyroPerTime.clear();
		for (int i = 0; i < 3; i++) {
			mAccelerationPerTime.add(new ArrayList<Float>());
			mGyroPerTime.add(new ArrayList<Float>());
		}
		collect(PREPARATION);
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
				break;
			case 7:
				mSecond.setText("1");
				break;
			case 8:
				mRegistration.finishGetMotion(mAcceleration, mGyro);
				break;
			case 10:
				mSecond.setText("3");
				mGetMotion.setText("モーションデータ取得");
				break;
		}
	}


	/**
	 * Collecting data from sensor.
	 *
	 * @param stage Stage of PREPARATION or GET_MOTION
	 */
	private void collect(int stage) {
		switch (stage) {
			case PREPARATION:
				countdown--;
				switch (countdown) {
					case 0:
						super.sendEmptyMessage(1);
						collect(GET_MOTION);
						break;
					default:
						super.sendEmptyMessage(0);
						try {
							Thread.sleep(PREPARATION_INTERVAL);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						collect(PREPARATION);
						break;
				}
				break;
			case GET_MOTION:
				if (mStatus == STATUS.DOWN) {
					for (int i = 0; i < 3; i++) {
						mAccelerationPerTime.get(i).add(mOriginAcceleration[i]);
						mGyroPerTime.get(i).add(mOriginGyro[i]);
					}

					try {
						Thread.sleep(GET_INTERVAL);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					collect(GET_MOTION);
				} else {
					// Correct data per time finished

					mAcceleration.add(new ArrayList<>(mAccelerationPerTime));
					mGyro.add(new ArrayList<>(mGyroPerTime));
					countTime++;
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


	/**
	 * Register sensor listener.
	 */
	public void registrationSensor() {
		mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
		mSensorManager.registerListener(this, mGyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
	}


	/**
	 * Un-Register sensor listener.
	 */
	public void unRegistrationSensor() {
		mSensorManager.unregisterListener(this);
	}


	/**
	 * Reset value using count of collecting data.
	 */
	public void reset() {
		countdown = 4;
		countTime = 0;
		sendEmptyMessage(10);
	}
}
