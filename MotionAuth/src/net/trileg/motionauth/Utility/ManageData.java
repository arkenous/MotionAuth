package net.trileg.motionauth.Utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import net.trileg.motionauth.Processing.CipherCrypt;

import java.io.*;
import java.util.ArrayList;


/**
 * Write data to SD Card.
 *
 * @author Kensuke Kosaka
 */
public class ManageData {
	private static final String APP_NAME = "MotionAuth";
	private FileOutputStream fos;
	private OutputStreamWriter osw;
	private BufferedWriter bw;

	/**
	 * Write float type 3-array data. Write destination is SD Card directory/folderName/userName/fileName+times+dimension
	 *
	 * @param folderName Directory name.
	 * @param dataName   Data name.
	 * @param userName   User name.
	 * @param data       Float type 3-array data to write.
	 */
	public void writeFloatThreeArrayData(String folderName, String dataName, String userName, float[][][] data) {
		LogUtil.log(Log.INFO);

		// Get status of SD Card mounting.
		String status = Environment.getExternalStorageState();

		// SD Card is not mounted.
		if (!status.equals(Environment.MEDIA_MOUNTED)) {
			LogUtil.log(Log.ERROR, "SDCard not mounted");
			return;
		}

		String SD_PATH = Environment.getExternalStorageDirectory().getPath();
		String FOLDER_PATH = SD_PATH + File.separator + APP_NAME + File.separator + folderName + File.separator + userName;

		File file = new File(FOLDER_PATH);

		try {
			if (!file.exists()) {
				if (!file.mkdirs()) {
					LogUtil.log(Log.DEBUG, "Make directory error");
				}
			}
		} catch (Exception e) {
			LogUtil.log(Log.ERROR, e.getMessage(), e.getCause());
		}

		try {
			String dimension = null;

			for (int i = 0; i < data.length; i++) {
				// X,Y,Z loop.
				for (int j = 0; j < data[i].length; j++) {
					if (j == 0) {
						dimension = "x";
					} else if (j == 1) {
						dimension = "y";
					} else if (j == 2) {
						dimension = "z";
					}

					String filePath = FOLDER_PATH + File.separator + dataName + String.valueOf(i) + dimension;
					file = new File(filePath);

					// Write data to file.
					fos = new FileOutputStream(file, false);
					osw = new OutputStreamWriter(fos, "UTF-8");
					bw = new BufferedWriter(osw);

					for (int k = 0; k < data[i][j].length; k++) {
						bw.write(dataName + "_" + dimension + "_" + String.valueOf(i + 1) + "@" + data[i][j][k] + "\n");
					}
					bw.close();
					osw.close();
					fos.close();
				}
			}
		} catch (Exception e) {
			LogUtil.log(Log.ERROR, e.getMessage(), e.getCause());
		}
	}


	/**
	 * Write double type 2-array data. Write destination is SD Card directory/folderName/userName/fileName+dimension
	 *
	 * @param folderName Directory name.
	 * @param dataName   Data name.
	 * @param userName   User name.
	 * @param data       Double type 2-array data to write.
	 * @return Return true when write data complete, otherwise false.
	 */
	public boolean writeDoubleTwoArrayData(String folderName, String dataName, String userName, double[][] data) {
		LogUtil.log(Log.INFO);

		String status = Environment.getExternalStorageState();
		if (!status.equals(Environment.MEDIA_MOUNTED)) {
			LogUtil.log(Log.ERROR, "SDCard not mounted");
			return false;
		}

		String SD_PATH = Environment.getExternalStorageDirectory().getPath();
		String FOLDER_PATH = SD_PATH + File.separator + APP_NAME + File.separator + folderName + File.separator + userName;

		File file = new File(FOLDER_PATH);

		try {
			if (!file.exists()) {
				if (!file.mkdirs()) {
					LogUtil.log(Log.ERROR, "Make directory error");
				}
			}
		} catch (Exception e) {
			LogUtil.log(Log.ERROR, e.getMessage(), e.getCause());
			return false;
		}

		try {
			String dimension = null;

			// X,Y,Z loop.
			for (int i = 0; i < data.length; i++) {
				if (i == 0) {
					dimension = "x";
				} else if (i == 1) {
					dimension = "y";
				} else if (i == 2) {
					dimension = "z";
				}

				String filePath = FOLDER_PATH + File.separator + dataName + String.valueOf(i) + dimension;
				file = new File(filePath);

				// Write data to file.
				fos = new FileOutputStream(file, false);
				osw = new OutputStreamWriter(fos, "UTF-8");
				bw = new BufferedWriter(osw);

				for (int j = 0; j < data[i].length; j++) {
					bw.write(dataName + "_" + dimension + "@" + data[i][j] + "\n");
				}
				bw.close();
				osw.close();
				fos.close();
			}
		} catch (Exception e) {
			LogUtil.log(Log.ERROR, e.getMessage(), e.getCause());
			return false;
		}
		return true;
	}


