package com.example.user.ncloudandroidapp.Controller;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.user.ncloudandroidapp.CustomDateFormat;
import com.example.user.ncloudandroidapp.Model.Item;
import com.example.user.ncloudandroidapp.Model.LocalGalleryItem;
import com.example.user.ncloudandroidapp.OAuthHelper;
import com.example.user.ncloudandroidapp.OAuthServerIntf;
import com.example.user.ncloudandroidapp.ProgressRequestBody;
import com.example.user.ncloudandroidapp.R;
import com.example.user.ncloudandroidapp.RetrofitBuilder;
import com.example.user.ncloudandroidapp.Room.FileDatabase;
import com.example.user.ncloudandroidapp.Room.UploadFile;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lombok.ToString;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocalDetailedImageActivity extends AppCompatActivity {

    public static final String EXTRA_LOCAL_PHOTO = "LocalDetailedImageActivity";
    private final String TAG = getClass().getSimpleName();

    public static final String EXTRA_UPLOAD_PHOTO = "LocalDetailedImageActivity";
    OAuthServerIntf server;

    public final String PARCELABLE_ARRAY_LIST = "UploadResultActivity.INTENT";

    OAuthHelper mOAuthHelper;

    public static final int OK = 200;
    public static final int CREATED = 201;
    public static final int INCOMPLETE = 308;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int PAYMENT_REQUIRED = 402;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int INTERNAL = 500;
    public static final int BAD_GATEWAY = 502;
    public static final int SERVICE_UNAVAILABLE = 503;
    public static final int GATEWAY_TIMEOUT = 504;

    @BindView(R.id.local_detailed_photo)
    PhotoView mPhotoView;

    @BindView(R.id.toolbar_detailed_local)
    Toolbar mToolbar;


    //@BindView(R.id.gdrive_progressbar)
    //ProgressBar mProgressBar;

    @BindView(R.id.progressbar_text)
    TextView mTextView;

    @BindView(R.id.bottom_nav_local)
    ConstraintLayout mConstraintLayout;

    @BindView(R.id.nav_upload_local)
    ImageButton mDownloadButton;
    @OnClick(R.id.nav_upload_local)
    void onUploadButtonClick(){
        upload();
       // uploadWithProgressBar();
    }

    @BindView(R.id.nav_delete_local)
    ImageButton mDeleteButton;
    @OnClick(R.id.nav_delete_local)
    void onDeleteButtonClick(){
        dialogBuilderDeleteFile();
    }


    LocalGalleryItem localGalleryItem;
    CustomDateFormat mCustomDateFormat = new CustomDateFormat();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_detailed_image);
        ButterKnife.bind(this);

        mOAuthHelper = new OAuthHelper(getApplicationContext());
     //   mProgressBar.bringToFront();
        mTextView.bringToFront();

        setSupportActionBar(mToolbar);
        server = RetrofitBuilder.getOAuthClient(getApplication());

        // Get the ActionBar here to configure the way it behaves.
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); //커스터마이징 하기 위해 필요

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_back);

        mToolbar.bringToFront();
        localGalleryItem = getIntent().getParcelableExtra(EXTRA_LOCAL_PHOTO);

        Glide.with(this)
                .load(localGalleryItem.getPath())
                .apply(RequestOptions.fitCenterTransform().error(R.drawable.error_img)
                        .placeholder(R.drawable.loading_img))
                .into(mPhotoView);

        getSupportActionBar().setTitle(localGalleryItem.getName());
        mPhotoView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if (mToolbar.getVisibility() == View.GONE) {
                    mToolbar.setVisibility(View.VISIBLE);
                    mConstraintLayout.setVisibility(View.VISIBLE);
                } else {
                    mToolbar.setVisibility(View.GONE);
                    mConstraintLayout.setVisibility(View.GONE);
                }
            }
        });


    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_local, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
           /* case R.id.action_upload:
                //upload();
                uploadWithProgressBar();
                return true;*/
            case android.R.id.home:
                Toast.makeText(LocalDetailedImageActivity.this, "Back Button", Toast.LENGTH_LONG).show();
                finish();
                return true;
            /*case R.id.action_local_delete:
                delete();
                return true;*/
            default:

                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
             //   Toast.makeText(LocalDetailedImageActivity.this, "Default", Toast.LENGTH_LONG).show();
                //return super.onOptionsItemSelected(item);
                return true;
        }
    }

    private void delete(){
        //String file_path = Environment.getExternalStorageDirectory() + File.separator + localGalleryItem.getName();
        File file = new File(localGalleryItem.getPath());
        Log.i(TAG , file.getAbsolutePath());

        if(file.exists()){

            file.delete();
            getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file) ));
            Toast.makeText(getApplicationContext(), "사진 삭제를 완료하였습니다.", Toast.LENGTH_SHORT).show();
        }

    }

    private MultipartBody.Part prepareFilePart(File file, String mimeType){
        final ProgressRequestBody fileBody = new ProgressRequestBody(getApplicationContext(), file, mimeType, new ProgressRequestBody.UploadCallbacks() {

            @Override
            public void onProgressUpdate(int percentage) {
                //mProgressBar.setProgress(percentage);

//                mTextView.setText(Integer.toString(percentage));
                //Log.i("TAG"+"progress", Integer.toString(percentage));
            }

            @Override
            public void onError() {

            }

            @Override
            public void onFinish() {

                //Log.i("TAG"+"finished", "finished");
                //mProgressBar.setProgress(100);
//                mTextView.setText("100");
            }
        });

        return MultipartBody.Part.createFormData("data", file.getName(), fileBody);
    }

    private void upload(){
        List<LocalGalleryItem> listItem = new ArrayList<>();
        localGalleryItem.setResult(Item.DOWNLOAD_BEFORE);
        listItem.add(localGalleryItem);

        Intent intent = new Intent(getApplicationContext(), UploadResultActivity.class);
        intent.putParcelableArrayListExtra("UPLOAD_LIST", (ArrayList<? extends Parcelable>) listItem);
        startActivity(intent);
    }
