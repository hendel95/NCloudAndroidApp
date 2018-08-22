package com.example.user.ncloudandroidapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import okhttp3.Connection;
import okhttp3.HttpUrl;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OAuthHelper {

    // you should either define client id and secret as constants or in string resources
    private final String CLIENT_ID = "610143887746-cuiinmh0rv9beoasd74gdcmuljhtcp4c.apps.googleusercontent.com";

    //private final String clientSecret = "your-client-secret";
    private static final String CODE = "code";

    private final String REDIRECT_URI = "com.example.user.ncloudandroidapp:/redirecturi";
    private static final String REDIRECT_URI_ROOT = "com.example.user.ncloudandroidapp";

    public static final String API_SCOPE = "https://www.googleapis.com/auth/drive";

    private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";

    private static final String USER_FIELD = "user/displayName, user/emailAddress";
    /**
     * GrantType:You are using a refresh_token when retrieveing the token
     */
    public static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";

    private final String TAG = "OAuthHelper";

    private String code;
    private Context mContext;

    OAuthServerIntf mOAuthServerIntf;

    public OAuthHelper(Context context) {
        mContext = context;
        mOAuthServerIntf =  RetrofitBuilder.getSimpleClient(context);
        checkTokenValied();

    }

    private boolean checkTokenValied(){
        OAuthToken oauthToken=OAuthToken.Factory.create();
        if (oauthToken==null||oauthToken.getAccessToken()==null) {
            //first case==first token request


            if(oauthToken==null||oauthToken.getRefreshToken()==null){
                Log.e(TAG, "onCreate: Launching authorization (first step)");
                //first step of OAUth: the authorization step
                makeAuthorizationRequest();
            }else{
                Log.e(TAG, "onCreate: refreshing the token :" + oauthToken);
                //refresh token case
                refreshTokenFormUrl(oauthToken);
            }

            return false;
        }
        //else just launch your MainActivity
        else {

            Log.e(TAG, "onCreate: Token available");
            return true;
        }
    }


    protected void makeAuthorizationRequest() {
        HttpUrl authorizeUrl = HttpUrl.parse("https://accounts.google.com/o/oauth2/v2/auth") //
                .newBuilder() //
                .addQueryParameter("client_id", CLIENT_ID)
                .addQueryParameter("scope", API_SCOPE)
                .addQueryParameter("redirect_uri", REDIRECT_URI)
                .addQueryParameter("response_type", CODE)
                .build();


        Intent i = new Intent(Intent.ACTION_VIEW);
        Log.e(TAG, "the url is : " + String.valueOf(authorizeUrl.url()));
        i.setData(Uri.parse(String.valueOf(authorizeUrl.url())));
        i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(i);
        //finish();
    }

    /**
     * Refresh the OAuth token
     */
    protected void refreshTokenFormUrl(OAuthToken oauthToken) {
        // OAuthServerIntf oAuthServer = RetrofitBuilder.getSimpleClient(this);
        Call<OAuthToken> refreshTokenFormCall = mOAuthServerIntf.refreshTokenForm(
                oauthToken.getRefreshToken(),
                CLIENT_ID,
                GRANT_TYPE_REFRESH_TOKEN
        );
        refreshTokenFormCall.enqueue(new Callback<OAuthToken>() {
            @Override
            public void onResponse(Call<OAuthToken> call, Response<OAuthToken> response) {
                Log.e(TAG, "===============New Call==========================");
                Log.e(TAG, "The call refreshTokenFormUrl succeed with code=" + response.code() + " and has body = " + response.body());
                //ok we have the token
                response.body().save();
                //startMainActivity(true);
            }

            @Override
            public void onFailure(Call<OAuthToken> call, Throwable t) {
                Log.e(TAG, "===============New Call==========================");
                Log.e(TAG, "The call refreshTokenFormCall failed", t);

            }
        });
    }

    /**
     * Retrieve the OAuth token
     */
    private void getTokenFormUrl() {
        //OAuthServerIntf oAuthServer = RetrofitBuilder.getSimpleClient(this);
        Call<OAuthToken> getRequestTokenFormCall = mOAuthServerIntf.requestTokenForm(
                code,
                CLIENT_ID,
                REDIRECT_URI,
                GRANT_TYPE_AUTHORIZATION_CODE
        );
        getRequestTokenFormCall.enqueue(new Callback<OAuthToken>() {
            @Override
            public void onResponse(Call<OAuthToken> call, Response<OAuthToken> response) {
                Log.e(TAG, "===============New Call==========================");
                Log.e(TAG, "The call getRequestTokenFormCall succeed with code=" + response.code() + " and has body = " + response.body());
                //ok we have the token
                response.body().save();
                //startMainActivity(true);
            }

            @Override
            public void onFailure(Call<OAuthToken> call, Throwable t) {
                Log.e(TAG, "===============New Call==========================");
                Log.e(TAG, "The call getRequestTokenFormCall failed", t);

            }
        });
    }

}
