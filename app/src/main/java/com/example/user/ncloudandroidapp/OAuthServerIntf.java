package com.example.user.ncloudandroidapp;

import com.example.user.ncloudandroidapp.Model.GalleryItem;
import com.example.user.ncloudandroidapp.Model.GalleryItems;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface OAuthServerIntf {
    /**
     * The call to request a token
     */
    @FormUrlEncoded
    @POST("oauth2/v4/token")
    Call<OAuthToken> requestTokenForm(
            @Field("code") String code,
            @Field("client_id") String client_id,
//            @Field("client_secret")String client_secret, //Is not relevant for Android application
            @Field("redirect_uri") String redirect_uri,
            @Field("grant_type") String grant_type);

    /**
     * The call to refresh a token
     */
    @FormUrlEncoded
    @POST("oauth2/v4/token")
    Call<OAuthToken> refreshTokenForm(
            @Field("refresh_token") String refresh_token,
            @Field("client_id") String client_id,
//            @Field("client_secret")String client_secret, //Is not relevant for Android application
            @Field("grant_type") String grant_type);

    //@POST("/upload/drive/v3/files?uploadType=media")


    /**
     * The call to retrieve the files of our User in GDrive
     */




    /*Multipart Upload*/
    @Multipart
    @POST("upload/drive/v3/files?uploadType=multipart")
    Call<ResponseBody> uploadFile(
            @Part MultipartBody.Part metaPart,
            @Part MultipartBody.Part mediaPart
    );

    /*Resumable Upload*/
    @POST("upload/drive/v3/files?uploadType=resumable")
    Call<ResponseBody> resumableUpload(
            @Header("X-Upload-Content-Type") String x_upload_content_type,
            @Header("X-Upload-Content-Length") String x_upload_content_length,
            @Header("Content-Type") String content_type,
            @Header("Content-Length") String content_length,
            @Body RequestBody requestBody
    );

    @PUT("upload/drive/v3/files?uploadType=resumable")
    Call<ResponseBody> sendSessionUri(
            @Query("upload_id") String upload_id,
            @Header("Content-Type") String content_type,
            @Header("Content-Length") String content_length
    );


    @POST("upload/drive/v3/files?uploadType=multipart")
    Call<ResponseBody> uploadMultipleFilesDynamic(
            @Part List<MultipartBody.Part> files
    );

    @Streaming
    @GET("drive/v3/files/{fileId}?alt=media")
    Call<ResponseBody> downloadFile(
            @Path("fileId") String fileId
    );

    @DELETE("drive/v3/files/{fileId}")
    Call<ResponseBody> deleteFile(
            @Path("fileId") String fileId
    );

    @GET("drive/v3/files")
    Call<GalleryItems> getFileDescription(
            @Query("fields") String fields,
            @Query("q") String q,
            @Query("orderBy") String orderBy,
            @Query("pageSize") Integer pageSize,
            @Query("pageToken") String pageToken
    );


}
