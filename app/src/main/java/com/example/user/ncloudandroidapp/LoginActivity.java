package com.example.user.ncloudandroidapp;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.media.MediaPlayer.MetricsConstants.ERROR_CODE;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    // you should either define client id and secret as constants or in string resources
    private final String CLIENT_ID = "610143887746-cuiinmh0rv9beoasd74gdcmuljhtcp4c.apps.googleusercontent.com";
    //private final String clientSecret = "your-client-secret";
    private static final String CODE = "code";
    private final String REDIRECT_URI = "com.example.user.ncloudandroidapp:/redirecturi";
    private static final String REDIRECT_URI_ROOT = "com.example.user.ncloudandroidapp";
    public static final String API_SCOPE = "https://www.googleapis.com/auth/drive";

    private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
    /**
     * GrantType:You are using a refresh_token when retrieveing the token
     */
    public static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
    private String code;
    /**
     * The error returned by the server at the authorization's first step
     */
    private String error;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //You can be created either because your user start your application
        //        <intent-filter>
        //            <action android:name="android.intent.action.MAIN" />
        //            <category android:name="android.intent.category.LAUNCHER" />
        //        </intent-filter>
        //either because the callBack of the Authorisation is called :
        //        <intent-filter>
        //            <action android:name="android.intent.action.VIEW" />
        //            <category android:name="android.intent.category.BROWSABLE" />
        //            <category android:name="android.intent.category.DEFAULT" />
        //            <data android:scheme="com.renaultnissan.acms.platform.oauth.githubsample" />
        //        </intent-filter>

        //Manage the callback case:
        Uri data = getIntent().getData();
        if (data != null && !TextUtils.isEmpty(data.getScheme())) {
            if (REDIRECT_URI_ROOT.equals(data.getScheme())) {
                code = data.getQueryParameter(CODE);
                error=data.getQueryParameter(ERROR_CODE);
                Log.e(TAG, "onCreate: handle result of authorization with code :" + code);
                if (!TextUtils.isEmpty(code)) {
                    getTokenFormUrl();
                }
                if(!TextUtils.isEmpty(error)) {
                    //a problem occurs, the user reject our granting request or something like that
                    Toast.makeText(this, R.string.loginactivity_grantsfails_quit,Toast.LENGTH_LONG).show();
                    Log.e(TAG, "onCreate: handle result of authorization with error :" + error);
                    //then die
                    finish();
                }
            }
        } else {
            //Manage the start application case:
            //If you don't have a token yet or if your token has expired , ask for it
            OAuthToken oauthToken=OAuthToken.Factory.create();
            if (oauthToken==null
                    ||oauthToken.getAccessToken()==null) {
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
            }
            //else just launch your MainActivity
            else {
                Log.e(TAG, "onCreate: Token available, just launch MainActivity");
                startMainActivity(false);
            }
        }
    }

    /***********************************************************
     *  Managing Authotization and Token process
     **********************************************************/

    /**
     * Make the Authorization request
     */
    private void makeAuthorizationRequest() {
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
        startActivity(i);
        finish();
    }

    /**
     * Refresh the OAuth token
     */
    private void refreshTokenFormUrl(OAuthToken oauthToken) {
        OAuthServerIntf oAuthServer = RetrofitBuilder.getSimpleClient(this);
        Call<OAuthToken> refreshTokenFormCall = oAuthServer.refreshTokenForm(
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
                startMainActivity(true);
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
        OAuthServerIntf oAuthServer = RetrofitBuilder.getSimpleClient(this);
        Call<OAuthToken> getRequestTokenFormCall = oAuthServer.requestTokenForm(
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
                startMainActivity(true);
            }

            @Override
            public void onFailure(Call<OAuthToken> call, Throwable t) {
                Log.e(TAG, "===============New Call==========================");
                Log.e(TAG, "The call getRequestTokenFormCall failed", t);

            }
        });
    }

    /***********************************************************
     *  Others business methods
     **********************************************************/

    /**
     * Start the next activity
     */
    private void startMainActivity(boolean newtask) {
        Intent i = new Intent(this, MainActivity.class);
        if(newtask){
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        startActivity(i);
        //you can die so
        finish();
    }

}
