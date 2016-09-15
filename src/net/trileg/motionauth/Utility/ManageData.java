package net.trileg.motionauth.Utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import net.trileg.motionauth.Processing.CipherCrypt;

import java.io.*;

import static android.content.Context.MODE_PRIVATE;
import static android.os.Environment.MEDIA_MOUNTED;
import static android.util.Log.*;
import static java.io.File.separator;
import static net.trileg.motionauth.Utility.Enum.NUM_AXIS;
import static net.trileg.motionauth.Utility.LogUtil.log;


/**
 * Write data to SD Card.
 *
 * @author Kensuke Kosaka
 */
public class ManageData {
  private static final String APP_NAME = "MotionAuth";
  private FileOutputStream fos;
  private OutputStreamWriter osw;
  private BufferedWriter bw;


  /**
   * @param userName   User name.
   * @param dataName   Data name.
   * @param sensorName Sensor name.
   * @param data       Double type 2-array data to write.
   * @return Return true when write data complete, otherwise false.
   */
  public boolean writeDoubleTwoArrayData(String userName, String dataName, String sensorName, double[][] data) {
    log(INFO);

    String status = Environment.getExternalStorageState();
    if (!status.equals(MEDIA_MOUNTED)) {
      log(ERROR, "SDCard not mounted");
      return false;
    }

    String SD_PATH = Environment.getExternalStorageDirectory().getPath();
    String FOLDER_PATH = SD_PATH + separator + APP_NAME + separator + userName + separator + dataName;

    File file = new File(FOLDER_PATH);

    try {
      if (!file.exists()) {
        if (!file.mkdirs()) {
          log(ERROR, "Make directory error");
        }
      }
    } catch (Exception e) {
      log(ERROR, e.getMessage(), e.getCause());
      return false;
    }

    try {
      String filePath = FOLDER_PATH + separator + sensorName + ".dat";
      file = new File(filePath);

      fos = new FileOutputStream(file, false);
      osw = new OutputStreamWriter(fos, "UTF-8");
      bw = new BufferedWriter(osw);

      for (int item = 0; item < data[0].length; item++) {
        bw.write(data[0][item] + "," + data[1][item] + "," + data[2][item] + ",\n");
      }

      bw.flush();
      bw.close();
      osw.close();
      fos.close();
    } catch (Exception e) {
      log(ERROR, e.getMessage(), e.getCause());
      return false;
    }
    return true;
  }


  /**
   * @param userName   User name.
   * @param dataName   Data name.
   * @param sensorName Sensor name.
   * @param data       Double type 3-array data to write.
   */
  public void writeDoubleThreeArrayData(String userName, String dataName, String sensorName, double[][][] data) {
    log(INFO);

    String status = Environment.getExternalStorageState();
    if (!status.equals(MEDIA_MOUNTED)) {
      log(ERROR, "SDCard not mounted");
    }

    String SD_PATH = Environment.getExternalStorageDirectory().getPath();
    String FOLDER_PATH = SD_PATH + separator + APP_NAME + separator + userName + separator + dataName;

    File file = new File(FOLDER_PATH);

    try {
      if (!file.exists()) {
        if (!file.mkdirs()) {
          log(ERROR, "Make directory Error");
        }
      }
    } catch (Exception e) {
      log(ERROR, e.getMessage(), e.getCause());
    }

    try {
      for (int time = 0; time < data.length; time++) {
        String filePath = FOLDER_PATH + separator + sensorName + String.valueOf(time) + ".dat";
        file = new File(filePath);

        fos = new FileOutputStream(file, false);
        osw = new OutputStreamWriter(fos, "UTF-8");
        bw = new BufferedWriter(osw);

        for (int item = 0; item < data[time][0].length; item++) {
          bw.write(data[time][0][item] + "," + data[time][1][item] + "," + data[time][2][item] + ",\n");
        }

        bw.flush();
        bw.close();
        osw.close();
        fos.close();
      }
    } catch (Exception e) {
      log(ERROR, e.getMessage(), e.getCause());
    }
  }


  /**
   * Save authentication key data which is collected from Registration.
   * @param userName User name.
   * @param averageVector Vector data.
   * @param ampValue Amplifier value.
   * @param context Caller context.
   */
  public void writeRegisterData(String userName, double[][] averageVector, double ampValue, Context context) {
    log(INFO);

    CipherCrypt mCipherCrypt = new CipherCrypt(context);

    String[][] averageVectorStr = new String[averageVector.length][averageVector[0].length];

    for (int axis = 0; axis < NUM_AXIS; axis++) {
      for (int item = 0; item < averageVector[axis].length; item++) {
        averageVectorStr[axis][item] = String.valueOf(averageVector[axis][item]);
      }
    }

    // 暗号化
    String[][] encryptedAverageVectorStr = mCipherCrypt.encrypt(averageVectorStr);

    // 配列データを特定文字列を挟んで連結する
    ConvertArrayAndString mConvertArrayAndString = new ConvertArrayAndString();
    String registerVectorData = mConvertArrayAndString.arrayToString(encryptedAverageVectorStr);

    SharedPreferences userPref = context.getApplicationContext().getSharedPreferences("UserList", MODE_PRIVATE);
    SharedPreferences.Editor userPrefEditor = userPref.edit();

    userPrefEditor.putString(userName, "");
    userPrefEditor.apply();

    SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(APP_NAME, MODE_PRIVATE);
    SharedPreferences.Editor editor = preferences.edit();

    editor.putString(userName+"vector", registerVectorData);
    editor.putString(userName+"apmlify", String.valueOf(ampValue));
    editor.apply();
  }


