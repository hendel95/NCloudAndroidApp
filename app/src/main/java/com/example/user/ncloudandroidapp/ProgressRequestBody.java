package com.example.user.ncloudandroidapp;

import android.app.ProgressDialog;
import android.content.Context;
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

    private static final int DEFAULT_BUFFER_SIZE = 2048;

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


        try {
            int read;
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            long uploaded = 0;
            int uploadPercent = 0;

            Handler handler = new Handler(Looper.getMainLooper());
            while ((read = in.read(buffer)) != -1) {
                Log.i("read", Integer.toString(read));

                uploadPercent = (int)(100 * uploaded / fileLength);
                mListener.onProgressUpdate(uploadPercent);

                uploaded += read;
                sink.write(buffer, 0, read);



             //   handler.post(new ProgressUpdater(uploaded, fileLength));
                Log.i("TAG"+"progress", Long.toString(uploaded) + "/" + Long.toString(fileLength) + "=" + Integer.toString(uploadPercent));
            }
        } finally {
            in.close();
            mListener.onFinish();
            Log.i("TAG"+"onfinished", "finished");

        }
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