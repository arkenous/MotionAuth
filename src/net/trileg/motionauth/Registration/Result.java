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
import net.trileg.motionauth.Processing.Amplifier;
import net.trileg.motionauth.Processing.Calc;
import net.trileg.motionauth.Processing.CorrectDeviation;
import net.trileg.motionauth.Processing.CosSimilarity;
import net.trileg.motionauth.Processing.Formatter;
import net.trileg.motionauth.Processing.RotateVector;
import net.trileg.motionauth.Utility.Enum;
import net.trileg.motionauth.Utility.ManageData;

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
  private static final int AMPLIFY = 2;
  private static final int FOURIER = 3;
  private static final int CONVERT = 4;
  private static final int DEVIATION = 5;
  private static final int COSINE_SIMILARITY = 6;
  private static final int NN_LEARNING = 7;
  private static final int FINISH = 8;

  private ManageData manageData = new ManageData();
  private Formatter formatter = new Formatter();
  private Amplifier amplifier = new Amplifier();
  private Fourier fourier = new Fourier();
  private Calc calc = new Calc();
  private CorrectDeviation correctDeviation = new CorrectDeviation();
  private Adjuster adjuster = new Adjuster();
  private CosSimilarity cosSimilarity = new CosSimilarity();

  private Registration registration;
  private Button getMotion;
  private ProgressDialog progressDialog;

  private double checkRange;
  private double amp;
  private String learnResult = "";
  private float[][][] linearAccel;
  private float[][][] gyro;
  private double[][] averageVector;
  private boolean result = false;

  // C++で書いたMLPライブラリの呼び出しに必要
  static {
    System.loadLibrary("mlp-lib");
  }


  Result(float[][][] linearAccel, float[][][] gyro, Button getMotion, ProgressDialog progressDialog,
         double checkRange, double amp, Registration registration) {
    log(INFO);
    this.linearAccel = linearAccel;
    this.gyro = gyro;
    this.getMotion = getMotion;
    this.progressDialog = progressDialog;
    this.checkRange = checkRange;
    this.amp = amp;
    this.registration = registration;
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
      case AMPLIFY:
        progressDialog.setMessage("データの増幅処理中");
        break;
      case FOURIER:
        progressDialog.setMessage("フーリエ変換中");
        break;
      case CONVERT:
        progressDialog.setMessage("データの変換中");
        break;
      case DEVIATION:
        progressDialog.setMessage("データのズレを修正中");
        break;
      case COSINE_SIMILARITY:
        progressDialog.setMessage("コサイン類似度を算出中");
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
          // モーションの平均値をファイルに書き出す
          manageData.writeDoubleTwoArrayData(userName, "RegRegistered", "vector", averageVector);
          manageData.writeRegisterData(userName, averageVector, amp, learnResult, registration);

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


  void setAmpAndRange(double amp, double checkRange) {
    log(INFO);
    this.amp = amp;
    this.checkRange = checkRange;
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

    manageData.writeDoubleThreeArrayData(userName, "RegFormatted", "linearAcceleration", linearAcceleration);
    manageData.writeDoubleThreeArrayData(userName, "RegFormatted", "gyroscope", gyroscope);

    // データの増幅処理
    if (amplifier.CheckValueRange(linearAcceleration, checkRange)
        || amplifier.CheckValueRange(gyroscope, checkRange)) {
      this.sendEmptyMessage(AMPLIFY);
      linearAcceleration = amplifier.Amplify(linearAcceleration, amp);
      gyroscope = amplifier.Amplify(gyroscope, amp);
    }

    manageData.writeDoubleThreeArrayData(userName, "RegAmplified", "linearAcceleration", linearAcceleration);
    manageData.writeDoubleThreeArrayData(userName, "RegAmplified", "gyroscope", gyroscope);

    // フーリエ変換によるローパスフィルタ
    this.sendEmptyMessage(FOURIER);
    linearAcceleration = fourier.LowpassFilter(linearAcceleration, "LinearAcceleration", userName);
    gyroscope = fourier.LowpassFilter(gyroscope, "Gyroscope", userName);

    manageData.writeDoubleThreeArrayData(userName, "RegLowpassed", "linearAcceleration", linearAcceleration);
    manageData.writeDoubleThreeArrayData(userName, "RegLowpassed", "gyroscope", gyroscope);

    log(DEBUG, "Finish fourier");

    // 加速度から変位，角速度から角度へ変換（第二引数はセンサの取得間隔）
    this.sendEmptyMessage(CONVERT);
    double[][][] linearDistance = calc.accelToDistance(linearAcceleration, Enum.SENSOR_DELAY_TIME);
    double[][][] angle = calc.gyroToAngle(gyroscope, Enum.SENSOR_DELAY_TIME);

    manageData.writeDoubleThreeArrayData(userName, "RegConverted", "linearDistance", linearDistance);
    manageData.writeDoubleThreeArrayData(userName, "RegConverted", "angle", angle);

    log(DEBUG, "After write data");

    // 変位データを角度データで回転させる
    RotateVector rotateVector = new RotateVector();
    double[][][] vector = new double[linearDistance.length][linearDistance[0].length][linearDistance[0][0].length];
    for (int time = 0; time < linearDistance.length; time++) {
      vector[time] = rotateVector.rotate(linearDistance[time], angle[time]);
    }

    manageData.writeDoubleThreeArrayData(userName, "Combined", "vector", vector);

    this.sendEmptyMessage(DEVIATION);

    //region 同一のモーションであるかの確認をし，必要に応じてズレ修正を行う
    log(DEBUG, "Before measure cosine similarity");

    // コサイン類似度を測る
    double[] vectorCosSimilarity = cosSimilarity.cosSimilarity(vector);

    Enum.MEASURE measure = cosSimilarity.measure(vectorCosSimilarity);

    log(DEBUG, "After measure cosine similarity");
    log(DEBUG, "measure = " + String.valueOf(measure));

    if (Enum.MEASURE.INCORRECT == measure) return false; // 類似度が0.4以下
    else if (Enum.MEASURE.MAYBE == measure) {
      log(DEBUG, "Deviation");
      // 類似度が0.4よりも高く，0.6以下の場合，ズレ修正を行う
      int count = 0;
      Enum.MODE mode = Enum.MODE.MAX;

      double[][][] originalVector = vector;

      // ズレ修正は基準値を最大値，最小値，中央値の順に置く．
      while (true) {
        switch (count) {
          case 0:
            mode = Enum.MODE.MAX;
            break;
          case 1:
            mode = Enum.MODE.MIN;
            break;
          case 2:
            mode = Enum.MODE.MEDIAN;
            break;
        }

        vector = correctDeviation.correctDeviation(vector, mode);

        vectorCosSimilarity = cosSimilarity.cosSimilarity(vector);

        Enum.MEASURE tmp = cosSimilarity.measure(vectorCosSimilarity);

        log(DEBUG, "MEASURE: " + String.valueOf(tmp));

        manageData.writeDoubleThreeArrayData(userName, "DeviatedData" + String.valueOf(mode), "vector", vector);

        if (tmp == Enum.MEASURE.PERFECT || tmp == Enum.MEASURE.CORRECT) break;

        vector = originalVector;

        if (count == 2) break; // Break this loop if all pattern attempts were failed

        count++;
      }
    } else if (measure == Enum.MEASURE.CORRECT || measure == Enum.MEASURE.PERFECT) log(DEBUG, "SUCCESS");
    else return false; // 到達しないはず
    //endregion

    manageData.writeDoubleThreeArrayData(userName, "AfterCalcData", "vector", vector);

    this.sendEmptyMessage(COSINE_SIMILARITY);

    // Calculate average data.
    averageVector = calculateAverage(vector);

    vectorCosSimilarity = cosSimilarity.cosSimilarity(vector);

    measure = cosSimilarity.measure(vectorCosSimilarity);
    log(DEBUG, "measure = " + measure);

    this.sendEmptyMessage(NN_LEARNING);
    //region ニューラルネットワークの学習をし，期待した出力が得られれば登録完了とする
    // 取得したデータを，ニューラルネットワークの教師入力用に調整する
    double[][] x = manipulateMotionDataToNeuralNetwork(vector);
    double[][] answer = new double[x.length][1];
    for (int time = 0; time < x.length; time++) {
      answer[time][0] = 0.1;
    }
    String neuronParams = "";

    learnResult = learn((short)x[0].length, (short)x[0].length, (short)answer[0].length, (short)1, neuronParams, x, answer);
    log(DEBUG, "learnResult: "+learnResult);

    // テストデータで期待した出力が得られるか確認する．出力が0.1よりも大きければ登録失敗とする
    for (int set = 0; set < x.length; ++set) {
      double[] result = out((short)x[set].length, (short)x[set].length, (short)1, (short)1, learnResult, x[set]);
      log(DEBUG, "set["+set+"] result[0]: "+result[0]);
      if (result[0] > 0.1) return false;
    }
    return true;
    //endregion
  }


  /**
   * 複数回分の入力モーションデータの平均値を計算する
   * @param input 入力データ
   * @return 平均値データ
   */
  private double[][] calculateAverage(double[][][] input) {
    log(INFO);
    double[][] output = new double[Enum.NUM_AXIS][input[0][0].length];
    for (int axis = 0; axis < Enum.NUM_AXIS; axis++) {
      for (int item = 0; item < output[axis].length; item++) {
        for (double[][] anInput : input) output[axis][item] += anInput[axis][item];
        output[axis][item] /= input.length;
      }
    }

    return output;
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
      for (int data = 0, dataPerAxis = 0; data < input[time][0].length * 3; data += 3, dataPerAxis++) {
        output[time][data] = input[time][0][dataPerAxis];
        output[time][data + 1] = input[time][1][dataPerAxis];
        output[time][data + 2] = input[time][2][dataPerAxis];
      }
    }

    return output;
  }


  /**
   * C++ネイティブのニューラルネットワーク学習メソッド
   * @param input 入力層のニューロン数
   * @param middle 中間層一層あたりのニューロン数
   * @param output 出力層のニューロン数
   * @param middleLayer 中間層の層数
   * @param neuronParams ニューロンの結合荷重の重みとAdaGradのgとバイアスをパイプで連結し，それらニューロンごとのデータをシングルクオートで連結した文字列データ
   * @param x 教師入力データ
   * @param answer 教師出力データ
   * @return 学習後のニューロンの結合荷重の重みと閾値をカンマで連結し，それらニューロンごとのデータをシングルクオートで連結した文字列データ
   */
  public native String learn(short input, short middle, short output, short middleLayer, String neuronParams, double[][] x, double[][] answer);

  /**
   * C++ネイティブのニューラルネットワーク出力メソッド
   * @param input 入力層のニューロン数
   * @param middle 中間層一層あたりのニューロン数
   * @param output 出力層のニューロン数
   * @param middleLayer 中間層の層数
   * @param neuronParams ニューロンの結合荷重の重みとAdaGradのgとバイアスをパイプで連結し，それらニューロンごとのデータをシングルクオートで連結した文字列データ
   * @param x 入力データ
   * @return ニューラルネットワークの出力
   */
  public native double[] out(short input, short middle, short output, short middleLayer, String neuronParams, double[] x);
}
