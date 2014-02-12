package com.example.motionauth.ViewDataList;

import java.io.File;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.example.motionauth.R;


/**
 * 登録されているユーザ名を一覧表示する．
 * ユーザ名が選択されたら，そのユーザのデータをViewRegistedDataアクティビティにて表示する
 */
public class RegistrantList extends Activity
	{
		// ファイル名を格納するためのリスト
		// List<String> fileName = null;
		String[] fileNameStr = null;

		String item;

		private static final String TAG = "RegistrantList";


		@Override
		protected void onCreate(Bundle savedInstanceState)
			{
				super.onCreate(savedInstanceState);

				// タイトルバーの非表示
				requestWindowFeature(Window.FEATURE_NO_TITLE);
				setContentView(R.layout.activity_registrant_list);

				Log.d(TAG, "+++ onCreate +++");

				registrantList();
			}


		/**
		 * 登録されているユーザ名のリストを表示する
		 * ユーザ名が選択されたら，そのユーザ名をViewRegistedDataに送る
		 */
		private void registrantList()
			{
				Log.d(TAG, "registrantList");

				// 登録されているユーザ名のリストを作成する
				fileNameStr = getRegistrantName();

				Log.d(TAG, "c");

				final ListView lv = (ListView) findViewById(R.id.listView1);

				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

				try
					{

						// アイテム追加
						for (int i = 0; i < fileNameStr.length; i++)
							{
								Log.d(TAG, "fileNameStr.length = " + fileNameStr.length);
								adapter.add(fileNameStr[i]);
							}
					}
				catch (NullPointerException e)
					{
						back("com.example.motionauth", "com.example.motionauth.Start", true);
						finish();
					}

				Log.d(TAG, "d");

				// リストビューにアダプタを設定
				lv.setAdapter(adapter);

				// リストビューのアイテムがクリックされた時
				lv.setOnItemClickListener(new OnItemClickListener()
				{
					@Override
					public void onItemClick(AdapterView<?> parent, View v, int position, long id)
						{
							// クリックされたアイテムを取得
							item = lv.getItemAtPosition(position).toString();

							// itemを次のアクティビティに送る
							moveActivity("com.example.motionauth", "com.example.motionauth.ViewDataList.ViewRegistedData", false);
						}

				});
			}


		/**
		 * 指定されたディレクトリ以下のファイルリストを作成する
		 *
		 * @return 作成されたString配列型のリスト
		 */
		private String[] getRegistrantName()
			{
				try
					{

						// 専用ディレクトリを指定
						String dirPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "MotionAuth";
						File dir = new File(dirPath);

						// 指定されたディレクトリのファイル名（ディレクトリ名）を取得
						final File[] files = dir.listFiles();
						final String[] str_items;
						str_items = new String[files.length];
						for (int i = 0; i < files.length; i++)
							{
								File file = files[i];
								str_items[i] = file.getName();
							}

						Log.d(TAG, "b");

						return str_items;

					}
				catch (NullPointerException e)
					{
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
		private void moveActivity(String pkgName, String actName, boolean flg)
			{
				Intent intent = new Intent();

				intent.setClassName(pkgName, actName);

				intent.putExtra("item", item);

				if (flg == true)
					{
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
					}

				startActivityForResult(intent, 0);
			}


		private void back(String pkgName, String actName, boolean flg)
			{
				Intent intent = new Intent();
				intent.setClassName(pkgName, actName);

				if (flg == true)
					{
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
					}

				startActivityForResult(intent, 0);
			}


		@Override
		public boolean onCreateOptionsMenu(Menu menu)
			{
				// Inflate the menu; this adds items to the action bar if it is present.
				getMenuInflater().inflate(R.menu.registrant_list, menu);
				return true;
			}

	}
