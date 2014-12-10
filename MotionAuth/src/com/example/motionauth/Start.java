package com.example.motionauth;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import com.example.motionauth.Utility.LogUtil;


/**
 * アプリを起動した際に最初に表示されるアクティビティ
 * モード選択を行う
 *
 * @author Kensuke Kousaka
 */
public class Start extends Activity {
	private final static int POSITIVE = 1;
	private final static int NEUTRAL = 2;
	private final static int NEGATIVE = 3;

	private final static int DOUBLE = 2;
	private final static int TRIPLE = 3;

	private Handler handler;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 設定ファイルのフラグを読み取ってログ出力を切り替える
		boolean isShowLog = getResources().getBoolean(R.bool.isShowLog);
		LogUtil.setShowLog(isShowLog);

		LogUtil.log(Log.INFO);

		// タイトルバーの非表示
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_start);

		selectMode();
	}


	/**
	 * モード選択
	 */
	private void selectMode () {
		LogUtil.log(Log.INFO);

		Button startBtn = (Button) findViewById(R.id.start);

		startBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick (View v) {
				LogUtil.log(Log.DEBUG, "Click start button");

				String[] btnMsg = {"データ閲覧モード", "認証試験モード", "新規登録モード"};
				Start.this.alertDialog(TRIPLE, btnMsg, "モード選択", "モードを選択してください");
				handler = new Handler() {
					public void handleMessage (Message msg) {
						if (msg.arg1 == POSITIVE) {
							LogUtil.log(Log.DEBUG, "POSITIVE");
							// 登録者一覧モード
							moveActivity("com.example.motionauth", "com.example.motionauth.ViewDataList.RegistrantList", true);
						}
						else if (msg.arg1 == NEUTRAL) {
							LogUtil.log(Log.DEBUG, "NEUTRAL");
							// 認証試験モード
							moveActivity("com.example.motionauth", "com.example.motionauth.Authentication.AuthNameInput", true);
						}
						else if (msg.arg1 == NEGATIVE) {
							LogUtil.log(Log.DEBUG, "NEGATIVE");
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
	private void alertDialog (int choiceNum, String[] btnMsg, String title, String msg) {
		LogUtil.log(Log.INFO);

		if (choiceNum == DOUBLE) {
			LogUtil.log(Log.DEBUG, "DOUBLE");

			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
				@Override
				public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event) {
					// アラート画面に特定のキー動作をかませる
					if (keyCode == KeyEvent.KEYCODE_BACK) {
						// Backキーが押された場合
						// ダイアログを閉じて，アクティビティを閉じる
						dialog.dismiss();
						Start.this.finish();

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
			alert.setPositiveButton(btnMsg[0], new DialogInterface.OnClickListener() {
				@Override
				public void onClick (DialogInterface dialog, int which) {
					Message msg1 = new Message();
					msg1.arg1 = POSITIVE;

					handler.sendMessage(msg1);
				}
			});

			alert.setNegativeButton(btnMsg[1], new DialogInterface.OnClickListener() {
				@Override
				public void onClick (DialogInterface dialog, int which) {
					Message msg1 = new Message();
					msg1.arg1 = NEGATIVE;

					handler.sendMessage(msg1);
				}
			});

			// ダイアログを表示する
			alert.show();
		}
		else if (choiceNum == TRIPLE) {
			LogUtil.log(Log.DEBUG, "TRIPLE");

			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
				@Override
				public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event) {
					// アラート画面に特定のキー動作をかませる
					if (keyCode == KeyEvent.KEYCODE_BACK) {
						// Backキーを押した場合
						// ダイアログを閉じて，アクティビティを閉じる
						dialog.dismiss();
						Start.this.finish();

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
			alert.setPositiveButton(btnMsg[0], new DialogInterface.OnClickListener() {
				@Override
				public void onClick (DialogInterface dialog, int which) {
					Message msg1 = new Message();
					msg1.arg1 = POSITIVE;

					handler.sendMessage(msg1);
				}
			});

			alert.setNeutralButton(btnMsg[1], new DialogInterface.OnClickListener() {
				@Override
				public void onClick (DialogInterface dialog, int which) {
					Message msg1 = new Message();
					msg1.arg1 = NEUTRAL;

					handler.sendMessage(msg1);
				}
			});

			alert.setNegativeButton(btnMsg[2], new DialogInterface.OnClickListener() {
				@Override
				public void onClick (DialogInterface dialog, int which) {
					Message msg1 = new Message();
					msg1.arg1 = NEGATIVE;

					handler.sendMessage(msg1);
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
	private void moveActivity (String pkgName, String actName, boolean flg) {
		LogUtil.log(Log.INFO);

		Intent intent = new Intent();

		intent.setClassName(pkgName, actName);

		if (flg) intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

		startActivityForResult(intent, 0);
	}
}
