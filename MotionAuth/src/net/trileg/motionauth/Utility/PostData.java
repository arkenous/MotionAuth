package net.trileg.motionauth.Utility;

import android.os.Build;
import android.webkit.MimeTypeMap;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class PostData {
  private static final String TWO_HYPHEN = "--";
  private static final String EOL = "\r\n";
  private static final String BOUNDARY = String.format("%x", new Random().hashCode());
  private static final String CHARSET = "UTF-8";


  public String postData(String requestUrl, HashMap<String, String> postData) {
    String result = "";

    // 送信するコンテンツを作る
    StringBuilder contentsBuilder = new StringBuilder();
    String closingContents = "";
    int contentsLength = 0;
    String fileTagName = "";
    String filePath = "";
    File file = null;
    FileInputStream fileInputStream = null;

    for (Map.Entry<String, String> data : postData.entrySet()) {
      String key = data.getKey();
      String val = data.getValue();

      // ファイル以外
      if (!new File(val).isFile()) {
        contentsBuilder.append(String.format("%s%s%s", TWO_HYPHEN, BOUNDARY, EOL));
        contentsBuilder.append(String.format("Content-Disposition: form-data; name=\"%s\"%s", key, EOL));
        contentsBuilder.append(EOL);
        contentsBuilder.append(val);
        contentsBuilder.append(EOL);
      } else {
        // ファイル情報を保持しておく
        fileTagName = key;
        filePath = val;
        file = new File(filePath);
      }
    }

    // ファイル情報のセット
    contentsBuilder.append(String.format("%s%s%s", TWO_HYPHEN, BOUNDARY, EOL));
    contentsBuilder.append(String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"%s", fileTagName, filePath, EOL));

    // ファイルがあるとき
    if (file != null) {
      // ファイルサイズの取得
      contentsLength += file.length();

      // MIME取得
      int extPos = filePath.lastIndexOf(".");
      String ext = (extPos > 0) ? filePath.substring(extPos + 1) : "";
      String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase());

      contentsBuilder.append(String.format("Content-Type: %s%s", mime, EOL));
    } else {
      contentsBuilder.append(String.format("Content-Type: application/octet-stream%s", EOL));
    }

    contentsBuilder.append(EOL);
    closingContents = String.format("%s%s%s%s%s", EOL, TWO_HYPHEN, BOUNDARY, TWO_HYPHEN, EOL);

    // コンテンツの長さを取得
    try {
      // StringBuilderを文字列にしてからバイト長を取得しないと
      // 実際送ったサイズと異なる場合があり，コンテンツを正しく送信できなくなる
      contentsLength += contentsBuilder.toString().getBytes(CHARSET).length;
      contentsLength += closingContents.getBytes(CHARSET).length;
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    // サーバへ接続する
    HttpURLConnection connection = null;
    DataOutputStream dataOutputStream = null;
    BufferedReader bufferedReader = null;

    try {
      URL url = new URL(requestUrl);
      connection = (HttpURLConnection)url.openConnection();

      connection.setDoInput(true);
      connection.setDoOutput(true);
      // キャッシュを使用しない
      connection.setUseCaches(false);
      // HTTPストリーミングを有効にする
      connection.setChunkedStreamingMode(0);

      connection.setRequestMethod("POST");

      connection.setRequestProperty("Connection", "Keep-Alive");

      connection.setRequestProperty("User-Agent", String.format("Mozilla/5.0 (Linux; U; Android %s;)", Build.VERSION.RELEASE));

      // POSTデータの形式を設定
      connection.setRequestProperty("Content-Type", String.format("multipart/form-data; boundary=%s", BOUNDARY));
      // POSTデータの長さを設定
      connection.setRequestProperty("Content-Length", String.valueOf(contentsLength));

      // データを送信する
      dataOutputStream = new DataOutputStream(connection.getOutputStream());
      dataOutputStream.writeBytes((contentsBuilder.toString()));

      // ファイルの送信
      if (file != null) {
        byte buffer[] = new byte[1024];
        fileInputStream = new FileInputStream(file);

        while (fileInputStream.read(buffer, 0, buffer.length) > -1) {
          dataOutputStream.write(buffer, 0, buffer.length);
        }
      }

      dataOutputStream.writeBytes(closingContents);

      // レスポンスを受信する
      int responseCode = connection.getResponseCode();
      // 接続が確立したとき
      if (responseCode == HttpURLConnection.HTTP_OK) {
        StringBuilder resultBuilder = new StringBuilder();
        String line = "";

        bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        // レスポンスの読み込み
        while ((line = bufferedReader.readLine()) != null) {
          resultBuilder.append(String.format("%s%s", line, EOL));
        }
        result = resultBuilder.toString();
      } else {
        result = String.valueOf(responseCode);
      }
    } catch (ProtocolException e) {
      e.printStackTrace();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (bufferedReader != null) bufferedReader.close();
        if (dataOutputStream != null) {
          dataOutputStream.flush();
          dataOutputStream.close();
        }
        if (fileInputStream != null) fileInputStream.close();
        if (connection != null) connection.disconnect();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return result;
  }
}
