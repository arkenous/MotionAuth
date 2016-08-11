package net.trileg.motionauth.Processing;

import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.widget.TextView;

import java.util.concurrent.Callable;

import static android.util.Log.DEBUG;
import static android.util.Log.INFO;
import static net.trileg.motionauth.Utility.LogUtil.log;

public class Timer extends Handler implements Callable<Boolean> {
  private static final int VIBRATOR_SHORT = 25;
  private int countSecond = 0;
  private Vibrator vibrator = null;
  private TextView second = null;


  public Timer (Vibrator vibrator, TextView second) {
    log(INFO);
    countSecond = 0;
    this.vibrator = vibrator;
    this.second = second;
  }

  @Override
  public Boolean call() throws Exception {
    log(INFO);
    count();

    return true;
  }

  @Override
  public void dispatchMessage(@NonNull Message message) {
    switch (message.what) {
      case 0:
        vibrator.vibrate(VIBRATOR_SHORT);
        second.setText(String.valueOf(countSecond));
        break;
    }
  }


  /**
   * Count second.
   */
  private void count() {
    log(INFO);
    while (!Thread.interrupted()) {
      log(DEBUG, "countSecond: "+countSecond);
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
        break;
      }
      countSecond++;
      if (!Thread.interrupted()) super.sendEmptyMessage(0);
    }
  }
}
