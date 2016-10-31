package net.trileg.motionauth;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.util.Log.DEBUG;
import static android.util.Log.INFO;
import static android.util.Log.VERBOSE;
import static android.view.KeyEvent.KEYCODE_BACK;
import static net.trileg.motionauth.Utility.LogUtil.log;
import static net.trileg.motionauth.Utility.LogUtil.setShowLog;


/**
 * Start application from here.
 * Select mode (Registration, Authentication View data).
 *
 * @author Kensuke Kosaka
 */
public class Start extends Activity {
  private Context mContext;

  static {
    System.loadLibrary("mlp-lib");
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mContext = this;

    // Read log configuration file and switch log output.
    boolean isShowLog = getResources().getBoolean(R.bool.isShowLog);
    setShowLog(isShowLog);

    log(INFO);

    // Disable title bar.
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.activity_start);

    double[][] input = {
        {0.0, 1.0},
        {2.0, 3.0}
    };
    double[][] retValue = mlplearn(input);
    for (double ret1[] : retValue) {
      for (double ret2 : ret1) {
        log(DEBUG, "Value: " + ret2);
      }
    }

    selectMode();
  }


  /**
   * Select mode.
   */
  private void selectMode() {
    log(INFO);

    Button startBtn = (Button) findViewById(R.id.start);
    startBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        log(VERBOSE, "Click start button.");

        String[] btnMsg = {"View data", "Authentication", "Registration"};
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
          @Override
          public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (keyCode == KEYCODE_BACK) {
              // Close dialog and finish activity if user click BACK KEY when dialog is showing.
              dialog.dismiss();
              finish();
              return true;
            }
            return false;
          }
        });
        // Dialog isn't closed when user tap outside of dialog.
        alert.setCancelable(false);
        alert.setTitle("Select mode");
        alert.setMessage("Please select mode.");

        alert.setPositiveButton(btnMsg[0], new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            moveActivity(getPackageName(), getPackageName() + ".ViewDataList.RegistrantList", true);
          }
        });
        alert.setNeutralButton(btnMsg[1], new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            moveActivity(getPackageName(), getPackageName() + ".Authentication.InputName", true);
          }
        });
        alert.setNegativeButton(btnMsg[2], new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            moveActivity(getPackageName(), getPackageName() + ".Registration.InputName", true);
          }
        });

        alert.show();
      }
    });
  }


  /**
   * Move activity.
   *
   * @param pkgName Destination package name.
   * @param actName Destination activity name.
   * @param flg     Whether to show this activity if user push BACK KEY.
   */
  private void moveActivity(String pkgName, String actName, boolean flg) {
    log(INFO);

    Intent intent = new Intent();
    intent.setClassName(pkgName, actName);
    if (flg) intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_NEW_TASK);

    startActivity(intent);
  }

  public native double[][] mlplearn(double[][] input);
}
