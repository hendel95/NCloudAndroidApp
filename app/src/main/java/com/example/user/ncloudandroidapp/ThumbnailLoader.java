package com.example.user.ncloudandroidapp;

import android.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;

public class ThumbnailLoader extends AsyncTaskLoader<Bitmap> {

    private Bitmap data;
    private Integer mediaId;

    public final String TAG = "ThumbnailLoader";

    public ThumbnailLoader(Context context, Integer mediaId) {
        super(context);
        this.mediaId = mediaId;
    }

    @Override
    public Bitmap loadInBackground() {
        if (mediaId != null) {
            Log.i(TAG, "loading from mediastore");
            ContentResolver res = getContext().getContentResolver();
            return MediaStore.Images.Thumbnails.getThumbnail(
                    res, mediaId, MediaStore.Images.Thumbnails.MINI_KIND, null);
        } else {
            Log.i(TAG, "mediaId was null!");
            return null;
        }
    }

    @Override
    protected void onStartLoading() {
        if (data != null){
            deliverResult(data);
        }
        else{
            forceLoad();
        }
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
    }

    @Override
    public void deliverResult(Bitmap data) {
        super.deliverResult(this.data = data);
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void onCanceled(Bitmap unneeded) {
        if ((unneeded != null) && (unneeded != data))
            unneeded.recycle();
    }

    @Override
    protected void onReset() {
        if (data != null) {
            data.recycle();
            data = null;
        }
    }
}