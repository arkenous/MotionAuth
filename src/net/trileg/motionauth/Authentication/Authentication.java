package net.trileg.motionauth.Authentication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import net.trileg.motionauth.R;
import net.trileg.motionauth.Utility.ListToArray;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.util.Log.*;
import static android.view.MotionEvent.*;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static net.trileg.motionauth.Authentication.InputName.userName;
import static net.trileg.motionauth.Utility.Enum.STATUS.DOWN;
import static net.trileg.motionauth.Utility.Enum.STATUS.UP;
import static net.trileg.motionauth.Utility.LogUtil.log;

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
  private ListToArray mListToArray = new ListToArray();


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

    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    TextView nameTv = (TextView) findViewById(R.id.textView1);
    secondTv = (TextView) findViewById(R.id.secondTextView);
    countSecondTv = (TextView) findViewById(R.id.textView4);
    getMotionBtn = (Button) findViewById(R.id.button1);

    nameTv.setText(userName + "さん読んでね！");
    mGetData = new GetData(this, getMotionBtn, secondTv, vibrator, UP);

    getMotionBtn.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
          case ACTION_DOWN:
            log(VERBOSE, "Action down getMotionBtn");
            getMotionBtn.setText("取得中");
            mGetData.changeStatus(DOWN);
            ExecutorService executorService = newSingleThreadExecutor();
            executorService.execute(mGetData);
            executorService.shutdown();
            break;
          case ACTION_UP:
            log(VERBOSE, "Action up getMotionBtn");
            mGetData.changeStatus(UP);
            break;
          case ACTION_CANCEL:
            log(VERBOSE, "Action cancel getMotionBtn");
            mGetData.changeStatus(UP);
            break;
        }
        return true;
      }
    });
  }


  /**
   * Call when data collecting is finished.
   * Call Result using ExecutorService to authenticate and show result.
   *
   * @param linearAccel Original linear acceleration data collecting from GetData.
   * @param gyro        Original gyroscope data collecting from GetData.
   */
  void finishGetMotion(ArrayList<ArrayList<Float>> linearAccel,
                       ArrayList<ArrayList<Float>> gyro) {
    log(INFO);
    if (getMotionBtn.isClickable()) getMotionBtn.setClickable(false);
    secondTv.setText("0");
    getMotionBtn.setText("認証処理中");

    log(DEBUG, "Start initialize ProgressDialog");

    ProgressDialog progressDialog = new ProgressDialog(this);
    progressDialog.setTitle("計算処理中");
    progressDialog.setMessage("しばらくお待ちください");
    progressDialog.setIndeterminate(false);
    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    progressDialog.setCancelable(false);

    log(DEBUG, "Finish initialize ProgressDialog");

    progressDialog.show();


    Result mResult = new Result(this, mListToArray.listTo2DArray(linearAccel),
        mListToArray.listTo2DArray(gyro), getMotionBtn, progressDialog, mGetData);
    ExecutorService executorService = newSingleThreadExecutor();
    executorService.execute(mResult);
    executorService.shutdown();
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
    mGetData.registrationSensor();
  }


  @Override
  protected void onPause() {
    super.onPause();
    mGetData.unRegistrationSensor();
  }
}
