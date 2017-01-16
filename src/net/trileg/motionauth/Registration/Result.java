package net.trileg.motionauth.Registration;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import net.trileg.motionauth.Utility.Enum;
import net.trileg.motionauth.Utility.ManageData;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

import static android.util.Log.DEBUG;
import static android.util.Log.ERROR;
import static android.util.Log.INFO;
import static net.trileg.motionauth.Registration.InputName.userName;
import static net.trileg.motionauth.Utility.LogUtil.log;

/**
 * Register and show result to user.
 *
 * @author Kensuke Kosaka
 */
class Result extends Handler implements Runnable {
  private static final int FORMAT = 1;
  private static final int FOURIER = 3;
  private static final int CONVERT = 4;
  private static final int NN_LEARNING = 7;
  private static final int FINISH = 8;

  private String hostname;
  private int port;

  private ManageData manageData = new ManageData();
  private Formatter formatter = new Formatter();
  private Fourier fourier = new Fourier();
  private Calc calc = new Calc();
  private Adjuster adjuster = new Adjuster();

  private Registration registration;
  private Button getMotion;
  private ProgressDialog progressDialog;

  private String[] learnResult = {"", ""};
  private float[][][] linearAccel;
  private float[][][] gyro;
  private boolean result = false;
  private int num_dimension = 0;


  Result(float[][][] linearAccel, float[][][] gyro, Button getMotion,
         ProgressDialog progressDialog, Registration registration) {
    log(INFO);
    this.linearAccel = linearAccel;
    this.gyro = gyro;
    this.getMotion = getMotion;
    this.progressDialog = progressDialog;
    this.registration = registration;
    this.hostname = registration.getResources().getString(R.string.serverHost);
    this.port = registration.getResources().getInteger(R.integer.serverPort);
  }


  @Override
  public void run() {
    manageData.writeFloatData(userName, "RegRaw", "linearAcceleration", linearAccel);
    manageData.writeFloatData(userName, "RegRaw", "gyroscope", gyro);

    result = calculate(linearAccel, gyro);
    this.sendEmptyMessage(FINISH);
  }


