package com.example.motionauth.ViewDataList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.example.motionauth.R;

import java.io.*;
import java.util.ArrayList;


/**
 * @author Kensuke Kousaka
 */
public class ViewRegistedRData extends Activity {
    private static final String TAG = ViewRegistedRData.class.getSimpleName();

    String item = null;


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v(TAG, "--- onCreate ---");

        setContentView(R.layout.activity_view_registed_rdata);

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
}
