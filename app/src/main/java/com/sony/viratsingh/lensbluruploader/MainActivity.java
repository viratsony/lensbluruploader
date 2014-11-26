package com.sony.viratsingh.lensbluruploader;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.sony.viratsingh.lensbluruploader.settings.SettingsDialog;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;

import retrofit.RestAdapter;
import retrofit.mime.TypedFile;


public class MainActivity extends Activity {
  public String endpoint;

  private ImageView googleImageView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Logger.plant(new Logger.AndroidTree());

    googleImageView = (ImageView) findViewById(R.id.google_image);

    endpoint = "http://54.215.153.79:3018";
    Intent intent = getIntent();

    if (intent != null) {
      if (intent.getType().startsWith("image/")) {
        Toast.makeText(this, "Image received in LensBlurUploader", Toast.LENGTH_LONG).show();
        InputStream stream = null;
        try {
          Bundle bundle = intent.getExtras();
          Uri uri = (Uri)bundle.get(Intent.EXTRA_STREAM);
          stream = getContentResolver().openInputStream(uri);
          Bitmap bitmap = BitmapFactory.decodeStream(stream);
          stream.close();
          googleImageView.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }

        handleImage(intent);
      }
    }
    // finish();
  }


  private void handleImage(Intent intent) {
    Uri imageUri = intent.getParcelableExtra(Intent.ACTION_GET_CONTENT);

//    new ImageDecodeTask().execute(imageUri);
//    try {
//      Bitmap bmp=BitmapFactory.decodeStream(this.getContentResolver().openInputStream(imageUri));
//      googleImageView.setImageBitmap(bmp);
//      Logger.info("imageUri in handleImage: " + imageUri);
//    } catch (FileNotFoundException e) {
//      e.printStackTrace();
//    }


//    bitmapWithoutCompressMethod(imageUri);

//    new ImageDecodeTask().execute(imageUri);

    String path = getImagePath(imageUri);
    new UploadTask().execute(path);

//    Logger.info("imageUri scheme: " + imageUri.getScheme());
//    Logger.info("imageUri: " + imageUri);
//
//    String encodedImageString = uriToInputStream(imageUri);
//    String imageFilePath = getRealPathFromUri(imageUri);
//    File file = new File(imageFilePath);
////
////    FileOutputStream out = null;
////    try {
////      out = new FileOutputStream(file);
////      for (int i = 0; i < 256; i++) {
////        out.write(i);
////      }
////
////      out.close();
////    } catch (FileNotFoundException e) {
////      e.printStackTrace();
////    } catch (IOException e) {
////      e.printStackTrace();
////    } finally {
////    }
//
//
//    TypedFile mediaFile = new TypedFile("image", file);
//
//
//    Logger.info("mediaFile exists? " + file.exists());
//    Logger.info("mediaFile absolute path: " + file.getAbsolutePath());
//
//    Logger.info("imageFilePath: " + imageFilePath);


  }

  public String getImagePath(Uri uri){
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

  public int uploadFile(String sourceFileUri) {

    int serverResponseCode = 0;
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

  private String uriToInputStream(Uri imageUri) {
    try {
      InputStream inputStream = getContentResolver().openInputStream(imageUri);
      byte[] byteArray = IOUtils.toByteArray(inputStream);
      String encodedByteArrayString = encodeToString(byteArray);
      Logger.info("encodedByteArrayString byteArray length: " + byteArray.length);
      Logger.info("encodedByteArrayString length: " + encodedByteArrayString.length());

      return encodedByteArrayString;

    } catch (FileNotFoundException e) {
      Logger.error("FileNotFoundException: " + e);
    } catch (IOException e) {
      Logger.error("IOException: " + e);
    }
    return "";
  }

  private class UploadTask extends AsyncTask<String, Void, Void> {

    @Override
    protected Void doInBackground(String... filename) {
      uploadFile(filename[0]);
//      Uri imageUri = uris[0];
//      Logger.info("imageUri in asynctask: " + imageUri);
//      try {
////        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
//        Bitmap bitmap = getBitmapFromUri(imageUri);
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//        byte[] b = baos.toByteArray();
//
////        InputStream iStream =   getContentResolver().openInputStream(imageUri);
////        byte[] b = getBytes(iStream);
//
//        String imageFilePath = getRealPathFromUri(imageUri);
//        File file = new File(imageFilePath);
//
//        FileOutputStream fos=new FileOutputStream(file.getPath());
//
//        fos.write(b);
//        fos.close();
//
//
//        Logger.info("file length: " + file.length());
//
//
//        TypedFile mediaFile = new TypedFile("image", file);
//
//        RestAdapter restAdapter = new RestAdapter.Builder()
//            .setEndpoint(endpoint)
//            .build();
//
//        UploadService uploadService = restAdapter.create(UploadService.class);
//        uploadService.uploadImage(mediaFile);

//
//      } catch (FileNotFoundException e) {
//        e.printStackTrace();
//      } catch (IOException e) {
//        e.printStackTrace();
//      }

      return null;
    }
  }

  private Bitmap getBitmapFromUri(Uri contentUri) {
    String path = null;
    String[] projection = { MediaStore.Images.Media.DATA };
    Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null);
    if (cursor.moveToFirst()) {
      int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
      path = cursor.getString(columnIndex);
    }
    cursor.close();
    Bitmap bitmap = BitmapFactory.decodeFile(path);
    return bitmap;
  }

  public byte[] getBytes(InputStream inputStream) throws IOException {
    ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
    int bufferSize = 1024;
    byte[] buffer = new byte[bufferSize];

    int len = 0;
    while ((len = inputStream.read(buffer)) != -1) {
      byteBuffer.write(buffer, 0, len);
    }
    return byteBuffer.toByteArray();
  }


//  private class UploadTask extends AsyncTask<TypedFile, Void, Void> {
//
//    @Override
//    protected Void doInBackground(TypedFile... files) {
////      String encodedImageString = strings[0];
//      TypedFile mediaFile = files[0];
//      RestAdapter restAdapter = new RestAdapter.Builder()
//          .setEndpoint(endpoint)
//          .build();
//
//      UploadService uploadService = restAdapter.create(UploadService.class);
//      uploadService.uploadImage(mediaFile);
//      return null;
//    }
//  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public String getEndpoint() {
    return this.endpoint;
  }

  private String getRealPathFromUri(Uri contentUri) {
    Cursor cursor = null;
    try {
      String[] proj = {MediaStore.Images.Media.DATA};
      cursor = getContentResolver().query(contentUri, proj, null, null, null);
      int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
      cursor.moveToFirst();
      return cursor.getString(column_index);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.action_settings) {
      FragmentManager fragmentManager = getFragmentManager();
      SettingsDialog settingsDialog = new SettingsDialog();
      settingsDialog.show(fragmentManager, "Settings Dialog");
    }
    return super.onOptionsItemSelected(item);
  }
  private String encodeToString(byte[] byteArray) {
    return Base64.encodeToString(byteArray, Base64.URL_SAFE);
  }





  private void compressToBitmapMethod(Uri imageUri) {
    try {
      Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
      byte[] b = baos.toByteArray();

      String encodedString = encodeToString(b);
      Logger.info("encodedString byteArray length: " + b.length);
      Logger.info("encodedString length: " + encodedString.length());

      bitmapWithoutCompressMethod(imageUri);

    } catch (FileNotFoundException e) {
      Logger.info("FileNotFound: " + e);
    }
  }


  private void apacheCommonsWay(File mediaFile) {
    try {
      byte[] byteArray = FileUtils.readFileToByteArray(mediaFile);
      String encodedString = encodeToString(byteArray);

      String sub_encodedString = encodedString.substring(0, 50);
      Logger.info("sub_encodedString: " + sub_encodedString);

    } catch (IOException e) {
      Logger.info("sub_encodedString try catch DID NOT WORK" + e);
    }
  }



