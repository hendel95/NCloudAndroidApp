package com.example.user.ncloudandroidapp.Controller;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
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

import com.example.user.ncloudandroidapp.Adapter.UploadResultRecyclerViewAdapter;
import com.example.user.ncloudandroidapp.CustomDateFormat;
import com.example.user.ncloudandroidapp.Model.GalleryItem;
import com.example.user.ncloudandroidapp.Model.HeaderItem;
import com.example.user.ncloudandroidapp.Model.Item;
import com.example.user.ncloudandroidapp.Model.LocalGalleryItem;
import com.example.user.ncloudandroidapp.OAuthHelper;
import com.example.user.ncloudandroidapp.OAuthServerIntf;
import com.example.user.ncloudandroidapp.ProgressRequestBody;
import com.example.user.ncloudandroidapp.R;
import com.example.user.ncloudandroidapp.RetrofitBuilder;
import com.example.user.ncloudandroidapp.Room.FileDatabase;
import com.example.user.ncloudandroidapp.Room.UploadFile;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadResultActivity extends AppCompatActivity {

    @BindView(R.id.upload_recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.nav_delete_upload)
    ImageButton mImageButton;

    @BindView(R.id.nav_remove_all_upload)
    Button mButton;

    @OnClick(R.id.nav_remove_all_upload)
    public void onDeleteButtonClick() {
        dialogBuilderDeleteAllFiles();

    }

    OAuthHelper mOAuthHelper;

    UploadResultRecyclerViewAdapter mUploadResultRecyclerViewAdapter;

    public static final int READY = 300;
    public static final int UPLOAD_REQUEST = 301;
    public static final int PROGRESS_UPDATE = 302;
    public static final int QUERY = 303;
    public static final int QUERY_FINISH = 304;
    public static final int UPLOAD_SUCCESS = 701;

    private final String TAG = "UploadResultActivity";

   // private Handler mainThreadHandler;
   // private UploadHandlerThread handlerThread;

    List<Item> mItemArrayList = new ArrayList<>();
    List<UploadFile> uploadFileListDatabase;
    List<LocalGalleryItem> uploadRequestList;

    OAuthServerIntf server;

    CustomDateFormat mDateFormat = new CustomDateFormat();

    Date currentDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_result);
        ButterKnife.bind(this);

        mOAuthHelper = new OAuthHelper(getApplicationContext());


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getApplicationContext(),
                LinearLayoutManager.VERTICAL);
        dividerItemDecoration.setDrawable(getApplicationContext().getResources().getDrawable(R.drawable.custom_decorator));


        uploadRequestList = getIntent().getParcelableArrayListExtra("UPLOAD_LIST");


        mUploadResultRecyclerViewAdapter = new UploadResultRecyclerViewAdapter(getApplicationContext());
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mUploadResultRecyclerViewAdapter);

        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        //activity 에서 실행된 것.

        server = RetrofitBuilder.getOAuthClient(getApplicationContext());


        if (uploadRequestList != null) {
            currentDate = new Date();
            Toast.makeText(getApplicationContext(), "사진 업로드 중입니다.", Toast.LENGTH_SHORT).show();

            for (LocalGalleryItem item : uploadRequestList) {
                mUploadResultRecyclerViewAdapter.add(item);
                mUploadResultRecyclerViewAdapter.notifyDataSetChanged();
                executeUpload(item);
            }
            mItemArrayList.clear();

        } else {
            loadFilesFromDatabase();
        }




       /* mainThreadHandler = new UploadActivityHandler(this);
        handlerThread = new UploadHandlerThread("UploadHandlerThread");
        handlerThread.start();*/
        /*
        if(!uploadRequestList.isEmpty()) {




        }*/
