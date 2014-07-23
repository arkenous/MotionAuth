package com.example.motionauth.ViewDataList;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.example.motionauth.R;

import java.io.*;
import java.util.ArrayList;


/**
 * @author Kensuke Kousaka
 * //TODO タイトルバー連続クリックで，認証試験モードのR値閲覧画面に移動する機能を追加する．
 */
public class ViewRegistedRData extends Activity {
    private static final String TAG = ViewRegistedRData.class.getSimpleName();

    String item = null;
    int flgCount;


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v(TAG, "--- onCreate ---");

        setContentView(R.layout.activity_view_registed_rdata);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.setHomeButtonEnabled(true);
            }
        }
        flgCount = 0;

        viewRegistedData();
    }


    private void viewRegistedData () {
        Log.v(TAG, "--- viewRegistedData ---");

        Intent intent = getIntent();
        item = intent.getStringExtra("item");

        ListView lv = (ListView) findViewById(R.id.listView1);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        ArrayList<String> dataList = readData();

        for (String i : dataList) {
            adapter.add(i);
        }

        lv.setAdapter(adapter);
    }


    private ArrayList<String> readData () {
        Log.v(TAG, "--- readData ---");
        ArrayList<String> dataList = new ArrayList<String>();

        String directoryPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "MotionAuth" + File.separator + "RegistLRdata" + File.separator + item;

        File directory = new File(directoryPath);
        File[] fileList = directory.listFiles();

        for (int i = 0; i < fileList.length; i++) {
            String filePath = directoryPath + File.separator + fileList[i].getName();

            File file = new File(filePath);

            try {
                FileInputStream fis = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                BufferedReader br = new BufferedReader(isr);
                String s;

                while ((s = br.readLine()) != null) {
                    dataList.add(s);
                }

                br.close();
                isr.close();
                fis.close();
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }
            catch (IOException e) {
                e.printStackTrace();
                return null;
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
                    moveActivity("com.example.motionauth", "com.example.motionauth.ViewDataList.ViewAuthRData", true);
                }
                else {
                    flgCount++;
                }
                return true;
        }
        return false;
    }


    private void moveActivity (String pkgName, String actName, boolean flg) {
        Log.v(TAG, "--- moveActivity ---");

        Intent intent = new Intent();
        intent.setClassName(pkgName, actName);

        intent.putExtra("item", item);

        if (flg) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        startActivityForResult(intent, 0);
    }
}
