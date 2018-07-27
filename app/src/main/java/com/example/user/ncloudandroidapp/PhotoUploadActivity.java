package com.example.user.ncloudandroidapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.user.ncloudandroidapp.Model.GalleryItem;
import com.example.user.ncloudandroidapp.Model.LocalGalleryItem;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhotoUploadActivity extends AppCompatActivity {
    public static final String EXTRA_GDRIVE_PHOTO = "PhotoUploadActivity.GDRIVE_PHOTO";
    public static final String EXTRA_LOCAL_PHOTO = "PhotoUploadActivity.LOCAL_PHOTO";

    private static final String GDRIVE = "CustomRecyclerViewAdapter";
    private static final String LOCAL = "LocalRecyclerViewAdapter";

    // private ImageView mImageView;
    @BindView(R.id.image_large)
    ImageView detailedImage;

    @BindView(R.id.title_text)
    TextView textTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);
        ButterKnife.bind(this);
      //  mImageView = (ImageView) findViewById(R.id.image_large);

        String className = getIntent().getStringExtra("class");
        switch (className){
            case GDRIVE:

                GalleryItem galleryItem = getIntent().getParcelableExtra(EXTRA_GDRIVE_PHOTO);

                Glide.with(this)
                        .load(galleryItem.getThumbnailLink()+"0")
                        .apply(RequestOptions.fitCenterTransform().error(R.drawable.error_img)
                                .placeholder(R.drawable.loading_img))
                        .into(detailedImage);

                textTitle.setText(galleryItem.getName());

                break;
            case LOCAL:
                LocalGalleryItem localGalleryItem = getIntent().getParcelableExtra(EXTRA_LOCAL_PHOTO);
/*
                Glide.with(this)
                        .load("file://" + localGalleryItem.getPath())
                        .apply(RequestOptions.fitCenterTransform().error(R.drawable.error_img)
                                .placeholder(R.drawable.loading_img))
                        .into(detailedImage);
*/

                Glide.with(this)
                        .load(localGalleryItem.getPath())
                        .apply(RequestOptions.fitCenterTransform().error(R.drawable.error_img)
                                .placeholder(R.drawable.loading_img))
                        .into(detailedImage);
                break;
        }

        /*

        Glide.with(this)
                .load(spacePhoto.getUrl())
                .asBitmap()
                .error(R.drawable.img)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)*/
    }
}
