package net.trileg.motionauth.Authentication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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
import static android.view.MotionEvent.*;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static net.trileg.motionauth.Authentication.InputName.userName;
import static net.trileg.motionauth.Utility.LogUtil.log;

/**
 * Authentication.
 *
 * @author Kensuke Kosaka
 */
public class Authentication extends Activity {
  private static final int VIBRATOR_LONG = 100;
  private static final int LEAST_MOTION_LENGTH = 10;

  private TextView second;
  private TextView unit;
  private TextView rest;
  private Button getMotion;
  private GetData linearAcceleration;
  private GetData gyroscope;
  private Future<Boolean> timer;
  private Future<ArrayList<ArrayList<Float>>> linearAccelerationFuture;
  private Future<ArrayList<ArrayList<Float>>> gyroscopeFuture;
  private ListToArray listToArray = new ListToArray();


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    log(INFO);

    setContentView(R.layout.activity_auth_motion);
    authentication();
  }


  /**
   * Registration event listener and call GetData using ExecutorService to collect data.
   */
  private void authentication() {
    log(INFO);

    final Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    TextView nameTv = (TextView) findViewById(R.id.userName);
    second = (TextView) findViewById(R.id.second);
    unit = (TextView) findViewById(R.id.unit);
    rest = (TextView) findViewById(R.id.rest);
    getMotion = (Button) findViewById(R.id.getMotion);

    nameTv.setText(userName + "さん読んでね！");

    getMotion.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
          case ACTION_DOWN:
            log(DEBUG, "Action down getMotion");

            getMotion.setText("取得中");
            rest.setText("");
            second.setText("0");
            unit.setText("秒");
            vibrator.vibrate(VIBRATOR_LONG);

            ExecutorService executorService = Executors.newFixedThreadPool(3);
            timer = executorService.submit(new Timer(vibrator, second));
            linearAcceleration = new GetData(Authentication.this, true);
            gyroscope = new GetData(Authentication.this, false);
            linearAccelerationFuture = executorService.submit(linearAcceleration);
            gyroscopeFuture = executorService.submit(gyroscope);

            executorService.shutdown();
            break;
          case ACTION_UP:
            log(DEBUG, "Action up getMotion");
            vibrator.vibrate(VIBRATOR_LONG);
            getMotion.setClickable(false);
            timer.cancel(true);
            linearAcceleration.unRegisterSensor();
            gyroscope.unRegisterSensor();

            organizeData(linearAccelerationFuture, gyroscopeFuture);
            break;
          case ACTION_CANCEL:
            log(DEBUG, "Action cancel getMotion");
            vibrator.vibrate(VIBRATOR_LONG);
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
   * Get data from GetData class, check data length, and call finishGetMotion.
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
      else finishGetMotion(linearAccelerationPerTime, gyroscopePerTime);
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
  private boolean checkDataLength(ArrayList<ArrayList<Float>> linearAcceleration,
                                  ArrayList<ArrayList<Float>> gyroscope) {
    log(INFO);
    return (linearAcceleration.get(0).size() >= LEAST_MOTION_LENGTH
            || gyroscope.get(0).size() >= LEAST_MOTION_LENGTH);
  }


  /**
   * Show re-collect dialog when data length is too short
   */
  private void reCollectDialog() {
    log(INFO);
    AlertDialog.Builder alert = new AlertDialog.Builder(Authentication.this);
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
   * Call Result using ExecutorService to authenticate and show result.
   *
   * @param linearAccel Original linear acceleration data collecting from GetData.
   * @param gyro        Original gyroscope data collecting from GetData.
   */
  private void finishGetMotion(ArrayList<ArrayList<Float>> linearAccel,
                               ArrayList<ArrayList<Float>> gyro) {
    log(INFO);
    log(WARN, "Auth Calculation Started!!!");
    if (getMotion.isClickable()) getMotion.setClickable(false);
    second.setText("0");
    getMotion.setText("認証処理中");

    log(DEBUG, "Start initialize ProgressDialog");

    ProgressDialog progressDialog = new ProgressDialog(this);
    progressDialog.setTitle("計算処理中");
    progressDialog.setMessage("しばらくお待ちください");
    progressDialog.setIndeterminate(false);
    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    progressDialog.setCancelable(false);

    log(DEBUG, "Finish initialize ProgressDialog");

    progressDialog.show();
    
    Result result = new Result(listToArray.listTo2DArray(linearAccel),
                               listToArray.listTo2DArray(gyro), getMotion,
                               progressDialog, Authentication.this);
    ExecutorService executorService = newSingleThreadExecutor();
    executorService.execute(result);
    executorService.shutdown();
  }


  /**
   * Reset data.
   */
  void reset() {
    log(INFO);
    init();
  }


  /**
   * Initialize TextView text and button clickable status.
   */
  private void init() {
    log(INFO);
    getMotion.setText("モーションデータ取得");
    rest.setText("あと");
    second.setText("1");
    unit.setText("回");
    getMotion.setClickable(true);
  }


  /**
   * Move to Start activity.
   */
  void finishAuthentication() {
    log(INFO);
    Intent intent = new Intent();
    intent.setClassName(getPackageName(), getPackageName() + ".Start");
    intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_NEW_TASK);

    startActivity(intent);
    finish();
  }


  @Override
  protected void onResume() {
    super.onResume();
  }


  @Override
  protected void onPause() {
    super.onPause();
  }
}