	/**
	 * Write double type 3-array data. Write destination is SD Card directory/folderName/userName/fileName+times+dimension
	 *
	 * @param folderName Directory name.
	 * @param dataName   Data name.
	 * @param userName   User name.
	 * @param data       Double type 3-array data to write.
	 */
	public void writeDoubleThreeArrayData(String folderName, String dataName, String userName, double[][][] data) {
		LogUtil.log(Log.INFO);

		String status = Environment.getExternalStorageState();
		if (!status.equals(Environment.MEDIA_MOUNTED)) {
			LogUtil.log(Log.ERROR, "SDCard not mounted");
		}

		String SD_PATH = Environment.getExternalStorageDirectory().getPath();
		String FOLDER_PATH = SD_PATH + File.separator + APP_NAME + File.separator + folderName + File.separator + userName;

		File file = new File(FOLDER_PATH);

		try {
			if (!file.exists()) {
				if (!file.mkdirs()) {
					LogUtil.log(Log.ERROR, "Make directory Error");
				}
			}
		} catch (Exception e) {
			LogUtil.log(Log.ERROR, e.getMessage(), e.getCause());
		}

		try {
			String dimension = null;

			for (int i = 0; i < data.length; i++) {
				// X,Y,Z loop.
				for (int j = 0; j < data[i].length; j++) {
					if (j == 0) {
						dimension = "x";
					} else if (j == 1) {
						dimension = "y";
					} else if (j == 2) {
						dimension = "z";
					}

					String filePath = FOLDER_PATH + File.separator + dataName + String.valueOf(i) + dimension;
					file = new File(filePath);

					// Write data to file.
					fos = new FileOutputStream(file, false);
					osw = new OutputStreamWriter(fos, "UTF-8");
					bw = new BufferedWriter(osw);

					for (int k = 0; k < data[0][0].length; k++) {
						bw.write(data[i][j][k] + "\n");
						bw.flush();
					}
					bw.close();
					osw.close();
					fos.close();
				}
			}
		} catch (Exception e) {
			LogUtil.log(Log.ERROR, e.getMessage(), e.getCause());
		}
	}


	/**
	 * Write double type 2-array data. Write destination is SD Card directory/folderName/userName/fileName+dimension
	 *
	 * @param folderName Directory name.
	 * @param dataName   Data name.
	 * @param userName   User name.
	 * @param data       Double type 2-array data to write.
	 */
	public void writeRData(String folderName, String dataName, String userName, double[][] data) {
		LogUtil.log(Log.INFO);

		String status = Environment.getExternalStorageState();
		if (!status.equals(Environment.MEDIA_MOUNTED)) {
			LogUtil.log(Log.ERROR, "SDCard not mounted");
		}

		String SD_PATH = Environment.getExternalStorageDirectory().getPath();
		String FOLDER_PATH = SD_PATH + File.separator + APP_NAME + File.separator + folderName + File.separator + userName;

		File file = new File(FOLDER_PATH);

		try {
			if (!file.exists()) {
				if (!file.mkdirs()) {
					LogUtil.log(Log.ERROR, "Make directory error");
				}
			}
		} catch (Exception e) {
			LogUtil.log(Log.ERROR, e.getMessage(), e.getCause());
		}

		try {
			String dimension = null;

			for (int i = 0; i < data.length; i++) {

				// X,Y,Z loop.
				for (int j = 0; j < data[i].length; j++) {
					if (j == 0) {
						dimension = "x";
					} else if (j == 1) {
						dimension = "y";
					} else if (j == 2) {
						dimension = "z";
					}

					String filePath = FOLDER_PATH + File.separator + dataName + String.valueOf(i) + dimension;
					file = new File(filePath);

					// Write data to file.
					fos = new FileOutputStream(file, false);
					osw = new OutputStreamWriter(fos, "UTF-8");
					bw = new BufferedWriter(osw);

					bw.write(dataName + "_" + dimension + "_" + String.valueOf(i + 1) + "@" + data[i][j] + "\n");
					bw.close();
					osw.close();
					fos.close();
				}
			}
		} catch (Exception e) {
			LogUtil.log(Log.ERROR, e.getMessage(), e.getCause());
		}
	}