  /**
   * Read registered data from SharedPreferences
   *
   * @param context  Context use to get Application unique SharedPreferences.
   * @param userName User name.
   * @return Double type 2-array registered data list.
   */
  public double[][] readRegisteredData(Context context, String userName) {
    log(INFO);
    Context mContext = context.getApplicationContext();

    SharedPreferences preferences = mContext.getSharedPreferences(APP_NAME, MODE_PRIVATE);

    String registeredVectorData = preferences.getString(userName+"vector", "");

    if ("".equals(registeredVectorData)) throw new RuntimeException();

    ConvertArrayAndString mConvertArrayAndString = new ConvertArrayAndString();
    CipherCrypt mCipherCrypt = new CipherCrypt(context);

    String[][] decryptedVector = mCipherCrypt.decrypt(mConvertArrayAndString.stringToArray(registeredVectorData));

    double[][] vector = new double[decryptedVector.length][decryptedVector[0].length];

    for (int axis = 0; axis < NUM_AXIS; axis++) {
      for (int item = 0; item < decryptedVector[axis].length; item++) {
        vector[axis][item] = Double.valueOf(decryptedVector[axis][item]);
      }
    }

    return vector;
  }


  /**
   * @param userName   User name.
   * @param dataName   Data name.
   * @param sensorName Sensor name.
   * @param data       Float type 3-array list data.
   */
  public void writeFloatData(String userName, String dataName, String sensorName, float[][][] data) {
    log(INFO);

    if (!Environment.getExternalStorageState().equals(MEDIA_MOUNTED)) {
      log(ERROR, "SD-Card not mounted");
      return;
    }

    String SD_PATH = Environment.getExternalStorageDirectory().getPath();
    String DIR_PATH = SD_PATH + separator + APP_NAME + separator + userName + separator + dataName;

    File file = new File(DIR_PATH);
    try {
      if (!file.exists()) {
        if (!file.mkdirs()) {
          log(DEBUG, "Make directory Error");
        }
      }
    } catch (Exception e) {
      log(ERROR, e.getMessage(), e.getCause());
    }

    try {
      for (int time = 0; time < data.length; time++) {
        String filePath = DIR_PATH + separator + sensorName + String.valueOf(time) + ".dat";
        file = new File(filePath);

        fos = new FileOutputStream(file, false);
        osw = new OutputStreamWriter(fos, "UTF-8");
        bw = new BufferedWriter(osw);

        for (int item = 0; item < data[time][0].length; item++) {
          bw.write(data[time][0][item] + "," + data[time][1][item] + "," + data[time][2][item] + ",\n");
        }

        bw.flush();
        bw.close();
        osw.close();
        fos.close();
      }
    } catch (IOException e) {
      log(ERROR, e.getMessage(), e.getCause());
    }
  }


  public void writeFloatData(String userName, String dataName, String sensorName, float[][] data) {
    log(INFO);

    if (!Environment.getExternalStorageState().equals(MEDIA_MOUNTED)) {
      log(ERROR, "SD-Card not mounted");
      return;
    }

    String SD_PATH = Environment.getExternalStorageDirectory().getPath();
    String DIR_PATH = SD_PATH + separator + APP_NAME + separator + userName + separator + dataName;

    File file = new File(DIR_PATH);
    try {
      if (!file.exists()) {
        if (!file.mkdirs()) {
          log(DEBUG, "Make directory Error");
        }
      }
    } catch (Exception e) {
      log(ERROR, e.getMessage(), e.getCause());
    }

    try {
      String filePath = DIR_PATH + separator + sensorName + ".dat";
      file = new File(filePath);

      fos = new FileOutputStream(file, false);
      osw = new OutputStreamWriter(fos, "UTF-8");
      bw = new BufferedWriter(osw);

      for (int item = 0; item < data[0].length; item++) {
        bw.write(data[0][item] + "," + data[1][item] + "," + data[2][item] + ",\n");
      }

      bw.flush();
      bw.close();
      osw.close();
      fos.close();
    } catch (IOException e) {
      log(ERROR, e.getMessage(), e.getCause());
    }
  }
}
