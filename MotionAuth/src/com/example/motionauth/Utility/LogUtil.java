package com.example.motionauth.Utility;

import android.util.Log;

/**
 * @author Kensuke Kousaka
 */
public class LogUtil {
	private static final String TAG = "Logging";

	private static boolean mIsShowLog = false;

	public static void setShowLog (boolean isShowLog) {
		mIsShowLog = isShowLog;
	}

	public static void log () {
		outputLog(Log.DEBUG, null, null);
	}

	public static void log (String message) {
		outputLog(Log.DEBUG, message, null);
	}

	public static void log (int type) {
		outputLog(type, null, null);
	}

	public static void log (int type, String message) {
		outputLog(type, message, null);
	}

	public static void log (int type, String message, Throwable throwable) {
		outputLog(type, message, throwable);
	}

	private static void outputLog (int type, String message, Throwable throwable) {
		if (!mIsShowLog) {
			// ログ出力フラグが立っていない場合は何もしない．
			return;
		}

		// ログのメッセージ部分にスタックトレース情報を付加する．
		if (message == null) {
			message = getStackTraceInfo();
		}
		else {
			message = getStackTraceInfo() + message;
		}

		// ログを出力
		switch (type) {
			case Log.DEBUG:
				if (throwable == null) {
					Log.d(TAG, message);
				}
				else {
					Log.d(TAG, message, throwable);
				}
				break;
			case Log.ERROR:
				if (throwable == null) {
					Log.e(TAG, message);
				}
				else {
					Log.e(TAG, message, throwable);
				}
				break;
			case Log.INFO:
				if (throwable == null) {
					Log.i(TAG, message);
				}
				else {
					Log.i(TAG, message, throwable);
				}
				break;
			case Log.VERBOSE:
				if (throwable == null) {
					Log.v(TAG, message);
				}
				else {
					Log.v(TAG, message, throwable);
				}
				break;
			case Log.WARN:
				if (throwable == null) {
					Log.w(TAG, message);
				}
				else {
					Log.w(TAG, message, throwable);
				}
				break;
		}
	}

	/**
	 * スタックトレースから呼び出し元の基本情報を取得
	 *
	 * @return <<className#methodName:lineNumber>>
	 */
	private static String getStackTraceInfo () {
		// 現在のスタックトレースを取得
		// 0:VM 1:スレッド 2:getStackTraceInfo() 3:outputLog() 4:logDebug()等 5:呼び出し元
		StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[5];

		String fullName = stackTraceElement.getClassName();
		String className = fullName.substring(fullName.lastIndexOf(".") + 1);
		String methodName = stackTraceElement.getMethodName();
		int lineNumber = stackTraceElement.getLineNumber();

		return "<<" + className + "#" + methodName + ":" + lineNumber + ">> ";
	}
}
