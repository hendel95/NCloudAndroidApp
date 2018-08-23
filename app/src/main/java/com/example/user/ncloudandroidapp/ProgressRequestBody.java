package com.example.user.ncloudandroidapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class ProgressRequestBody extends RequestBody {
    private File mFile;
    private String mPath;
    private UploadCallbacks mListener;
    private String mMimeType;
    private Context mContext;

    //private static final int DEFAULT_BUFFER_SIZE = 2048;
    private static final int DEFAULT_BUFFER_SIZE = 4096;

    public interface UploadCallbacks {
        void onProgressUpdate(int percentage);
        void onError();
        void onFinish();
    }

    public ProgressRequestBody(Context context, final File file, String mimeType, final  UploadCallbacks listener) {
        mContext = context;
        mFile = file;
        mListener = listener;
        this.mMimeType = mimeType;

       // ProgressDialog asyncDialog = new ProgressDialog(mContext);
        //asyncDialog.show();
    }


    @Override
    public MediaType contentType() {
        // i want to upload only images
       // MediaType contentType = MediaType.parse("application/json; charset=UTF-8");

       // return MediaType.parse("application/json; charset=UTF-8");
        //return MediaType.parse("image/*");
        return MediaType.parse(mMimeType);
    }


    @Override
    public long contentLength() throws IOException {
        return mFile.length();
    }


    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        long fileLength = mFile.length();
        FileInputStream in = new FileInputStream(mFile);
        boolean uploadFail = false;

        try {
            int read;
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            long uploaded = 0;
            int uploadPercent = 0;
            int currentUploadPercent = 0;

            Log.i("WRITE TO", mFile.getName() + "start");
            Handler handler = new Handler(Looper.getMainLooper());
            while ((read = in.read(buffer)) != -1) {
               // Log.i("read", Integer.toString(read));
                if (!isUnMeteredNetWork()) {
                    uploadFail = true;
                    break;
                }
                uploadPercent = (int) (100 * uploaded / fileLength);

                if(currentUploadPercent+1 <= uploadPercent) {
                    mListener.onProgressUpdate(uploadPercent);
                    //handler.post(new ProgressUpdater(uploaded, fileLength));

                    Log.i("UPLOAD", Integer.toString(uploadPercent));
                    currentUploadPercent = uploadPercent;
                }


                uploaded += read;

                sink.write(buffer, 0, read);

               // Log.i("TAG"+"progress", Long.toString(uploaded) + "/" + Long.toString(fileLength) + "=" + Integer.toString(uploadPercent));
            }
        } finally {

            if(uploadFail){
                in.close();
                mListener.onError();
            }else {
                in.close();
                mListener.onFinish();
                Log.i("TAG" + "onfinished", "finished");
            }
        }
    }


    private boolean isUnMeteredNetWork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            return networkInfo.getType() == ConnectivityManager.TYPE_WIFI || networkInfo.getType() == ConnectivityManager.TYPE_WIMAX;
        }
        return false;
    }

    private class ProgressUpdater implements Runnable {
        private long mUploaded;
        private long mTotal;
        public ProgressUpdater(long uploaded, long total) {
            mUploaded = uploaded;
            mTotal = total;
        }

        @Override
        public void run() {
            mListener.onProgressUpdate((int)(100 * mUploaded / mTotal));
        }
    }
}