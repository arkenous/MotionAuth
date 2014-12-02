package com.example.motionauth.ViewDataList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.example.motionauth.R;
import com.example.motionauth.Utility.LogUtil;

import java.util.ArrayList;
import java.util.Map;


/**
 * 登録されているユーザ名を一覧表示する．
 * ユーザ名が選択されたら，そのユーザのデータをViewRegistedDataアクティビティにて表示する
 *
 * @author Kensuke Kousaka
 */
public class RegistrantList extends Activity {
	String item;


	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LogUtil.log(Log.INFO);

		// タイトルバーの非表示
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_registrant_list);

		registrantList();
	}


	/**
	 * 登録されているユーザ名のリストを表示する
	 * ユーザ名が選択されたら，そのユーザ名をViewRegistedDataに送る
	 */
	private void registrantList () {
		LogUtil.log(Log.INFO);

		// 登録されているユーザ名のリストを作成する
		ArrayList<String> userList = getRegistrantName();

		final ListView lv = (ListView) findViewById(R.id.listView1);

		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

		try {
			// アイテム追加
			for (String s : userList) adapter.add(s);
		}
		catch (NullPointerException e) {
			AlertDialog.Builder alert = new AlertDialog.Builder(RegistrantList.this);
			alert.setTitle("エラー");
			alert.setMessage("登録されていないユーザです．\nスタート画面に戻ります．");
			alert.setCancelable(false);
			alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick (DialogInterface dialog, int which) {
					RegistrantList.this.moveActivity("com.example.motionauth", "com.example.motionauth.Start", true);
				}
			});
			alert.show();
			finish();
		}

		// リストビューにアダプタを設定
		lv.setAdapter(adapter);

		// リストビューのアイテムがクリックされた時
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick (AdapterView<?> parent, View v, int position, long id) {
				LogUtil.log(Log.DEBUG, "Click item");

				// クリックされたアイテムを取得
				item = lv.getItemAtPosition(position).toString();

				// itemを次のアクティビティに送る
				RegistrantList.this.moveActivity("com.example.motionauth", "com.example.motionauth.ViewDataList.ViewRegistedData", true);
			}
		});
	}


	/**
	 * 指定されたディレクトリ以下のファイルリストを作成する
	 *
	 * @return 作成されたString配列型のリスト
	 */
	private ArrayList<String> getRegistrantName () {
		LogUtil.log(Log.INFO);

		Context mContext = RegistrantList.this.getApplicationContext();
		SharedPreferences preferences = mContext.getSharedPreferences("UserList", Context.MODE_PRIVATE);

		ArrayList<String> keyList = new ArrayList<>();

		Map<String, ?> allEntries = preferences.getAll();
		for (Map.Entry<String, ?> entry : allEntries.entrySet()) keyList.add(entry.getKey());

		return keyList;
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

		if (actName.equals("com.example.motionauth.ViewDataList.ViewRegistedData")) intent.putExtra("item", item);

		if (flg) intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

		startActivityForResult(intent, 0);
	}
}
