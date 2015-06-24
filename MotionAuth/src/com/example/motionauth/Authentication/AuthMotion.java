package com.example.motionauth.Authentication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.motionauth.R;
import com.example.motionauth.Utility.LogUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ユーザ認証を行う
 *
 * @author Kensuke Kousaka
 */
public class AuthMotion extends Activity {
	private TextView secondTv;
	private TextView countSecondTv;
	private Button getMotionBtn;

	private GetData mGetData;
	private AuthMotion mAuthMotion;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LogUtil.log(Log.INFO);

		setContentView(R.layout.activity_auth_motion);
		mAuthMotion = this;

		authMotion();
	}

	/**
	 * 認証画面にイベントリスナ等を設定する
	 */
	private void authMotion() {
		LogUtil.log(Log.INFO);

		Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		TextView nameTv = (TextView) findViewById(R.id.textView1);
		secondTv = (TextView) findViewById(R.id.secondTextView);
		countSecondTv = (TextView) findViewById(R.id.textView4);
		getMotionBtn = (Button) findViewById(R.id.button1);

		nameTv.setText(AuthNameInput.name + "さん読んでね！");
		mGetData = new GetData(mAuthMotion, getMotionBtn, secondTv, vibrator, this);

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

		Result mResult = new Result(mAuthMotion, accel, gyro, getMotionBtn, progressDialog, this, mGetData);
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.execute(mResult);
	}


	public void finishAuth() {
		LogUtil.log(Log.INFO);
		Intent intent = new Intent();
		intent.setClassName("com.example.motionauth", "com.example.motionauth.Start");
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

		startActivityForResult(intent, 0);
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
