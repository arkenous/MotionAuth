package net.trileg.motionauth.Registration;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import net.trileg.motionauth.R;
import net.trileg.motionauth.Utility.LogUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * モーションを新規登録する
 *
 * @author Kensuke Kousaka
 */
public class Registration extends Activity {
	private TextView secondTv;
	private TextView countSecondTv;
	private Button getMotionBtn;
	private GetData mGetData;
	private Result mResult;
	private Registration mRegistration;

	private double checkRangeValue = 2.0;
	private double ampValue = 2.0;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtil.log(Log.INFO);

		setContentView(R.layout.activity_regist_motion);
		mRegistration = this;
		registMotion();
	}


	/**
	 * モーション登録画面にイベントリスナ等を設定する
	 */
	public void registMotion() {
		LogUtil.log(Log.INFO);

		Vibrator mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		TextView nameTv = (TextView) findViewById(R.id.textView2);
		secondTv = (TextView) findViewById(R.id.secondTextView);
		countSecondTv = (TextView) findViewById(R.id.textView4);
		getMotionBtn = (Button) findViewById(R.id.button1);

		nameTv.setText(InputName.name + "さん読んでね！");
		mGetData = new GetData(mRegistration, getMotionBtn, secondTv, countSecondTv, mVibrator, this);

		getMotionBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LogUtil.log(Log.VERBOSE, "Click get motion button");
				if (v.isClickable()) {
					// ボタンをクリックできないようにする
					v.setClickable(false);
					getMotionBtn.setText("インターバル中");
					countSecondTv.setText("秒");

					ExecutorService executorService = Executors.newSingleThreadExecutor();
					executorService.execute(mGetData);
				}
			}
		});
	}


	public void finishGetMotion(float[][][] accel, float[][][] gyro) {
		LogUtil.log(Log.INFO);
		if (getMotionBtn.isClickable()) getMotionBtn.setClickable(false);
		secondTv.setText("0");
		getMotionBtn.setText("データ処理中");

		LogUtil.log(Log.DEBUG, "Start initialize progress dialog");

		ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setTitle("計算処理中");
		progressDialog.setMessage("しばらくお待ちください");
		progressDialog.setIndeterminate(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setCancelable(false);

		LogUtil.log(Log.DEBUG, "Finish initialize Progress dialog");

		progressDialog.show();

		mResult = new Result(mRegistration, accel, gyro, getMotionBtn, progressDialog, checkRangeValue,
				ampValue, this, mGetData);
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.submit(mResult);
	}


	@Override
	protected void onResume() {
		super.onResume();
		LogUtil.log(Log.INFO);
		mGetData.registrationSensor();
	}


	@Override
	protected void onPause() {
		super.onPause();
		LogUtil.log(Log.INFO);
		mGetData.unRegistrationSensor();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.regist_motion, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.change_range_value:
				if (getMotionBtn.isClickable()) {
					LayoutInflater inflater = LayoutInflater.from(Registration.this);
					View seekView = inflater.inflate(R.layout.seekdialog, (ViewGroup) findViewById(R.id.dialog_root));

					// 閾値調整
					SeekBar thresholdSeekBar = (SeekBar) seekView.findViewById(R.id.threshold);
					final TextView thresholdSeekText = (TextView) seekView.findViewById(R.id.thresholdtext);
					thresholdSeekText.setText("増幅器にかけるかどうかを判断する閾値を調整できます．\n" +
							"2.5を中心に，値が小さければ登録・認証が難しくなり，大きければ易しくなります．\n" +
							"現在の値は" + checkRangeValue + "です．");

					thresholdSeekBar.setMax(30);
					thresholdSeekBar.setProgress(16);
					thresholdSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
						@Override
						public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
							checkRangeValue = (seekBar.getProgress() + 10) / 10.0;
							thresholdSeekText.setText("増幅器にかけるかどうかを判断する閾値を調整できます．\n" +
									"2.5を中心に，値が小さければ登録・認証が難しくなり，大きければ易しくなります．\n" +
									"現在の値は" + checkRangeValue + "です．");
						}

						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {
						}

						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {
							checkRangeValue = (seekBar.getProgress() + 10) / 10.0;
							thresholdSeekText.setText("増幅器にかけるかどうかを判断する閾値を調整できます．\n" +
									"2.5を中心に，値が小さければ登録・認証が難しくなり，大きければ易しくなります．\n" +
									"現在の値は" + checkRangeValue + "です．");
						}
					});

					// 増幅値調整
					SeekBar ampvalSeekBar = (SeekBar) seekView.findViewById(R.id.ampval);
					final TextView ampvalText = (TextView) seekView.findViewById(R.id.ampvaltext);
					ampvalText.setText("増幅器にかける場合に，何倍増幅するかを調整できます．標準は2倍です．\n" +
							"現在の値は" + ampValue + "です．");

					ampvalSeekBar.setMax(10);
					ampvalSeekBar.setProgress(2);
					ampvalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
						@Override
						public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
							ampValue = seekBar.getProgress() * 1.0;
							ampvalText.setText("増幅器にかける場合に，何倍増幅するかを調整できます．標準は2倍です．\n" +
									"現在の値は" + ampValue + "です．");
						}

						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {
						}

						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {
							ampValue = seekBar.getProgress() * 1.0;
							ampvalText.setText("増幅器にかける場合に，何倍増幅するかを調整できます．標準は2倍です．\n" +
									"現在の値は" + ampValue + "です．");
						}
					});

					AlertDialog.Builder dialog = new AlertDialog.Builder(Registration.this);
					dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
						@Override
						public boolean onKey(DialogInterface dialog1, int keyCode, KeyEvent event) {
							return keyCode == KeyEvent.KEYCODE_BACK;
						}
					});
					dialog.setTitle("増幅器設定");
					dialog.setView(seekView);
					dialog.setCancelable(false);
					dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog1, int which) {
							mResult.setAmpAndRange(ampValue, checkRangeValue);
						}
					});
					dialog.show();
				}
				return true;

			case R.id.reset:
				if (getMotionBtn.isClickable()) {
					AlertDialog.Builder alert = new AlertDialog.Builder(Registration.this);
					alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
						@Override
						public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
							return keyCode == KeyEvent.KEYCODE_BACK;
						}
					});
					alert.setCancelable(false);
					alert.setTitle("データ取得リセット");
					alert.setMessage("本当にデータ取得をやり直しますか？");
					alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});

					alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mGetData.reset();
						}
					});

					alert.show();
				}
				return true;
		}
		return false;
	}


	/**
	 * スタート画面に移動するメソッド
	 */
	public void finishRegist() {
		LogUtil.log(Log.INFO);
		Intent intent = new Intent();
		intent.setClassName(getPackageName(), getPackageName() + ".Start");
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

		startActivityForResult(intent, 0);
		finish();
	}
}
