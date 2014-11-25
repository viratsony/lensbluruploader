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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
    endpoint = getResources().getString(R.string.endpoint);

    Intent intent = getIntent();

    if (intent != null) {
      if (intent.getType().startsWith("image/")) {
        Toast.makeText(this, "Image received in LensBlurUploader", Toast.LENGTH_LONG).show();
        handleImage(intent);
      }
    }
    // finish();
  }


  private void handleImage(Intent intent) {
    Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
    Logger.info("imageUri scheme: " + imageUri.getScheme());
    Logger.info("imageUri: " + imageUri);

    String encodedImageString = uriToInputStream(imageUri);
    String imageFilePath = getRealPathFromUri(imageUri);
    File file = new File(imageFilePath);
    TypedFile mediaFile = new TypedFile("image", file);

    Logger.info("mediaFile exists? " + file.exists());
    Logger.info("mediaFile absolute path: " + file.getAbsolutePath());

    Logger.info("imageFilePath: " + imageFilePath);

//    new UploadTask().execute(mediaFile);

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

  private String encodeToString(byte[] byteArray) {
    return Base64.encodeToString(byteArray, Base64.URL_SAFE);
  }

  private class UploadTask extends AsyncTask<TypedFile, Void, Void> {

    @Override
    protected Void doInBackground(TypedFile... files) {
//      String encodedImageString = strings[0];
      TypedFile mediaFile = files[0];
      RestAdapter restAdapter = new RestAdapter.Builder()
          .setEndpoint(endpoint)
          .build();

      UploadService uploadService = restAdapter.create(UploadService.class);
      uploadService.uploadImage(mediaFile);
      return null;
    }
  }

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





//  private void compressToBitmapMethod(Uri imageUri) {
//    try {
//      Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
//      ByteArrayOutputStream baos = new ByteArrayOutputStream();
//      bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//      byte[] b = baos.toByteArray();
//
//      String encodedString = encodeToString(b);
//      Logger.info("encodedString byteArray length: " + b.length);
//      Logger.info("encodedString length: " + encodedString.length());
//
//      bitmapWithoutCompressMethod(imageUri);
//
//    } catch (FileNotFoundException e) {
//      Logger.info("FileNotFound: " + e);
//    }
//  }
//
//
//  private void apacheCommonsWay(File mediaFile) {
//    try {
//      byte[] byteArray = FileUtils.readFileToByteArray(mediaFile);
//      String encodedString = encodeToString(byteArray);
//
//      String sub_encodedString = encodedString.substring(0, 50);
//      Logger.info("sub_encodedString: " + sub_encodedString);
//
//    } catch (IOException e) {
//      Logger.info("sub_encodedString try catch DID NOT WORK" + e);
//    }
//  }
//
//

//
//  private void bitmapWithoutCompressMethod(Uri imageUri) {
//    try {
//      // Create bitmap from Uri
//      Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
//      googleImageView.setImageBitmap(bitmap);
//
//      new ImageDecodeTask().execute(bitmap);
//
//
//    } catch (FileNotFoundException e) {
//      e.printStackTrace();
//    }
//  }
//
//  private class ImageDecodeTask extends AsyncTask<Bitmap, Void, String> {
//
//    @Override
//    protected String doInBackground(Bitmap... bitmaps) {
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
//
//      return encodedImageString;
//    }
//
//    @Override
//    protected void onPostExecute(String encodedImageString) {
//      super.onPostExecute(encodedImageString);
//
//      // TODO - Upload the Base64 String to Charlie's endpoint
//    }
//  }
}
