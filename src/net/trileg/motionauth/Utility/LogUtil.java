package net.trileg.motionauth.Utility;

import android.util.Log;

import static android.util.Log.*;


public class LogUtil {
  private static final String TAG = "Logging";

  private static boolean mIsShowLog = false;

  public static void setShowLog(boolean isShowLog) {
    mIsShowLog = isShowLog;
  }

  public static void log() {
    outputLog(DEBUG, null, null);
  }

  public static void log(String message) {
    outputLog(DEBUG, message, null);
  }

  public static void log(int type) {
    outputLog(type, null, null);
  }

  public static void log(int type, String message) {
    outputLog(type, message, null);
  }

  public static void log(int type, String message, Throwable throwable) {
    outputLog(type, message, throwable);
  }

  private static void outputLog(int type, String message, Throwable throwable) {
    if (!mIsShowLog)
      // ログ出力フラグが立っていない場合は何もしない．
      return;

    // ログのメッセージ部分にスタックトレース情報を付加する．
    if (message == null) message = getStackTraceInfo();
    else message = getStackTraceInfo() + message;

    // ログを出力
    switch (type) {
      case DEBUG:
        if (throwable == null) Log.d(TAG, message);
        else Log.d(TAG, message, throwable);
        break;
      case ERROR:
        if (throwable == null) Log.e(TAG, message);
        else Log.e(TAG, message, throwable);
        break;
      case INFO:
        if (throwable == null) Log.i(TAG, message);
        else Log.i(TAG, message, throwable);
        break;
      case VERBOSE:
        if (throwable == null) Log.v(TAG, message);
        else Log.v(TAG, message, throwable);
        break;
      case WARN:
        if (throwable == null) Log.w(TAG, message);
        else Log.w(TAG, message, throwable);
        break;
    }
  }

  /**
   * Get basic information of caller from stacktrace.
   *
   * @return <<className#methodName:lineNumber>>
   */
  private static String getStackTraceInfo() {
    // 0:VM 1:thread 2:getStackTraceInfo() 3:outputLog() 4:logDebug(), etc. 5:caller
    StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[5];

    String fullName = stackTraceElement.getClassName();
    String className = fullName.substring(fullName.lastIndexOf(".") + 1);
    String methodName = stackTraceElement.getMethodName();
    int lineNumber = stackTraceElement.getLineNumber();

    return "<<" + className + "#" + methodName + ":" + lineNumber + ">> ";
  }
}
