package com.example.motionauth.Utility;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.KeyEvent;

/**
 * アラート作成・表示クラス
 *
 * @author Kensuke Kousaka
 */
public class Alert {
    private static final String TAG = Alert.class.getSimpleName();
    private int result = 0;

    public int createAlert (String[] btnMsg, String title, String msg, Context context) {
        if (btnMsg.length == 1) {
            Log.d(TAG, "btnMsg branch : 1");
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        return true;
                    }
                    return false;
                }
            });

            alert.setCancelable(false);

            alert.setTitle(title);
            alert.setMessage(msg);

            alert.setNeutralButton(btnMsg[0], new DialogInterface.OnClickListener() {
                @Override
                public void onClick (DialogInterface dialog, int which) {
                    result = DialogInterface.BUTTON_NEUTRAL;
                }
            });

            alert.show();
        }
        else if (btnMsg.length == 2) {
            Log.d(TAG, "btnMsg branch : 2");
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        return true;
                    }
                    return false;
                }
            });

            alert.setCancelable(false);

            alert.setTitle(title);
            alert.setMessage(msg);

            alert.setNegativeButton(btnMsg[0], new DialogInterface.OnClickListener() {
                @Override
                public void onClick (DialogInterface dialog, int which) {
                    result = DialogInterface.BUTTON_NEGATIVE;
                }
            });

            alert.setPositiveButton(btnMsg[1], new DialogInterface.OnClickListener() {
                @Override
                public void onClick (DialogInterface dialog, int which) {
                    result = DialogInterface.BUTTON_POSITIVE;
                }
            });

            alert.show();
        }
        else if (btnMsg.length == 3) {
            Log.d(TAG, "btnMsg branch : 3");
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        return true;
                    }
                    return false;
                }
            });

            alert.setCancelable(false);

            alert.setTitle(title);
            alert.setMessage(msg);

            alert.setNegativeButton(btnMsg[0], new DialogInterface.OnClickListener() {
                @Override
                public void onClick (DialogInterface dialog, int which) {
                    result = DialogInterface.BUTTON_NEGATIVE;
                }
            });

            alert.setNeutralButton(btnMsg[1], new DialogInterface.OnClickListener() {
                @Override
                public void onClick (DialogInterface dialog, int which) {
                    result = DialogInterface.BUTTON_NEUTRAL;
                }
            });

            alert.setPositiveButton(btnMsg[2], new DialogInterface.OnClickListener() {
                @Override
                public void onClick (DialogInterface dialog, int which) {
                    result = DialogInterface.BUTTON_POSITIVE;
                }
            });

            alert.show();
        }

        Log.d(TAG, "result : " + result);
        return result;
    }
}
