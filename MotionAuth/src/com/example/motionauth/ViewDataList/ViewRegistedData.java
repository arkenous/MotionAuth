package com.example.motionauth.ViewDataList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.example.motionauth.R;


/**
 * RegistrantListより渡されたユーザ名を元に，そのユーザのデータを表示する
 */
public class ViewRegistedData extends Activity {
    String item = null;

    private static final String TAG = "ViewRegistedData";


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // タイトルバーの非表示
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_view_registed_data);

        viewRegistedData();
    }


    /**
     * ユーザのデータをリスト表示する
     */
    private void viewRegistedData () {
        // RegistrantListから渡されたユーザ名を受け取る
        Intent intent = getIntent();
        item = intent.getStringExtra("item");

        ListView lv = (ListView) findViewById(R.id.listView1);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        ArrayList<String> dataList = readData();

        // アイテム追加
        for (int i = 0; i < dataList.size(); i++) {
            adapter.add(dataList.get(i));
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
        ArrayList<String> dataList = new ArrayList<String>();

        Log.d(TAG, "readData");
        String filePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "MotionAuth" + File.separator + "MotionAuth" + File.separator + item;
        File file = new File(filePath);

        try {
            Log.d(TAG, "tryIn");
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
}
