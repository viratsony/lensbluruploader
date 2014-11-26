package com.sony.viratsingh.lensbluruploader;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.sony.viratsingh.lensbluruploader.R;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class MyActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_my);
    Logger.plant(new Logger.AndroidTree());
    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

    StrictMode.setThreadPolicy(policy);
    final Button button = (Button) findViewById(R.id.upload_button);
    button.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        // To open up a gallery browser
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),1);
      }
    });
  }
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    String path = getImagePath(data.getData());

    Logger.info("MyActivity debug uri: " + data.getData());
    Logger.info("MyActivity debug uri stream: " + data.getParcelableExtra(Intent.EXTRA_STREAM));
    Logger.info("MyActivity debug uri file path: " + path);
    System.out.println(path + "  ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd");

    new UploadTask().execute(path);
  }
  public String getImagePath(Uri uri){
    Logger.info("MyActivity debug uri path: " + uri.getPath());
    Cursor cursor = getContentResolver().query(uri, null, null, null, null);
    cursor.moveToFirst();
    String document_id = cursor.getString(0);
    document_id = document_id.substring(document_id.lastIndexOf(":")+1);
    cursor.close();

    cursor = getContentResolver().query(
        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
    cursor.moveToFirst();
    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
    cursor.close();

    return path;
  }
  int serverResponseCode = 0;
  public int uploadFile(String sourceFileUri) {


    String fileName = sourceFileUri;

    HttpURLConnection conn = null;
    DataOutputStream dos = null;
    String lineEnd = "\r\n";
    String twoHyphens = "--";
    String boundary = "*****";
    int bytesRead, bytesAvailable, bufferSize;
    byte[] buffer;
    int maxBufferSize = 1 * 1024 * 1024;
    File sourceFile = new File(sourceFileUri);

    if (!sourceFile.isFile()) {

      System.out.println("no file found !!!!!!!!!!!!");

      return 0;

    }
    else
    {
      try {

        // open a URL connection to the Servlet
        FileInputStream fileInputStream = new FileInputStream(sourceFile);
        URL url = new URL("http://54.215.153.79:3018/upload");

        // Open a HTTP  connection to  the URL
        conn = (HttpURLConnection) url.openConnection();
        conn.setDoInput(true); // Allow Inputs
        conn.setDoOutput(true); // Allow Outputs
        conn.setUseCaches(false); // Don't use a Cached Copy
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("ENCTYPE", "multipart/form-data");
        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
        conn.setRequestProperty("uploaded_file", fileName);

        dos = new DataOutputStream(conn.getOutputStream());

        dos.writeBytes(twoHyphens + boundary + lineEnd);
        dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fileName + "\"" + lineEnd);

        dos.writeBytes(lineEnd);

        // create a buffer of  maximum size
        bytesAvailable = fileInputStream.available();

        bufferSize = Math.min(bytesAvailable, maxBufferSize);
        buffer = new byte[bufferSize];

        // read file and write it into form...
        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

        while (bytesRead > 0) {

          dos.write(buffer, 0, bufferSize);
          bytesAvailable = fileInputStream.available();
          bufferSize = Math.min(bytesAvailable, maxBufferSize);
          bytesRead = fileInputStream.read(buffer, 0, bufferSize);

        }

        // send multipart form data necesssary after file data...
        dos.writeBytes(lineEnd);
        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

        // Responses from the server (code and message)
        serverResponseCode = conn.getResponseCode();
        String serverResponseMessage = conn.getResponseMessage();

        System.out.println("uploadFile HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);

        if(serverResponseCode == 200){

          System.out.println("succcesss!!!!!!!!!!!!!!!!!!!!!!!");
        }

        //close the streams //
        fileInputStream.close();
        dos.flush();
        dos.close();

      } catch (Exception e) {
        e.printStackTrace();

        System.out.println("Upload file to server Exception Exception : "
            + e.getMessage());
      }
      return serverResponseCode;

    } // End else block
  }

  private class UploadTask extends AsyncTask<String, Void, Void> {

    @Override
    protected Void doInBackground(String... paths) {
      uploadFile(paths[0]);
      return null;
    }
  }

}