	/**
	 * Write double type 2-array data. Write destination is SD Card directory/MotionAuth/folderName/userName
	 *
	 * @param folderName Directory name.
	 * @param userName   User name.
	 * @param R_accel    Double type 1-array distance-R data.
	 * @param R_gyro     Double type 1-array angle-R data.
	 */
	public void writeRData(String folderName, String userName, double[] R_accel, double[] R_gyro) {
		LogUtil.log(Log.INFO);

		String status = Environment.getExternalStorageState();
		if (!status.equals(Environment.MEDIA_MOUNTED)) {
			LogUtil.log(Log.ERROR, "SDCard not mounted");
		}

		String SD_PATH = Environment.getExternalStorageDirectory().getPath();
		String FOLDER_PATH = SD_PATH + File.separator + APP_NAME + File.separator + folderName;

		File file = new File(FOLDER_PATH);

		try {
			if (!file.exists()) {
				if (!file.mkdirs()) {
					LogUtil.log(Log.ERROR, "Make directory error");
				}
			}
		} catch (Exception e) {
			LogUtil.log(Log.ERROR, e.getMessage(), e.getCause());
		}

		try {
			String filePath = FOLDER_PATH + File.separator + userName;
			file = new File(filePath);

			// Write data to file.
			fos = new FileOutputStream(file, false);
			osw = new OutputStreamWriter(fos, "UTF-8");
			bw = new BufferedWriter(osw);

			for (int i = 0; i < 2; i++) {
				if (i == 0) {
					for (int j = 0; j < 3; j++) {
						if (j == 0) {
							bw.write("R_accel_x@" + R_accel[j] + "\n");
							bw.flush();
						}
						if (j == 1) {
							bw.write("R_accel_y@" + R_accel[j] + "\n");
							bw.flush();
						}
						if (j == 2) {
							bw.write("R_accel_z@" + R_accel[j] + "\n");
							bw.flush();
						}
					}
				} else if (i == 1) {
					for (int j = 0; j < 3; j++) {
						if (j == 0) {
							bw.write("R_gyro_x@" + R_gyro[j] + "\n");
							bw.flush();
						}
						if (j == 1) {
							bw.write("R_gyro_y@" + R_gyro[j] + "\n");
							bw.flush();
						}
						if (j == 2) {
							bw.write("R_gyro_z@" + R_gyro[j] + "\n");
							bw.flush();
						}
					}
				}
			}
			bw.close();
			osw.close();
			fos.close();
		} catch (Exception e) {
			LogUtil.log(Log.ERROR, e.getMessage(), e.getCause());
		}
	}


	public void writeR(String folderName, String userName, double data) {
		LogUtil.log(Log.INFO);

		String status = Environment.getExternalStorageState();
		if (!status.equals(Environment.MEDIA_MOUNTED)) {
			LogUtil.log(Log.ERROR, "SDCard not mounted");
		}

		String SD_PATH = Environment.getExternalStorageDirectory().getPath();
		String FOLDER_PATH = SD_PATH + File.separator + APP_NAME + File.separator + folderName;

		File file = new File(FOLDER_PATH);

		try {
			if (!file.exists()) {
				if (!file.mkdirs()) {
					LogUtil.log(Log.ERROR, "Make directory error");
				}
			}
		} catch (Exception e) {
			LogUtil.log(Log.ERROR, e.getMessage(), e.getCause());
		}

		try {
			String filePath = FOLDER_PATH + File.separator + userName;
			file = new File(filePath);

			fos = new FileOutputStream(file, false);
			osw = new OutputStreamWriter(fos);
			bw = new BufferedWriter(osw);

			bw.write(String.valueOf(data));

			bw.close();
			osw.close();
			fos.close();
		} catch (Exception e) {
			LogUtil.log(Log.ERROR, e.getMessage(), e.getCause());
		}
	}


	/**
	 * Save authentication key data which is collected from Registration.
	 *
	 * @param userName        User name.
	 * @param averageDistance Double type 2-array average distance data.
	 * @param averageAngle    Double type 2-array average angle data.
	 * @param ampValue        Amplifier value.
	 * @param context         Caller context.
	 */
	public void writeRegisterData(String userName, double[][] averageDistance, double[][] averageAngle, double ampValue, Context context) {

		LogUtil.log(Log.INFO);

		CipherCrypt mCipherCrypt = new CipherCrypt(context);

		String[][] averageDistanceStr = new String[averageDistance.length][averageDistance[0].length];
		String[][] averageAngleStr = new String[averageAngle.length][averageAngle[0].length];

		for (int i = 0; i < averageDistance.length; i++) {
			for (int j = 0; j < averageDistance[i].length; j++) {
				averageDistanceStr[i][j] = String.valueOf(averageDistance[i][j]);
				averageAngleStr[i][j] = String.valueOf(averageAngle[i][j]);
			}
		}

		// 暗号化
		String[][] encryptedAverageDistanceStr = mCipherCrypt.encrypt(averageDistanceStr);
		String[][] encryptedAverageAngleStr = mCipherCrypt.encrypt(averageAngleStr);

		// 配列データを特定文字列を挟んで連結する
		ConvertArrayAndString mConvertArrayAndString = new ConvertArrayAndString();
		String registerDistanceData = mConvertArrayAndString.arrayToString(encryptedAverageDistanceStr);
		String registerAngleData = mConvertArrayAndString.arrayToString(encryptedAverageAngleStr);

		Context mContext = context.getApplicationContext();
		SharedPreferences userPref = mContext.getSharedPreferences("UserList", Context.MODE_PRIVATE);
		SharedPreferences.Editor userPrefEditor = userPref.edit();

		userPrefEditor.putString(userName, "");
		userPrefEditor.apply();

		SharedPreferences preferences = mContext.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();

		editor.putString(userName + "distance", registerDistanceData);
		editor.putString(userName + "angle", registerAngleData);
		editor.putString(userName + "amplify", String.valueOf(ampValue));
		editor.apply();
	}


