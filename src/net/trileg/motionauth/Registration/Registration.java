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
import net.trileg.motionauth.Processing.GetData;
import net.trileg.motionauth.Processing.Timer;
import net.trileg.motionauth.R;
import net.trileg.motionauth.Utility.ListToArray;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.util.Log.*;
import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.MotionEvent.*;
import static net.trileg.motionauth.Registration.InputName.userName;
import static net.trileg.motionauth.Utility.LogUtil.log;

/**
 * Registration.
 *
 * @author Kensuke Kosaka
 */
public class Registration extends Activity {
  private static final int VIBRATOR_LONG = 100;
  private static final int LEAST_MOTION_LENGTH = 10;
  private TextView second;
  private TextView unit;
  private TextView rest;
  private Button getMotion;
  private Result result;
  private ListToArray listToArray = new ListToArray();
  private Registration registration;

  private double checkRangeValue = 2.0;
  private double ampValue = 2.0;
  private Future<Boolean> timer;
  private GetData linearAcceleration;
  private GetData gyroscope;
  private Future<ArrayList<ArrayList<Float>>> linearAccelerationFuture;
  private Future<ArrayList<ArrayList<Float>>> gyroscopeFuture;

  private ArrayList<ArrayList<ArrayList<Float>>> linearAccelerationData = new ArrayList<>();
  private ArrayList<ArrayList<ArrayList<Float>>> gyroscopeData = new ArrayList<>();
  private int countTime = 0;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    log(INFO);

    setContentView(R.layout.activity_regist_motion);
    registration = this;
    registration();
  }


  /**
   * Registration event listener and call GetData using ExecutorService to collect data.
   */
  private void registration() {
    log(INFO);

    final Vibrator mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    TextView nameTv = (TextView) findViewById(R.id.textView2);
    second = (TextView) findViewById(R.id.secondTextView);
    unit = (TextView) findViewById(R.id.textView4);
    rest = (TextView) findViewById(R.id.rest);
    getMotion = (Button) findViewById(R.id.button1);

    nameTv.setText(userName + "さん読んでね！");

    getMotion.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
          case ACTION_DOWN:
            log(VERBOSE, "Action down getMotion");

            getMotion.setText("取得中");
            rest.setText("");
            second.setText("0");
            unit.setText("秒");
            mVibrator.vibrate(VIBRATOR_LONG);

            ExecutorService executorService = Executors.newFixedThreadPool(3);
            timer = executorService.submit(new Timer(mVibrator, second));
            linearAcceleration = new GetData(registration, true);
            linearAccelerationFuture = executorService.submit(linearAcceleration);
            gyroscope = new GetData(registration, false);
            gyroscopeFuture = executorService.submit(gyroscope);

            executorService.shutdown();
            break;
          case ACTION_UP:
            log(VERBOSE, "Action up getMotion");
            mVibrator.vibrate(VIBRATOR_LONG);
            getMotion.setClickable(false);
            timer.cancel(true);
            linearAcceleration.unRegisterSensor();
            gyroscope.unRegisterSensor();

            organizeData(linearAccelerationFuture, gyroscopeFuture);
            break;
          case ACTION_CANCEL:
            log(VERBOSE, "Action up getMotion");
            mVibrator.vibrate(VIBRATOR_LONG);
            getMotion.setClickable(false);
            timer.cancel(true);
            linearAcceleration.unRegisterSensor();
            gyroscope.unRegisterSensor();

            organizeData(linearAccelerationFuture, gyroscopeFuture);
            break;
        }
        return true;
      }
    });
  }


  /**
   * Get data from GetData class, check data length, add data to list,
   * and call finishGetMotion when countTime is 3.
   * @param linearAccelerationFuture Future instance of linearAcceleration
   * @param gyroscopeFuture Future instance of gyroscope
   */
  private void organizeData(Future<ArrayList<ArrayList<Float>>> linearAccelerationFuture,
                            Future<ArrayList<ArrayList<Float>>> gyroscopeFuture) {
    log(INFO);
    try {
      ArrayList<ArrayList<Float>> linearAccelerationPerTime = linearAccelerationFuture.get();
      ArrayList<ArrayList<Float>> gyroscopePerTime = gyroscopeFuture.get();

      if (!checkDataLength(linearAccelerationPerTime, gyroscopePerTime)) reCollectDialog();
      else {
        linearAccelerationData.add(new ArrayList<>(linearAccelerationPerTime));
        gyroscopeData.add(new ArrayList<>(gyroscopePerTime));
        countTime++;
        if (countTime == 3) finishGetMotion(linearAccelerationData, gyroscopeData);
        else init();
      }
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
      init();
    }
  }


  /**
   * Check data length
   * @param linearAcceleration linearAcceleration data
   * @param gyroscope gyroscope data
   * @return return true if data length >= LEAST_MOTION_LENGTH
   */
  private boolean checkDataLength(ArrayList<ArrayList<Float>> linearAcceleration, ArrayList<ArrayList<Float>> gyroscope) {
    log(INFO);
    return (linearAcceleration.get(0).size() >= LEAST_MOTION_LENGTH || gyroscope.get(0).size() >= LEAST_MOTION_LENGTH);
  }


  /**
   * Show re-collect dialog when data length is too short
   */
  private void reCollectDialog() {
    log(INFO);
    AlertDialog.Builder alert = new AlertDialog.Builder(registration);
    alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
      @Override
      public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
        return keyCode == KeyEvent.KEYCODE_BACK;
      }
    });
    alert.setCancelable(false);
    alert.setTitle("モーション取り直し");
    alert.setMessage("モーションの入力時間が短すぎます．もう少し長めに入力してください．");
    alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int which) {
        reset();
      }
    }).show();
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
    if (getMotion.isClickable()) getMotion.setClickable(false);
    second.setText("0");
    getMotion.setText("データ処理中");

    log(DEBUG, "Start initialize progress dialog");

    ProgressDialog progressDialog = new ProgressDialog(this);
    progressDialog.setTitle("計算処理中");
    progressDialog.setMessage("しばらくお待ちください");
    progressDialog.setIndeterminate(false);
    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    progressDialog.setCancelable(false);

    log(DEBUG, "Finish initialize Progress dialog");

    progressDialog.show();

    result = new Result(registration, listToArray.listTo3DArray(linearAcceleration),
        listToArray.listTo3DArray(gyro), getMotion, progressDialog,
        checkRangeValue, ampValue, this);
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    executorService.submit(result);
    executorService.shutdown();
  }


  /**
   * Reset data and counter.
   */
  void reset() {
    log(INFO);
    linearAccelerationData.clear();
    gyroscopeData.clear();
    countTime = 0;
    init();
  }


  /**
   * Initialize TextView text and button clickable status.
   */
  private void init() {
    log(INFO);
    getMotion.setText("モーションデータ取得");
    rest.setText("あと");
    second.setText(String.valueOf(3 - countTime));
    unit.setText("回");
    getMotion.setClickable(true);
  }

  @Override
  protected void onResume() {
    super.onResume();
    log(INFO);
  }


  @Override
  protected void onPause() {
    super.onPause();
    //TODO モーションセンサの停止コードを書く
    log(INFO);
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
        if (getMotion.isClickable()) {
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
              result.setAmpAndRange(ampValue, checkRangeValue);
            }
          });
          dialog.show();
        }
        return true;

      case R.id.reset:
        if (getMotion.isClickable()) {
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
              reset();
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
