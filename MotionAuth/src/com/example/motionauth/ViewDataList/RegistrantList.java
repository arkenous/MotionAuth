package com.example.motionauth.ViewDataList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.example.motionauth.R;

import java.io.File;


/**
 * 登録されているユーザ名を一覧表示する．
 * ユーザ名が選択されたら，そのユーザのデータをViewRegistedDataアクティビティにて表示する
 *
 * @author Kensuke Kousaka
 */
public class RegistrantList extends Activity {
    private static final String TAG = RegistrantList.class.getSimpleName();

    // ファイル名を格納するためのリスト
    String[] fileNameStr = null;

    String item;


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v(TAG, "--- onCreate ---");

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
        Log.v(TAG, "--- registrantList ---");

        // 登録されているユーザ名のリストを作成する
        fileNameStr = getRegistrantName();

        final ListView lv = (ListView) findViewById(R.id.listView1);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        try {

            // アイテム追加
            for (String s : fileNameStr) {
                adapter.add(s);
            }
        }
        catch (NullPointerException e) {
            AlertDialog.Builder alert = new AlertDialog.Builder(RegistrantList.this);
            alert.setTitle("エラー");
            alert.setMessage("登録されていないユーザです．\nスタート画面に戻ります．");
            alert.setCancelable(false);
            alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick (DialogInterface dialog, int which) {
                    moveActivity("com.example.motionauth", "com.example.motionauth.Start", true);
                }
            });
            alert.show();
            finish();
        }

        // リストビューにアダプタを設定
        lv.setAdapter(adapter);

        // リストビューのアイテムがクリックされた時
        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick (AdapterView<?> parent, View v, int position, long id) {
                Log.i(TAG, "Click Item");

                // クリックされたアイテムを取得
                item = lv.getItemAtPosition(position).toString();

                // itemを次のアクティビティに送る
                moveActivity("com.example.motionauth", "com.example.motionauth.ViewDataList.ViewRegistedData", true);
            }

        });
    }


    /**
     * 指定されたディレクトリ以下のファイルリストを作成する
     *
     * @return 作成されたString配列型のリスト
     */
    private String[] getRegistrantName () {
        Log.v(TAG, "--- getRegistrantName ---");

        try {
            // 専用ディレクトリを指定
            String dirPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "MotionAuth" + File.separator + "MotionAuth";
            File dir = new File(dirPath);

            // 指定されたディレクトリのファイル名（ディレクトリ名）を取得
            final File[] files = dir.listFiles();
            final String[] str_items;
            str_items = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                str_items[i] = file.getName();
            }

            return str_items;

        }
        catch (NullPointerException e) {
            return null;
        }
    }


    /**
     * アクティビティを移動する
     *
     * @param pkgName 移動先のパッケージ名
     * @param actName 移動先のアクティビティ名
     * @param flg     戻るキーを押した際にこのアクティビティを表示させるかどうか
     */
    private void moveActivity (String pkgName, String actName, boolean flg) {
        Log.v(TAG, "--- moveActivity ---");

        Intent intent = new Intent();

        intent.setClassName(pkgName, actName);

        if (actName.equals("com.example.motionauth.ViewDataList.ViewRegistedData")) {
            intent.putExtra("item", item);
        }

        if (flg) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        startActivityForResult(intent, 0);
    }
}
