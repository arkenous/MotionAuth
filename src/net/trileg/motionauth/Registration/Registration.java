package net.trileg.motionauth.Registration;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.*;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import net.trileg.motionauth.R;
import net.trileg.motionauth.Utility.ListToArray;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.util.Log.*;
import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.MotionEvent.*;
import static net.trileg.motionauth.Registration.InputName.userName;
import static net.trileg.motionauth.Utility.Enum.STATUS.DOWN;
import static net.trileg.motionauth.Utility.Enum.STATUS.UP;
import static net.trileg.motionauth.Utility.LogUtil.log;

/**
 * Registration.
 *
 * @author Kensuke Kosaka
 */
public class Registration extends Activity {
  private static final int VIBRATOR_LONG = 100;
  private TextView secondTv;
  private TextView countSecondTv;
  private TextView rest;
  private Button getMotionBtn;
  private GetData mGetData;
  private Result mResult;
  private ListToArray mListToArray = new ListToArray();
  private Registration mRegistration;

  private double checkRangeValue = 2.0;
  private double ampValue = 2.0;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    log(INFO);

    setContentView(R.layout.activity_regist_motion);
    mRegistration = this;
    registration();
  }


  /**
   * Registration event listener and call GetData using ExecutorService to collect data.
   */
  private void registration() {
    log(INFO);

    final Vibrator mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    TextView nameTv = (TextView) findViewById(R.id.textView2);
    secondTv = (TextView) findViewById(R.id.secondTextView);
    countSecondTv = (TextView) findViewById(R.id.textView4);
    rest = (TextView) findViewById(R.id.rest);
    getMotionBtn = (Button) findViewById(R.id.button1);

    nameTv.setText(userName + "さん読んでね！");
    mGetData = new GetData(mRegistration, secondTv, getMotionBtn, mVibrator, UP, this);

    getMotionBtn.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
          case ACTION_DOWN:
            log(VERBOSE, "Action down getMotionBtn");
            getMotionBtn.setText("取得中");
            rest.setText("");
            secondTv.setText("0");
            countSecondTv.setText("秒");
            mVibrator.vibrate(VIBRATOR_LONG);
            mGetData.changeStatus(DOWN);
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(mGetData);
            executorService.shutdown();
            break;
          case ACTION_UP:
            log(VERBOSE, "Action up getMotionBtn");
            mGetData.changeStatus(UP);
            mVibrator.vibrate(VIBRATOR_LONG);
            getMotionBtn.setText("モーションデータ取得");
            rest.setText("あと");
            countSecondTv.setText("回");
            break;
          case ACTION_CANCEL:
            log(VERBOSE, "Action cancel getMotionBtn");
            mGetData.changeStatus(UP);
            mVibrator.vibrate(VIBRATOR_LONG);
            getMotionBtn.setText("モーションデータ取得");
            rest.setText("あと");
            countSecondTv.setText("回");
            break;
        }
        return true;
      }
    });
  }


  /**
   * Call when data collecting is finished.
   * Call Result using ExecutorService to register and show result.
   *
   * @param linearAcceleration Original linear acceleration data collecting from GetData.
   * @param gyro               Original gyroscope data collecting from GetData.
   */
  void finishGetMotion(ArrayList<ArrayList<ArrayList<Float>>> linearAcceleration,
                       ArrayList<ArrayList<ArrayList<Float>>> gyro) {
    log(INFO);
    if (getMotionBtn.isClickable()) getMotionBtn.setClickable(false);
    secondTv.setText("0");
    getMotionBtn.setText("データ処理中");

    log(DEBUG, "Start initialize progress dialog");

    ProgressDialog progressDialog = new ProgressDialog(this);
    progressDialog.setTitle("計算処理中");
    progressDialog.setMessage("しばらくお待ちください");
    progressDialog.setIndeterminate(false);
    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    progressDialog.setCancelable(false);

    log(DEBUG, "Finish initialize Progress dialog");

    progressDialog.show();

    mResult = new Result(mRegistration, mListToArray.listTo3DArray(linearAcceleration),
        mListToArray.listTo3DArray(gyro), getMotionBtn, progressDialog,
        checkRangeValue, ampValue, this, mGetData);
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    executorService.submit(mResult);
    executorService.shutdown();
  }


  @Override
  protected void onResume() {
    super.onResume();
    log(INFO);
    mGetData.registrationSensor();
  }


  @Override
  protected void onPause() {
    super.onPause();
    log(INFO);
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

          //region 閾値調整
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
          //endregion

          //region 増幅値調整
          SeekBar amplifierSeekBar = (SeekBar) seekView.findViewById(R.id.amplifier);
          final TextView amplifierText = (TextView) seekView.findViewById(R.id.amplifierText);
          amplifierText.setText("増幅器にかける場合に，何倍増幅するかを調整できます．標準は2倍です．\n" +
              "現在の値は" + ampValue + "です．");

          amplifierSeekBar.setMax(10);
          amplifierSeekBar.setProgress(2);
          amplifierSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
              ampValue = seekBar.getProgress() * 1.0;
              amplifierText.setText("増幅器にかける場合に，何倍増幅するかを調整できます．標準は2倍です．\n" +
                  "現在の値は" + ampValue + "です．");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
              ampValue = seekBar.getProgress() * 1.0;
              amplifierText.setText("増幅器にかける場合に，何倍増幅するかを調整できます．標準は2倍です．\n" +
                  "現在の値は" + ampValue + "です．");
            }
          });
          //endregion

          AlertDialog.Builder dialog = new AlertDialog.Builder(Registration.this);
          dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog1, int keyCode, KeyEvent event) {
              return keyCode == KEYCODE_BACK;
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
              return keyCode == KEYCODE_BACK;
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
   * Move to Start activity.
   */
  void finishRegistration() {
    log(INFO);

    Intent intent = new Intent();
    intent.setClassName(getPackageName(), getPackageName() + ".Start");
    intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_NEW_TASK);

    startActivity(intent);
    finish();
  }
}
