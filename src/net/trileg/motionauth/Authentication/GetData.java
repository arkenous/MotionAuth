package net.trileg.motionauth.Authentication;

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
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.TextView;
import net.trileg.motionauth.Utility.Enum.STATUS;

import java.util.ArrayList;

import static android.hardware.Sensor.TYPE_GYROSCOPE;
import static android.hardware.Sensor.TYPE_LINEAR_ACCELERATION;
import static android.hardware.SensorManager.SENSOR_DELAY_GAME;
import static net.trileg.motionauth.Utility.Enum.NUM_AXIS;

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
  private static final int LEAST_MOTION_LENGTH = 10;

  private Button mGetMotion;
  private TextView mSecond;
  private Vibrator mVibrator;
  private SensorManager mSensorManager;
  private Sensor mLinearAccelerationSensor;
  private Sensor mGyroscopeSensor;
  private Authentication mAuthentication;

  private int collectTime = 0;
  private int countSecond = 0;

  private STATUS mStatus;

  private float[] mOriginLinearAcceleration = new float[NUM_AXIS];
  private float[] mOriginGyro = new float[NUM_AXIS];
  private ArrayList<ArrayList<Float>> mLinearAcceleration = new ArrayList<>();
  private ArrayList<ArrayList<Float>> mGyroscope = new ArrayList<>();


  /**
   * @param authentication Authentication class context.
   * @param getMotion      Get motion button.
   * @param second         Second TextView.
   * @param vibrator       Vibrator.
   * @param status         Status of touch event.
   */
  GetData(Authentication authentication, Button getMotion, TextView second,
          Vibrator vibrator, STATUS status) {
    mAuthentication = authentication;
    mGetMotion = getMotion;
    mSecond = second;
    mVibrator = vibrator;
    mStatus = status;

    mSensorManager = (SensorManager) mAuthentication.getSystemService(Context.SENSOR_SERVICE);
    mLinearAccelerationSensor = mSensorManager.getDefaultSensor(TYPE_LINEAR_ACCELERATION);
    mGyroscopeSensor = mSensorManager.getDefaultSensor(TYPE_GYROSCOPE);
  }


  void changeStatus(STATUS status) {
    mStatus = status;
  }


  @Override
  public void run() {
    mLinearAcceleration.clear();
    mGyroscope.clear();
    for (int axis = 0; axis < NUM_AXIS; axis++) {
      mLinearAcceleration.add(new ArrayList<Float>());
      mGyroscope.add(new ArrayList<Float>());
    }
    collect();
  }


  /**
   * Collecting data from sensor.
   */
  private void collect() {
    if (mStatus == STATUS.DOWN) {
      for (int axis = 0; axis < NUM_AXIS; axis++) {
        mLinearAcceleration.get(axis).add(mOriginLinearAcceleration[axis]);
        mGyroscope.get(axis).add(mOriginGyro[axis]);
      }

      try {
        Thread.sleep(GET_INTERVAL);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      collectTime++;
      if (collectTime % 33 == 0) {
        countSecond++;
        super.sendEmptyMessage(1);
      }
      collect();
    } else {
      if (collectTime < LEAST_MOTION_LENGTH) {
        sendEmptyMessage(0);
      } else {
        sendEmptyMessage(5); // Completed collecting data.
      }
    }
  }


  @Override
  public void dispatchMessage(@NonNull Message msg) {
    switch (msg.what) {
      case 0:
        AlertDialog.Builder alert = new AlertDialog.Builder(mAuthentication);
        alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
          @Override
          public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            return keyCode == KeyEvent.KEYCODE_BACK;
          }
        });
        alert.setCancelable(false);
        alert.setTitle("モーション取り直し");
        alert.setMessage("モーションの入力時間が短すぎます．もう少し長めに入力してください．");
        alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            reset();
          }
        });
        alert.show();
      case 1:
        mVibrator.vibrate(VIBRATOR_SHORT);
        mSecond.setText(String.valueOf(countSecond));
        break;
      case 5:
        mVibrator.vibrate(VIBRATOR_LONG);
        mAuthentication.finishGetMotion(mLinearAcceleration, mGyroscope);
        break;
      case 10:
        mSecond.setText("1");
        mGetMotion.setText("モーションデータ取得");
        break;
    }
  }


  @Override
  public void onSensorChanged(SensorEvent event) {
    if (event.sensor.getType() == TYPE_LINEAR_ACCELERATION)
      mOriginLinearAcceleration = event.values.clone();
    if (event.sensor.getType() == TYPE_GYROSCOPE)
      mOriginGyro = event.values.clone();
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
    collectTime = 0;
    countSecond = 0;
    sendEmptyMessage(10);
  }
}
