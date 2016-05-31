package net.trileg.motionauth.Registration;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import net.trileg.motionauth.Lowpass.Fourier;
import net.trileg.motionauth.Processing.*;
import net.trileg.motionauth.Utility.Enum;
import net.trileg.motionauth.Utility.LogUtil;
import net.trileg.motionauth.Utility.ManageData;

import java.util.ArrayList;

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
  private static final int FINISH = 10;

  private ManageData mManageData = new ManageData();
  private Formatter mFormatter = new Formatter();
  private Amplifier mAmplifier = new Amplifier();
  private Fourier mFourier = new Fourier();
  private Calc mCalc = new Calc();
  private CorrectDeviation mCorrectDeviation = new CorrectDeviation();
  private Adjuster mAdjuster = new Adjuster();
  private CosSimilarity mCosSimilarity = new CosSimilarity();

  private Registration mRegistration;
  private Button mGetMotion;
  private Context mContext;
  private GetData mGetData;
  private ProgressDialog mProgressDialog;
  private double mCheckRange;
  private double mAmp;

  private float[][][] mLinearAccel;
  private float[][][] mGyro;
  private double[][] averageLinearDistance;
  private double[][] averageAngle;
  private boolean result = false;


  Result(Registration registration, float[][][] linearAccel,
         float[][][] gyro, Button getMotion, ProgressDialog progressDialog,
         double checkRange, double amp, Context context, GetData getData) {
    mRegistration = registration;
    mLinearAccel = linearAccel;
    mGyro = gyro;
    mGetMotion = getMotion;
    mProgressDialog = progressDialog;
    mCheckRange = checkRange;
    mAmp = amp;
    mContext = context;
    mGetData = getData;
  }


  @Override
  public void run() {
    mManageData.writeFloatData(InputName.name, "RegRaw", "linearAcceleration", mLinearAccel);
    mManageData.writeFloatData(InputName.name, "RegRaw", "gyroscope", mGyro);

    result = calculate(mLinearAccel, mGyro);
    this.sendEmptyMessage(FINISH);
  }


  @Override
  public void dispatchMessage(@NonNull Message msg) {
    if (mGetMotion.isClickable()) mGetMotion.setClickable(false);
    switch (msg.what) {
      case FORMAT:
        mProgressDialog.setMessage("データのフォーマット中");
        break;
      case AMPLIFY:
        mProgressDialog.setMessage("データの増幅処理中");
        break;
      case FOURIER:
        mProgressDialog.setMessage("フーリエ変換中");
        break;
      case CONVERT:
        mProgressDialog.setMessage("データの変換中");
        break;
      case DEVIATION:
        mProgressDialog.setMessage("データのズレを修正中");
        break;
      case COSINE_SIMILARITY:
        mProgressDialog.setMessage("コサイン類似度を算出中");
        break;
      case FINISH:
        mProgressDialog.dismiss();
        LogUtil.log(Log.DEBUG, "ProgressDialog was dismissed now");

        if (!result) {
          AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
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
              mGetData.reset();
            }
          });

          alert.show();
        } else {
          // 3回のモーションの平均値をファイルに書き出す
          mManageData.writeDoubleTwoArrayData(InputName.name, "RegRegistered", "linearDistance", averageLinearDistance);
          mManageData.writeDoubleTwoArrayData(InputName.name, "RegRegistered", "angle", averageAngle);
          mManageData.writeRegisterData(InputName.name, averageLinearDistance, averageAngle, mAmp, mContext);

          AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
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
              mRegistration.finishRegistration();
            }
          });

          alert.show();
        }
        break;
      default:
        LogUtil.log(Log.ERROR, "Something went wrong");
        break;
    }
  }


  void setAmpAndRange(double amp, double checkRange) {
    mAmp = amp;
    mCheckRange = checkRange;
  }


  /**
   * データ加工，計算処理を行う
   */
  private boolean calculate(float[][][] linearAccelList, float[][][] gyroList) {
    LogUtil.log(Log.INFO);

    // 複数回のデータ取得について，データ数を揃える
    ArrayList<float[][][]> adjusted = mAdjuster.adjust(linearAccelList, gyroList);
    float[][][] linearAccel = adjusted.get(0);
    float[][][] gyro = adjusted.get(1);

    mManageData.writeFloatData(InputName.name, "RegAdjusted", "linearAcceleration", linearAccel);
    mManageData.writeFloatData(InputName.name, "RegAdjusted", "gyroscope", gyro);

    // データのフォーマット
    this.sendEmptyMessage(FORMAT);
    double[][][] linearAcceleration = mFormatter.convertFloatToDouble(linearAccel);
    double[][][] gyroscope = mFormatter.convertFloatToDouble(gyro);

    mManageData.writeDoubleThreeArrayData(InputName.name, "RegFormatted", "linearAcceleration", linearAcceleration);
    mManageData.writeDoubleThreeArrayData(InputName.name, "RegFormatted", "gyroscope", gyroscope);

    // データの増幅処理
    if (mAmplifier.CheckValueRange(linearAcceleration, mCheckRange)
        || mAmplifier.CheckValueRange(gyroscope, mCheckRange)) {
      this.sendEmptyMessage(AMPLIFY);
      linearAcceleration = mAmplifier.Amplify(linearAcceleration, mAmp);
      gyroscope = mAmplifier.Amplify(gyroscope, mAmp);
    }

    mManageData.writeDoubleThreeArrayData(InputName.name, "RegAmplified", "linearAcceleration", linearAcceleration);
    mManageData.writeDoubleThreeArrayData(InputName.name, "RegAmplified", "gyroscope", gyroscope);

    // フーリエ変換によるローパスフィルタ
    this.sendEmptyMessage(FOURIER);
    linearAcceleration = mFourier.LowpassFilter(linearAcceleration, "LinearAcceleration");
    gyroscope = mFourier.LowpassFilter(gyroscope, "Gyroscope");

    mManageData.writeDoubleThreeArrayData(InputName.name, "RegLowpassed", "linearAcceleration", linearAcceleration);
    mManageData.writeDoubleThreeArrayData(InputName.name, "RegLowpassed", "gyroscope", gyroscope);

    LogUtil.log(Log.DEBUG, "Finish fourier");

    // 加速度から距離，角速度から角度へ変換（第二引数はセンサの取得間隔）
    this.sendEmptyMessage(CONVERT);
    double[][][] linearDistance = mCalc.accelToDistance(linearAcceleration, Enum.SENSOR_DELAY_TIME);
    double[][][] angle = mCalc.gyroToAngle(gyroscope, Enum.SENSOR_DELAY_TIME);

    mManageData.writeDoubleThreeArrayData(InputName.name, "RegConverted", "linearDistance", linearDistance);
    mManageData.writeDoubleThreeArrayData(InputName.name, "RegConverted", "angle", angle);

    LogUtil.log(Log.DEBUG, "After write data");

    this.sendEmptyMessage(DEVIATION);

    //region 同一のモーションであるかの確認をし，必要に応じてズレ修正を行う
    LogUtil.log(Log.DEBUG, "Before measure cosine similarity");

    // コサイン類似度を測る
    double[] linearDistanceCosSimilarity = mCosSimilarity.cosSimilarity(linearDistance);
    double[] angleCosSimilarity = mCosSimilarity.cosSimilarity(angle);

    Enum.MEASURE measure = mCosSimilarity.measure(linearDistanceCosSimilarity, angleCosSimilarity);

    LogUtil.log(Log.INFO, "After measure cosine similarity");
    LogUtil.log(Log.INFO, "measure = " + String.valueOf(measure));

    if (Enum.MEASURE.BAD == measure) {
      // 相関係数が0.4以下
      return false;
    } else if (Enum.MEASURE.INCORRECT == measure) {
      LogUtil.log(Log.DEBUG, "Deviation");
      // 相関係数が0.4よりも高く，0.6以下の場合
      // ズレ修正を行う
      int count = 0;
      Enum.MODE mode = Enum.MODE.MAX;
      Enum.TARGET target = Enum.TARGET.DISTANCE;

      double[][][] originalLinearDistance = linearDistance;
      double[][][] originalAngle = angle;

      // ズレ修正は基準値を最大値，最小値，中央値の順に置き，さらにdistance, linearDistance, angleの順にベースを置く．
      while (true) {
        switch (count) {
          case 0:
            mode = Enum.MODE.MAX;
            target = Enum.TARGET.LINEAR_DISTANCE;
            break;
          case 1:
            mode = Enum.MODE.MAX;
            target = Enum.TARGET.ANGLE;
            break;
          case 2:
            mode = Enum.MODE.MIN;
            target = Enum.TARGET.LINEAR_DISTANCE;
            break;
          case 3:
            mode = Enum.MODE.MIN;
            target = Enum.TARGET.ANGLE;
            break;
          case 4:
            mode = Enum.MODE.MEDIAN;
            target = Enum.TARGET.LINEAR_DISTANCE;
            break;
          case 5:
            mode = Enum.MODE.MEDIAN;
            target = Enum.TARGET.ANGLE;
            break;
        }

        double[][][][] deviatedValue = mCorrectDeviation.correctDeviation(originalLinearDistance,
            originalAngle, mode, target);

        for (int time = 0; time < Enum.NUM_TIME; time++) {
          for (int axis = 0; axis < Enum.NUM_AXIS; axis++) {
            for (int item = 0; item < linearDistance[time][axis].length; item++) {
              linearDistance[time][axis][item] = deviatedValue[0][time][axis][item];
              angle[time][axis][item] = deviatedValue[1][time][axis][item];
            }
          }
        }

        linearDistanceCosSimilarity = mCosSimilarity.cosSimilarity(linearDistance);
        angleCosSimilarity = mCosSimilarity.cosSimilarity(angle);

        Enum.MEASURE tmp = mCosSimilarity.measure(linearDistanceCosSimilarity, angleCosSimilarity);

        LogUtil.log(Log.INFO, "MEASURE: " + String.valueOf(tmp));

        mManageData.writeDoubleThreeArrayData(InputName.name, "DeviatedData" + String.valueOf(mode), "linearDistance", linearDistance);
        mManageData.writeDoubleThreeArrayData(InputName.name, "DeviatedData" + String.valueOf(mode), "angle", angle);


        if (tmp == Enum.MEASURE.PERFECT || tmp == Enum.MEASURE.CORRECT) {
          break;
        }

        linearDistance = originalLinearDistance;
        angle = originalAngle;

        if (count == 5) {
          // Break this loop if all pattern attempts were failed
          break;
        }

        count++;
      }
    } else if (measure == Enum.MEASURE.CORRECT || measure == Enum.MEASURE.PERFECT) {
      LogUtil.log(Log.INFO, "SUCCESS");
    } else {
      return false;
    }
    //endregion

    mManageData.writeDoubleThreeArrayData(InputName.name, "AfterCalcData", "linearDistance", linearDistance);
    mManageData.writeDoubleThreeArrayData(InputName.name, "AfterCalcData", "angle", angle);

    this.sendEmptyMessage(COSINE_SIMILARITY);

    // Calculate average data.
    averageLinearDistance = calculateAverage(linearDistance);
    averageAngle = calculateAverage(angle);

    linearDistanceCosSimilarity = mCosSimilarity.cosSimilarity(linearDistance);
    angleCosSimilarity = mCosSimilarity.cosSimilarity(angle);

    measure = mCosSimilarity.measure(linearDistanceCosSimilarity, angleCosSimilarity);
    LogUtil.log(Log.INFO, "measure = " + measure);
    return measure == Enum.MEASURE.CORRECT || measure == Enum.MEASURE.PERFECT;
  }


  private double[][] calculateAverage(double[][][] input) {
    double[][] output = new double[Enum.NUM_AXIS][input[0][0].length];
    for (int axis = 0; axis < Enum.NUM_AXIS; axis++) {
      for (int item = 0; item < output[axis].length; item++) {
        output[axis][item] = (input[0][axis][item] + input[1][axis][item] + input[2][axis][item]) / 3;
      }
    }

    return output;
  }
}
