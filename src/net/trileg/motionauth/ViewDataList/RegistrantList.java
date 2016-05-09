package net.trileg.motionauth.ViewDataList;

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
import net.trileg.motionauth.R;
import net.trileg.motionauth.Utility.LogUtil;

import java.util.ArrayList;
import java.util.Map;


/**
 * Show registered user name.
 *
 * @author Kensuke Kosaka
 */
public class RegistrantList extends Activity {
  private String item;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    LogUtil.log(Log.INFO);

    // Disable title bar.
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.activity_registrant_list);

    registrantList();
  }


  /**
   * Show registered user name.
   * Send user name to ViewRegisteredData when user name is selected.
   */
  private void registrantList() {
    LogUtil.log(Log.INFO);

    // Create registered user name list.
    ArrayList<String> userList = getRegistrantName();

    final ListView lv = (ListView) findViewById(R.id.listView1);

    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

    try {
      for (String s : userList) adapter.add(s);
    } catch (NullPointerException e) {
      AlertDialog.Builder alert = new AlertDialog.Builder(RegistrantList.this);
      alert.setTitle("エラー");
      alert.setMessage("登録されていないユーザです．\nスタート画面に戻ります．");
      alert.setCancelable(false);
      alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          RegistrantList.this.moveActivity(getPackageName(), getPackageName() + ".Start", true);
        }
      });
      alert.show();
      finish();
    }

    lv.setAdapter(adapter);

    // When list item is selected.
    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        LogUtil.log(Log.VERBOSE, "Click item");

        // Get item value which selected.
        item = lv.getItemAtPosition(position).toString();

        RegistrantList.this.moveActivity(getPackageName(), getPackageName() + ".ViewDataList.ViewRegisteredData", true);
      }
    });
  }


  /**
   * Create file list under specified directory.
   *
   * @return Created String type list.
   */
  private ArrayList<String> getRegistrantName() {
    LogUtil.log(Log.INFO);

    Context mContext = RegistrantList.this.getApplicationContext();
    SharedPreferences preferences = mContext.getSharedPreferences("UserList", Context.MODE_PRIVATE);

    ArrayList<String> keyList = new ArrayList<>();

    Map<String, ?> allEntries = preferences.getAll();
    for (Map.Entry<String, ?> entry : allEntries.entrySet()) keyList.add(entry.getKey());

    return keyList;
  }


  /**
   * Move activity.
   *
   * @param pkgName Destination package name.
   * @param actName Destination activity name.
   * @param flg     Whether to show this activity if user push BACK KEY.
   */
  private void moveActivity(String pkgName, String actName, boolean flg) {
    LogUtil.log(Log.INFO);

    Intent intent = new Intent();
    intent.setClassName(pkgName, actName);
    if (actName.equals(getPackageName() + ".ViewDataList.ViewRegisteredData")) intent.putExtra("item", item);
    if (flg) intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

    startActivity(intent);
  }
}
