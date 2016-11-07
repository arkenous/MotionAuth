package net.trileg.motionauth.ViewDataList;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import net.trileg.motionauth.R;
import net.trileg.motionauth.Utility.ManageData;

import java.util.ArrayList;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;
import static android.util.Log.INFO;
import static net.trileg.motionauth.Utility.Enum.NUM_AXIS;
import static net.trileg.motionauth.Utility.LogUtil.log;


/**
 * Show specified user's data.
 *
 * @author Kensuke Kosaka
 */
public class ViewRegisteredData extends Activity {
  private String item = null;
  private int flgCount;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    log(INFO);

    setContentView(R.layout.activity_view_registed_data);

    if (SDK_INT >= ICE_CREAM_SANDWICH) {
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
    log(INFO);

    // Receive user name sent from RegistrantList
    Intent intent = getIntent();
    item = intent.getStringExtra("item");

    ListView lv = (ListView) findViewById(R.id.dataList);

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
    log(INFO);
    ArrayList<String> dataList = new ArrayList<>();

    ManageData mManageData = new ManageData();
    double[][] readVector = mManageData.readRegisteredData(ViewRegisteredData.this, item);

    String[][] registeredVector = new String[readVector.length][readVector[0].length];
    for (int axis = 0; axis < NUM_AXIS; axis++) {
      for (int item = 0; item < readVector[axis].length; item++) {
        registeredVector[axis][item] = String.valueOf(readVector[axis][item]);
      }
    }

    Context mContext = ViewRegisteredData.this.getApplicationContext();
    SharedPreferences preferences = mContext.getSharedPreferences("MotionAuth", MODE_PRIVATE);

    String ampValue = preferences.getString(item + "amplify", "");

    if ("".equals(ampValue)) throw new RuntimeException();

    String index = "";

    for (int axis = 0; axis < NUM_AXIS; axis++) {
      switch (axis) {
        case 0:
          index = "VectorX";
          break;
        case 1:
          index = "VectorY";
          break;
        case 2:
          index = "VectorZ";
          break;
      }
      for (int item = 0; item < registeredVector[axis].length; item++) {
        dataList.add(index + " : " + registeredVector[axis][item] + " : " + ampValue);
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
          moveActivity(getPackageName(), getPackageName() + ".Start", true);
        } else {
          flgCount++;
        }
        return true;
    }
    return false;
  }


  private void moveActivity(String pkgName, String actName, boolean flg) {
    log(INFO);

    Intent intent = new Intent();
    intent.setClassName(pkgName, actName);

    if (flg) intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_NEW_TASK);

    startActivity(intent);
    finish();
  }
}
