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
import com.example.motionauth.Processing.CipherCrypt;
import com.example.motionauth.R;

import java.io.*;
import java.util.ArrayList;


/**
 * RegistrantListより渡されたユーザ名を元に，そのユーザのデータを表示する
 *
 * @author Kensuke Kousaka
 */
public class ViewRegistedData extends Activity {
    private static final String TAG = ViewRegistedData.class.getSimpleName();

    String item = null;
    int flgCount;


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v(TAG, "--- onCreate ---");

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
    //TODO 復号処理を行い，データを読み込む
    private ArrayList<String> readData () {
        Log.v(TAG, "--- readData ---");
        ArrayList<String> dataList = new ArrayList<String>();
        CipherCrypt mCipherCrypt = new CipherCrypt(ViewRegistedData.this);

        String filePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "MotionAuth" + File.separator + "MotionAuth" + File.separator + item;
        File file = new File(filePath);

        try {
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String s;

            ArrayList<String> amplify = new ArrayList<String>();
            ArrayList<String> index = new ArrayList<String>();
            ArrayList<String> encryptedDataList = new ArrayList<String>();

            while ((s = br.readLine()) != null) {
                // 読みだした各行のうち，データ部分のみを抜き出す
                String[] splitAmplify = s.split(":");
                amplify.add(splitAmplify[1]);

                String[] splitIndex = splitAmplify[0].split("@");
                index.add(splitIndex[0]);

                String encryptedData = splitIndex[1];
                encryptedDataList.add(encryptedData);

//                dataList.add(s);
            }

            br.close();
            isr.close();
            fis.close();

            String[] encryptedDataArray = (String[]) encryptedDataList.toArray(new String[0]);
            String[] decryptedDataArray = mCipherCrypt.decrypt(encryptedDataArray);

            for (int i = 0; i < decryptedDataArray.length; i++) {
                dataList.add(index.get(i) + "@" + decryptedDataArray[i] + ":" + amplify.get(i));
            }

            return dataList;
        }
        catch (FileNotFoundException e) {
            return dataList;
        }
        catch (UnsupportedEncodingException e) {
            return dataList;
        }
        catch (IOException e) {
            return dataList;
        }
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
