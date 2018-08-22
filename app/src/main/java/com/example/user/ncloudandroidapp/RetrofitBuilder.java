package com.example.user.ncloudandroidapp;

import android.content.Context;
import android.support.annotation.NonNull;


import java.io.File;
import java.net.CookieHandler;
import java.net.CookieManager;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class RetrofitBuilder {

    /***********************************************************
     *  Constants
     **********************************************************/
    /**
     * Root URL
     * (always ends with a /)
     */
    public static final String BASE_URL = "https://www.googleapis.com/";

    /***********************************************************
     * Getting OAuthServerIntf instance using Retrofit creation
     **********************************************************/
    /**
     * A basic client to make unauthenticated calls
     * @param ctx
     * @return OAuthServerIntf instance
     */
    public static OAuthServerIntf getSimpleClient(Context ctx) {
        //Using Default HttpClient
        Retrofit retrofit = new Retrofit.Builder()
                .client(getSimpleOkHttpClient(ctx))
                .addConverterFactory(new StringConverterFactory())
                .addConverterFactory(MoshiConverterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        OAuthServerIntf webServer = retrofit.create(OAuthServerIntf.class);
        return webServer;
    }

    /**
     * An autenticated client to make authenticated callsgoo
     * The token is automaticly added in the Header of the request
     * @param ctx
     * @return OAuthServerIntf instance
     */
    public static OAuthServerIntf getOAuthClient(Context ctx) {
        // now it's using the cach
        // Using my HttpClient
        Retrofit raCustom = new Retrofit.Builder()
                .client(getOAuthOkHttpClient(ctx))
                .baseUrl(BASE_URL)
                .addConverterFactory(new StringConverterFactory())
                .addConverterFactory(MoshiConverterFactory.create())
                .build();
        OAuthServerIntf webServer = raCustom.create(OAuthServerIntf.class);
        return webServer;
    }

    /***********************************************************
     * OkHttp Clients
     **********************************************************/

    /**
     * Return a simple OkHttpClient v:
     * have a cache
     * have a HttpLogger
     */
    @NonNull
    public static OkHttpClient getSimpleOkHttpClient(Context ctx) {
        // Define the OkHttp Client with its cache!
        // Assigning a CacheDirectory
        File myCacheDir=new File(ctx.getCacheDir(),"OkHttpCache");
        // You should create it...
        int cacheSize=1024*1024;
        Cache cacheDir=new Cache(myCacheDir,cacheSize);


        HttpLoggingInterceptor httpLogInterceptor=new HttpLoggingInterceptor();
        httpLogInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        return new OkHttpClient.Builder()
                //add a cache
                .cache(cacheDir)
                .addInterceptor(httpLogInterceptor)
                .build();
    }

    /**
     * Return a OAuth OkHttpClient v:
     * have a cache
     * have a HttpLogger
     * add automaticly the token in the header of each request because of the oAuthInterceptor
     * @param ctx
     * @return
     */
    @NonNull
    public static OkHttpClient getOAuthOkHttpClient(Context ctx) {
        // Define the OkHttp Client with its cache!
        // Assigning a CacheDirectory

        CookieHandler cookieHandler = new CookieManager();

        File myCacheDir=new File(ctx.getCacheDir(),"OkHttpCache");
        // You should create it...
        int cacheSize=1024*1024;
        Cache cacheDir=new Cache(myCacheDir,cacheSize);
        Interceptor oAuthInterceptor=new OAuthInterceptor();
        HttpLoggingInterceptor httpLogInterceptor=new HttpLoggingInterceptor();
        httpLogInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        return new OkHttpClient.Builder()
                .cache(cacheDir)
                .cookieJar(new JavaNetCookieJar(cookieHandler))
                .addInterceptor(oAuthInterceptor)
                .addInterceptor(httpLogInterceptor)
                .build();
    }

}
