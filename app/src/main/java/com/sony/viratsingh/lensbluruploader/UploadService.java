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


//  @Headers({"Content-Type: application/octet-stream"})
//  @Multipart @POST("/")
//  Response uploadImage(@Part("image") TypedFile image);

  @Headers({"Content-Type: application/octet-stream"})
  @FormUrlEncoded @POST("/")
  Response uploadImage(@Field("fileUpload") TypedFile image);
}
