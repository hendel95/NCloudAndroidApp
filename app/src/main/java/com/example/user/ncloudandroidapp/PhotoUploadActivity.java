package com.example.user.ncloudandroidapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFile;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhotoUploadActivity extends AppCompatActivity {
    public static final String EXTRA_SPACE_PHOTO = "PhotoUploadActivity.SPACE_PHOTO";
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
        GalleryItem galleryItem = getIntent().getParcelableExtra(EXTRA_SPACE_PHOTO);

        Glide.with(this)
                .load(galleryItem.getThumbnailLink()+"0")
                .apply(RequestOptions.centerCropTransform().error(R.drawable.no_img)
                .placeholder(R.drawable.no_img))
                .into(detailedImage);

        textTitle.setText(galleryItem.getName());
        /*

        Glide.with(this)
                .load(spacePhoto.getUrl())
                .asBitmap()
                .error(R.drawable.img)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)*/
    }
}
