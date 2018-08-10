package com.example.user.ncloudandroidapp;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ActionMenuView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.user.ncloudandroidapp.Adapter.GDriveRecyclerViewAdapter;
import com.example.user.ncloudandroidapp.Model.GalleryItem;
import com.example.user.ncloudandroidapp.Model.LocalGalleryItem;
import com.github.chrisbanes.photoview.PhotoView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocalDetailedImageActivity extends AppCompatActivity {

    public static final String EXTRA_LOCAL_PHOTO = "LocalDetailedImageActivity";
    public static final String TAG = "LocalDetailedImageActivity";
    OAuthServerIntf server;

    @BindView(R.id.local_detailed_photo)
    PhotoView mPhotoView;

    @BindView(R.id.toolbar_detailed_local)
    Toolbar mToolbar;

    LocalGalleryItem localGalleryItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_detailed_image);
        ButterKnife.bind(this);

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
                } else {
                    mToolbar.setVisibility(View.GONE);
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
            case R.id.action_upload:
                upload();
                return true;
            case android.R.id.home:
                Toast.makeText(LocalDetailedImageActivity.this, "Back Button", Toast.LENGTH_LONG).show();
                finish();
                return true;
            case R.id.action_local_delete:
                delete();
                return true;
            default:

                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                Toast.makeText(LocalDetailedImageActivity.this, "Default", Toast.LENGTH_LONG).show();
                //return super.onOptionsItemSelected(item);
                return true;
        }
    }

    private void delete(){
        //String file_path = Environment.getExternalStorageDirectory() + File.separator + localGalleryItem.getName();
        File file = new File(localGalleryItem.getPath());
        Log.i(TAG, file.getAbsolutePath());

        if(file.exists()){

            file.delete();
            getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file) ));

        }

    }

    private void upload() {

        File file = new File(localGalleryItem.getPath());

        MediaType contentType = MediaType.parse("application/json; charset=UTF-8");
        String content = "{\"name\": \"" + file.getName() + "\"}";

        MultipartBody.Part metaPart = MultipartBody.Part.create(RequestBody.create(contentType, content));
        String mineType = localGalleryItem.getMimeType();
        MultipartBody.Part mediaPart = MultipartBody.Part.create(RequestBody.create(MediaType.parse(mineType), file));

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
    }




}

