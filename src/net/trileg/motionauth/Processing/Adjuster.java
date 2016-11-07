package net.trileg.motionauth.Processing;

import java.util.ArrayList;

import static android.util.Log.INFO;
import static net.trileg.motionauth.Utility.Enum.NUM_AXIS;
import static net.trileg.motionauth.Utility.LogUtil.log;

/**
 * Adjustment data length.
 * Set adjustment base to longest data.
 *
 * @author Kensuke Kosaka
 */
public class Adjuster {

  public ArrayList<float[][][]> adjust(float[][][] linearAcceleration, float[][][] gyroscope) {
    log(INFO);
    // Get max length of each time data
    ArrayList<Integer> linearAccelerationLengthList = new ArrayList<>();
    ArrayList<Integer> gyroscopeLengthList = new ArrayList<>();
    for (float[][] aLinearAcceleration : linearAcceleration) linearAccelerationLengthList.add(aLinearAcceleration[0].length);
    for (float[][] aGyroscope : gyroscope) gyroscopeLengthList.add(aGyroscope[0].length);

    int maxTime = 0;
    int maxLength = 0;

    for (int time = 0; time < linearAcceleration.length; time++) {
      if (maxLength < linearAccelerationLengthList.get(time)) {
        maxTime = time;
        maxLength = linearAccelerationLengthList.get(time);
      }
    }

    for (int time = 0; time < gyroscope.length; time++) {
      if (maxLength < gyroscopeLengthList.get(time)) {
        maxTime = time;
        maxLength = gyroscopeLengthList.get(time);
      }
    }

    // Adjust data length to even number for low pass filtering
    if (maxLength % 2 != 0) maxLength -= 1;

    float[][][] linearAccelerationArray = new float[linearAcceleration.length][linearAcceleration[0].length][maxLength];
    float[][][] gyroscopeArray = new float[gyroscope.length][gyroscope[0].length][maxLength];

    for (int time = 0; time < linearAcceleration.length; time++) {
      if (time == maxTime) {
        // データ数が最も多かった回
        // 最も多かった方のセンサにはデータをそのまま入れ，そうでない方には足りない分をゼロ足し
        if (linearAccelerationLengthList.get(time) > gyroscopeLengthList.get(time)) {
          int diff = maxLength - gyroscopeLengthList.get(time);
          for (int axis = 0; axis < NUM_AXIS; axis++) {
            for (int length = 0; length < maxLength; length++) {
              linearAccelerationArray[time][axis][length] = linearAcceleration[time][axis][length];
              if (length < maxLength - diff) gyroscopeArray[time][axis][length] = gyroscope[time][axis][length];
              else gyroscopeArray[time][axis][length] = 0;
            }
          }
        } else {
          int diff = maxLength - linearAccelerationLengthList.get(time);
          for (int axis = 0; axis < NUM_AXIS; axis++) {
            for (int length = 0; length < maxLength; length++) {
              gyroscopeArray[time][axis][length] = gyroscope[time][axis][length];
              if (length < maxLength - diff) linearAccelerationArray[time][axis][length] = linearAcceleration[time][axis][length];
              else linearAccelerationArray[time][axis][length] = 0;
            }
          }
        }
      } else {
        int linearAccelerationDiff = maxLength - linearAccelerationLengthList.get(time);
        int gyroscopeDiff = maxLength - gyroscopeLengthList.get(time);

        for (int axis = 0; axis < NUM_AXIS; axis++) {
          for (int length = 0; length < maxLength; length++) {
            if (length < maxLength - linearAccelerationDiff) linearAccelerationArray[time][axis][length] = linearAcceleration[time][axis][length];
            else linearAccelerationArray[time][axis][length] = 0;

            if (length < maxLength - gyroscopeDiff) gyroscopeArray[time][axis][length] = gyroscope[time][axis][length];
            else gyroscopeArray[time][axis][length] = 0;
          }
        }
      }
    }

    ArrayList<float[][][]> result = new ArrayList<>();
    result.add(linearAccelerationArray);
    result.add(gyroscopeArray);
    return result;
  }


  public float[][] adjust(float[][] input, int registeredDataLength) {
    log(INFO);
    float[][] result = new float[input.length][registeredDataLength];

    if (registeredDataLength < input[0].length) {
      // New data is longer than registered data.
      for (int axis = 0; axis < NUM_AXIS; axis++) {
        for (int length = 0; length < registeredDataLength; length++) {
          result[axis][length] = input[axis][length];
        }
      }
    } else if (registeredDataLength > input[0].length) {
      // New data is shorter than registered data.
      int diff = registeredDataLength - input[0].length;

      for (int axis = 0; axis < NUM_AXIS; axis++) {
        for (int length = 0; length < registeredDataLength; length++) {
          if (length < registeredDataLength - diff) {
            result[axis][length] = input[axis][length];
          } else {
            result[axis][length] = 0;
          }
        }
      }
    } else {
      for (int axis = 0; axis < NUM_AXIS; axis++) {
        for (int length = 0; length < registeredDataLength; length++) {
          result[axis][length] = input[axis][length];
        }
      }
    }

    return result;
  }
}
