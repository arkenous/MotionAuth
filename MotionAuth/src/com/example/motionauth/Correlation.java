package com.example.motionauth;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.example.motionauth.Utility.*;
import com.example.motionauth.Utility.Enum;


public class Correlation
	{
		/**
		 * 相関係数を求め，同一のモーションであるかどうかを確認する
		 *
		 * @param context      呼び出し元のcontext
		 * @param distance     double型の3次元配列距離データ
		 * @param angle        double型の三次元配列角度データ
		 * @param ave_distance double型の二次元配列距離データ
		 * @param ave_angle    double型の二次元配列角度データ
		 * @param threshold    int型の閾値
		 * @return 同一モーションであればtrueが，そうでなければfalseが返される
		 */
		public static Enum.MEASURE measureCorrelation(Context context, double[][][] distance, double[][][] angle, double[][] ave_distance, double[][] ave_angle, double threshold)
			{
				// 相関係数の計算

				// Calculate of Average A
				float[][] sample_accel = new float[3][3];
				float[][] sample_gyro = new float[3][3];

				// iは回数
				for (int i = 0; i < 3; i++)
					{
						// jはXYZ
						for (int j = 0; j < 3; j++)
							{
								for (int k = 0; k < 100; k++)
									{
										sample_accel[i][j] += distance[i][j][k];
										sample_gyro[i][j] += angle[i][j][k];
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

				// Calculate of Average B
				float ave_accel[] = new float[3];
				float ave_gyro[] = new float[3];

				for (int i = 0; i < 3; i++)
					{
						for (int j = 0; j < 100; j++)
							{
								ave_accel[i] += ave_distance[i][j];
								ave_gyro[i] += ave_angle[i][j];
							}
					}

				for (int i = 0; i < 3; i++)
					{
						ave_accel[i] /= 99;
						ave_gyro[i] /= 99;
					}

				// Calculate of Sxx
				float Sxx_accel[][] = new float[3][3];
				float Sxx_gyro[][] = new float[3][3];

				for (int i = 0; i < 3; i++)
					{
						for (int j = 0; j < 3; j++)
							{
								for (int k = 0; k < 100; k++)
									{
										Sxx_accel[i][j] += Math.pow((distance[i][j][k] - sample_accel[i][j]), 2);
										Sxx_gyro[i][j] += Math.pow((angle[i][j][k] - sample_gyro[i][j]), 2);
									}
							}
					}

				// Calculate of Syy
				float Syy_accel[] = new float[3];
				float Syy_gyro[] = new float[3];

				for (int i = 0; i < 3; i++)
					{
						for (int j = 0; j < 100; j++)
							{
								Syy_accel[i] += Math.pow((ave_distance[i][j] - ave_accel[i]), 2);
								Syy_gyro[i] += Math.pow((ave_angle[i][j] - ave_gyro[i]), 2);
							}
					}

				// Calculate of Sxy
				float[][] Sxy_accel = new float[3][3];
				float[][] Sxy_gyro = new float[3][3];

				for (int i = 0; i < 3; i++)
					{
						for (int j = 0; j < 3; j++)
							{
								for (int k = 0; k < 100; k++)
									{
										Sxy_accel[i][j] += (distance[i][j][k] - sample_accel[i][j]) * (ave_distance[j][k] - ave_accel[j]);
										Sxy_gyro[i][j] += (angle[i][j][k] - sample_gyro[i][j]) * (ave_angle[j][k] - ave_gyro[j]);
									}
							}
					}

				// Calculate of R
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

				if (!WriteData.writeRData("RegistLRdata", "R_accel", RegistNameInput.name, R_accel, context))
					{
						Toast.makeText(context, "Error", Toast.LENGTH_LONG).show();
					}
				if (!WriteData.writeRData("RegistLRdata", "R_gyro", RegistNameInput.name, R_gyro, context))
					{
						Toast.makeText(context, "Error", Toast.LENGTH_LONG).show();
					}

				if ((R_accel[0][0] > threshold && R_accel[1][0] > threshold) || (R_accel[1][0] > threshold && R_accel[2][0] > threshold) || (R_accel[0][0] > threshold && R_accel[2][0] > threshold))
					{
						if ((R_accel[0][1] > threshold && R_accel[1][1] > threshold) || (R_accel[1][1] > threshold && R_accel[2][1] > threshold) || (R_accel[0][1] > threshold && R_accel[2][1] > threshold))
							{
								if ((R_accel[0][2] > threshold && R_accel[1][2] > threshold) || (R_accel[1][2] > threshold && R_accel[2][2] > threshold) || (R_accel[0][2] > threshold && R_accel[2][2] > threshold))
									{
										if ((R_gyro[0][0] > threshold && R_gyro[1][0] > threshold) || (R_gyro[1][0] > threshold && R_gyro[2][0] > threshold) || (R_gyro[0][0] > threshold || R_gyro[2][0] > threshold))
											{
												if ((R_gyro[0][1] > threshold && R_gyro[1][1] > threshold) || (R_gyro[1][1] > threshold && R_gyro[2][1] > threshold) || (R_gyro[0][1] > threshold || R_gyro[2][1] > threshold))
													{
														if ((R_gyro[0][2] > threshold && R_gyro[1][2] > threshold) || (R_gyro[1][2] > threshold && R_gyro[2][2] > threshold)
																|| (R_gyro[0][2] > threshold && R_gyro[2][2] > threshold))
															{
																if (threshold == 0.4)
																	{
																		// thresholdにLOOSEが渡されていて，相関係数がそれ以上であると判定された場合
																		// 相関係数が0.8とかであれば，ズレを修正する必要なしと判断する
																		if ((R_accel[0][0] > 0.7 && R_accel[1][0] > 0.7) || (R_accel[1][0] > 0.7 && R_accel[2][0] > 0.7) || (R_accel[0][0] > 0.7 && R_accel[2][0] > 0.7))
																			{
																				if ((R_accel[0][1] > 0.7 && R_accel[1][1] > 0.7) || (R_accel[1][1] > 0.7 && R_accel[2][1] > 0.7) || (R_accel[0][1] > 0.7 && R_accel[2][1] > 0.7))
																					{
																						if ((R_accel[0][2] > 0.7 && R_accel[1][2] > 0.7) || (R_accel[1][2] > 0.7 && R_accel[2][2] > 0.7) || (R_accel[0][2] > 0.7 && R_accel[2][2] > 0.7))
																							{
																								if ((R_gyro[0][0] > 0.7 && R_gyro[1][0] > 0.7) || (R_gyro[1][0] > 0.7 && R_gyro[2][0] > 0.7) || (R_gyro[0][0] > 0.7 && R_gyro[2][0] > 0.7))
																									{
																										if ((R_gyro[0][1] > 0.7 && R_gyro[1][1] > 0.7) || (R_gyro[1][1] > 0.7 && R_gyro[2][1] > 0.7) || (R_gyro[0][1] > 0.7 && R_gyro[2][1] > 0.7))
																											{
																												if ((R_gyro[0][2] > 0.7 && R_gyro[1][2] > 0.7) || (R_gyro[1][2] > 0.7 && R_gyro[2][2] > 0.7) || (R_gyro[0][2] > 0.7 && R_gyro[2][2] > 0.7))
																													{
																														return Enum.MEASURE.PERFECT;
																													}
																												else
																													{
																														return Enum.MEASURE.CORRECT;
																													}
																											}
																										else
																											{
																												return Enum.MEASURE.CORRECT;
																											}
																									}
																								else
																									{
																										return Enum.MEASURE.CORRECT;
																									}
																							}
																						else
																							{
																								return Enum.MEASURE.CORRECT;
																							}
																					}
																				else
																					{
																						return Enum.MEASURE.CORRECT;
																					}
																			}
																		else
																			{
																				return Enum.MEASURE.CORRECT;
																			}
																	}
																return Enum.MEASURE.CORRECT;
															}
														else
															{
																return Enum.MEASURE.INCORRECT;
															}
													}
												else
													{
														return Enum.MEASURE.INCORRECT;
													}
											}
										else
											{
												return Enum.MEASURE.INCORRECT;
											}
									}
								else
									{
										return Enum.MEASURE.INCORRECT;
									}
							}
						else
							{
								return Enum.MEASURE.INCORRECT;
							}
					}
				else
					{
						return Enum.MEASURE.INCORRECT;
					}
			}
	}
