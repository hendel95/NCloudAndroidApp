package com.example.user.ncloudandroidapp;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.user.ncloudandroidapp.Model.GalleryItem;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GDriveDetailedImageActivity extends AppCompatActivity {
    public static final String EXTRA_GDRIVE_PHOTO = "GDriveDetailedImageActivity";
    private static final String TAG = "GDriveDetailedImageActivity";
    GalleryItem galleryItem;

    @BindView(R.id.gdrive_detailed_photo)
    PhotoView mPhotoView;

    @BindView(R.id.toolbar_detailed_gdrive)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gdrive_detailed_image);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

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
                } else {
                    mToolbar.setVisibility(View.GONE);
                }
            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_download:
                download();
                return true;
            case android.R.id.home:
                Toast.makeText(GDriveDetailedImageActivity.this, "Back Button", Toast.LENGTH_LONG).show();
                finish();
                return true;
            case R.id.action_gdrive_delete:
                delete();
                return true;
            default:

                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                Toast.makeText(GDriveDetailedImageActivity.this, "Default", Toast.LENGTH_LONG).show();
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

    public void download() {

        OAuthServerIntf server = RetrofitBuilder.getOAuthClient(getApplication());
        Call<ResponseBody> responseBodyCall = server.downloadFile(galleryItem.getId());
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                if (response.code() == 200 && response.body() != null) {
                    Log.d(TAG, "server contacted and has file");

                    new AsyncTask<Void, Void, Void>(){
                        @Override
                        protected Void doInBackground(Void... voids){
                            boolean writtenToDisk = writeResponseBodyToDisk(response.body());

                            Log.d(TAG, "file download was a success? " + writtenToDisk);
                            return null;
                        }
                    }.execute();


                    //response.body()
                } else {
                    Log.d(TAG, "server contact failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "error");
            }
        });

    }

    private boolean writeResponseBodyToDisk(ResponseBody body) {

        try {
            //새로운 Directory 생성
            String file_url = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "GDRIVE" + File.separator;


            File dir = new File(file_url);
            //directory 없으면 생성
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = galleryItem.getName();
            String localPath = file_url + File.separator + fileName;

            //Local Path에 파일 생성

            File downloadFile = new File(localPath);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(downloadFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                }


                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA,
                       localPath);
                values.put(MediaStore.Images.Media.MIME_TYPE, galleryItem.getMimeType());
                //values.put(MediaStore.Images.Media.DATE_ADDED, galleryItem.getCreatedTime());
                getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }


    private void delete(){
        OAuthServerIntf server = RetrofitBuilder.getOAuthClient(getApplication());
        Call<ResponseBody> responseBodyCall = server.deleteFile(galleryItem.getId());
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.code() == 204){
                    Log.i(TAG, "File deleted sucessfully!!");
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

}