/*
        else{

            uploadFileListDatabase = FileDatabase.getDatabase(getApplicationContext()).getFileDao().getAllUploadedFiles();

            if (!uploadFileListDatabase.isEmpty()) {

                mItemArrayList.clear();

                String tempDate = uploadFileListDatabase.get(0).getMDate();

                mItemArrayList.add(new HeaderItem(tempDate));

                for (UploadFile uploadFile : uploadFileListDatabase) {
                    LocalGalleryItem item = new LocalGalleryItem();
                    item.setThumbnailPath(uploadFile.getMThumbnailPath());
                    item.setName(uploadFile.getMName());
                    if (!tempDate.equals(uploadFile.getMDate())) {
                        mItemArrayList.add(new HeaderItem(uploadFile.getMDate()));
                        tempDate = uploadFile.getMDate();
                    }
                    item.setUploadTime(uploadFile.getMDate());
                    mItemArrayList.add(item);
                }

                mUploadResultRecyclerViewAdapter.addAll(mItemArrayList);

            }

        }*/
    }


    public void loadFilesFromDatabase() {
        uploadFileListDatabase = FileDatabase.getDatabase(getApplicationContext()).getFileDao().getAllUploadedFiles();

        if (!uploadFileListDatabase.isEmpty()) {

            mItemArrayList.clear();
            mUploadResultRecyclerViewAdapter.clear();
            String tempDate = uploadFileListDatabase.get(0).getMDate();

            mItemArrayList.add(new HeaderItem(mDateFormat.dateFormatting(tempDate, Item.ROOM_ITEM_TYPE)));

            for (UploadFile uploadFile : uploadFileListDatabase) {
                LocalGalleryItem item = new LocalGalleryItem();
                item.setThumbnailPath(uploadFile.getMThumbnailPath());
                item.setName(uploadFile.getMName());
                item.setResult(uploadFile.getMResult());
                Log.e(TAG+"THIS", mDateFormat.dateFormatting(uploadFile.getMDate(), Item.ROOM_ITEM_TYPE));
                if (!tempDate.equals(uploadFile.getMDate())) {
                    mItemArrayList.add(new HeaderItem(mDateFormat.dateFormatting(uploadFile.getMDate(), Item.ROOM_ITEM_TYPE)));
                    tempDate = uploadFile.getMDate();
                }
                item.setUploadTime(mDateFormat.dateFormatting(uploadFile.getMDate(), Item.ROOM_ITEM_TYPE));
                mItemArrayList.add(item);
            }

            mUploadResultRecyclerViewAdapter.addAll(mItemArrayList);

        }
    }


    public void refresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FileDatabase.getDatabase(getApplicationContext()).getFileDao().clearAllUploadedFiles();
                mUploadResultRecyclerViewAdapter.clear();
                mUploadResultRecyclerViewAdapter.notifyDataSetChanged();
            }
        }, 1000);
    }

    private void dialogBuilderDeleteAllFiles() {


        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("업로드 목록 삭제");
        builder.setMessage("업로드 목록 전체를 삭제하시겠습니까?");
        builder.setPositiveButton(R.string.ok_dialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                refresh();
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

    /*
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case READY:
                    handlerThread.sendQueryRequest();
                    break;
                case QUERY_FINISH:
                    //DateFormat dateFormat = new SimpleDateFormat("yyyy. MM. dd HH:mm", Locale.KOREA);
                    //for (DriveFile file : downloadRequestList) {
                    //    downloadItemList.add(0, new DownloadItem(file.getName(), dateFormat.format(new Date()), file.getThumbnailLink(), 0, file.getWidth(), file.getHeight()));
                   // }
                   // adapter.notifyDataSetChanged();

                    for (LocalGalleryItem item : uploadRequestList) {
                        mItemArrayList.add(item);
                    }
                    mUploadResultRecyclerViewAdapter.notifyDataSetChanged();
                    createUploadRequest();
                    break;
                case PROGRESS_UPDATE:
                    int position = (int) msg.obj;
                    mUploadResultRecyclerViewAdapter.progressUpdate(position, msg.arg1);
                    break;
            }
        }

        private void createUploadRequest() {
            for (LocalGalleryItem localGalleryItem : uploadRequestList) {
                handlerThread.sendUploadRequest(localGalleryItem);
            }
        }

        class UploadHandlerThread extends HandlerThread {
            private Handler handler;

            public UploadHandlerThread(String name) {
                super(name);
            }

            @Override
            protected void onLooperPrepared() {
                handler = new Handler(getLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        //LocalGalleryItem file;
                        switch (msg.what) {
                            case QUERY:
                                createUploadList();
                                break;
                            case UPLOAD_REQUEST:
                                LocalGalleryItem file = (LocalGalleryItem) msg.obj;
                                executeUpload(file);
                                break;
                            case UPLOAD_SUCCESS:
                                mainThreadHandler.sendEmptyMessage(UPLOAD_SUCCESS);
                                //file = (LocalGalleryItem) msg.obj;
                                //file.setStatus("UPLOADED");
                                //appDatabase.fileDAO().updateFileStatus(status);
                                break;

                        }
                    }
                };
                Message message = handler.obtainMessage(READY);
                mainThreadHandler.sendMessageAtFrontOfQueue(message);
            }

            public void sendUploadRequest(LocalGalleryItem localGalleryItem) {
                Message message = handler.obtainMessage(UPLOAD_REQUEST, localGalleryItem);
                handler.sendMessage(message);
            }


            public void sendQueryRequest() {
                Message message = handler.obtainMessage(QUERY);
                handler.sendMessageAtFrontOfQueue(message);
            }


    */
    private MultipartBody.Part prepareFilePart(final File file, String mimeType, final int updateItemPosition, final LocalGalleryItem item, final LocalGalleryItem galleryItem) {
        final ProgressRequestBody fileBody = new ProgressRequestBody(getApplicationContext(), file, mimeType, new ProgressRequestBody.UploadCallbacks() {
            @Override
            public void onProgressUpdate(int percentage) {

                   /* Message message = mainThreadHandler.obtainMessage(PROGRESS_UPDATE, updateItemPosition);
                    message.arg1 = percentage;
                    mainThreadHandler.sendMessage(message);*/
            //
                //    mUploadResultRecyclerViewAdapter.setProgressUpdate(updateItemPosition, percentage);
                //mProgressBar.setProgress(percentage);


//                mTextView.setText(Integer.toString(percentage));
                //Log.i("TAG"+"progress", Integer.toString(percentage));
            }

            @Override
            public void onError() {
                UploadFile uploadFile = new UploadFile(item.getName(), item.getThumbnailPath(), mDateFormat.DateToString(currentDate, Item.ROOM_HEADER_TYPE), Item.DOWNLOAD_FAILED);
                FileDatabase.getDatabase(getApplicationContext()).getFileDao().insertUploadFile(uploadFile);

                LocalGalleryItem item = new LocalGalleryItem();
                item = galleryItem;
                item.setResult(Item.DOWNLOAD_FAILED);
                item.setUploadTime(mDateFormat.dateFormatting(uploadFile.getMDate(), Item.ROOM_ITEM_TYPE));
                //int position = mUploadResultRecyclerViewAdapter.getItemPosition(galleryItem);
                mUploadResultRecyclerViewAdapter.setItemResult(updateItemPosition, item);
            }

             @Override
            public void onFinish() {
                UploadFile uploadFile = new UploadFile(item.getName(), item.getThumbnailPath(), mDateFormat.DateToString(currentDate, Item.ROOM_HEADER_TYPE), Item.DOWNLOAD_SUCCESS);
                FileDatabase.getDatabase(getApplicationContext()).getFileDao().insertUploadFile(uploadFile);

                LocalGalleryItem item = new LocalGalleryItem();
                item = galleryItem;
                item.setResult(Item.DOWNLOAD_SUCCESS);
                item.setUploadTime(mDateFormat.dateFormatting(uploadFile.getMDate(), Item.ROOM_ITEM_TYPE));
                //int position = mUploadResultRecyclerViewAdapter.getItemPosition(galleryItem);
                mUploadResultRecyclerViewAdapter.setItemResult(updateItemPosition, item);
            }
        });

        return MultipartBody.Part.createFormData("data", file.getName(), fileBody);
    }


    protected void executeUpload(final LocalGalleryItem galleryItem) {

        int updateItemPosition = uploadRequestList.indexOf(galleryItem);

        final LocalGalleryItem item = galleryItem;
        File file = new File(item.getPath());

        String content = "{\"name\": \"" + file.getName() + "\"}";
        MediaType contentType = MediaType.parse("application/json; charset=UTF-8");

        RequestBody propertyBody = RequestBody.create(contentType, content);

        String mimeType = item.getMimeType();

        final MultipartBody.Part dataPart = prepareFilePart(file, mimeType, updateItemPosition, item, galleryItem);

        Call<ResponseBody> call = server.uploadFile(propertyBody, dataPart);
        Log.i(TAG, call.request().toString());

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //Toast.makeText(getApplicationContext(), "uploading successfully", Toast.LENGTH_SHORT).show();
               // mUploadResultRecyclerViewAdapter.notifyDataSetChanged();

                Log.i(TAG, response.headers().toString());
               /* if (response.isSuccessful()) {
                    //  uploadedItems.add(item);
                    Log.i("Uploaded", response.body().toString());
                    UploadFile uploadFile = new UploadFile(item.getName(), item.getThumbnailPath(), mDateFormat.DateToString(currentDate, Item.ROOM_HEADER_TYPE), Item.DOWNLOAD_SUCCESS);
                    FileDatabase.getDatabase(getApplicationContext()).getFileDao().insertUploadFile(uploadFile);

                    LocalGalleryItem item = new LocalGalleryItem();
                    item = galleryItem;
                    item.setResult(Item.DOWNLOAD_SUCCESS);
                    item.setUploadTime(mDateFormat.dateFormatting(uploadFile.getMDate(), Item.ROOM_ITEM_TYPE));
                    int position = mUploadResultRecyclerViewAdapter.getItemPosition(galleryItem);
                    mUploadResultRecyclerViewAdapter.setItemResult(position, item);

                } else {
                    UploadFile uploadFile = new UploadFile(item.getName(), item.getThumbnailPath(), mDateFormat.DateToString(currentDate, Item.ROOM_HEADER_TYPE), Item.DOWNLOAD_FAILED);
                    FileDatabase.getDatabase(getApplicationContext()).getFileDao().insertUploadFile(uploadFile);

                    LocalGalleryItem item = new LocalGalleryItem();
                    item = galleryItem;
                    item.setResult(Item.DOWNLOAD_FAILED);
                    item.setUploadTime(mDateFormat.dateFormatting(uploadFile.getMDate(), Item.ROOM_ITEM_TYPE));
                    int position = mUploadResultRecyclerViewAdapter.getItemPosition(galleryItem);
                    mUploadResultRecyclerViewAdapter.setItemResult(position, item);

                }*/


            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                UploadFile uploadFile = new UploadFile(item.getName(), item.getThumbnailPath(), mDateFormat.DateToString(currentDate, Item.ROOM_HEADER_TYPE), Item.DOWNLOAD_FAILED);
                FileDatabase.getDatabase(getApplicationContext()).getFileDao().insertUploadFile(uploadFile);

                LocalGalleryItem item = new LocalGalleryItem();
                item = galleryItem;
                item.setResult(Item.DOWNLOAD_FAILED);
                item.setUploadTime(mDateFormat.dateFormatting(uploadFile.getMDate(), Item.ROOM_ITEM_TYPE));
                int position = mUploadResultRecyclerViewAdapter.getItemPosition(galleryItem);
                mUploadResultRecyclerViewAdapter.setItemResult(position, item);

            }
        });


    }


}

/*
    public class UploadActivityHandler extends Handler {

        private final WeakReference<UploadResultActivity> activityWeakReference;

        public UploadActivityHandler(UploadResultActivity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            UploadResultActivity activity = activityWeakReference.get();
            activity.handleMessage(msg);
        }
    }*/





