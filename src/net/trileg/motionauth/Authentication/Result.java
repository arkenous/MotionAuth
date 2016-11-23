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
import net.trileg.motionauth.Processing.Adjuster;
import net.trileg.motionauth.Processing.Amplifier;
import net.trileg.motionauth.Processing.Calc;
import net.trileg.motionauth.Processing.CosSimilarity;
import net.trileg.motionauth.Processing.Formatter;
import net.trileg.motionauth.Processing.RotateVector;
import net.trileg.motionauth.Utility.Enum;
import net.trileg.motionauth.Utility.ManageData;

import static android.content.Context.MODE_PRIVATE;
import static android.util.Log.DEBUG;
import static android.util.Log.INFO;
import static net.trileg.motionauth.Authentication.InputName.userName;
import static net.trileg.motionauth.Utility.Enum.MEASURE.CORRECT;
import static net.trileg.motionauth.Utility.Enum.MEASURE.INCORRECT;
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
  private static final int NN_OUT = 7;
  private static final int FINISH = 8;

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
  private String[] learnResult;

  // C++で書いたMLPライブラリの呼び出しに必要
  static {
    System.loadLibrary("mlp-lib");
  }


  Result(float[][] linearAccel, float[][] gyro, Button getMotion, ProgressDialog progressDialog, Authentication authentication) {
    log(INFO);
    this.linearAccel = linearAccel;
    this.gyro = gyro;
    this.getMotion = getMotion;
    this.progressDialog = progressDialog;
    this.authentication = authentication;
  }


  @Override
  public void run() {
    manageData.writeFloatData(userName, "AuthRaw", "linearAcceleration", linearAccel);
    manageData.writeFloatData(userName, "AuthRaw", "gyroscope", gyro);

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
      case NN_OUT:
        progressDialog.setMessage("ニューラルネットワークの計算中");
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
    log(INFO);
    this.sendEmptyMessage(READ_DATA);
    registeredVector = manageData.readRegisteredData(authentication, userName);

    SharedPreferences preferences = authentication.getApplicationContext().getSharedPreferences("MotionAuth", MODE_PRIVATE);
    String registeredAmplify = preferences.getString(userName + "amplify", "");
    if ("".equals(registeredAmplify)) throw new RuntimeException();
    amp = Double.valueOf(registeredAmplify);
    learnResult = manageData.readLearnResult(authentication, userName);
  }


  /**
   * Calculate data and authenticate.
   *
   * @param linearAccel Linear acceleration data which collect in Authentication.GetData.
   * @param gyro        Gyroscope data which collect in Authentication.GetData.
   * @return true if authentication is succeed, otherwise false.
   */
  private boolean calculate(float[][] linearAccel, float[][] gyro) {
    log(INFO);
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

    // 学習済みニューラルネットワークの出力を得る
    this.sendEmptyMessage(NN_OUT);
    double[] x = manipulateMotionDataToNeuralNetwork(vector);
    double[] result = out(1, learnResult, x);
    for (int i = 0; i < result.length; i++) log(DEBUG, "Neural Network Output["+i+"]: "+result[i]);

    manageData.writeDoubleOneArrayData(userName, "AuthNNOut", "NNOut", result);

    // コサイン類似度を測る
    log(DEBUG, "Before Cosine Similarity");
    double vectorCosSimilarity = cosSimilarity.cosSimilarity(vector, registeredVector);
    log(DEBUG, "After Cosine Similarity");

    manageData.writeDoubleSingleData(userName, "AuthCosSimilarity", "vectorCosSimilarity", vectorCosSimilarity);

    this.sendEmptyMessage(COSINE_SIMILARITY);
    Enum.MEASURE measure = cosSimilarity.measure(vectorCosSimilarity);

    // ニューラルネットワークの結果，正規モーションでないと判定された場合
    if (result[0] > 0.1) return false;

    // コサイン類似度が低い場合
    if (measure == INCORRECT) return false;
    else {
      // コサイン類似度が0.4より高く，ニューラルネットワークの結果，正規モーションであると判定された場合
      if (measure == PERFECT || measure == CORRECT) {
        //TODO コサイン類似度が0.6より高ければ，ニューラルネットワークに追加学習を行う
        log(DEBUG, "追加学習を行う");
      }
      return true;
    }
  }

  /**
   * モーションデータをニューラルネットワークの入力用に組み直す
   * @param input 組み直すモーションデータ
   * @return 組み直したモーションデータ
   */
  private double[] manipulateMotionDataToNeuralNetwork(double[][] input) {
    log(INFO);
    double[] output = new double[input[0].length * 3]; // データ長 * 軸数

    for (int data = 0, dataPerAxis = 0; data < input[0].length * 3; data += 3, dataPerAxis++) {
      output[data] = input[0][dataPerAxis];
      output[data + 1] = input[1][dataPerAxis];
      output[data + 2] = input[2][dataPerAxis];
    }
    return output;
  }


  /**
   * C++ネイティブのニューラルネットワーク出力メソッド
   * @param middleLayer 中間層の層数
   * @param neuronParams SdAとMLPのニューロンパラメータ
   * @param x 入力データ
   * @return 出力データ
   */
  public native double[] out(long middleLayer, String[] neuronParams, double[] x);
}
