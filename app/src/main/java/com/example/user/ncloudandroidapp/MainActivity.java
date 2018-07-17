package com.example.user.ncloudandroidapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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

public class MainActivity extends AppCompatActivity  {
    private static final String TAG = "MainActivity";
    private static final String FIELDS = "files/id, files/name, files/mimeType, files/thumbnailLink, files/createdTime";
    private static final String Q = "mimeType contains 'image' and trashed = false";
    private static final int DEFAULT_SPAN_COUNT = 3;

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
                    Toast.makeText(MainActivity.this, response.message()+"\r\n"+getString(R.string.http_code_200), Toast.LENGTH_SHORT).show();
                    configViews(response);
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


    private void configViews(Response<GalleryItems> response){

        // RecyclerView.LayoutManager layoutManager = new GridLayoutManager(MainActivity.this, 3);
        //RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), DEFAULT_SPAN_COUNT);

        //mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setRecycledViewPool(new RecyclerView.RecycledViewPool());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(gridLayoutManager);

        // CustomRecyclerViewAdapter adapter = new CustomRecyclerViewAdapter(MainActivity.this, response.body().getFiles());
        CustomRecyclerViewAdapter adapter = new CustomRecyclerViewAdapter( response.body().getFiles(), gridLayoutManager, DEFAULT_SPAN_COUNT);

        //ImageGalleryAdapter adapter = new ImageGalleryAdapter(MainActivity.this, response.body().getFiles());
        mRecyclerView.setAdapter(adapter);
    }

    private class ImageGalleryAdapter extends RecyclerView.Adapter<ImageGalleryAdapter.MyViewHolder>{

        private static final int TYPE_HEADER = 0; // 헤더

        @Override
        public ImageGalleryAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent , int viewType){
           /* if(viewType == TYPE_HEADER) {

                Context context = parent.getContext();
                LayoutInflater inflater = LayoutInflater.from(context);
                View photoView = inflater.inflate(R.layout.header_layout, parent, false); //헤더일때 뷰 인플레이트 다르게
                ImageGalleryAdapter.MyViewHolder viewHolder = new ImageGalleryAdapter.MyViewHolder(photoView);
                return viewHolder;

            }
            else{ //TYPE_ITEM
*/
                Context context = parent.getContext();
                LayoutInflater inflater = LayoutInflater.from(context);
                View photoView = inflater.inflate(R.layout.gallery_item, parent, false);
                ImageGalleryAdapter.MyViewHolder viewHolder = new ImageGalleryAdapter.MyViewHolder(photoView);
                return viewHolder;
  //          }
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



    ////////////////////////////////////////////////////////////////////////////////////////////////
    public class CustomRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private int mDefaultSpanCount;
        private List<GalleryItem> itemObjects;

        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;

        public CustomRecyclerViewAdapter(List<GalleryItem> itemObjects, GridLayoutManager gridLayoutManager, int defaultSpanCount) {
            this.itemObjects = itemObjects;
            mDefaultSpanCount = defaultSpanCount;
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    //return isHeaderType(position) ? mDefaultSpanCount : 1;
                    return position == 0 ? mDefaultSpanCount : 1; // 수정 예정
                }
            });
        }

        private boolean isHeaderType(int position) {
            return itemObjects.get(position).getItemType() == TYPE_HEADER ? true : false;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_HEADER) {
                View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_layout, parent, false);
                return new HeaderViewHolder(layoutView);
            } else if (viewType == TYPE_ITEM) {
                View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_item, parent, false);
                return new ItemViewHolder(layoutView);
            }
            throw new RuntimeException("No match for " + viewType + ".");
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            GalleryItem mObject = itemObjects.get(position);
            if(holder instanceof HeaderViewHolder){ //header 인 경우 binding
                ((HeaderViewHolder) holder).headerTitle.setText(mObject.getCreatedTime());

            }else if(holder instanceof ItemViewHolder){ //list item 인 경우 binding
                //GalleryItem photo = itemObjects.get(position);
                ImageView imageView =  ((ItemViewHolder) holder).mPhotoImageView;

                Glide.with(MainActivity.this)
                        .load(mObject.getThumbnailLink())
                        .apply(new RequestOptions().placeholder(R.drawable.no_img))
                        .into(imageView);
            }
        }

        private GalleryItem getItem(int position) {
            return itemObjects.get(position);
        }

        @Override
        public int getItemCount() {
            return itemObjects.size();
        }
        @Override
        public int getItemViewType(int position) {
            if (isPositionHeader(position))
                return TYPE_HEADER;
            return TYPE_ITEM;
        }
        private boolean isPositionHeader(int position) {
            return position == 0;
        }

        public class HeaderViewHolder extends RecyclerView.ViewHolder{
            public TextView headerTitle;
            public HeaderViewHolder(View itemView) {
                super(itemView);
                headerTitle = (TextView)itemView.findViewById(R.id.headerTitle);
            }
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

            public ImageView mPhotoImageView;

            public ItemViewHolder(View itemView){
                super(itemView);
                mPhotoImageView = (ImageView)itemView.findViewById(R.id.fragment_gallery_image_view);
                itemView.setOnClickListener(this);
            }
            @Override
            public void onClick(View view){
                int position = getAdapterPosition();
                if(position != RecyclerView.NO_POSITION){
                    GalleryItem galleryItem = itemObjects.get(position);
                    //Intent intent = new Intent(mContext, PhotoUploadActivity.class);
                    Intent intent = new Intent(view.getContext(), PhotoUploadActivity.class);
                    intent.putExtra(PhotoUploadActivity.EXTRA_SPACE_PHOTO, galleryItem);
                    startActivity(intent);
                }
            }

        }

        //private List<GalleryItem> mSpacePhotos;
//        private Context mContext;

       /* public CustomRecyclerViewAdapter(Context context, List<GalleryItem> spacePhotos){
            mContext = context;
            itemObjects = spacePhotos;
        }*/
    }




}
