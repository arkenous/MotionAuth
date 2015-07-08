package net.trileg.motionauth.ViewDataList;

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
import net.trileg.motionauth.R;
import net.trileg.motionauth.Utility.LogUtil;
import net.trileg.motionauth.Utility.ManageData;

import java.util.ArrayList;


/**
 * Show specified user's data.
 *
 * @author Kensuke Kosaka
 */
public class ViewRegisteredData extends Activity {
	String item = null;
	int flgCount;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LogUtil.log(Log.INFO);

		setContentView(R.layout.activity_view_registed_data);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			ActionBar actionBar = getActionBar();
			if (actionBar != null) actionBar.setHomeButtonEnabled(true);
		}
		flgCount = 0;

		viewRegisteredData();
	}


	/**
	 * Show user data.
	 */
	private void viewRegisteredData() {
		LogUtil.log(Log.INFO);

		// Receive user name sent from RegistrantList
		Intent intent = getIntent();
		item = intent.getStringExtra("item");

		ListView lv = (ListView) findViewById(R.id.listView1);

		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

		ArrayList<String> dataList = readData();

		for (String i : dataList) adapter.add(i);

		lv.setAdapter(adapter);
	}


	/**
	 * Read data.
	 *
	 * @return String type list.
	 */
	private ArrayList<String> readData() {
		LogUtil.log(Log.INFO);
		ArrayList<String> dataList = new ArrayList<>();

		ManageData mManageData = new ManageData();
		ArrayList<double[][]> readData = mManageData.readRegisteredData(ViewRegisteredData.this, item);
		double[][] readDistance = readData.get(0);
		double[][] readAngle = readData.get(1);

		String[][] registeredDistance = new String[3][100], registeredAngle = new String[3][100];
		for (int i = 0; i < readDistance.length; i++) {
			for (int j = 0; j < readDistance[i].length; j++) {
				registeredDistance[i][j] = String.valueOf(readDistance[i][j]);
				registeredAngle[i][j] = String.valueOf(readAngle[i][j]);
			}
		}

		Context mContext = ViewRegisteredData.this.getApplicationContext();
		SharedPreferences preferences = mContext.getSharedPreferences("MotionAuth", Context.MODE_PRIVATE);

		String ampValue = preferences.getString(item + "amplify", "");

		if ("".equals(ampValue)) throw new RuntimeException();

		String index = "";

		for (int i = 0; i < registeredDistance.length; i++) {
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
			for (int j = 0; j < registeredDistance[i].length; j++) {
				dataList.add(index + " : " + registeredDistance[i][j] + " : " + ampValue);
			}
		}

		for (int i = 0; i < registeredAngle.length; i++) {
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
			for (int j = 0; j < registeredAngle[i].length; j++) {
				dataList.add(index + " : " + registeredAngle[i][j] + " : " + ampValue);
			}
		}

		return dataList;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				if (flgCount == 9) {
					flgCount = 0;
					moveActivity(getPackageName(), getPackageName() + ".ViewDataList.ViewRegisteredRData", true);
				} else {
					flgCount++;
				}
				return true;
		}
		return false;
	}


	private void moveActivity(String pkgName, String actName, boolean flg) {
		LogUtil.log(Log.INFO);

		Intent intent = new Intent();
		intent.setClassName(pkgName, actName);

		intent.putExtra("item", item);

		if (flg) intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

		startActivity(intent);
	}
}
