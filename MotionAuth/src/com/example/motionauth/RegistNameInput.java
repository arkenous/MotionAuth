package com.example.motionauth;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class RegistNameInput extends Activity
{
	public static String name;


	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate (savedInstanceState);
		setContentView (R.layout.activity_regist_name_input);

		nameInput ();
	}


	/**
	 * ユーザの名前入力を受け付ける処理
	 */
	private void nameInput ()
	{
		final EditText nameInput = (EditText) findViewById (R.id.nameInputEditText);

		nameInput.addTextChangedListener (new TextWatcher ()
		{
			// 変更前
			public void beforeTextChanged (CharSequence s, int start, int count, int after)
			{

			}


			// 変更直前
			public void onTextChanged (CharSequence s, int start, int before, int count)
			{

			}


			// 変更後
			public void afterTextChanged (Editable s)
			{
				// ユーザの入力した名前をnameに格納
				name = nameInput.getText ().toString ();
			}
		});

		nameInput.setOnKeyListener (new OnKeyListener ()
		{
			public boolean onKey (View v, int keyCode, KeyEvent event)
			{
				if (event.getAction () == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER)
				{
					// ソフトウェアキーボードのEnterキーを押した時，ソフトウェアキーボードを閉じる
					InputMethodManager inputMethodManager = (InputMethodManager) getSystemService (Context.INPUT_METHOD_SERVICE);
					inputMethodManager.hideSoftInputFromWindow (v.getWindowToken (), 0);

					return true;
				}
				return false;
			}
		});

		// OKボタンを押した時に，次のアクティビティに移動
		final Button ok = (Button) findViewById (R.id.okButton);

		ok.setOnClickListener (new OnClickListener ()
		{
			public void onClick (View v)
			{
				// nameが入力されているかの確認
				if (nameInput.getText().toString().isEmpty())
				{
					Toast.makeText(RegistNameInput.this, "名前が入力されていません", Toast.LENGTH_LONG).show();
				}
				else
				{
					moveActivity("com.example.motionauth", "com.example.motionauth.RegistMotion", false);
				}
			}
		});
	}


	@Override
	public boolean onCreateOptionsMenu (Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater ().inflate (R.menu.regist_name_input, menu);
		return true;
	}


	/**
	 * アクティビティを移動する
	 * @param pkgName 移動先のパッケージ名
	 * @param actName 移動先のアクティビティ名
	 * @param flg 戻るキーを押した際にこのアクティビティを表示させるかどうか
	 */
	private void moveActivity (String pkgName, String actName, boolean flg)
	{
		Intent intent = new Intent ();

		intent.setClassName (pkgName, actName);

		if (flg == true)
		{
			intent.setFlags (Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		}

		startActivityForResult (intent, 0);
	}

}
