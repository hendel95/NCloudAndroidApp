package com.example.user.ncloudandroidapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String FIELDS = "files/id, files/name, files/mimeType, files/thumbnailLink";
    private static final String Q = "mimeType contains 'image' and trashed = false";


  //  @BindView(R.id.image_view)
   // ImageView mImageView;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listGDriveUserFiles();
        ButterKnife.bind(this);

    }

    private void listGDriveUserFiles(){
        OAuthServerIntf server=RetrofitBuilder.getOAuthClient(this);

        Call<GalleryItems> galleryItemCall = server.getFileDescription(FIELDS, Q );

        galleryItemCall.enqueue(new Callback<GalleryItems>() {
            @Override
            public void onResponse(Call<GalleryItems> call, Response<GalleryItems> response){
                if(response.code()==200&&response.body()!=null){
                    //Toast.makeText(MainActivity.this, response.message()+"\r\n"+getString(R.string.http_code_200), Toast.LENGTH_SHORT).show();

                    RecyclerView.LayoutManager layoutManager = new GridLayoutManager(MainActivity.this, 3);
                    //RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
                    mRecyclerView.setHasFixedSize(true);
                    mRecyclerView.setLayoutManager(layoutManager);

                    ImageGalleryAdapter adapter = new ImageGalleryAdapter(MainActivity.this, response.body().getFiles());
                    mRecyclerView.setAdapter(adapter);

                }else if(response.code()==400){
                    Toast.makeText(MainActivity.this, response.message()+"\r\n"+getString(R.string.http_code_400), Toast.LENGTH_SHORT).show();
                }else if(response.code()==401){
                    Toast.makeText(MainActivity.this, response.message()+"\r\n"+getString(R.string.http_code_401), Toast.LENGTH_SHORT).show();
                }else if(response.code()==403){
                    Toast.makeText(MainActivity.this, response.message()+"\r\n"+getString(R.string.http_code_403), Toast.LENGTH_SHORT).show();
                }else if(response.code()==404){
                    Toast.makeText(MainActivity.this, response.message()+"\r\n"+getString(R.string.http_code_404), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GalleryItems> call, Throwable t) {
                Log.e(TAG,"The call listFilesCall failed",t);
            }

        });

    }

    private class ImageGalleryAdapter extends RecyclerView.Adapter<ImageGalleryAdapter.MyViewHolder>{

        @Override
        public ImageGalleryAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent , int viewType){
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View photoView = inflater.inflate(R.layout.gallery_item, parent, false);
            ImageGalleryAdapter.MyViewHolder viewHolder = new ImageGalleryAdapter.MyViewHolder(photoView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ImageGalleryAdapter.MyViewHolder holder, int position){
            GalleryItem photo = mSpacePhotos.get(position);
            ImageView imageView = holder.mPhotoImageView;



            Glide.with(mContext)
                    .load(photo.getThumbnailLink())
                    .apply(new RequestOptions().placeholder(R.drawable.no_img))
                    .into(imageView);
        }

        @Override
        public int getItemCount(){
            return (mSpacePhotos.size());
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            public ImageView mPhotoImageView;

            public MyViewHolder(View itemView){
                super(itemView);
                mPhotoImageView = (ImageView)itemView.findViewById(R.id.fragment_gallery_image_view);
                itemView.setOnClickListener(this);
            }
            @Override
            public void onClick(View view){
                int position = getAdapterPosition();
                if(position != RecyclerView.NO_POSITION){
                    GalleryItem galleryItem = mSpacePhotos.get(position);
                    Intent intent = new Intent(mContext, PhotoUploadActivity.class);
                    intent.putExtra(PhotoUploadActivity.EXTRA_SPACE_PHOTO, galleryItem);
                    startActivity(intent);
                }
            }
        }

        private List<GalleryItem> mSpacePhotos;
        private Context mContext;

        public ImageGalleryAdapter(Context context, List<GalleryItem> spacePhotos){
            mContext = context;
            mSpacePhotos = spacePhotos;
        }

    }
}
