package com.example.motionauth.Registration;

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
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.motionauth.*;
import com.example.motionauth.Utility.Enum;

import java.io.*;


/**
 * モーションを新規登録する
 * @author Kensuke Kousaka
 */
public class RegistMotion extends Activity implements SensorEventListener
	{
		private static final String TAG = RegistMotion.class.getSimpleName();

		private SensorManager mSensorManager;
		private Sensor mAccelerometerSensor;
		private Sensor mGyroscopeSensor;

		// モーションの生データ
		private float vAccelo[];
		private float vGyro[];

		private boolean btnStatus = false;

		private static final int TIMEOUT_MESSAGE = 1;

		// データを取得する間隔
		private static final int INTERVAL = 30;

		// 相関の閾値
		private static final double LOOSE = 0.4;
		private static final double STRICT = 0.6;

		// データ取得カウント用
		private int accelCount = 0;
		private int gyroCount = 0;
		private int getCount = 0;

		private float accelo_tmp[][][] = new float[3][3][100];
		private float gyro_tmp[][][] = new float[3][3][100];

		private double accelo[][][] = new double[3][3][100];
		private double gyro[][][] = new double[3][3][100];


		// 移動平均の際に用いる
		private double outputData = 0.0;
		private int count = 0;

		// 移動平均後のデータを格納する配列
		private double moveAverageDistance[][][] = new double[3][3][100];
		private double moveAverageAngle[][][] = new double[3][3][100];

		private double aveMoveAverageDistance[][] = new double[3][100];
		private double aveMoveAverageAngle[][] = new double[3][100];

		TextView secondTv;
		TextView countSecondTv;
		Button getMotionBtn;


		private WriteData mWriteData = new WriteData();
		private Correlation mCorrelation = new Correlation();


		@Override
		protected void onCreate(Bundle savedInstanceState)
			{
				super.onCreate(savedInstanceState);

				// タイトルバーの非表示
				requestWindowFeature(Window.FEATURE_NO_TITLE);
				setContentView(R.layout.activity_regist_motion);

				registMotion();
			}


		/**
		 * モーション登録画面にイベントリスナ等を設定する
		 */
		private void registMotion()
			{
				// センササービス，各種センサを取得する
				mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
				mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
				mGyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

				TextView nameTv = (TextView) findViewById(R.id.textView2);
				secondTv = (TextView) findViewById(R.id.secondTextView);
				countSecondTv = (TextView) findViewById(R.id.textView4);
				getMotionBtn = (Button) findViewById(R.id.button1);

				nameTv.setText(RegistNameInput.name + "さん読んでね！");

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
					Log.d(TAG, "dispatchMessageIn");

					if (msg.what == TIMEOUT_MESSAGE && btnStatus == true)
						{
							Log.d(TAG, "ifIn");
							if (accelCount < 100 && gyroCount < 100 && getCount >= 0 && getCount < 3)
								{
									// 取得した値を，0.03秒ごとに配列に入れる
									for (int i = 0; i < 3; i++)
										{
											accelo_tmp[getCount][i][accelCount] = vAccelo[i];
										}

									for (int i = 0; i < 3; i++)
										{
											gyro_tmp[getCount][i][gyroCount] = vGyro[i];
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

									// INTERVALで指定したミリ秒後に再度timeHandler（これ自身）を呼び出す
									timeHandler.sendEmptyMessageDelayed(TIMEOUT_MESSAGE, INTERVAL);
								}
							else if (accelCount >= 100 && gyroCount >= 100 && getCount >= 0 && getCount < 4)
								{
									// 取得完了
									btnStatus = false;
									getCount++;
									countSecondTv.setText("回");
									getMotionBtn.setText("モーションデータ取得");

									accelCount = 0;
									gyroCount = 0;

									// TODO 画面に番号を表示するのではなく，音声で出力させる
									if (getCount == 1)
										{
											secondTv.setText("2");
										}
									if (getCount == 2)
										{
											secondTv.setText("1");
										}

									if (getCount == 3)
										{
											// 全データ取得完了（3回分の加速度，ジャイロを取得完了）
											secondTv.setText("0");

											// 生データをアウトプット
											mWriteData.writeFloatThreeArrayData("RegistRawData", "rawAccelo", RegistNameInput.name, accelo_tmp, RegistMotion.this);
											mWriteData.writeFloatThreeArrayData("RegistRawData", "rawGyro", RegistNameInput.name, gyro_tmp, RegistMotion.this);

											calc();

											// TODO Correlationに渡して処理し，返り値を利用する
											soukan();
										}
								}
							else
								{
									Log.d(TAG, "elseIn");
									super.dispatchMessage(msg);
								}
						}
				}
		};


		/**
		 * データ加工，計算処理を行う
		 */
		private void calc()
			{
				Log.d(TAG, "calc");

				// データ加工，計算処理
				// データの桁揃え

				for (int i = 0; i < 3; i++)
					{
						for (int j = 0; j < 3; j++)
							{
								// 原データの桁揃え
								// Formatterに配列ごと渡して処理する（ここでfor文を使わないようにする）
								for (int k = 0; k < 100; k++)
									{
										// データのフォーマット
										accelo[i][j][k] = Formatter.floatToDoubleFormatter(accelo_tmp[i][j][k]);
										gyro[i][j][k] = Formatter.floatToDoubleFormatter(gyro_tmp[i][j][k]);
									}

								// 移動平均ローパス
								// TODO 別クラスに分離できるか検討
								for (int k = 0; k < 100; k++)
									{
										double tmp = lowpass(accelo[i][j][k]);
										tmp = (tmp * 0.03 * 0.03) / 2 * 1000;
										moveAverageDistance[i][j][k] = Formatter.doubleToDoubleFormatter(tmp);
									}
								for (int k = 0; k < 100; k++)
									{
										double tmp = lowpass(gyro[i][j][k]);
										tmp = (tmp * 0.03 * 0.03) / 2 * 1000;
										moveAverageAngle[i][j][k] = Formatter.doubleToDoubleFormatter(tmp);
									}
							}
					}

				mWriteData.writeDoubleThreeArrayData("FormatRawData", "rawAccelo", RegistNameInput.name, accelo, RegistMotion.this);
				mWriteData.writeDoubleThreeArrayData("FormatRawData", "rawGyro", RegistNameInput.name, gyro, RegistMotion.this);

				// measureCorrelation用の平均値データを作成
				for (int i = 0; i < 3; i++)
					{
						for (int j = 0; j < 100; j++)
							{
								aveMoveAverageDistance[i][j] = (moveAverageDistance[0][i][j] + moveAverageDistance[1][i][j] + moveAverageDistance[2][i][j]) / 3;
								aveMoveAverageAngle[i][j] = (moveAverageAngle[0][i][j] + moveAverageAngle[1][i][j] + moveAverageAngle[2][i][j]) / 3;
							}
					}


				//region 同一のモーションであるかの確認をし，必要に応じてズレ修正を行う
				Enum.MEASURE measure = mCorrelation.measureCorrelation(this, moveAverageDistance, moveAverageAngle, aveMoveAverageDistance, aveMoveAverageAngle, LOOSE);

				if (Enum.MEASURE.INCORRECT == measure)
					{
						// 相関係数が0.4以下
						Toast.makeText(RegistMotion.this, "同一モーションですか？", Toast.LENGTH_SHORT).show();
						return;
					}
				else if (Enum.MEASURE.CORRECT == measure)
					{
						// 相関係数が0.4よりも高く，0.8以下の場合
						// ズレ修正を行う
						moveAverageDistance = CorrectDeviation.correctDeviation(moveAverageDistance);
						moveAverageAngle = CorrectDeviation.correctDeviation(moveAverageAngle);
					}
				else if (Enum.MEASURE.PERFECT == measure)
					{
						// 相関係数が0.8よりも高い場合
						// ズレ修正を行わず，スキップする
					}
				else
					{
						// なにかがおかしい
						Toast.makeText(RegistMotion.this, "Error", Toast.LENGTH_LONG).show();
					}
				//endregion


				// ズレ修正後の平均値データを出す
				for (int i = 0; i < 3; i++)
					{
						for (int j = 0; j < 100; j++)
							{
								aveMoveAverageDistance[i][j] = (moveAverageDistance[0][i][j] + moveAverageDistance[1][i][j] + moveAverageDistance[2][i][j]) / 3;

								aveMoveAverageAngle[i][j] = (moveAverageAngle[0][i][j] + moveAverageAngle[1][i][j] + moveAverageAngle[2][i][j]) / 3;
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
				Log.d(TAG, "lowpass");

				if (count == 100)
					{
						outputData = 0.0;
						count = 0;
					}

				outputData = outputData * 0.9 + data * 0.1;

				count++;
				return outputData;
			}


		/**
		 * 相関係数を導出し，ユーザが入力した3回のモーションの類似性を確認する
		 */
		private void soukan()
			{
				Log.d(TAG, "soukan");
				// 相関係数の計算

				//region Calculate of Average A
				float[][] sample_accel = new float[3][3];

				float[][] sample_gyro = new float[3][3];

				// iは1回目，2回目，3回目
				for (int i = 0; i < 3; i++)
					{
						for (int k = 0; k < 3; k++)
							{
								for (int j = 0; j < 100; j++)
									{
										sample_accel[i][k] += moveAverageDistance[i][k][j];
										sample_gyro[i][k] += moveAverageAngle[i][k][j];
									}
							}
					}

				for (int i = 0; i < 3; i++)
					{
						for (int j = 0; j < 3; j++)
							{
								sample_accel[i][j] /= 99;
								sample_gyro[i][j] /= 99;
							}
					}
				//endregion



				//region Calculate of Average B
				float ave_accel[] = new float[3];
				float ave_gyro[] = new float[3];

				for (int j = 0; j < 3; j++)
					{
						for (int i = 0; i < 100; i++)
							{
								ave_accel[j] += aveMoveAverageDistance[j][i];
								ave_gyro[j] += aveMoveAverageAngle[j][i];
							}
					}

				for (int i = 0; i < 3; i++)
					{
						ave_accel[i] /= 99;
						ave_gyro[i] /= 99;
					}
				//endregion



				//region Calculate of Sxx
				float Sxx_accel[][] = new float[3][3];
				float Sxx_gyro[][] = new float[3][3];

				for (int i = 0; i < 3; i++)
					{
						for (int k = 0; k < 3; k++)
							{
								for (int j = 0; j < 100; j++)
									{
										Sxx_accel[i][k] += Math.pow((moveAverageDistance[i][k][j] - sample_accel[i][k]), 2);
										Sxx_gyro[i][k] += Math.pow((moveAverageAngle[i][k][j] - sample_gyro[i][k]), 2);
									}
							}
					}
				//endregion



				//region Calculate of Syy
				float Syy_accel[] = new float[3];

				float Syy_gyro[] = new float[3];

				for (int j = 0; j < 3; j++)
					{
						for (int i = 0; i < 100; i++)
							{
								Syy_accel[j] += Math.pow((aveMoveAverageDistance[j][i] - ave_accel[j]), 2);
								Syy_gyro[j] += Math.pow((aveMoveAverageAngle[j][i] - ave_gyro[j]), 2);
							}
					}
				//endregion



				//region Calculate of Sxy
				float[][] Sxy_accel = new float[3][3];
				float[][] Sxy_gyro = new float[3][3];

				for (int i = 0; i < 3; i++)
					{
						for (int k = 0; k < 3; k++)
							{
								for (int j = 0; j < 100; j++)
									{
										Sxy_accel[i][k] += (moveAverageDistance[i][k][j] - sample_accel[i][k]) * (aveMoveAverageDistance[k][j] - ave_accel[k]);
										Sxy_gyro[i][k] += (moveAverageAngle[i][k][j] - sample_gyro[i][k]) * (aveMoveAverageAngle[k][j] - ave_gyro[k]);
									}
							}
					}
				//endregion



				//region Calculate of R
				double[][] R_accel = new double[3][3];
				double[][] R_gyro = new double[3][3];

				for (int i = 0; i < 3; i++)
					{
						for (int j = 0; j < 3; j++)
							{
								R_accel[i][j] = Sxy_accel[i][j] / Math.sqrt(Sxx_accel[i][j] * Syy_accel[j]);
								R_gyro[i][j] = Sxy_gyro[i][j] / Math.sqrt(Sxx_gyro[i][j] * Syy_gyro[j]);
							}
					}
				//endregion


				mWriteData.writeRData("RegistSRdata", "R_accel", RegistNameInput.name, R_accel, RegistMotion.this);
				mWriteData.writeRData("RegistSRdata", "R_gyro", RegistNameInput.name, R_gyro, RegistMotion.this);

				//region 相関係数の判定
				// 相関係数が一定以上なら保存する（ユーザ名のテキストファイルに書き出す）
				if ((R_accel[0][0] > STRICT && R_accel[1][0] > STRICT) || (R_accel[1][0] > STRICT && R_accel[2][0] > STRICT) || (R_accel[0][0] > STRICT && R_accel[2][0] > STRICT))
					{
						if ((R_accel[0][1] > STRICT && R_accel[1][1] > STRICT) || (R_accel[1][1] > STRICT && R_accel[2][1] > STRICT) || (R_accel[0][1] > STRICT && R_accel[2][1] > STRICT))
							{
								if ((R_accel[0][2] > STRICT && R_accel[1][2] > STRICT) || (R_accel[1][2] > STRICT && R_accel[2][2] > STRICT) || (R_accel[0][2] > STRICT && R_accel[2][2] > STRICT))
									{
										if ((R_gyro[0][0] > STRICT && R_gyro[1][0] > STRICT) || (R_gyro[1][0] > STRICT && R_gyro[2][0] > STRICT) || (R_gyro[0][0] > STRICT || R_gyro[2][0] > STRICT))
											{
												if ((R_gyro[0][1] > STRICT && R_gyro[1][1] > STRICT) || (R_gyro[1][1] > STRICT && R_gyro[2][1] > STRICT) || (R_gyro[0][1] > STRICT || R_gyro[2][1] > STRICT))
													{
														if ((R_gyro[0][2] > STRICT && R_gyro[1][2] > STRICT) || (R_gyro[1][2] > STRICT && R_gyro[2][2] > STRICT) || (R_gyro[0][2] > STRICT && R_gyro[2][2] > STRICT))
															{
																getMotionBtn.setText("認証登録中");
																Toast.makeText(this, "モーションを登録中です", Toast.LENGTH_SHORT).show();

																// 3回のモーションの平均値をファイルに書き出す
																writeData();
															}
														else
															{
																Toast.makeText(this, "モーション登録に失敗しました", Toast.LENGTH_SHORT).show();
															}
													}
												else
													{
														Toast.makeText(this, "モーション登録に失敗しました", Toast.LENGTH_SHORT).show();
													}
											}
										else
											{
												Toast.makeText(this, "モーション登録に失敗しました", Toast.LENGTH_SHORT).show();
											}
									}
								else
									{
										Toast.makeText(this, "モーション登録に失敗しました", Toast.LENGTH_SHORT).show();
									}
							}
						else
							{
								Toast.makeText(this, "モーション登録に失敗しました", Toast.LENGTH_SHORT).show();
							}
					}
				else
					{
						Toast.makeText(this, "モーション登録に失敗しました", Toast.LENGTH_SHORT).show();
						Log.d(TAG, "失敗");
					}
				//endregion
			}


		/**
		 * モーションデータの平均値をSDカードの指定したディレクトリの出力するメソッド
		 */
		// TODO WriteDataに任せる（ここに書かない）
		private void writeData()
			{
				try
					{
						String filePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "MotionAuth" + File.separator + RegistNameInput.name;
						File file = new File(filePath);
						file.getParentFile().mkdir();
						FileOutputStream fos;

						fos = new FileOutputStream(file, false);
						OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
						BufferedWriter bw = new BufferedWriter(osw);

						// TODO "@"を他の記号で代用できないか調べる
						for (int i = 0; i < 100; i++)
							{
								bw.write("ave_distance_x@" + aveMoveAverageDistance[0][i] + "\n");
								bw.flush();
							}

						for (int i = 0; i < 100; i++)
							{
								bw.write("ave_distance_y@" + aveMoveAverageDistance[1][i] + "\n");
								bw.flush();
							}

						for (int i = 0; i < 100; i++)
							{
								bw.write("ave_distance_z@" + aveMoveAverageDistance[2][i] + "\n");
								bw.flush();
							}

						for (int i = 0; i < 100; i++)
							{
								bw.write("ave_angle_x@" + aveMoveAverageAngle[0][i] + "\n");
								bw.flush();
							}

						for (int i = 0; i < 100; i++)
							{
								bw.write("ave_angle_y@" + aveMoveAverageAngle[1][i] + "\n");
								bw.flush();
							}

						for (int i = 0; i < 100; i++)
							{
								bw.write("ave_angle_z@" + aveMoveAverageAngle[2][i] + "\n");
								bw.flush();
							}

						bw.close();
						fos.close();

						Toast.makeText(this, "finish", Toast.LENGTH_LONG).show();

						finishRegist();
					}
				catch (IOException e)
					{
						Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
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


		@Override
		public boolean onCreateOptionsMenu(Menu menu)
			{
				// Inflate the menu; this adds items to the action bar if it is present.
				getMenuInflater().inflate(R.menu.regist_motion, menu);
				return true;
			}


		/**
		 * スタート画面に移動するメソッド
		 */
		private void finishRegist()
			{
				Intent intent = new Intent();

				intent.setClassName("com.example.motionauth", "com.example.motionauth.Start");

				startActivityForResult(intent, 0);
				finish();
			}
	}
