package com.example.motionauth;

import android.content.Context;
import android.util.Log;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class Lowpass
{
	public static void LowpassFilter(double[] data, Context context)
	{
		DoubleFFT_1D fft = new DoubleFFT_1D (data.length);

		Log.d("FFT", "a");

		// フーリエ変換（FFT）の実行
		fft.realForward (data);

		Log.d("FFT", "b");

		// dataの偶数要素は実数成分，奇数要素は虚数成分

		// フーリエ変換後の値をアウトプット
		WriteData.writeSingleArrayData ("FFT", "beforeFFT", RegistNameInput.name, data, context);

		for (int i = 0; i < 100;i = i + 2)
		{
			if (data[i] > 10 || data[i] < -10)
			{
				data[i] = 0;
				data[i + 1] = 0;
			}
		}
		Log.d("FFT", "c");

		WriteData.writeSingleArrayData ("FFT", "afterFFT", RegistNameInput.name, data, context);

		fft.realInverse (data, true);

		WriteData.writeSingleArrayData ("FFT", "afteraccelo0X", RegistNameInput.name, data, context);
	}
}
