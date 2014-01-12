package com.example.motionauth.Authentication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.motionauth.Authentication.AuthNameInput;
import com.example.motionauth.Formatter;
import com.example.motionauth.R;

import java.io.*;

public class AuthMotion extends Activity implements SensorEventListener
	{
		private SensorManager mSensorManager;
		private Sensor mAccelerometerSensor;
		private Sensor mGyroscopeSensor;

		private static final int TIMEOUT_MESSAGE = 1;
		private static final int INTERVAL = 30;

		TextView secondTv;
		TextView countSecondTv;
		Button getMotionBtn;

		private boolean btnStatus = false;

		private float vAccelo[];
		private float vGyro[];

		private int accelCount = 0;
		private int gyroCount = 0;
		private int getCount = 0;

		private float accelo_tmp[][] = new float[3][100];

		private float gyro_tmp[][] = new float[3][100];

		private double accelo[][] = new double[3][100];

		private double gyro[][] = new double[3][100];

		// 移動平均ローパス用変数
		private double outputData = 0.0;
		private int count = 0;

		// 移動量平均後のデータを格納する配列
		private double moveAverageDistance[][] = new double[3][100];
		private double moveAverageAngle[][] = new double[3][100];


		@Override
		protected void onCreate(Bundle savedInstanceState)
			{
				super.onCreate(savedInstanceState);
				setContentView(R.layout.activity_auth_motion);

				authMotion();
			}


		private void authMotion()
			{
				mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
				mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
				mGyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

				TextView nameTv = (TextView) findViewById(R.id.textView1);
				secondTv = (TextView) findViewById(R.id.secondTextView);
				countSecondTv = (TextView) findViewById(R.id.textView4);
				getMotionBtn = (Button) findViewById(R.id.button1);

				nameTv.setText(AuthNameInput.name + "さん読んでね！");

				getMotionBtn.setOnClickListener(new OnClickListener()
				{
					public void onClick(View v)
						{
							if (btnStatus == false)
								{
									btnStatus = true;
									getMotionBtn.setText("取得中");
									countSecondTv.setText("秒");
									timeHandler.sendEmptyMessage(TIMEOUT_MESSAGE);
								}
							else
								{

								}
						}
				});
			}


		@Override
		public void onSensorChanged(SensorEvent event)
			{
				if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
					{
						vAccelo = event.values.clone();
					}
				if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE)
					{
						vGyro = event.values.clone();
					}
			}

		Handler timeHandler = new Handler()
		{
			@Override
			public void dispatchMessage(Message msg)
				{
					if (msg.what == TIMEOUT_MESSAGE && btnStatus == true)
						{
							if (accelCount < 100 && gyroCount < 100 && getCount >= 0 && getCount < 3)
								{
									// 取得した値を，0.03秒ごとに配列に入れる
									for (int i = 0; i < 3; i++)
										{
											accelo_tmp[i][accelCount] = vAccelo[i];
										}

									for (int i = 0; i < 3; i++)
										{
											gyro_tmp[i][gyroCount] = vGyro[i];
										}

									accelCount++;
									gyroCount++;

									if (accelCount == 1)
										{
											secondTv.setText("3");
										}
									if (accelCount == 33)
										{
											secondTv.setText("2");
										}
									if (accelCount == 66)
										{
											secondTv.setText("1");
										}

									timeHandler.sendEmptyMessageDelayed(TIMEOUT_MESSAGE, INTERVAL);
								}
							else if (accelCount >= 100 && gyroCount >= 100)
								{
									// 取得完了
									btnStatus = false;
									getMotionBtn.setText("モーションデータ取得完了");

									calc();

									soukan();
								}
						}
					else
						{
							super.dispatchMessage(msg);
						}
				}
		};


		/**
		 * データ加工・計算処理を行う
		 */
		//TODO データのズレを，登録された平均値データと比較して修正する
		private void calc()
			{
				for (int i = 0; i < 3; i++)
					{
						// 原データの桁揃え
						for (int j = 0; j < 100; j++)
							{
								accelo[i][j] = Formatter.floatToDoubleFormatter(accelo_tmp[i][j]);
								gyro[i][j] = Formatter.floatToDoubleFormatter(gyro_tmp[i][j]);
							}

						// 移動平均ローパス
						for (int j = 0; j < 100; j++)
							{
								double tmp = lowpass(accelo[i][j]);
								tmp = (tmp * 0.03 * 0.03) / 2 * 1000;
								moveAverageDistance[i][j] = Formatter.doubleToDoubleFormatter(tmp);
							}
						for (int j = 0; j < 100; j++)
							{
								double tmp = lowpass(gyro[i][j]);
								tmp = (tmp * 0.03 * 0.03) / 2 * 1000;
								moveAverageAngle[i][j] = Formatter.doubleToDoubleFormatter(tmp);
							}
					}


			}


		/**
		 * 移動量平均ローパスフィルタ
		 *
		 * @param data ローパスをかけるdouble型のデータ
		 * @return ローパスをかけ終わったdouble型のデータ
		 */
		private double lowpass(double data)
			{
				if (count == 100)
					{
						outputData = 0.0;
						count = 0;
					}

				outputData = outputData * 0.9 + data * 0.1;

				count++;
				return outputData;
			}


		private void soukan()
			{
				// 相関係数の計算

				// Calculate of average A
				// ユーザが入力したデータ
				float[] sample_accel = new float[3];

				float[] sample_gyro = new float[3];

				for (int i = 0; i < 3; i++)
					{
						for (int j = 0; j < 100; j++)
							{
								sample_accel[i] += moveAverageDistance[i][j];

								sample_gyro[i] += moveAverageAngle[i][j];
							}
					}

				for (int i = 0; i < 3; i++)
					{
						sample_accel[i] /= 99;

						sample_gyro[i] /= 99;
					}

				// Calculate of average B
				float ave_accel[] = new float[3];

				float ave_gyro[] = new float[3];

				float registed_ave_distance[][] = new float[3][100];

				float registed_ave_angle[][] = new float[3][100];

				int readCount = 0;

				try
					{
						String filePath = Environment.getExternalStorageDirectory() + File.separator + "MotionAuth" + File.separator + AuthNameInput.name;
						File file = new File(filePath);
						FileInputStream fis = new FileInputStream(file);
						InputStreamReader isr = new InputStreamReader(fis);
						BufferedReader br = new BufferedReader(isr);

						String beforeSplitData;
						String[] afterSplitData = new String[2];

						while ((beforeSplitData = br.readLine()) != null)
							{
								afterSplitData = beforeSplitData.split("@");

								if (afterSplitData[0].equals("ave_distance_x"))
									{
										registed_ave_distance[0][readCount] = Float.valueOf(afterSplitData[1]);
										if (readCount == 99)
											{
												readCount = 0;
											}
										else
											{
												readCount++;
											}
									}
								if (afterSplitData[0].equals("ave_distance_y"))
									{
										registed_ave_distance[1][readCount] = Float.valueOf(afterSplitData[1]);
										if (readCount == 99)
											{
												readCount = 0;
											}
										else
											{
												readCount++;
											}
									}
								if (afterSplitData[0].equals("ave_distance_z"))
									{
										registed_ave_distance[2][readCount] = Float.valueOf(afterSplitData[1]);
										if (readCount == 99)
											{
												readCount = 0;
											}
										else
											{
												readCount++;
											}
									}
								if (afterSplitData[0].equals("ave_angle_x"))
									{
										registed_ave_angle[0][readCount] = Float.valueOf(afterSplitData[1]);
										if (readCount == 99)
											{
												readCount = 0;
											}
										else
											{
												readCount++;
											}
									}
								if (afterSplitData[0].equals("ave_angle_y"))
									{
										registed_ave_angle[1][readCount] = Float.valueOf(afterSplitData[1]);
										if (readCount == 99)
											{
												readCount = 0;
											}
										else
											{
												readCount++;
											}
									}
								if (afterSplitData[0].equals("ave_angle_z"))
									{
										registed_ave_angle[2][readCount] = Float.valueOf(afterSplitData[1]);
										if (readCount == 99)
											{
												readCount = 0;
											}
										else
											{
												readCount++;
											}
									}
							}

						br.close();
						isr.close();
						fis.close();
					}
				catch (IOException e)
					{

					}

				for (int i = 0; i < 3; i++)
					{
						for (int j = 0; j < 100; j++)
							{
								ave_accel[i] += registed_ave_distance[i][j];
								ave_gyro[i] += registed_ave_angle[i][j];
							}
					}

				for (int i = 0; i < 3; i++)
					{
						ave_accel[i] /= 99;
						ave_gyro[i] /= 99;
					}

				// Calculate of Sxx
				float Sxx_accel[] = new float[3];

				float Sxx_gyro[] = new float[3];

				for (int i = 0; i < 3; i++)
					{
						for (int j = 0; j < 100; j++)
							{
								Sxx_accel[i] += Math.pow((moveAverageDistance[i][j] - sample_accel[i]), 2);

								Sxx_gyro[i] += Math.pow((moveAverageAngle[i][j] - sample_gyro[i]), 2);
							}
					}

				// Calculate of Syy
				float Syy_accel[] = new float[3];

				float Syy_gyro[] = new float[3];

				for (int i = 0; i < 3; i++)
					{
						for (int j = 0; j < 100; j++)
							{
								Syy_accel[i] += Math.pow((registed_ave_distance[i][j] - ave_accel[i]), 2);

								Syy_gyro[i] += Math.pow((registed_ave_angle[i][j] - ave_gyro[i]), 2);
							}
					}

				// Calculate of Sxy
				float Sxy_accel[] = new float[3];

				float Sxy_gyro[] = new float[3];

				for (int i = 0; i < 3; i++)
					{
						for (int j = 0; j < 100; j++)
							{
								Sxy_accel[i] += (moveAverageDistance[i][j] - sample_accel[i]) * (registed_ave_distance[i][j] - ave_accel[i]);
								Sxy_gyro[i] += (moveAverageAngle[i][j] - sample_gyro[i]) * (registed_ave_angle[i][j] - ave_gyro[i]);
							}
					}

				// Calculate of R
				double R_accel[] = new double[3];

				double R_gyro[] = new double[3];

				for (int i = 0; i < 3; i++)
					{
						R_accel[i] = Sxy_accel[i] / Math.sqrt(Sxx_accel[i] * Syy_accel[i]);

						R_gyro[i] = Sxy_gyro[i] / Math.sqrt(Sxx_gyro[i] * Syy_gyro[i]);
					}

				// 相関係数が一定以上あるなら認証成功
				if (R_accel[0] > 0.5)
					{
						if (R_accel[1] > 0.5)
							{
								if (R_accel[2] > 0.5)
									{
										if (R_gyro[0] > 0.5)
											{
												if (R_gyro[1] > 0.5)
													{
														if (R_gyro[2] > 0.5)
															{
																Toast.makeText(this, "認証成功", Toast.LENGTH_LONG).show();
																Toast.makeText(this, "距離X軸: " + R_accel[0], Toast.LENGTH_SHORT).show();
																Toast.makeText(this, "距離Y軸: " + R_accel[1], Toast.LENGTH_SHORT).show();
																Toast.makeText(this, "距離Z軸: " + R_accel[2], Toast.LENGTH_SHORT).show();
																Toast.makeText(this, "角度X軸: " + R_gyro[0], Toast.LENGTH_SHORT).show();
																Toast.makeText(this, "角度Y軸: " + R_gyro[1], Toast.LENGTH_SHORT).show();
																Toast.makeText(this, "角度Z軸: " + R_gyro[2], Toast.LENGTH_SHORT).show();

																moveActivity("com.example.motionauth", "com.example.motionauth.Start", true);
															}
														else
															{
																Toast.makeText(this, "認証失敗", Toast.LENGTH_LONG).show();
																Toast.makeText(this, "角度Z軸: " + R_gyro[2], Toast.LENGTH_SHORT).show();
															}
													}
												else
													{
														Toast.makeText(this, "認証失敗", Toast.LENGTH_LONG).show();
														Toast.makeText(this, "角度Y軸: " + R_gyro[1], Toast.LENGTH_SHORT).show();
													}
											}
										else
											{
												Toast.makeText(this, "認証失敗", Toast.LENGTH_LONG).show();
												Toast.makeText(this, "角度X軸: " + R_gyro[0], Toast.LENGTH_SHORT).show();
											}
									}
								else
									{
										Toast.makeText(this, "認証失敗", Toast.LENGTH_LONG).show();
										Toast.makeText(this, "距離Z軸: " + R_accel[2], Toast.LENGTH_SHORT).show();
									}
							}
						else
							{
								Toast.makeText(this, "認証失敗", Toast.LENGTH_LONG).show();
								Toast.makeText(this, "距離Y軸: " + R_accel[1], Toast.LENGTH_SHORT).show();
							}
					}
				else
					{
						Toast.makeText(this, "認証失敗", Toast.LENGTH_LONG).show();
						Toast.makeText(this, "距離X軸: " + R_accel[0], Toast.LENGTH_SHORT).show();
					}
			}


		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy)
			{

			}


		@Override
		protected void onResume()
			{
				super.onResume();

				mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
				mSensorManager.registerListener(this, mGyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
			}


		@Override
		protected void onPause()
			{
				super.onPause();

				mSensorManager.unregisterListener(this);
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
				getMenuInflater().inflate(R.menu.auth_motion, menu);
				return true;
			}

	}
