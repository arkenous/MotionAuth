package net.trileg.motionauth.Authentication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import net.trileg.motionauth.R;
import net.trileg.motionauth.Utility.LogUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Authentication.
 *
 * @author Kensuke Kosaka
 */
public class Authentication extends Activity {
	private TextView secondTv;
	private TextView countSecondTv;
	private Button getMotionBtn;

	private GetData mGetData;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LogUtil.log(Log.INFO);

		setContentView(R.layout.activity_auth_motion);

		authentication();
	}


	/**
	 * Registration event lister and call GetData using ExecutorService to collect data.
	 */
	private void authentication() {
		LogUtil.log(Log.INFO);

		Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		TextView nameTv = (TextView) findViewById(R.id.textView1);
		secondTv = (TextView) findViewById(R.id.secondTextView);
		countSecondTv = (TextView) findViewById(R.id.textView4);
		getMotionBtn = (Button) findViewById(R.id.button1);

		nameTv.setText(InputName.userName + "さん読んでね！");
		mGetData = new GetData(this, getMotionBtn, secondTv, vibrator);

		getMotionBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LogUtil.log(Log.VERBOSE, "Click get motion button");
				if (v.isClickable()) {
					v.setClickable(false);
					getMotionBtn.setText("インターバル中");
					countSecondTv.setText("秒");

					ExecutorService executorService = Executors.newSingleThreadExecutor();
					executorService.execute(mGetData);
				}
			}
		});
	}


	/**
	 * Call when data collecting is finished.
	 * Call Result using ExecutorService to authenticate and show result.
	 *
	 * @param accel Original acceleration data collecting from GetData.
	 * @param gyro  Original gyroscope data collecting from GetData.
	 */
	public void finishGetMotion(float[][] accel, float[][] gyro) {
		LogUtil.log(Log.INFO);
		if (getMotionBtn.isClickable()) getMotionBtn.setClickable(false);
		secondTv.setText("0");
		getMotionBtn.setText("認証処理中");

		LogUtil.log(Log.DEBUG, "Start initialize ProgressDialog");

		ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setTitle("計算処理中");
		progressDialog.setMessage("しばらくお待ちください");
		progressDialog.setIndeterminate(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setCancelable(false);

		LogUtil.log(Log.DEBUG, "Finish initialize ProgressDialog");

		progressDialog.show();

		Result mResult = new Result(this, accel, gyro, getMotionBtn, progressDialog, mGetData);
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.execute(mResult);
	}


	/**
	 * Move to Start activity.
	 */
	public void finishAuthentication() {
		LogUtil.log(Log.INFO);
		Intent intent = new Intent();
		intent.setClassName(getPackageName(), getPackageName() + ".Start");
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

		startActivity(intent);
		finish();
	}


	@Override
	protected void onResume() {
		super.onResume();
		mGetData.registrationSensor();
	}


	@Override
	protected void onPause() {
		super.onPause();
		mGetData.unRegistrationSensor();
	}
}
