package com.example.motionauth.ViewDataList;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.example.motionauth.R;

import java.io.*;
import java.util.ArrayList;


/**
 * RegistrantListより渡されたユーザ名を元に，そのユーザのデータを表示する
 */
public class ViewRegistedData extends Activity {
    private static final String TAG = ViewRegistedData.class.getSimpleName();

    String item = null;
    int flgCount;


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v(TAG, "--- onCreate ---");

        // タイトルバーの非表示
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_view_registed_data);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.setHomeButtonEnabled(true);
            }
        }
        flgCount = 0;

        viewRegistedData();
    }


    /**
     * ユーザのデータをリスト表示する
     */
    private void viewRegistedData () {
        Log.v(TAG, "--- viewRegistedData ---");

        // RegistrantListから渡されたユーザ名を受け取る
        Intent intent = getIntent();
        item = intent.getStringExtra("item");

        ListView lv = (ListView) findViewById(R.id.listView1);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        ArrayList<String> dataList = readData();

        // アイテム追加
        for (String i : dataList) {
            adapter.add(i);
        }

        // リストビューにアダプタを設定
        lv.setAdapter(adapter);
    }


    /**
     * データを読み取る
     *
     * @return 取得したデータ
     */
    private ArrayList<String> readData () {
        Log.v(TAG, "--- readData ---");
        ArrayList<String> dataList = new ArrayList<String>();

        String filePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "MotionAuth" + File.separator + "MotionAuth" + File.separator + item;
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

            return dataList;
        }
        catch (FileNotFoundException e) {
            return null;
        }
        catch (UnsupportedEncodingException e) {
            return null;
        }
        catch (IOException e) {
            return null;
        }
    }


    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_registed_data, menu);
        return true;
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
