package net.trileg.motionauth;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import net.trileg.motionauth.Utility.LogUtil;


/**
 * Start application from here.
 * Select mode (Registration, Authentication View data).
 *
 * @author Kensuke Kosaka
 */
public class Start extends Activity {
  private Context mContext;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mContext = this;

    // Read log configuration file and switch log output.
    boolean isShowLog = getResources().getBoolean(R.bool.isShowLog);
    LogUtil.setShowLog(isShowLog);

    LogUtil.log(Log.INFO);

    // Disable title bar.
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.activity_start);

    selectMode();
  }


  /**
   * Select mode.
   */
  private void selectMode() {
    LogUtil.log(Log.INFO);

    Button startBtn = (Button) findViewById(R.id.start);
    startBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        LogUtil.log(Log.VERBOSE, "Click start button.");

        String[] btnMsg = {"View data", "Authentication", "Registration"};
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
          @Override
          public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
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
    LogUtil.log(Log.INFO);

    Intent intent = new Intent();
    intent.setClassName(pkgName, actName);
    if (flg) intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

    startActivity(intent);
  }
}
