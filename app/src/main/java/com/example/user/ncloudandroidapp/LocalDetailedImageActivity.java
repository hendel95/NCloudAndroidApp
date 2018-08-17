package com.example.user.ncloudandroidapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.user.ncloudandroidapp.Model.Item;
import com.example.user.ncloudandroidapp.Model.LocalGalleryItem;
import com.example.user.ncloudandroidapp.Room.FileDatabase;
import com.example.user.ncloudandroidapp.Room.UploadFile;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

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

    public static final String EXTRA_UPLOAD_PHOTO = "LocalDetailedImageActivity";
    OAuthServerIntf server;

    public final String PARCELABLE_ARRAY_LIST = "UploadResultActivity.INTENT";



    @BindView(R.id.local_detailed_photo)
    PhotoView mPhotoView;

    @BindView(R.id.toolbar_detailed_local)
    Toolbar mToolbar;


    @BindView(R.id.gdrive_progressbar)
    ProgressBar mProgressBar;

    @BindView(R.id.progressbar_text)
    TextView mTextView;

    LocalGalleryItem localGalleryItem;
    CustomDateFormat mCustomDateFormat = new CustomDateFormat();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_detailed_image);
        ButterKnife.bind(this);

        mProgressBar.bringToFront();
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
                //upload();
                uploadWithProgressBar();
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
        Log.i(TAG , file.getAbsolutePath());

        if(file.exists()){

            file.delete();
            getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file) ));

        }

    }

    private MultipartBody.Part prepareFilePart(File file, String mimeType){
        final ProgressRequestBody fileBody = new ProgressRequestBody(getApplicationContext(), file, mimeType, new ProgressRequestBody.UploadCallbacks() {

            @Override
            public void onProgressUpdate(int percentage) {
                mProgressBar.setProgress(percentage);

//                mTextView.setText(Integer.toString(percentage));
                //Log.i("TAG"+"progress", Integer.toString(percentage));
            }

            @Override
            public void onError() {

            }

            @Override
            public void onFinish() {
                //Log.i("TAG"+"finished", "finished");
                mProgressBar.setProgress(100);
//                mTextView.setText("100");
            }
        });

        return MultipartBody.Part.createFormData("data", file.getName(), fileBody);
    }

    private void uploadWithProgressBar(){
        File file = new File(localGalleryItem.getPath());

        String content = "{\"name\": \"" + file.getName() + "\"}";
        final ArrayList<LocalGalleryItem> localGalleryItemArrayList = new ArrayList<>();

        //MultipartBody.Part metaPart = MultipartBody.Part.createFormData("metaPart", file.getName(), fileBody);
       // MultipartBody.Part metaPart1 = MultipartBody.Part.create(RequestBody.create(fileBody.contentType(), content));
        RequestBody propertyBody = RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), content );
       //MultipartBody.Part metaPart = MultipartBody.Part.createFormData("metaPart", file.getName(), fileBody.create(fileBody.contentType(), content));

        String mimeType = localGalleryItem.getMimeType();

        //MultipartBody.Part media = MultipartBody.Part.createFormData("file", file.getName(), fileBody);


        //MultipartBody.Part mediaPart = MultipartBody.Part.create(RequestBody.create(MediaType.parse(mimeType), file));

        final MultipartBody.Part dataPart = prepareFilePart(file, mimeType);
        localGalleryItemArrayList.add(localGalleryItem);

        final Call<ResponseBody> galleryItemCall = server.uploadFile(propertyBody,dataPart);
        galleryItemCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                Toast.makeText(LocalDetailedImageActivity.this, "uploading successfully", Toast.LENGTH_SHORT).show();

                Date currentDate = new Date();
                UploadFile uploadFile = new UploadFile(localGalleryItem.getName(), localGalleryItem.getThumbnailPath(), mCustomDateFormat.DateToString(currentDate, Item.ROOM_ITEM_TYPE));
                FileDatabase.getDatabase(getApplicationContext()).getFileDao().insertUploadFile(uploadFile);

                Intent intent = new Intent(getApplicationContext(), UploadResultActivity.class);
                startActivity(intent);

                Log.d("response", response.body().toString());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(LocalDetailedImageActivity.this, "uploading failed", Toast.LENGTH_SHORT).show();

            }
        });



    }

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
    }




}

