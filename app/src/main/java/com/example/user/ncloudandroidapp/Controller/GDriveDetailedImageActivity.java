package com.example.user.ncloudandroidapp.Controller;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.user.ncloudandroidapp.CustomDateFormat;
import com.example.user.ncloudandroidapp.Model.GalleryItem;
import com.example.user.ncloudandroidapp.Model.Item;
import com.example.user.ncloudandroidapp.OAuthHelper;
import com.example.user.ncloudandroidapp.OAuthServerIntf;
import com.example.user.ncloudandroidapp.R;
import com.example.user.ncloudandroidapp.RetrofitBuilder;
import com.example.user.ncloudandroidapp.Room.DownloadFile;
import com.example.user.ncloudandroidapp.Room.FileDatabase;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GDriveDetailedImageActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();


    public static final int OK = 200;
    public static final int CREATED = 201;
    public static final int INCOMPLETE = 308;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int GATEWAY_TIMEOUT = 504;

    public static final String EXTRA_GDRIVE_PHOTO = "GDriveDetailedImageActivity";
    private OAuthServerIntf server;
    private OAuthHelper mOAuthHelper;

    CustomDateFormat mCustomDateFormat = new CustomDateFormat();

    GalleryItem galleryItem;

    @BindView(R.id.gdrive_detailed_photo)
    PhotoView mPhotoView;

    @BindView(R.id.toolbar_detailed_gdrive)
    Toolbar mToolbar;

    @BindView(R.id.bottom_nav_gdrive)
    ConstraintLayout mConstraintLayout;

    @BindView(R.id.nav_download_gdrive)
    ImageButton mDownloadButton;
    @OnClick(R.id.nav_download_gdrive)
    void onDownloadButtonClick(){
        download();
    }

    @BindView(R.id.nav_delete_gdrive)
    ImageButton mDeleteButton;
    @OnClick(R.id.nav_delete_gdrive)
    void onDeleteButtonClick(){
        dialogBuilderDeleteFile();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gdrive_detailed_image);

        ButterKnife.bind(this);

        mOAuthHelper = new OAuthHelper(getApplicationContext());


        setSupportActionBar(mToolbar);

        server = RetrofitBuilder.getOAuthClient(getApplication());

        // Get the ActionBar here to configure the way it behaves.
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); //커스터마이징 하기 위해 필요

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_back);
        mToolbar.bringToFront();

        galleryItem = getIntent().getParcelableExtra(EXTRA_GDRIVE_PHOTO);

        Glide.with(this)
                .load(galleryItem.getThumbnailLink() + "0")
                .apply(RequestOptions.fitCenterTransform().error(R.drawable.error_img)
                        .placeholder(R.drawable.loading_img))
                .into(mPhotoView);

        getSupportActionBar().setTitle(galleryItem.getName());
        mPhotoView.setOnClickListener(new View.OnClickListener() {

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
           /* case R.id.action_download:
                download();
                return true;*/
            case android.R.id.home:
                Toast.makeText(GDriveDetailedImageActivity.this, "Back Button", Toast.LENGTH_LONG).show();
                finish();
                return true;
          /*  case R.id.action_gdrive_delete:
                delete();
                return true;*/
            default:

                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
              //  Toast.makeText(GDriveDetailedImageActivity.this, "Default", Toast.LENGTH_LONG).show();
                //return super.onOptionsItemSelected(item);
                return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_gdrive, menu);
        return true;
    }


    private void download(){
        List<GalleryItem> downloadRequestList = new ArrayList<>();
        downloadRequestList.add(galleryItem);
        Intent intent = new Intent(getApplicationContext(), DownloadResultActivity.class);
        intent.putParcelableArrayListExtra("DOWNLOAD_LIST", (ArrayList<? extends Parcelable>) downloadRequestList);
        startActivity(intent);
    }

    private void delete(){
     //   OAuthServerIntf server = RetrofitBuilder.getOAuthClient(getApplication());
        Call<ResponseBody> responseBodyCall = server.deleteFile(galleryItem.getId());
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.code() == 204){
                    Toast.makeText(getApplicationContext(), "파일 삭제가 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "File deleted successfully!!");
                }
                else{
                    Log.e(TAG, "error caused from deleting function");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "error");
            }
        });
    }


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








