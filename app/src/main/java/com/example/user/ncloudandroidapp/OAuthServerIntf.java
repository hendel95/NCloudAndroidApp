package com.example.user.ncloudandroidapp;

import com.example.user.ncloudandroidapp.Model.GalleryItems;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface OAuthServerIntf {
    /**
     * The call to request a token
     */
    @FormUrlEncoded
    @POST("oauth2/v4/token")
    Call<OAuthToken> requestTokenForm(
            @Field("code")String code,
            @Field("client_id")String client_id,
//            @Field("client_secret")String client_secret, //Is not relevant for Android application
            @Field("redirect_uri")String redirect_uri,
            @Field("grant_type")String grant_type);

    /**
     * The call to refresh a token
     */
    @FormUrlEncoded
    @POST("oauth2/v4/token")
    Call<OAuthToken> refreshTokenForm(
            @Field("refresh_token")String refresh_token,
            @Field("client_id")String client_id,
//            @Field("client_secret")String client_secret, //Is not relevant for Android application
            @Field("grant_type")String grant_type);
    /**
     * The call to retrieve the files of our User in GDrive
     */


    @GET("drive/v3/files")
    Call<GalleryItems> getFileDescription(
            @Query("fields") String fields,
            @Query("q") String q,
            @Query("orderBy") String orderBy,
            @Query("pageSize") Integer pageSize,
            @Query("pageToken") String pageToken
  );

}
