package net.trileg.motionauth.Authentication;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Button;
import net.trileg.motionauth.Lowpass.Fourier;
import net.trileg.motionauth.Processing.Amplifier;
import net.trileg.motionauth.Processing.Calc;
import net.trileg.motionauth.Processing.Correlation;
import net.trileg.motionauth.Processing.Formatter;
import net.trileg.motionauth.Utility.Enum;
import net.trileg.motionauth.Utility.LogUtil;
import net.trileg.motionauth.Utility.ManageData;

import java.util.ArrayList;

/**
 * Authenticate and show result to user.
 *
 * @author Kensuke Kosaka
 */
public class Result extends Handler implements Runnable {
	private static final int READ_DATA = 1;
	private static final int FORMAT = 2;
	private static final int AMPLIFY = 3;
	private static final int FOURIER = 4;
	private static final int CONVERT = 5;
	private static final int CORRELATION = 6;
	private static final int FINISH = 10;

	private ManageData mManageData = new ManageData();
	private Formatter mFormatter = new Formatter();
	private Amplifier mAmplifier = new Amplifier();
	private Fourier mFourier = new Fourier();
	private Calc mCalc = new Calc();
	private Correlation mCorrelation = new Correlation();

	private Authentication mAuthentication;
	private Button mGetMotion;
	private GetData mGetData;
	private ProgressDialog mProgressDialog;
	private double mAmp;
	private float[][] mAccel;
	private float[][] mGyro;
	private double[][] registeredDistance = new double[3][100];
	private double[][] registeredAngle = new double[3][100];
	private boolean result = false;


	public Result(Authentication authentication, float[][] accel, float[][] gyro, Button getMotion,
	              ProgressDialog progressDialog, GetData getData) {
		mAuthentication = authentication;
		mAccel = accel;
		mGyro = gyro;
		mGetMotion = getMotion;
		mProgressDialog = progressDialog;
		mGetData = getData;
	}


	@Override
	public void run() {
		readRegisteredData();
		result = calculate(mAccel, mGyro);

		this.sendEmptyMessage(FINISH);
	}


	@Override
	public void dispatchMessage(@NonNull Message msg) {
		if (mGetMotion.isClickable()) mGetMotion.setClickable(false);
		switch (msg.what) {
			case READ_DATA:
				mProgressDialog.setMessage("登録データの読み込み中");
				break;
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
			case CORRELATION:
				mProgressDialog.setMessage("相関係数を算出中");
				break;
			case FINISH:
				mProgressDialog.dismiss();
				if (!result) {
					LogUtil.log(Log.INFO, "False authentication");
					AlertDialog.Builder alert = new AlertDialog.Builder(mAuthentication);
					alert.setTitle("認証失敗");
					alert.setMessage("認証に失敗しました");
					alert.setCancelable(false);
					alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mGetData.reset();
						}
					});
					alert.show();
				} else {
					LogUtil.log(Log.INFO, "Success authentication");
					AlertDialog.Builder alert = new AlertDialog.Builder(mAuthentication);
					alert.setTitle("認証成功");
					alert.setMessage("認証に成功しました．\nスタート画面に戻ります");
					alert.setCancelable(false);
					alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mAuthentication.finishAuthentication();
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
		ArrayList<double[][]> readDataList = mManageData.readRegisteredData(mAuthentication, InputName.userName);
		registeredDistance = readDataList.get(0);
		registeredAngle = readDataList.get(1);

		SharedPreferences preferences = mAuthentication.getApplicationContext().getSharedPreferences("MotionAuth", Context.MODE_PRIVATE);
		String registeredAmplify = preferences.getString(InputName.userName + "amplify", "");
		if ("".equals(registeredAmplify)) throw new RuntimeException();
		mAmp = Double.valueOf(registeredAmplify);
	}


	/**
	 * Calculate data and authenticate.
	 *
	 * @param accel Acceleration data which collect in Authentication.GetData.
	 * @param gyro  Gyroscope data which collect in Authentication.GetData.
	 * @return true if authentication is succeed, otherwise false.
	 */
	private boolean calculate(float[][] accel, float[][] gyro) {
		this.sendEmptyMessage(FORMAT);
		double[][] accelDouble = mFormatter.floatToDoubleFormatter(accel);
		double[][] gyroDouble = mFormatter.floatToDoubleFormatter(gyro);

		this.sendEmptyMessage(AMPLIFY);
		accelDouble = mAmplifier.Amplify(accelDouble, mAmp);
		gyroDouble = mAmplifier.Amplify(gyroDouble, mAmp);

		this.sendEmptyMessage(FOURIER);
		accelDouble = mFourier.LowpassFilter(accelDouble, "accel");
		gyroDouble = mFourier.LowpassFilter(gyroDouble, "gyro");

		this.sendEmptyMessage(CONVERT);
		double[][] distance = mCalc.accelToDistance(accelDouble, 0.03);
		double[][] angle = mCalc.gyroToAngle(gyroDouble, 0.03);

		this.sendEmptyMessage(FORMAT);
		distance = mFormatter.doubleToDoubleFormatter(distance);
		angle = mFormatter.doubleToDoubleFormatter(angle);

		this.sendEmptyMessage(CORRELATION);
		Enum.MEASURE measure = mCorrelation.measureCorrelation(distance, angle, registeredDistance, registeredAngle);
		return measure == Enum.MEASURE.CORRECT;
	}
}
