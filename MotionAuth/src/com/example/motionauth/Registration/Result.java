package com.example.motionauth.Registration;

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
import com.example.motionauth.Lowpass.Fourier;
import com.example.motionauth.Processing.*;
import com.example.motionauth.Utility.Enum;
import com.example.motionauth.Utility.LogUtil;
import com.example.motionauth.Utility.ManageData;

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

	private RegistMotion mRegistMotion;
	private Button mGetMotion;
	private Context mContext;
	private GetData mGetData;
	private ProgressDialog mProgressDialog;
	private double mCheckRange;
	private double mAmp;
	private float[][][] mAccel;
	private float[][][] mGyro;
	private double[][] averageDistance = new double[3][100];
	private double[][] averageAngle = new double[3][100];
	private boolean result = false;


	public Result(RegistMotion registMotion, float[][][] accel, float[][][] gyro, Button getMotion,
	              ProgressDialog progressDialog, double checkRange, double amp, Context context, GetData getData) {
		mRegistMotion = registMotion;
		mAccel = accel;
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
		mManageData.writeFloatThreeArrayData("RegistrationRaw", "Acceleration", RegistNameInput.name, mAccel);
		mManageData.writeFloatThreeArrayData("RegistrationRaw", "Gyroscope", RegistNameInput.name, mGyro);

		result = calc(mAccel, mGyro);
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
					mManageData.writeRegistedData(RegistNameInput.name, averageDistance, averageAngle, mAmp, mContext);

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
							mRegistMotion.finishRegist();
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
	public boolean calc(float[][][] accel, float[][][] gyro) {
		LogUtil.log(Log.INFO);

		// データの桁揃え
		this.sendEmptyMessage(FORMAT);
		double[][][] accel_double = mFormatter.floatToDoubleFormatter(accel);
		double[][][] gyro_double = mFormatter.floatToDoubleFormatter(gyro);

		//TODO 回数ごとのデータの時間的長さを揃える

		mManageData.writeDoubleThreeArrayData("BeforeAMP", "accel", RegistNameInput.name, accel_double);
		mManageData.writeDoubleThreeArrayData("BeforeAMP", "gyro", RegistNameInput.name, gyro_double);

		// データの増幅処理
		if (mAmplifier.CheckValueRange(accel_double, mCheckRange) || mAmplifier.CheckValueRange(gyro_double, mCheckRange)) {
			this.sendEmptyMessage(AMPLIFY);
			accel_double = mAmplifier.Amplify(accel_double, mAmp);
			gyro_double = mAmplifier.Amplify(gyro_double, mAmp);
		}

		mManageData.writeDoubleThreeArrayData("AfterAMP", "accel", RegistNameInput.name, accel_double);
		mManageData.writeDoubleThreeArrayData("AfterAMP", "gyro", RegistNameInput.name, gyro_double);

		// フーリエ変換によるローパスフィルタ
		this.sendEmptyMessage(FOURIER);
		accel_double = mFourier.LowpassFilter(accel_double, "accel");
		gyro_double = mFourier.LowpassFilter(gyro_double, "gyro");

		mManageData.writeDoubleThreeArrayData("AfterLowpass", "accel", RegistNameInput.name, accel_double);
		mManageData.writeDoubleThreeArrayData("AfterLowpass", "gyro", RegistNameInput.name, gyro_double);

		LogUtil.log(Log.DEBUG, "Finish fourier");

		// 加速度から距離，角速度から角度へ変換
		this.sendEmptyMessage(CONVERT);
		double[][][] distance = mCalc.accelToDistance(accel_double, 0.03);
		double[][][] angle = mCalc.gyroToAngle(gyro_double, 0.03);

		this.sendEmptyMessage(FORMAT);
		distance = mFormatter.doubleToDoubleFormatter(distance);
		angle = mFormatter.doubleToDoubleFormatter(angle);

		mManageData.writeDoubleThreeArrayData("convertData", "distance", RegistNameInput.name, distance);
		mManageData.writeDoubleThreeArrayData("convertData", "angle", RegistNameInput.name, angle);

		LogUtil.log(Log.DEBUG, "After write data");

		this.sendEmptyMessage(DEVIATION);
		// measureCorrelation用の平均値データを作成
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 100; j++) {
				averageDistance[i][j] = (distance[0][i][j] + distance[1][i][j] + distance[2][i][j]) / 3;
				averageAngle[i][j] = (angle[0][i][j] + angle[1][i][j] + angle[2][i][j]) / 3;
				LogUtil.log(Log.DEBUG, "averageDistance: " + averageDistance[i][j]);
			}
		}

		//region 同一のモーションであるかの確認をし，必要に応じてズレ修正を行う
		com.example.motionauth.Utility.Enum.MEASURE measure = mCorrelation.measureCorrelation(distance, angle, averageDistance, averageAngle);

		LogUtil.log(Log.INFO, "After measure correlation");
		LogUtil.log(Log.INFO, "measure = " + String.valueOf(measure));

		if (Enum.MEASURE.BAD == measure) {
			// 相関係数が0.4以下
			return false;
		} else if (Enum.MEASURE.INCORRECT == measure) {
			LogUtil.log(Log.DEBUG, "Deviation");
			// 相関係数が0.4よりも高く，0.6以下の場合
			// ズレ修正を行う
			int time = 0;
			Enum.MODE mode = Enum.MODE.MAX;
			Enum.TARGET target = Enum.TARGET.DISTANCE;

			double[][][] originalDistance = distance;
			double[][][] originalAngle = angle;

			// ズレ修正は基準値を最大値，最小値，中央値の順に置き，さらに距離，角度の順にベースを置く．
			while (true) {
				switch (time) {
					case 0:
						mode = Enum.MODE.MAX;
						target = Enum.TARGET.DISTANCE;
						break;
					case 1:
						mode = Enum.MODE.MAX;
						target = Enum.TARGET.ANGLE;
						break;
					case 2:
						mode = Enum.MODE.MIN;
						target = Enum.TARGET.DISTANCE;
						break;
					case 3:
						mode = Enum.MODE.MIN;
						target = Enum.TARGET.ANGLE;
						break;
					case 4:
						mode = Enum.MODE.MEDIAN;
						target = Enum.TARGET.DISTANCE;
						break;
					case 5:
						mode = Enum.MODE.MEDIAN;
						target = Enum.TARGET.ANGLE;
						break;
				}

				double[][][][] deviatedValue = mCorrectDeviation.correctDeviation(originalDistance, originalAngle, mode, target);

				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 3; j++) {
						for (int k = 0; k < 100; k++) {
							distance[i][j][k] = deviatedValue[0][i][j][k];
							angle[i][j][k] = deviatedValue[1][i][j][k];
						}
					}
				}

				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 100; j++) {
						averageDistance[i][j] = (distance[0][i][j] + distance[1][i][j] + distance[2][i][j]) / 3;
						averageAngle[i][j] = (angle[0][i][j] + angle[1][i][j] + angle[2][i][j]) / 3;
					}
				}

				Enum.MEASURE tmp = mCorrelation.measureCorrelation(distance, angle, averageDistance, averageAngle);

				LogUtil.log(Log.INFO, "MEASURE: " + String.valueOf(tmp));

				mManageData.writeDoubleThreeArrayData("deviatedData" + String.valueOf(mode), "distance", RegistNameInput.name, distance);
				mManageData.writeDoubleThreeArrayData("deviatedData" + String.valueOf(mode), "angle", RegistNameInput.name, angle);


				if (tmp == Enum.MEASURE.PERFECT || tmp == Enum.MEASURE.CORRECT) {
					break;
				} else if (time == 2) {
					// 相関係数が低いまま，アラートなどを出す？
					distance = originalDistance;
					angle = originalAngle;
					break;
				}

				time++;
			}
		} else if (measure == Enum.MEASURE.CORRECT || measure == Enum.MEASURE.PERFECT) {
			LogUtil.log(Log.INFO, "SUCCESS");
		} else {
			// なにかがおかしい
			return false;
		}
		//endregion

		mManageData.writeDoubleThreeArrayData("AfterCalcData", "afterFormatDistance", RegistNameInput.name, distance);
		mManageData.writeDoubleThreeArrayData("AfterCalcData", "afterFormatAngle", RegistNameInput.name, angle);

		this.sendEmptyMessage(CORRELATION);
		// ズレ修正後の平均値データを出す
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 100; j++) {
				averageDistance[i][j] = (distance[0][i][j] + distance[1][i][j] + distance[2][i][j]) / 3;
				averageAngle[i][j] = (angle[0][i][j] + angle[1][i][j] + angle[2][i][j]) / 3;
			}
		}

		measure = mCorrelation.measureCorrelation(distance, angle, averageDistance, averageAngle);
		LogUtil.log(Log.INFO, "measure = " + measure);
		return measure == Enum.MEASURE.CORRECT || measure == Enum.MEASURE.PERFECT;
	}
}
