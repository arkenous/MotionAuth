
package com.example.motionauth;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnKeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.os.Message;
import android.os.Handler;


/**
 * アプリを起動した際に最初に表示されるアクティビティ
 * モード選択を行う
 *
 * @author Kensuke Kousaka
 */
public class Start extends Activity
    {
        private Handler handler;

        private final static int POSITIVE = 1;
        private final static int NEUTRAL = 2;
        private final static int NEGATIVE = 3;

        private final static int DOUBLE = 2;
        private final static int TRIPLE = 3;


        @Override
        protected void onCreate (Bundle savedInstanceState)
            {
                super.onCreate(savedInstanceState);

                // タイトルバーの非表示
                requestWindowFeature(Window.FEATURE_NO_TITLE);
                setContentView(R.layout.activity_start);

                chooseMode();
            }


        /**
         * モード選択
         */
        private void chooseMode ()
            {
                Button startbtn = (Button) findViewById(R.id.start);

                startbtn.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick (View v)
                        {
                            String[] btnMsg = {"登録者一覧モード", "認証試験モード", "新規登録モード"};
                            alertDialog(TRIPLE, btnMsg, "モード選択", "モードを選択してください");
                            handler = new Handler()
                            {
                                public void handleMessage (Message msg)
                                    {
                                        if (msg.arg1 == POSITIVE)
                                            {
                                                // 登録者一覧モード
                                                moveActivity("com.example.motionauth", "com.example.motionauth.ViewDataList.RegistrantList", true);
                                            }
                                        else if (msg.arg1 == NEUTRAL)
                                            {
                                                // 認証試験モード
                                                moveActivity("com.example.motionauth", "com.example.motionauth.Authentication.AuthNameInput", true);
                                            }
                                        else if (msg.arg1 == NEGATIVE)
                                            {
                                                // 新規登録モード
                                                moveActivity("com.example.motionauth", "com.example.motionauth.Registration.RegistNameInput", true);
                                            }
                                    }
                            };
                        }
                });
            }


        /**
         * アラートダイアログ作成
         *
         * @param choiceNum 2択か3択か
         * @param btnMsg    選択肢ボタンの文字列
         * @param title     ダイアログのタイトル
         * @param msg       ダイアログの説明
         */
        private void alertDialog (int choiceNum, String[] btnMsg, String title, String msg)
            {
                if (choiceNum == DOUBLE)
                    {
                        AlertDialog.Builder alert = new AlertDialog.Builder(this);
                        alert.setOnKeyListener(new OnKeyListener()
                        {
                            public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event)
                                {
                                    // アラート画面に特定のキー動作をかませる
                                    if (keyCode == KeyEvent.KEYCODE_BACK)
                                        {
                                            // Backキーが押された場合
                                            // ダイアログを閉じて，アクティビティを閉じる
                                            dialog.dismiss();
                                            finish();

                                            return true;
                                        }
                                    return false;
                                }
                        });

                        // ダイアログ外をタッチしてもダイアログが閉じないようにする
                        alert.setCancelable(false);

                        alert.setTitle(title);
                        alert.setMessage(msg);

                        // PositiveButtonにより，ダイアログの左側に配置される
                        alert.setPositiveButton(btnMsg[0], new DialogInterface.OnClickListener()
                        {
                            public void onClick (DialogInterface dialog, int which)
                                {
                                    Message msg = new Message();
                                    msg.arg1 = POSITIVE;

                                    handler.sendMessage(msg);
                                }
                        });

                        alert.setNegativeButton(btnMsg[1], new DialogInterface.OnClickListener()
                        {
                            public void onClick (DialogInterface dialog, int which)
                                {
                                    Message msg = new Message();
                                    msg.arg1 = NEGATIVE;

                                    handler.sendMessage(msg);
                                }
                        });

                        // ダイアログを表示する
                        alert.show();
                    }
                else if (choiceNum == TRIPLE)
                    {
                        AlertDialog.Builder alert = new AlertDialog.Builder(this);
                        alert.setOnKeyListener(new OnKeyListener()
                        {
                            public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event)
                                {
                                    // アラート画面に特定のキー動作をかませる
                                    if (keyCode == KeyEvent.KEYCODE_BACK)
                                        {
                                            // Backキーを押した場合
                                            // ダイアログを閉じて，アクティビティを閉じる
                                            dialog.dismiss();
                                            finish();

                                            return true;
                                        }
                                    return false;
                                }
                        });

                        // ダイアログ外をタッチしてもダイアログを閉じないようにする
                        alert.setCancelable(false);

                        alert.setTitle(title);
                        alert.setMessage(msg);

                        // PositiveButtonにより，ダイアログの左側に配置される
                        alert.setPositiveButton(btnMsg[0], new DialogInterface.OnClickListener()
                        {
                            public void onClick (DialogInterface dialog, int which)
                                {
                                    Message msg = new Message();
                                    msg.arg1 = POSITIVE;

                                    handler.sendMessage(msg);
                                }
                        });

                        alert.setNeutralButton(btnMsg[1], new DialogInterface.OnClickListener()
                        {
                            public void onClick (DialogInterface dialog, int which)
                                {
                                    Message msg = new Message();
                                    msg.arg1 = NEUTRAL;

                                    handler.sendMessage(msg);
                                }
                        });

                        alert.setNegativeButton(btnMsg[2], new DialogInterface.OnClickListener()
                        {
                            public void onClick (DialogInterface dialog, int which)
                                {
                                    Message msg = new Message();
                                    msg.arg1 = NEGATIVE;

                                    handler.sendMessage(msg);
                                }
                        });

                        // ダイアログを表示する
                        alert.show();
                    }
            }


        /**
         * アクティビティを移動する
         *
         * @param pkgName 移動先のパッケージ名
         * @param actName 移動先のアクティビティ名
         * @param flg     戻るキーを押した際にこのアクティビティを表示させるかどうか
         */
        private void moveActivity (String pkgName, String actName, boolean flg)
            {
                Intent intent = new Intent();

                intent.setClassName(pkgName, actName);

                if (flg == true)
                    {
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    }

                startActivityForResult(intent, 0);
            }
    }
