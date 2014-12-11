package com.example.motionauth.ViewDataList;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.example.motionauth.R;
import com.example.motionauth.Utility.LogUtil;
import com.example.motionauth.Utility.ManageData;

import java.util.ArrayList;


/**
 * RegistrantListより渡されたユーザ名を元に，そのユーザのデータを表示する
 *
 * @author Kensuke Kousaka
 */
public class ViewRegistedData extends Activity {
	String item = null;
	int flgCount;


	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LogUtil.log(Log.INFO);

		setContentView(R.layout.activity_view_registed_data);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			ActionBar actionBar = getActionBar();
			if (actionBar != null) actionBar.setHomeButtonEnabled(true);
		}
		flgCount = 0;

		viewRegistedData();
	}


	/**
	 * ユーザのデータをリスト表示する
	 */
	private void viewRegistedData () {
		LogUtil.log(Log.INFO);

		// RegistrantListから渡されたユーザ名を受け取る
		Intent intent = getIntent();
		item = intent.getStringExtra("item");

		ListView lv = (ListView) findViewById(R.id.listView1);

		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

		ArrayList<String> dataList = readData();

		// アイテム追加
		for (String i : dataList) adapter.add(i);

		// リストビューにアダプタを設定
		lv.setAdapter(adapter);
	}


	/**
	 * データを読み取る
	 *
	 * @return 取得したデータ
	 */
	private ArrayList<String> readData () {
		LogUtil.log(Log.INFO);
		ArrayList<String> dataList = new ArrayList<>();

		ManageData mManageData = new ManageData();
		ArrayList<double[][]> readData = mManageData.readRegistedData(ViewRegistedData.this, item);
		double[][] readDistance = readData.get(0);
		double[][] readAngle = readData.get(1);

		String[][] registedDistance = new String[3][100], registedAngle = new String[3][100];
		for (int i = 0; i < readDistance.length; i++) {
			for (int j = 0; j < readDistance[i].length; j++) {
				registedDistance[i][j] = String.valueOf(readDistance[i][j]);
				registedAngle[i][j] = String.valueOf(readAngle[i][j]);
			}
		}

		Context mContext = ViewRegistedData.this.getApplicationContext();
		SharedPreferences preferences = mContext.getSharedPreferences("MotionAuth", Context.MODE_PRIVATE);

		String ampValue = preferences.getString(item + "amplify", "");

		if ("".equals(ampValue)) throw new RuntimeException();

		String index = "";

		for (int i = 0; i < registedDistance.length; i++) {
			switch (i) {
				case 0:
					index = "DistanceX";
					break;
				case 1:
					index = "DistanceY";
					break;
				case 2:
					index = "DistanceZ";
					break;
			}
			for (int j = 0; j < registedDistance[i].length; j++) {
				dataList.add(index + " : " + registedDistance[i][j] + " : " + ampValue);
			}
		}

		for (int i = 0; i < registedAngle.length; i++) {
			switch (i) {
				case 0:
					index = "AngleX";
					break;
				case 1:
					index = "AngleY";
					break;
				case 2:
					index = "AngleZ";
					break;
			}
			for (int j = 0; j < registedAngle[i].length; j++) {
				dataList.add(index + " : " + registedAngle[i][j] + " : " + ampValue);
			}
		}

		return dataList;
	}


	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				if (flgCount == 9) {
					flgCount = 0;
					moveActivity("com.example.motionauth", "com.example.motionauth.ViewDataList.ViewRegistedRData", true);
				}
				else {
					flgCount++;
				}
				return true;
		}
		return false;
	}


	private void moveActivity (String pkgName, String actName, boolean flg) {
		LogUtil.log(Log.INFO);

		Intent intent = new Intent();
		intent.setClassName(pkgName, actName);

		intent.putExtra("item", item);

		if (flg) intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

		startActivityForResult(intent, 0);
	}
}
