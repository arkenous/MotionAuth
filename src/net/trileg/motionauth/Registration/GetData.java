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
import net.trileg.motionauth.Utility.Enum;

import java.util.ArrayList;

import static android.hardware.Sensor.*;
import static android.hardware.SensorManager.SENSOR_DELAY_GAME;
import static net.trileg.motionauth.Utility.Enum.NUM_AXIS;
import static net.trileg.motionauth.Utility.Enum.STATUS.DOWN;

/**
 * Collecting data and return it.
 * Data collecting is running on the ExecutorService thread.
 *
 * @author Kensuke Kosaka
 */
class GetData extends Handler implements Runnable, SensorEventListener {
  private static final int VIBRATOR_SHORT = 25;
  private static final int VIBRATOR_LONG = 100;

  private static final int GET_INTERVAL = 30;

  private Button mGetMotion;
  private TextView mSecond;
  private TextView mCount;
  private Vibrator mVibrator;
  private SensorManager mSensorManager;
  private Sensor mLinearAccelerationSensor;
  private Sensor mGyroscopeSensor;
  private Registration mRegistration;

  private int countTime = 0;
  private int collectTime = 0;

  private Enum.STATUS mStatus;

  private float[] mOriginLinearAcceleration = new float[3];
  private float[] mOriginGyro = new float[3];

  private ArrayList<ArrayList<Float>> mLinearAccelerationPerTime = new ArrayList<>();
  private ArrayList<ArrayList<Float>> mGyroPerTime = new ArrayList<>();
  private ArrayList<ArrayList<ArrayList<Float>>> mLinearAcceleration = new ArrayList<>();
  private ArrayList<ArrayList<ArrayList<Float>>> mGyro = new ArrayList<>();


  GetData(Registration registration, Button getMotion, TextView second, TextView count, Vibrator vibrator,
          Enum.STATUS status, Context context) {
    mRegistration = registration;
    mGetMotion = getMotion;
    mSecond = second;
    mCount = count;
    mVibrator = vibrator;
    mStatus = status;

    mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    mLinearAccelerationSensor = mSensorManager.getDefaultSensor(TYPE_LINEAR_ACCELERATION);
    mGyroscopeSensor = mSensorManager.getDefaultSensor(TYPE_GYROSCOPE);
  }


  void changeStatus(Enum.STATUS status) {
    mStatus = status;
  }


  @Override
  public void run() {
    mLinearAccelerationPerTime.clear();
    mGyroPerTime.clear();
    for (int axis = 0; axis < NUM_AXIS; axis++) {
      mLinearAccelerationPerTime.add(new ArrayList<Float>());
      mGyroPerTime.add(new ArrayList<Float>());
    }
    collect();
  }


  @Override
  public void dispatchMessage(@NonNull Message msg) {
    switch (msg.what) {
      case 1:
        mVibrator.vibrate(VIBRATOR_SHORT);
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
        mRegistration.finishGetMotion(mLinearAcceleration, mGyro);
        break;
      case 10:
        mSecond.setText("3");
        mGetMotion.setText("モーションデータ取得");
        break;
    }
  }


  /**
   * Collecting data from sensor.
   */
  private void collect() {
        if (mStatus == DOWN) {
          for (int axis = 0; axis < NUM_AXIS; axis++) {
            mLinearAccelerationPerTime.get(axis).add(mOriginLinearAcceleration[axis]);
            mGyroPerTime.get(axis).add(mOriginGyro[axis]);
          }

          try {
            Thread.sleep(GET_INTERVAL);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          collectTime++;
          if (collectTime % 33 == 0) {
            super.sendEmptyMessage(1);
          }
          collect();
        } else {
          // Correct data per time finished
          mLinearAcceleration.add(new ArrayList<>(mLinearAccelerationPerTime));
          mGyro.add(new ArrayList<>(mGyroPerTime));
          countTime++;
          collectTime = 0;
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
  }


  @Override
  public void onSensorChanged(SensorEvent event) {
    if (event.sensor.getType() == TYPE_LINEAR_ACCELERATION) mOriginLinearAcceleration = event.values.clone();
    if (event.sensor.getType() == TYPE_GYROSCOPE) mOriginGyro = event.values.clone();
  }


  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
  }


  /**
   * Register sensor listener.
   */
  void registrationSensor() {
    mSensorManager.registerListener(this, mLinearAccelerationSensor, SENSOR_DELAY_GAME);
    mSensorManager.registerListener(this, mGyroscopeSensor, SENSOR_DELAY_GAME);
  }


  /**
   * Un-Register sensor listener.
   */
  void unRegistrationSensor() {
    mSensorManager.unregisterListener(this);
  }


  /**
   * Reset value using count of collecting data.
   */
  void reset() {
    countTime = 0;
    collectTime = 0;
    mLinearAcceleration.clear();
    mGyro.clear();
    sendEmptyMessage(10);
  }
}
