package com.example.user.ncloudandroidapp.Controller;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.ncloudandroidapp.Adapter.DownloadResultRecyclerViewAdapter;
import com.example.user.ncloudandroidapp.CustomDateFormat;
import com.example.user.ncloudandroidapp.Model.GalleryItem;
import com.example.user.ncloudandroidapp.Model.HeaderItem;
import com.example.user.ncloudandroidapp.Model.Item;
import com.example.user.ncloudandroidapp.Model.LocalGalleryItem;
import com.example.user.ncloudandroidapp.OAuthHelper;
import com.example.user.ncloudandroidapp.OAuthServerIntf;
import com.example.user.ncloudandroidapp.R;
import com.example.user.ncloudandroidapp.RetrofitBuilder;
import com.example.user.ncloudandroidapp.Room.DownloadFile;
import com.example.user.ncloudandroidapp.Room.FileDatabase;

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

public class DownloadResultActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    private final int OK = 200;


    private OAuthHelper mOAuthHelper;
    private OAuthServerIntf server;

    @BindView(R.id.download_recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.nav_delete_download)
    ImageButton mImageButton;

    @OnClick(R.id.nav_delete_download)
    public void deleteButtonClick() {
        finish();
    }

    @BindView(R.id.nav_remove_all_download)
    Button mButton;

    @OnClick(R.id.nav_remove_all_download)
    public void onDeleteButtonClick() {
        dialogBuilderDeleteAllFiles();

    }

    DownloadResultRecyclerViewAdapter mDownloadResultRecyclerViewAdapter;
    List<Item> mItemArrayList = new ArrayList<>();

    List<GalleryItem> downloadRequestList;

    List<DownloadFile> downloadFileList;

    CustomDateFormat mCustomDateFormat = new CustomDateFormat();

    Date currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_result);
        ButterKnife.bind(this);

        server = RetrofitBuilder.getOAuthClient(getApplication());

        mOAuthHelper = new OAuthHelper(getApplicationContext());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getApplicationContext(),
                LinearLayoutManager.VERTICAL);
        dividerItemDecoration.setDrawable(getApplicationContext().getResources().getDrawable(R.drawable.custom_decorator));

        mDownloadResultRecyclerViewAdapter = new DownloadResultRecyclerViewAdapter(getApplicationContext());
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mDownloadResultRecyclerViewAdapter);



        downloadRequestList = getIntent().getParcelableArrayListExtra("DOWNLOAD_LIST");

        if (downloadRequestList != null) {

            currentDate = new Date();

            Toast.makeText(getApplicationContext(), "사진 다운로드 중입니다.", Toast.LENGTH_SHORT).show();
            for (GalleryItem item : downloadRequestList) {
                download(item);
            }
            mItemArrayList.clear();
        }else{
            loadFilesFromDatabase();
        }

        //loadFilesFromDatabase();

    }


    public void clear() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FileDatabase.getDatabase(getApplicationContext()).getFileDao().clearAllDownloadedFiles();
                mDownloadResultRecyclerViewAdapter.clear();
                mDownloadResultRecyclerViewAdapter.notifyDataSetChanged();
            }
        }, 1000);
    }

    private void loadFilesFromDatabase() {

        downloadFileList = FileDatabase.getDatabase(getApplicationContext()).getFileDao().getAllDownloadedFiles();

        if (!downloadFileList.isEmpty()) {
            mDownloadResultRecyclerViewAdapter.clear();

            String tempDate = downloadFileList.get(0).getDate();

            mItemArrayList.add(new HeaderItem(mCustomDateFormat.dateFormatting(tempDate, Item.ROOM_ITEM_TYPE)));

            for (DownloadFile downloadFile : downloadFileList) {
                GalleryItem item = new GalleryItem();
                item.setThumbnailLink(downloadFile.getThumbnailPath());
                item.setName(downloadFile.getName());
                item.setResult(downloadFile.getResult());
                if (!tempDate.equals(downloadFile.getDate())) {
                    mItemArrayList.add(new HeaderItem(mCustomDateFormat.dateFormatting(downloadFile.getDate(), Item.ROOM_ITEM_TYPE)));
                    tempDate = downloadFile.getDate();
                }
                item.setDownloadTime(mCustomDateFormat.dateFormatting(downloadFile.getDate(), Item.ROOM_ITEM_TYPE));
                mItemArrayList.add(item);
            }

            mDownloadResultRecyclerViewAdapter.addAll(mItemArrayList);
            //mItemArrayList.clear();


            /*
            for (DownloadFile downloadFild : downloadFileList) {
                addFileToList(downloadFild);
            }*/

        }

    }

    private void dialogBuilderDeleteAllFiles() {


        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("다운로드 목록 삭제");
        builder.setMessage("다운로드 목록 전체를 삭제하시겠습니까?");
        builder.setPositiveButton(R.string.ok_dialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clear();
                Toast.makeText(getApplicationContext(), "삭제 완료", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(R.string.cancel_dialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();

    }

    private void addFileToList(DownloadFile downloadFile) {
        GalleryItem item = new GalleryItem();

        List<Item> itemList = new ArrayList<>();

        if (mItemArrayList.isEmpty()) {//비어있을 때

            item.setThumbnailLink(downloadFile.getThumbnailPath());
            item.setName(downloadFile.getName());
            itemList.add(new HeaderItem(mCustomDateFormat.dateFormatting(downloadFile.getDate(), Item.ROOM_ITEM_TYPE)));
            item.setDownloadTime(mCustomDateFormat.dateFormatting(downloadFile.getDate(),Item.ROOM_ITEM_TYPE));
            item.setResult(downloadFile.getResult());
            /*
            switch (downloadFile.getResult()) {
                case DOWNLOAD_SUCCESS:
                    item.setDownloadTime(mCustomDateFormat.DateToString(downloadFile.getDate(),Item.ROOM_ITEM_TYPE));
                    break;

                case DOWNLOAD_DUPLICATED:
                    item.setDownloadTime(getString(R.string.duplicated_file_name));
                    break;

                case DOWNLOAD_FAILED:

                    item.setDownloadTime(getString(R.string.download_failed));
                    break;
            }*/
            itemList.add(item);
        } else {
            int size = mItemArrayList.size();

            String tempDate = ((GalleryItem) (mItemArrayList.get(size - 1))).getDownloadTime();

            item.setThumbnailLink(downloadFile.getThumbnailPath());
            item.setName(downloadFile.getName());
          /* if (!tempDate.equals(downloadFile.getDate())) {
                itemList.add(new HeaderItem(downloadFile.getDate()));
            }*/
            item.setDownloadTime(mCustomDateFormat.dateFormatting(downloadFile.getDate(),Item.ROOM_ITEM_TYPE));
            item.setResult(downloadFile.getResult());
            /*
            switch (downloadFile.getResult()) {
                case DOWNLOAD_SUCCESS:
                    item.setDownloadTime(downloadFile.getDate());
                    break;

                case DOWNLOAD_DUPLICATED:
                    item.setDownloadTime(getString(R.string.duplicated_file_name));
                    break;

                case DOWNLOAD_FAILED:

                    item.setDownloadTime(getString(R.string.download_failed));
                    break;
            }
            */
            itemList.add(item);

        }

        mItemArrayList.addAll(itemList);
        mDownloadResultRecyclerViewAdapter.addAll(itemList);

    }


    public void download(final GalleryItem galleryItem) {

        //  OAuthServerIntf server = RetrofitBuilder.getOAuthClient(getApplication());
        Call<ResponseBody> responseBodyCall = server.downloadFile(galleryItem.getId());
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                if (response.code() == OK && response.body() != null) {

                    new AsyncTask<Void, Integer, Integer>() {
                        @Override
                        protected void onPreExecute() {
                            mDownloadResultRecyclerViewAdapter.add(galleryItem);

                            //addFileToList(downloadFile);
                            mDownloadResultRecyclerViewAdapter.notifyDataSetChanged();

                        }

                        @Override
                        protected void onPostExecute(Integer result) {

                            DownloadFile downloadFile = new DownloadFile(galleryItem.getName(), galleryItem.getThumbnailLink(), mCustomDateFormat.DateToString(currentDate, Item.ROOM_HEADER_TYPE), result);
                            FileDatabase.getDatabase(getApplicationContext()).getFileDao().insertDownloadFile(downloadFile);
                            GalleryItem item = new GalleryItem();
                            item = galleryItem;
                            item.setResult(result);
                            item.setDownloadTime(mCustomDateFormat.dateFormatting(downloadFile.getDate(),Item.ROOM_ITEM_TYPE));
                            int position = mDownloadResultRecyclerViewAdapter.getItemPosition(galleryItem);
                            mDownloadResultRecyclerViewAdapter.setItemResult(position, item);
                            //mDownloadResultRecyclerViewAdapter.notifyDataSetChanged();

                            //Date currentDate = new Date();
                            /*DownloadFile downloadFile = new DownloadFile(galleryItem.getName(), galleryItem.getThumbnailLink(), mCustomDateFormat.DateToString(currentDate, Item.ROOM_HEADER_TYPE), result);
                            FileDatabase.getDatabase(getApplicationContext()).getFileDao().insertDownloadFile(downloadFile);

                            mDownloadResultRecyclerViewAdapter.add(galleryItem);

                            //addFileToList(downloadFile);
                            mDownloadResultRecyclerViewAdapter.notifyDataSetChanged();*/


                        }

                        @Override
                        protected void onProgressUpdate(Integer... values) {
                            super.onProgressUpdate(values[0]);
                            int position = mDownloadResultRecyclerViewAdapter.getItemPosition(galleryItem);
                            mDownloadResultRecyclerViewAdapter.setProgressUpdate(position, values[0]);

                        }

                        @Override
                        protected Integer doInBackground(Void... voids) {

                            //int writtenToDisk = writeResponseBodyToDisk(response.body(), galleryItem);
                            //return writtenToDisk;
                            ResponseBody body = response.body();
                            try {
                                //새로운 Directory 생성
                                String file_url = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "GDRIVE";


                                File dir = new File(file_url);
                                //directory 없으면 생성
                                if (!dir.exists()) {
                                    dir.mkdirs();
                                }

                                String fileName = galleryItem.getName();


                                String localPath = file_url + File.separator + fileName;

                                //Local Path에 파일 생성

                                File downloadFile = new File(localPath);

                                if (downloadFile.exists()) {

                                    return Item.DOWNLOAD_DUPLICATED;
                                }

                                InputStream inputStream = null;
                                OutputStream outputStream = null;

                                try {
                                    byte[] fileReader = new byte[4096];

                                    long fileSize = body.contentLength();
                                    long fileSizeDownloaded = 0;

                                    inputStream = body.byteStream();
                                    outputStream = new FileOutputStream(downloadFile);
                                    int percentage = 0;
                                    while (true) {
                                        int read = inputStream.read(fileReader);

                                        if (read == -1) {
                                            break;
                                        }
                                        percentage = (int)(fileSizeDownloaded * 100/fileSize);
                                        publishProgress(percentage);
                                        outputStream.write(fileReader, 0, read);

                                        fileSizeDownloaded += read;


                                        Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                                    }


                                    ContentValues values = new ContentValues();
                                    values.put(MediaStore.Images.Media.DATA,
                                            downloadFile.getAbsolutePath());
                                    values.put(MediaStore.Images.Media.MIME_TYPE, galleryItem.getMimeType());
                                    // values.put(MediaStore.Images.ImageColumns.ORIENTATION, galleryItem.getOrientation());
                                    //values.put(MediaStore.Images.Media.DATE_ADDED, galleryItem.getCreatedTime());
                                    getContentResolver().insert(
                                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                                    outputStream.flush();

                                    return Item.DOWNLOAD_SUCCESS;
                                } catch (IOException e) {
                                    return Item.DOWNLOAD_FAILED;
                                } finally {
                                    if (inputStream != null) {
                                        inputStream.close();
                                    }

                                    if (outputStream != null) {
                                        outputStream.close();
                                    }
                                }
                            } catch (IOException e) {
                                return Item.DOWNLOAD_FAILED;
                            }
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

    private int writeResponseBodyToDisk(ResponseBody body, GalleryItem galleryItem) {

        try {
            //새로운 Directory 생성
            String file_url = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "GDRIVE";


            File dir = new File(file_url);
            //directory 없으면 생성
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = galleryItem.getName();


            String localPath = file_url + File.separator + fileName;

            //Local Path에 파일 생성

            File downloadFile = new File(localPath);

            if (downloadFile.exists()) {

                return Item.DOWNLOAD_DUPLICATED;
            }

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
                        downloadFile.getAbsolutePath());
                values.put(MediaStore.Images.Media.MIME_TYPE, galleryItem.getMimeType());
                // values.put(MediaStore.Images.ImageColumns.ORIENTATION, galleryItem.getOrientation());
                //values.put(MediaStore.Images.Media.DATE_ADDED, galleryItem.getCreatedTime());
                getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                outputStream.flush();

                return Item.DOWNLOAD_SUCCESS;
            } catch (IOException e) {
                return Item.DOWNLOAD_FAILED;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return Item.DOWNLOAD_FAILED;
        }
    }


}