//
  private void bitmapWithoutCompressMethod(Uri imageUri) {
    try {
      // Create bitmap from Uri
//      Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
      Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
      if (bitmap == null) {
        Logger.info("bitmap is null in bitmapwithoutcompress");
      }
      googleImageView.setImageBitmap(bitmap);

//      new ImageDecodeTask().execute(bitmap);


    } catch (FileNotFoundException e) {
      Logger.info("FileNotFound in bitmapWIthoutcompressMethod: " + e);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private class ImageDecodeTask extends AsyncTask<Uri, Void, Bitmap> {

    @Override
    protected Bitmap doInBackground(Uri... uris) {

      try {
        Uri imageUri = uris[0];
        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
        if (bitmap == null) {
          Logger.info("bitmap is null in bitmapwithoutcompress");
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(byteBuffer);
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = byteBuffer.array();

//        InputStream iStream =   getContentResolver().openInputStream(imageUri);
//        byte[] b = getBytes(iStream);

        String imageFilePath = getRealPathFromUri(imageUri);
        File file = new File(imageFilePath);

        FileOutputStream fos=new FileOutputStream(file.getPath());

        fos.write(b);
        fos.close();


        Logger.info("file length: " + file.length());


        TypedFile mediaFile = new TypedFile("image", file);

        RestAdapter restAdapter = new RestAdapter.Builder()
            .setEndpoint(endpoint)
            .build();

        UploadService uploadService = restAdapter.create(UploadService.class);
        uploadService.uploadImage(mediaFile);
        return bitmap;
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }


//
//      // Create byte[] from bitmap
//      Bitmap bitmap = bitmaps[0];
//      ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getByteCount());
//      bitmap.copyPixelsToBuffer(byteBuffer);
//      byte[] bytes = byteBuffer.array();
//
//      Logger.info("bytes length: " + bytes.length);
//
//      // Encode the byte[] to a Base64 String
//      String encodedImageString = encodeToString(bytes);
//      Logger.info("encodedImageString length: " + encodedImageString.length());

      return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
      super.onPostExecute(bitmap);
      googleImageView.setImageBitmap(bitmap);

      // TODO - Upload the Base64 String to Charlie's endpoint
    }
  }
}
