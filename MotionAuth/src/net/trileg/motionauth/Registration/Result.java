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
public class Result extends Handler implements Runnable {
  private static final int FORMAT = 1;
  private static final int AMPLIFY = 2;
  private static final int FOURIER = 3;
  private static final int CONVERT = 4;
  private static final int DEVIATION = 5;
  private static final int CORRELATION = 6;
  private static final int FINISH = 10;

  private ManageData mManageData = new ManageData();
  private Formatter mFormatter = new Formatter();
  private Amplifier mAmplifier = new Amplifier();
  private Fourier mFourier = new Fourier();
  private Calc mCalc = new Calc();
  private Correlation mCorrelation = new Correlation();
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

  private ArrayList<ArrayList<ArrayList<Float>>> mAccel;
  private ArrayList<ArrayList<ArrayList<Float>>> mLinearAccel;
  private ArrayList<ArrayList<ArrayList<Float>>> mGyro;
  private double[][] averageDistance;
  private double[][] averageLinearDistance;
  private double[][] averageAngle;
  private boolean result = false;


  public Result(Registration registration, ArrayList<ArrayList<ArrayList<Float>>> accel,
                ArrayList<ArrayList<ArrayList<Float>>> linearAccel, ArrayList<ArrayList<ArrayList<Float>>> gyro,
                Button getMotion, ProgressDialog progressDialog, double checkRange, double amp, Context context,
                GetData getData) {
    mRegistration = registration;
    mAccel = accel;
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
    mManageData.writeFloatData("RegistrationRaw", InputName.name, "Acceleration", mAccel);
    mManageData.writeFloatData("RegistrationRaw", InputName.name, "LinearAcceleration", mLinearAccel);
    mManageData.writeFloatData("RegistrationRaw", InputName.name, "Gyroscope", mGyro);

    result = calculate(mAccel, mLinearAccel, mGyro);
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
      case CORRELATION:
        mProgressDialog.setMessage("相関係数を算出中");
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
          mManageData.writeRegisterData(InputName.name, averageDistance, averageLinearDistance, averageAngle, mAmp, mContext);

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


  public void setAmpAndRange(double amp, double checkRange) {
    mAmp = amp;
    mCheckRange = checkRange;
  }


  /**
   * データ加工，計算処理を行う
   */
  public boolean calculate(ArrayList<ArrayList<ArrayList<Float>>> accelList,
                           ArrayList<ArrayList<ArrayList<Float>>> linearAccelList,
                           ArrayList<ArrayList<ArrayList<Float>>> gyroList) {
    LogUtil.log(Log.INFO);

    // 複数回のデータ取得について，データ数を揃える
    ArrayList<float[][][]> adjusted = mAdjuster.adjust(accelList, linearAccelList, gyroList);
    float[][][] accel = adjusted.get(0);
    float[][][] linearAccel = adjusted.get(1);
    float[][][] gyro = adjusted.get(2);

    // データのフォーマット
    this.sendEmptyMessage(FORMAT);
    double[][][] acceleration = mFormatter.convertFloatToDouble(accel);
    double[][][] linearAcceleration = mFormatter.convertFloatToDouble(linearAccel);
    double[][][] gyroscope = mFormatter.convertFloatToDouble(gyro);

    mManageData.writeDoubleThreeArrayData("BeforeAMP", "Acceleration", InputName.name, acceleration);
    mManageData.writeDoubleThreeArrayData("BeforeAMP", "LinearAcceleration", InputName.name, linearAcceleration);
    mManageData.writeDoubleThreeArrayData("BeforeAMP", "Gyroscope", InputName.name, gyroscope);

    // データの増幅処理
    if (mAmplifier.CheckValueRange(acceleration, mCheckRange)
        || mAmplifier.CheckValueRange(linearAcceleration, mCheckRange)
        || mAmplifier.CheckValueRange(gyroscope, mCheckRange)) {
      this.sendEmptyMessage(AMPLIFY);
      acceleration = mAmplifier.Amplify(acceleration, mAmp);
      linearAcceleration = mAmplifier.Amplify(linearAcceleration, mAmp);
      gyroscope = mAmplifier.Amplify(gyroscope, mAmp);
    }

    mManageData.writeDoubleThreeArrayData("BeforeLowpass", "Acceleration", InputName.name, acceleration);
    mManageData.writeDoubleThreeArrayData("BeforeLowpass", "LinearAcceleration", InputName.name, linearAcceleration);
    mManageData.writeDoubleThreeArrayData("BeforeLowpass", "Gyroscope", InputName.name, gyroscope);

    // フーリエ変換によるローパスフィルタ
    this.sendEmptyMessage(FOURIER);
    acceleration = mFourier.LowpassFilter(acceleration, "Acceleration");
    linearAcceleration = mFourier.LowpassFilter(linearAcceleration, "LinearAcceleration");
    gyroscope = mFourier.LowpassFilter(gyroscope, "Gyroscope");

    mManageData.writeDoubleThreeArrayData("BeforeConvert", "Acceleration", InputName.name, acceleration);
    mManageData.writeDoubleThreeArrayData("BeforeConvert", "LinearAcceleration", InputName.name, linearAcceleration);
    mManageData.writeDoubleThreeArrayData("BeforeConvert", "Gyroscope", InputName.name, gyroscope);

    LogUtil.log(Log.DEBUG, "Finish fourier");

    // 加速度から距離，角速度から角度へ変換（第二引数はセンサの取得間隔）
    this.sendEmptyMessage(CONVERT);
    double[][][] distance = mCalc.accelToDistance(acceleration, Enum.SENSOR_DELAY_TIME);
    double[][][] linearDistance = mCalc.accelToDistance(linearAcceleration, Enum.SENSOR_DELAY_TIME);
    double[][][] angle = mCalc.gyroToAngle(gyroscope, Enum.SENSOR_DELAY_TIME);

    mManageData.writeDoubleThreeArrayData("BeforeDeviation", "distance", InputName.name, distance);
    mManageData.writeDoubleThreeArrayData("BeforeDeviation", "linearDistance", InputName.name, linearDistance);
    mManageData.writeDoubleThreeArrayData("BeforeDeviation", "angle", InputName.name, angle);

    LogUtil.log(Log.DEBUG, "After write data");


    //TODO この段階でコサイン類似度の測定を行い，データをアウトプットする
    // コサイン類似度を測る
    LogUtil.log(Log.DEBUG, "Before CosSimilarity");
    mCosSimilarity.cosSimilarity(distance);
    mCosSimilarity.cosSimilarity(linearAcceleration);
    mCosSimilarity.cosSimilarity(angle);
    LogUtil.log(Log.DEBUG, "After CosSimilarity");


    this.sendEmptyMessage(DEVIATION);
    // measureCorrelation用の平均値データを作成
    averageDistance = new double[Enum.NUM_AXIS][distance[0][0].length];
    averageLinearDistance = new double[Enum.NUM_AXIS][linearDistance[0][0].length];
    averageAngle = new double[Enum.NUM_AXIS][angle[0][0].length];

    averageDistance = calculateAverage(distance);
    averageLinearDistance = calculateAverage(linearDistance);
    averageAngle = calculateAverage(angle);


    //TODO 相関係数を出力させ，コサイン類似度のものと比較する
    //region 同一のモーションであるかの確認をし，必要に応じてズレ修正を行う
    LogUtil.log(Log.DEBUG, "Before measure correlation");
    Enum.MEASURE measure = mCorrelation.measureCorrelation(distance, linearDistance, angle, averageDistance,
        averageLinearDistance, averageAngle);

    LogUtil.log(Log.INFO, "After measure correlation");
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

      double[][][] originalDistance = distance;
      double[][][] originalLinearDistance = linearDistance;
      double[][][] originalAngle = angle;

      // ズレ修正は基準値を最大値，最小値，中央値の順に置き，さらにdistance, linearDistance, angleの順にベースを置く．
      while (true) {
        switch (count) {
          case 0:
            mode = Enum.MODE.MAX;
            target = Enum.TARGET.DISTANCE;
            break;
          case 1:
            mode = Enum.MODE.MAX;
            target = Enum.TARGET.LINEAR_DISTANCE;
            break;
          case 2:
            mode = Enum.MODE.MAX;
            target = Enum.TARGET.ANGLE;
            break;
          case 3:
            mode = Enum.MODE.MIN;
            target = Enum.TARGET.DISTANCE;
            break;
          case 4:
            mode = Enum.MODE.MIN;
            target = Enum.TARGET.LINEAR_DISTANCE;
            break;
          case 5:
            mode = Enum.MODE.MIN;
            target = Enum.TARGET.ANGLE;
            break;
          case 6:
            mode = Enum.MODE.MEDIAN;
            target = Enum.TARGET.DISTANCE;
            break;
          case 7:
            mode = Enum.MODE.MEDIAN;
            target = Enum.TARGET.LINEAR_DISTANCE;
            break;
          case 8:
            mode = Enum.MODE.MEDIAN;
            target = Enum.TARGET.ANGLE;
            break;
        }

        double[][][][] deviatedValue = mCorrectDeviation.correctDeviation(originalDistance, originalLinearDistance,
            originalAngle, mode, target);

        for (int time = 0; time < Enum.NUM_TIME; time++) {
          for (int axis = 0; axis < Enum.NUM_AXIS; axis++) {
            for (int item = 0; item < distance[time][axis].length; item++) {
              distance[time][axis][item] = deviatedValue[0][time][axis][item];
              linearDistance[time][axis][item] = deviatedValue[1][time][axis][item];
              angle[time][axis][item] = deviatedValue[2][time][axis][item];
            }
          }
        }

        averageDistance = calculateAverage(distance);
        averageLinearDistance = calculateAverage(linearDistance);
        averageAngle = calculateAverage(angle);

        Enum.MEASURE tmp = mCorrelation.measureCorrelation(distance, linearDistance, angle, averageDistance,
            averageLinearDistance, averageAngle);

        LogUtil.log(Log.INFO, "MEASURE: " + String.valueOf(tmp));

        mManageData.writeDoubleThreeArrayData("DeviatedData" + String.valueOf(mode), "distance", InputName.name, distance);
        mManageData.writeDoubleThreeArrayData("DeviatedData" + String.valueOf(mode), "linearDistance", InputName.name, linearDistance);
        mManageData.writeDoubleThreeArrayData("DeviatedData" + String.valueOf(mode), "angle", InputName.name, angle);


        if (tmp == Enum.MEASURE.PERFECT || tmp == Enum.MEASURE.CORRECT) {
          break;
        }

        distance = originalDistance;
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

    mManageData.writeDoubleThreeArrayData("AfterCalcData", "afterFormatDistance", InputName.name, distance);
    mManageData.writeDoubleThreeArrayData("AfterCalcData", "afterFormatLinearDistance", InputName.name, linearDistance);
    mManageData.writeDoubleThreeArrayData("AfterCalcData", "afterFormatAngle", InputName.name, angle);

    this.sendEmptyMessage(CORRELATION);

    // Calculate average data.
    averageDistance = calculateAverage(distance);
    averageLinearDistance = calculateAverage(linearDistance);
    averageAngle = calculateAverage(angle);

    measure = mCorrelation.measureCorrelation(distance, linearDistance, angle, averageDistance, averageLinearDistance, averageAngle);
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
