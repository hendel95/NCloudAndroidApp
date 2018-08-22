package com.example.user.ncloudandroidapp;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.example.user.ncloudandroidapp.Controller.MainActivity;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class OAuthInterceptor implements Interceptor{
    private static final String TAG = "OAuthInterceptor";
    private String accessToken,accessTokenType;
    @Override
    public Response intercept(Chain chain) throws IOException {
        //find the token
        OAuthToken oauthToken = OAuthToken.Factory.create();
        accessToken=oauthToken.getAccessToken();
        accessTokenType=oauthToken.getTokenType();
        //add it to the request
        Request.Builder builder = chain.request().newBuilder();
        if (!TextUtils.isEmpty(accessToken)&&!TextUtils.isEmpty(accessTokenType)) {
            Log.e(TAG,"In the interceptor adding the header authorization with : "+accessTokenType+" " + accessToken);
            builder.header("Authorization",accessTokenType+" " + accessToken);
        }else{
            Log.e(TAG,"문제가 발생하였습니다 : "+accessTokenType+" " + accessToken);
            //you should launch the loginActivity to fix that:
            Intent i = new Intent(MyApplication.instance, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            MyApplication.instance.startActivity(i);
        }
        //proceed to the call
        return chain.proceed(builder.build());
    }

}