  @Override
  public void dispatchMessage(@NonNull Message msg) {
    if (getMotion.isClickable()) getMotion.setClickable(false);
    switch (msg.what) {
      case FORMAT:
        progressDialog.setMessage("データのフォーマット中");
        break;
      case FOURIER:
        progressDialog.setMessage("フーリエ変換中");
        break;
      case CONVERT:
        progressDialog.setMessage("データの変換中");
        break;
      case NN_LEARNING:
        progressDialog.setMessage("ニューラルネットワークの学習中");
        break;
      case FINISH:
        progressDialog.dismiss();
        log(DEBUG, "ProgressDialog was dismissed now");

        if (!result) {
          AlertDialog.Builder alert = new AlertDialog.Builder(registration);
          alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
              return keyCode == KeyEvent.KEYCODE_BACK;
            }
          });
          alert.setCancelable(false);
          alert.setTitle("登録失敗");
          alert.setMessage("登録に失敗しました．やり直して下さい");
          alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              registration.reset();
            }
          });

          alert.show();
        } else {
          manageData.writeRegisterData(userName, learnResult, num_dimension, registration);

          AlertDialog.Builder alert = new AlertDialog.Builder(registration);
          alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
              return keyCode == KeyEvent.KEYCODE_BACK;
            }
          });
          alert.setCancelable(false);
          alert.setTitle("登録完了");
          alert.setMessage("登録が完了しました．\nスタート画面に戻ります");
          alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              registration.finishRegistration();
            }
          });

          alert.show();
        }
        break;
      default:
        log(ERROR, "Something went wrong");
        break;
    }
  }


  /**
   * データ加工，計算処理を行う
   */
  private boolean calculate(float[][][] linearAccelList, float[][][] gyroList) {
    log(INFO);

    // 複数回のデータ取得について，データ数を揃える
    ArrayList<float[][][]> adjusted = adjuster.adjust(linearAccelList, gyroList);
    float[][][] linearAccel = adjusted.get(0);
    float[][][] gyro = adjusted.get(1);

    manageData.writeFloatData(userName, "RegAdjusted", "linearAcceleration", linearAccel);
    manageData.writeFloatData(userName, "RegAdjusted", "gyroscope", gyro);

    // データのフォーマット
    this.sendEmptyMessage(FORMAT);
    double[][][] linearAcceleration = formatter.convertFloatToDouble(linearAccel);
    double[][][] gyroscope = formatter.convertFloatToDouble(gyro);

    manageData.writeDoubleThreeArrayData(userName, "RegFormatted",
                                         "linearAcceleration", linearAcceleration);
    manageData.writeDoubleThreeArrayData(userName, "RegFormatted",
                                         "gyroscope", gyroscope);

    // フーリエ変換によるローパスフィルタ
    this.sendEmptyMessage(FOURIER);
    linearAcceleration = fourier.LowpassFilter(linearAcceleration, "LinearAcceleration", userName);
    gyroscope = fourier.LowpassFilter(gyroscope, "Gyroscope", userName);

    manageData.writeDoubleThreeArrayData(userName, "RegLowpassed",
                                         "linearAcceleration", linearAcceleration);
    manageData.writeDoubleThreeArrayData(userName, "RegLowpassed",
                                         "gyroscope", gyroscope);

    log(DEBUG, "Finish fourier");

    // 加速度から変位，角速度から角度へ変換（第二引数はセンサの取得間隔）
    this.sendEmptyMessage(CONVERT);
    double[][][] linearDistance = calc.accelToDistance(linearAcceleration, Enum.SENSOR_DELAY_TIME);
    double[][][] angle = calc.gyroToAngle(gyroscope, Enum.SENSOR_DELAY_TIME);

    manageData.writeDoubleThreeArrayData(userName, "RegConverted",
                                         "linearDistance", linearDistance);
    manageData.writeDoubleThreeArrayData(userName, "RegConverted",
                                         "angle", angle);

    log(DEBUG, "After write data");

    // 変位データを角度データで回転させる
    RotateVector rotateVector = new RotateVector();
    double[][][] vector
        = new double[linearDistance.length][linearDistance[0].length][linearDistance[0][0].length];
    for (int time = 0; time < linearDistance.length; time++)
      vector[time] = rotateVector.rotate(linearDistance[time], angle[time]);

    manageData.writeDoubleThreeArrayData(userName, "RegCombined", "vector", vector);

    manageData.writeDoubleThreeArrayData(userName, "RegAfterCalcData", "vector", vector);

    this.sendEmptyMessage(NN_LEARNING);
    //region ニューラルネットワークの学習をし，期待した出力が得られれば登録完了とする
    // 取得したデータを，ニューラルネットワークの教師入力用に調整する
    double[][] x = manipulateMotionDataToNeuralNetwork(vector);
    num_dimension = x[0].length;

    manageData.writeNNInputData(userName, "NNInputData", "vector", x);

    //Socketでサーバにモード，ユーザ名，データ入力回数，データ次元数，データを渡す
    try {
      log(DEBUG, "Waiting for connecting to server");
      Socket socket = new Socket(hostname, port);
      log(DEBUG, "Socket connected");
      log(DEBUG, "Preparing stream");
      DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      DataInputStream inputStream = new DataInputStream(socket.getInputStream());
      String line;

      // Send mode value
      outputStream.writeInt(0);
      outputStream.flush();
      log(DEBUG, "Send mode value");

      // Send user name
      outputStream.writeBytes(userName+"\n");
      outputStream.flush();
      log(DEBUG, "Send user name");

      // Send number of input time
      outputStream.writeInt(x.length);
      outputStream.flush();
      log(DEBUG, "Send number of input time");

      // Send number of data dimension
      outputStream.writeInt(num_dimension);
      outputStream.flush();
      log(DEBUG, "Send number of data dimension");

      // Send data
      for (int time = 0, t_size = x.length; time < t_size; ++time) {
        for (int dimen = 0, d_size = x[time].length; dimen < d_size; ++dimen) {
          outputStream.writeDouble(x[time][dimen]);
        }
      }
      outputStream.flush();
      log(DEBUG, "Send data");

      // Receive neuron size
      int neuron_size = inputStream.readInt();
      log(DEBUG, "Receive neuron size");
      learnResult = new String[neuron_size];

      // Receive neuron parameters
      for(int neuron = 0; neuron < neuron_size; ++neuron) {
        if ((line = bufferedReader.readLine()) != null) learnResult[neuron] = line;
      }
      log(DEBUG, "Receive neuron parameters");

      return true;
    } catch (IOException e) {
      // Make registration failure if socket connection was broken
      e.printStackTrace();
      return false;
    }
    //endregion
  }


  /**
   * モーションデータをニューラルネットワークの教師入力用に組み直す
   * @param input 組み直すモーションデータ
   * @return 組み直したモーションデータ
   */
  private double[][] manipulateMotionDataToNeuralNetwork(double[][][] input) {
    log(INFO);
    double[][] output = new double[input.length][input[0][0].length * 3]; // 入力回数 * (データ長 * 軸数）

    for (int time = 0; time < input.length; ++time) {
      for (int data = 0, dataPerAxis = 0;
           data < input[time][0].length * 3;
           data += 3, dataPerAxis++) {
        output[time][data] = input[time][0][dataPerAxis];
        output[time][data + 1] = input[time][1][dataPerAxis];
        output[time][data + 2] = input[time][2][dataPerAxis];
      }
    }

    return output;
  }
}
