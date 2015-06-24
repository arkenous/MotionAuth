package com.example.motionauth.Authentication;

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
import com.example.motionauth.Lowpass.Fourier;
import com.example.motionauth.Processing.Amplifier;
import com.example.motionauth.Processing.Calc;
import com.example.motionauth.Processing.Correlation;
import com.example.motionauth.Processing.Formatter;
import com.example.motionauth.Utility.Enum;
import com.example.motionauth.Utility.LogUtil;
import com.example.motionauth.Utility.ManageData;

import java.util.ArrayList;

public class Result extends Handler implements Runnable {
	private static final int FINISH = 5;

	private ManageData mManageData = new ManageData();
	private Formatter mFormatter = new Formatter();
	private Amplifier mAmplifier = new Amplifier();
	private Fourier mFourier = new Fourier();
	private Calc mCalc = new Calc();
	private Correlation mCorrelation = new Correlation();

	private AuthMotion mAuthMotion;
	private Button mGetMotion;
	private Context mContext;
	private GetData mGetData;
	private ProgressDialog mProgressDialog;
	private double mAmp;
	private float[][] mAccel;
	private float[][] mGyro;

	private double[][] registeredDistance = new double[3][100];
	private double[][] registeredAngle = new double[3][100];

	private boolean result = false;


	public Result(AuthMotion authMotion, float[][] accel, float[][] gyro, Button getMotion,
	              ProgressDialog progressDialog, Context context, GetData getData) {
		mAuthMotion = authMotion;
		mAccel = accel;
		mGyro = gyro;
		mGetMotion = getMotion;
		mProgressDialog = progressDialog;
		mContext = context;
		mGetData = getData;
	}


	@Override
	public void run() {
		readRegisteredData();
		result = calc(mAccel, mGyro);

		this.sendEmptyMessage(FINISH);
	}


	@Override
	public void dispatchMessage(@NonNull Message msg) {
		if (mGetMotion.isClickable()) mGetMotion.setClickable(false);
		switch (msg.what) {
			case FINISH:
				mProgressDialog.dismiss();
				if (!result) {
					LogUtil.log(Log.INFO, "False authentication");
					AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
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
					AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
					alert.setTitle("認証成功");
					alert.setMessage("認証に成功しました．\nスタート画面に戻ります");
					alert.setCancelable(false);
					alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mAuthMotion.finishAuth();
						}
					});
					alert.show();
				}
		}
	}


	private void readRegisteredData() {
		ArrayList<double[][]> readDataList = mManageData.readRegistedData(mContext, AuthNameInput.name);
		registeredDistance = readDataList.get(0);
		registeredAngle = readDataList.get(1);

		SharedPreferences preferences = mContext.getApplicationContext().getSharedPreferences("MotionAuth", Context.MODE_PRIVATE);
		String registeredAmplify = preferences.getString(AuthNameInput.name + "amplify", "");
		if ("".equals(registeredAmplify)) throw new RuntimeException();
		mAmp = Double.valueOf(registeredAmplify);
	}


	private boolean calc(float[][] accel, float[][] gyro) {
		double[][] accelDouble = mFormatter.floatToDoubleFormatter(accel);
		double[][] gyroDouble = mFormatter.floatToDoubleFormatter(gyro);

		accelDouble = mAmplifier.Amplify(accelDouble, mAmp);
		gyroDouble = mAmplifier.Amplify(gyroDouble, mAmp);

		accelDouble = mFourier.LowpassFilter(accelDouble, "accel");
		gyroDouble = mFourier.LowpassFilter(gyroDouble, "gyro");

		double[][] distance = mCalc.accelToDistance(accelDouble, 0.03);
		double[][] angle = mCalc.gyroToAngle(gyroDouble, 0.03);
		distance = mFormatter.doubleToDoubleFormatter(distance);
		angle = mFormatter.doubleToDoubleFormatter(angle);

		Enum.MEASURE measure = mCorrelation.measureCorrelation(distance, angle, registeredDistance, registeredAngle);
		return measure == Enum.MEASURE.CORRECT;
	}
}
