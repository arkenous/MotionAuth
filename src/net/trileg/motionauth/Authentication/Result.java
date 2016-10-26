package net.trileg.motionauth.Authentication;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.widget.Button;
import net.trileg.motionauth.Lowpass.Fourier;
import net.trileg.motionauth.Processing.*;
import net.trileg.motionauth.Utility.Enum;
import net.trileg.motionauth.Utility.ManageData;

import static android.content.Context.MODE_PRIVATE;
import static android.util.Log.INFO;
import static net.trileg.motionauth.Authentication.InputName.userName;
import static net.trileg.motionauth.Utility.Enum.MEASURE.CORRECT;
import static net.trileg.motionauth.Utility.Enum.MEASURE.PERFECT;
import static net.trileg.motionauth.Utility.Enum.SENSOR_DELAY_TIME;
import static net.trileg.motionauth.Utility.LogUtil.log;

/**
 * Authenticate and show result to user.
 *
 * @author Kensuke Kosaka
 */
class Result extends Handler implements Runnable {
  private static final int READ_DATA = 1;
  private static final int FORMAT = 2;
  private static final int AMPLIFY = 3;
  private static final int FOURIER = 4;
  private static final int CONVERT = 5;
  private static final int COSINE_SIMILARITY = 6;
  private static final int FINISH = 10;

  private ManageData manageData = new ManageData();
  private Formatter formatter = new Formatter();
  private Amplifier amplifier = new Amplifier();
  private Fourier fourier = new Fourier();
  private Calc calc = new Calc();
  private Adjuster adjuster = new Adjuster();
  private CosSimilarity cosSimilarity = new CosSimilarity();

  private Authentication authentication;
  private Button getMotion;
  private ProgressDialog progressDialog;
  private double amp;

  private float[][] linearAccel;
  private float[][] gyro;
  private double[][] registeredVector;
  private boolean result = false;


  Result(Authentication authentication, float[][] linearAccel, float[][] gyro,
         Button getMotion, ProgressDialog progressDialog) {
    this.authentication = authentication;
    this.linearAccel = linearAccel;
    this.gyro = gyro;
    this.getMotion = getMotion;
    this.progressDialog = progressDialog;
  }


  @Override
  public void run() {
    manageData.writeFloatData(userName, "AuthenticationRaw", "linearAcceleration", linearAccel);
    manageData.writeFloatData(userName, "AuthenticationRaw", "gyroscope", gyro);

    readRegisteredData();
    manageData.writeDoubleTwoArrayData(userName, "AuthRegistered", "vector", registeredVector);
    result = calculate(linearAccel, gyro);

    this.sendEmptyMessage(FINISH);
  }


  @Override
  public void dispatchMessage(@NonNull Message msg) {
    if (getMotion.isClickable()) getMotion.setClickable(false);
    switch (msg.what) {
      case READ_DATA:
        progressDialog.setMessage("登録データの読み込み中");
        break;
      case FORMAT:
        progressDialog.setMessage("データのフォーマット中");
        break;
      case AMPLIFY:
        progressDialog.setMessage("データの増幅処理中");
        break;
      case FOURIER:
        progressDialog.setMessage("フーリエ変換中");
        break;
      case CONVERT:
        progressDialog.setMessage("データの変換中");
        break;
      case COSINE_SIMILARITY:
        progressDialog.setMessage("コサイン類似度を算出中");
        break;
      case FINISH:
        progressDialog.dismiss();
        if (!result) {
          log(INFO, "False authentication");
          AlertDialog.Builder alert = new AlertDialog.Builder(authentication);
          alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
              return keyCode == KeyEvent.KEYCODE_BACK;
            }
          });
          alert.setTitle("認証失敗");
          alert.setMessage("認証に失敗しました");
          alert.setCancelable(false);
          alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              authentication.reset();
            }
          });
          alert.show();
        } else {
          log(INFO, "Success authentication");
          AlertDialog.Builder alert = new AlertDialog.Builder(authentication);
          alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
              return keyCode == KeyEvent.KEYCODE_BACK;
            }
          });
          alert.setTitle("認証成功");
          alert.setMessage("認証に成功しました．\nスタート画面に戻ります");
          alert.setCancelable(false);
          alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              authentication.finishAuthentication();
            }
          });
          alert.show();
        }
        break;
    }
  }


  /**
   * Read already registered data.
   */
  private void readRegisteredData() {
    this.sendEmptyMessage(READ_DATA);
    registeredVector = manageData.readRegisteredData(authentication, userName);

    SharedPreferences preferences = authentication.getApplicationContext().getSharedPreferences("MotionAuth", MODE_PRIVATE);
    String registeredAmplify = preferences.getString(userName + "amplify", "");
    if ("".equals(registeredAmplify)) throw new RuntimeException();
    amp = Double.valueOf(registeredAmplify);
  }


  /**
   * Calculate data and authenticate.
   *
   * @param linearAccel Linear acceleration data which collect in Authentication.GetData.
   * @param gyro        Gyroscope data which collect in Authentication.GetData.
   * @return true if authentication is succeed, otherwise false.
   */
  private boolean calculate(float[][] linearAccel, float[][] gyro) {
    linearAccel = adjuster.adjust(linearAccel, registeredVector[0].length);
    gyro = adjuster.adjust(gyro, registeredVector[0].length);

    this.sendEmptyMessage(FORMAT);
    double[][] linearAcceleration = formatter.convertFloatToDouble(linearAccel);
    double[][] gyroscope = formatter.convertFloatToDouble(gyro);

    this.sendEmptyMessage(AMPLIFY);
    linearAcceleration = amplifier.Amplify(linearAcceleration, amp);
    gyroscope = amplifier.Amplify(gyroscope, amp);

    this.sendEmptyMessage(FOURIER);
    linearAcceleration = fourier.LowpassFilter(linearAcceleration, "linearAccel", userName);
    gyroscope = fourier.LowpassFilter(gyroscope, "gyro", userName);

    this.sendEmptyMessage(CONVERT);
    double[][] linearDistance = calc.accelToDistance(linearAcceleration, SENSOR_DELAY_TIME);
    double[][] angle = calc.gyroToAngle(gyroscope, SENSOR_DELAY_TIME);

    RotateVector rotateVector = new RotateVector();
    double[][] vector = rotateVector.rotate(linearDistance, angle);

    manageData.writeDoubleTwoArrayData(userName, "AuthAfterCalcData", "vector", vector);

    manageData.writeDoubleTwoArrayData(userName, "NNTest", "RegisteredVector", registeredVector);
    manageData.writeDoubleTwoArrayData(userName, "NNTest", "InputVector", vector);

    // コサイン類似度を測る
    log(INFO, "Before Cosine Similarity");
    double vectorSimilarity = cosSimilarity.cosSimilarity(vector, registeredVector);
    log(INFO, "After Cosine Similarity");

    this.sendEmptyMessage(COSINE_SIMILARITY);
    Enum.MEASURE measure = cosSimilarity.measure(vectorSimilarity);
    return measure == PERFECT || measure == CORRECT;
  }
}
