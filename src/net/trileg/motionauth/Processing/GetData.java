package net.trileg.motionauth.Processing;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import static android.hardware.Sensor.TYPE_GYROSCOPE;
import static android.hardware.Sensor.TYPE_LINEAR_ACCELERATION;
import static android.hardware.SensorManager.SENSOR_DELAY_FASTEST;
import static android.util.Log.DEBUG;
import static android.util.Log.INFO;
import static net.trileg.motionauth.Utility.Enum.NUM_AXIS;
import static net.trileg.motionauth.Utility.Enum.SENSOR_DELAY_TIME;
import static net.trileg.motionauth.Utility.LogUtil.log;

public class GetData extends Handler implements Callable<ArrayList<ArrayList<Float>>>, SensorEventListener {
  private SensorManager sensorManager;
  private Sensor linearAcceleration;
  private Sensor gyroscope;

  private boolean isAcceleration;
  private ArrayList<ArrayList<Float>> dataPerTime = new ArrayList<>();
  private boolean isActive;
  private int time;
  private long firstMillis;


  public GetData(Context context, boolean isAcceleration) {
    log(INFO);
    time = 0;
    firstMillis = 0;
    this.isAcceleration = isAcceleration;
    this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    if (this.isAcceleration) this.linearAcceleration = sensorManager.getDefaultSensor(TYPE_LINEAR_ACCELERATION);
    else this.gyroscope = sensorManager.getDefaultSensor(TYPE_GYROSCOPE);

    dataPerTime.clear();
    for (int axis = 0; axis < NUM_AXIS; axis++) dataPerTime.add(new ArrayList<Float>());
    isActive = false;
  }


  /**
   * Call from onSensorChanged. Add data array to ArrayList.
   * @param data Data array
   */
  private void collect(float[] data) {
    log(DEBUG);
    for (int axis = 0; axis < NUM_AXIS; axis++) dataPerTime.get(axis).add(data[axis]);
  }


  @Override
  public ArrayList<ArrayList<Float>> call() throws Exception {
    log(INFO);
    registerSensor();

    while(isActive){
      log(DEBUG, "wait...");
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    log(DEBUG, "return data");
    return dataPerTime;
  }


  @Override
  public void onSensorChanged(SensorEvent sensorEvent) {
    if (time == 1){
      long secondMillis = System.currentTimeMillis();
      SENSOR_DELAY_TIME = (secondMillis - firstMillis) / 1000.0;
      time++;
    } else if (time == 0) {
      firstMillis = System.currentTimeMillis();
      time++;
    }
    collect(sensorEvent.values.clone());
  }


  @Override
  public void onAccuracyChanged(Sensor sensor, int i) {}


  /**
   * Register sensor listener, and set isActive flag true.
   */
  private void registerSensor() {
    log(INFO);
    if (this.isAcceleration) sensorManager.registerListener(this, linearAcceleration, SENSOR_DELAY_FASTEST);
    else sensorManager.registerListener(this, gyroscope, SENSOR_DELAY_FASTEST);
    isActive = true;
  }


  /**
   * Un-register sensor listener, and set isActive flag false.
   */
  public void unRegisterSensor() {
    log(INFO);
    sensorManager.unregisterListener(this);
    isActive = false;
  }
}
