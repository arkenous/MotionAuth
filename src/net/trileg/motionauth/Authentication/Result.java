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
import net.trileg.motionauth.Processing.Calc;
import net.trileg.motionauth.Processing.Formatter;
import net.trileg.motionauth.Processing.RotateVector;
import net.trileg.motionauth.R;
import net.trileg.motionauth.Utility.ManageData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static android.content.Context.MODE_PRIVATE;
import static android.util.Log.DEBUG;
import static android.util.Log.INFO;
import static net.trileg.motionauth.Authentication.InputName.userName;
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
  private static final int FOURIER = 4;
  private static final int CONVERT = 5;
  private static final int NN_OUT = 7;
  private static final int RE_LEARN = 8;
  private static final int FINISH = 9;

  private String hostname;
  private int port;

  private ManageData manageData = new ManageData();
  private Formatter formatter = new Formatter();
  private Fourier fourier = new Fourier();
  private Calc calc = new Calc();
  private Adjuster adjuster = new Adjuster();

  private Authentication authentication;
  private Button getMotion;
  private ProgressDialog progressDialog;

  private float[][] linearAccel;
  private float[][] gyro;
  private boolean result = false;
  private String[] learnResult;
  private int num_dimension;

  // C++で書いたMLPライブラリの呼び出しに必要
  static {
    System.loadLibrary("mlp-lib");
  }


  Result(float[][] linearAccel, float[][] gyro, Button getMotion,
         ProgressDialog progressDialog, Authentication authentication) {
    log(INFO);
    this.linearAccel = linearAccel;
    this.gyro = gyro;
    this.getMotion = getMotion;
    this.progressDialog = progressDialog;
    this.authentication = authentication;
    this.hostname = authentication.getResources().getString(R.string.serverHost);
    this.port = authentication.getResources().getInteger(R.integer.serverPort);
  }


  @Override
  public void run() {
    manageData.writeFloatData(userName, "AuthRaw", "linearAcceleration", linearAccel);
    manageData.writeFloatData(userName, "AuthRaw", "gyroscope", gyro);

    readRegisteredData();
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
      case FOURIER:
        progressDialog.setMessage("フーリエ変換中");
        break;
      case CONVERT:
        progressDialog.setMessage("データの変換中");
        break;
      case NN_OUT:
        progressDialog.setMessage("ニューラルネットワークの計算中");
        break;
      case RE_LEARN:
        progressDialog.setMessage("ニューラルネットワークの追加学習中");
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

    learnResult = manageData.readLearnResult(authentication, userName);

    SharedPreferences preferences
        = authentication.getApplicationContext().getSharedPreferences("MotionAuth", MODE_PRIVATE);
    num_dimension = preferences.getInt(userName + "num_dimension", -1);
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
    linearAccel = adjuster.adjust(linearAccel, num_dimension);
    gyro = adjuster.adjust(gyro, num_dimension);

    this.sendEmptyMessage(FORMAT);
    double[][] linearAcceleration = formatter.convertFloatToDouble(linearAccel);
    double[][] gyroscope = formatter.convertFloatToDouble(gyro);

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

    //Socketでサーバにモード，ユーザ名，データ入力回数，データ次元数，データを渡す
    try {
      Socket socket = new Socket(hostname, port);
      DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
      DataInputStream inputStream = new DataInputStream(socket.getInputStream());

      // Send mode value
      outputStream.writeInt(1);
      outputStream.flush();

      // Send user name
      outputStream.writeBytes(userName+"\n");
      outputStream.flush();

      // Send number of input time
      outputStream.writeInt(1);
      outputStream.flush();

      // Send number of data dimension
      outputStream.writeInt(x.length);
      outputStream.flush();

      // Send data
      for (int dimen = 0, d_size = x.length; dimen < d_size; ++dimen) {
        outputStream.writeDouble(x[dimen]);
      }
      outputStream.flush();


      // Send SdA parameter length
      outputStream.writeInt(learnResult.length);
      outputStream.flush();

      // Send SdA parameter
      for (int i = 0, size = learnResult.length; i < size; ++i) {
        outputStream.writeBytes(learnResult[i]+"\n");
      }
      outputStream.flush();

      // Receive result
      double result = inputStream.readDouble();

      log(DEBUG, "Neural Network Output: "+result);
      manageData.writeDoubleSingleData(userName, "AuthNNOut", "NNOut", result);

      if (result >= 0.5) return false;

      return true;
    } catch (IOException e) {
      //Socketで通信できない場合は，保存したニューロンデータを用いてローカルで認証する
      double result = out(learnResult, x);

      if (result >= 0.5) return false;
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
   * @param neuronParams NNのニューロンパラメータ
   * @param x 入力データ
   * @return 出力データ
   */
  public native double out(String[] neuronParams, double[] x);
}
