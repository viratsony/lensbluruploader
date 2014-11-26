package com.sony.viratsingh.lensbluruploader;

import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.Headers;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedFile;

public interface UploadService {


//  @Headers({"Content-Type: multipart/form-data; boundary=***"})
  @Multipart @POST("/upload")
  Response uploadImage(@Part("image") TypedFile image);

//  @Headers({"Content-Type: multipart/form-data; boundary=***"})
//  @FormUrlEncoded @POST("/upload/")
//  Response uploadImage(@Field("fileUpload") TypedFile image);
}