	/**
	 * Read registered data from SharedPreferences
	 *
	 * @param context Context use to get Application unique SharedPreferences.
	 * @param userName User name.
	 * @return Double type 2-array registered data list.
	 */
	public ArrayList<double[][]> readRegisteredData(Context context, String userName) {
		LogUtil.log(Log.INFO);
		Context mContext = context.getApplicationContext();

		SharedPreferences preferences = mContext.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);

		String registeredDistanceData = preferences.getString(userName + "distance", "");
		String registeredAngleData = preferences.getString(userName + "angle", "");

		if ("".equals(registeredDistanceData)) throw new RuntimeException();

		ConvertArrayAndString mConvertArrayAndString = new ConvertArrayAndString();
		CipherCrypt mCipherCrypt = new CipherCrypt(context);

		String[][] decryptedDistance = mCipherCrypt.decrypt(mConvertArrayAndString.stringToArray(registeredDistanceData));
		String[][] decryptedAngle = mCipherCrypt.decrypt(mConvertArrayAndString.stringToArray(registeredAngleData));

		double[][] distance = new double[3][100], angle = new double[3][100];

		for (int i = 0; i < decryptedDistance.length; i++) {
			for (int j = 0; j < decryptedDistance[i].length; j++) {
				distance[i][j] = Double.valueOf(decryptedDistance[i][j]);
				angle[i][j] = Double.valueOf(decryptedAngle[i][j]);
			}
		}

		ArrayList<double[][]> result = new ArrayList<>();
		result.add(distance);
		result.add(angle);

		return result;
	}


	/**
	 * Write float type 3-array list data. Write destination is SD Card directory/MotionAuth/dir/user/type+times+dimension
	 *
	 * @param dir  Directory name.
	 * @param user User name.
	 * @param type Sensor name.
	 * @param data Float type 3-array list data.
	 */
	public void writeThreeDimenList(String dir, String user, String type, ArrayList<ArrayList<ArrayList<Float>>> data) {
		LogUtil.log(Log.INFO);

		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			LogUtil.log(Log.ERROR, "SD-Card not mounted");
			return;
		}

		String SD_PATH = Environment.getExternalStorageDirectory().getPath();
		String DIR_PATH = SD_PATH + File.separator + APP_NAME + File.separator + dir + File.separator + user;

		File file = new File(DIR_PATH);
		try {
			if (!file.exists()) {
				if (!file.mkdirs()) {
					LogUtil.log(Log.DEBUG, "Make directory Error");
				}
			}
		} catch (Exception e) {
			LogUtil.log(Log.ERROR, e.getMessage(), e.getCause());
		}

		try {
			String dimension;

			for (int time = 0; time < data.size(); time++) {
				for (int dimen = 0; dimen < data.get(time).size(); dimen++) {
					if (dimen == 0) {
						dimension = "x";
					} else if (dimen == 1) {
						dimension = "y";
					} else if (dimen == 2) {
						dimension = "z";
					} else {
						dimension = "";
					}

					String filePath = DIR_PATH + File.separator + type + String.valueOf(time) + dimension;
					file = new File(filePath);

					// Write data to file.
					fos = new FileOutputStream(file, false);
					osw = new OutputStreamWriter(fos, "UTF-8");
					bw = new BufferedWriter(osw);

					for (int item = 0; item < data.get(time).get(dimen).size(); item++) {
						bw.write(type + "_" + dimension + "_" + String.valueOf(time + 1)
								+ "@" + data.get(time).get(dimen).get(item) + "\n");
					}

					bw.close();
					osw.close();
					fos.close();
				}
			}
		} catch (IOException e) {
			LogUtil.log(Log.ERROR, e.getMessage(), e.getCause());
		}
	}
}
