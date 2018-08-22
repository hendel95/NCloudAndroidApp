package com.example.user.ncloudandroidapp;

import com.example.user.ncloudandroidapp.Model.GalleryItems;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
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


    @GET("auth/drive")
    Call<ResponseBody> getUserInfo(
            @Query("fields") String fields
    );


    /*

    @Multipart
    @POST("upload/drive/v3/files?uploadType=multipart")
    Call<ResponseBody> uploadMultipleFilesDynamic(

            //@PartMap Map<String, String> partMap
            @Part List<MultipartBody.Part> metaPartList
            //@Part List<MultipartBody.Part> mediaPartList
    );
*/

    /*Multipart Upload*/
   // @Headers({"Content-Type: application/json; charset=UTF-8"})

    @Multipart
    @POST("upload/drive/v3/files?uploadType=multipart")
    Call<ResponseBody> uploadMultipleFiles(
            @Part MultipartBody.Part metaPart,
            @Part MultipartBody.Part mediaPart
    );


    //upload -> multipart
    @Multipart
    @POST("upload/drive/v3/files?uploadType=multipart")
    Call<ResponseBody> uploadFile(
            @Part("description") RequestBody description,
            @Part MultipartBody.Part dataPart
    );



    //download
    @Streaming
    @GET("drive/v3/files/{fileId}?alt=media")
    Call<ResponseBody> downloadFile(
            @Path("fileId") String fileId
    );

    //delete
    @DELETE("drive/v3/files/{fileId}")
    Call<ResponseBody> deleteFile(
            @Path("fileId") String fileId
    );

    //get files from gdrive
    @GET("drive/v3/files")
    Call<GalleryItems> getFileDescription(
            @Query("fields") String fields,
            @Query("q") String q,
            @Query("orderBy") String orderBy,
            @Query("pageSize") Integer pageSize,
            @Query("pageToken") String pageToken
    );


}