/*
    private void uploadWithProgressBar(){
        File file = new File(localGalleryItem.getPath());

        String content = "{\"name\": \"" + file.getName() + "\"}";
       // final ArrayList<LocalGalleryItem> localGalleryItemArrayList = new ArrayList<>();

        RequestBody propertyBody = RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), content );
        String mimeType = localGalleryItem.getMimeType();

        final MultipartBody.Part dataPart = prepareFilePart(file, mimeType);
        //localGalleryItemArrayList.add(localGalleryItem);

        Toast.makeText(getApplicationContext(), "사진 업로드 중입니다.", Toast.LENGTH_SHORT).show();
        final Call<ResponseBody> galleryItemCall = server.uploadFile(propertyBody,dataPart);
        galleryItemCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                if(response.body() != null) {
                    if (response.code() == OK) { //200
                        //Toast.makeText(LocalDetailedImageActivity.this, "uploading successfully", Toast.LENGTH_SHORT).show();

                        Date currentDate = new Date();
                        UploadFile uploadFile = new UploadFile(localGalleryItem.getName(), localGalleryItem.getThumbnailPath(), mCustomDateFormat.DateToString(currentDate, Item.ROOM_ITEM_TYPE));
                        FileDatabase.getDatabase(getApplicationContext()).getFileDao().insertUploadFile(uploadFile);

                        //Intent intent = new Intent(getApplicationContext(), UploadResultActivity.class);
                        //startActivity(intent);

                        Log.d("response", response.body().toString());

                    }
                    else if(response.code() == FORBIDDEN){ //403
                        Toast.makeText(getApplicationContext(), R.string.http_code_403, Toast.LENGTH_SHORT).show();
                    }
                    else if(response.code() == INTERNAL){ //500
                        Toast.makeText(getApplicationContext(), R.string.http_code_403, Toast.LENGTH_SHORT).show();
                    }
                    else if(response.code() == BAD_GATEWAY){ //502
                        Toast.makeText(getApplicationContext(), R.string.http_code_403, Toast.LENGTH_SHORT).show();
                    }
                    else if(response.code() == SERVICE_UNAVAILABLE){ //503
                        Toast.makeText(getApplicationContext(), R.string.http_code_403, Toast.LENGTH_SHORT).show();
                    }
                    else if(response.code() == GATEWAY_TIMEOUT){ //504
                        Toast.makeText(getApplicationContext(), R.string.http_code_504, Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Cannot upload image", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(LocalDetailedImageActivity.this, "uploading failed", Toast.LENGTH_SHORT).show();

            }
        });



    }*/
/*
    private void upload() {

        File file = new File(localGalleryItem.getPath());

        MediaType contentType = MediaType.parse("application/json; charset=UTF-8");
        String content = "{\"name\": \"" + file.getName() + "\"}";

        MultipartBody.Part metaPart = MultipartBody.Part.create(RequestBody.create(contentType, content));
        String mimeType = localGalleryItem.getMimeType();
        MultipartBody.Part mediaPart = MultipartBody.Part.create(RequestBody.create(MediaType.parse(mimeType), file));

        //OAuthServerIntf server = RetrofitBuilder.getOAuthClient(getApplication());
        final Call<ResponseBody> galleryItemCall = server.uploadMultipleFiles(metaPart, mediaPart);
        galleryItemCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Toast.makeText(LocalDetailedImageActivity.this, "uploading successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(LocalDetailedImageActivity.this, "uploading failed", Toast.LENGTH_SHORT).show();

            }
        });
    }*/

    private void dialogBuilderDeleteFile() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("삭제");
        builder.setMessage(R.string.delete_dialog);
        builder.setPositiveButton(R.string.ok_dialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "사진을 삭제 중입니다.", Toast.LENGTH_LONG).show();

                delete();
            }
        });

        builder.setNegativeButton(R.string.cancel_dialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
    }


}

