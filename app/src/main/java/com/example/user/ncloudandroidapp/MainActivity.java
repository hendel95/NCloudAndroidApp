package com.example.user.ncloudandroidapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
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
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String FIELDS = "files/id, files/name, files/mimeType, files/thumbnailLink, files/createdTime";
    private static final String Q = "mimeType contains 'image' and trashed = false";
    private static final int DEFAULT_SPAN_COUNT = 3;
    private List<Item> mItemList = new ArrayList<>();
    private CustomRecyclerViewAdapter mAdapter;
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


    private void listGDriveUserFiles() {
        OAuthServerIntf server = RetrofitBuilder.getOAuthClient(this);

        Call<GalleryItems> galleryItemCall = server.getFileDescription(FIELDS, Q);

        galleryItemCall.enqueue(new Callback<GalleryItems>() {
            @Override
            public void onResponse(Call<GalleryItems> call, Response<GalleryItems> response) {
                if (response.code() == 200 && response.body() != null) {
                    Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_200), Toast.LENGTH_SHORT).show();
                    configViews(response);
                } else if (response.code() == 400) {
                    Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_400), Toast.LENGTH_SHORT).show();
                } else if (response.code() == 401) {
                    Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_401), Toast.LENGTH_SHORT).show();
                } else if (response.code() == 403) {
                    Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_403), Toast.LENGTH_SHORT).show();
                } else if (response.code() == 404) {
                    Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_404), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GalleryItems> call, Throwable t) {
                Log.e(TAG, "The call listFilesCall failed", t);
            }

        });

    }


    private void configViews(Response<GalleryItems> response) {

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), DEFAULT_SPAN_COUNT);

        mRecyclerView.setRecycledViewPool(new RecyclerView.RecycledViewPool());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(gridLayoutManager);


       List<GalleryItem> itemsGallery = new ArrayList<>();
       itemsGallery.addAll(response.body().getFiles());

        Collections.sort(itemsGallery, new Comparator<GalleryItem>() {
            @Override
            public int compare(GalleryItem o1, GalleryItem o2) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                try {
                    Date d1 = format.parse(o1.getCreatedTime());
                    Date d2 = format.parse(o2.getCreatedTime());
                    return d2.compareTo(d1);
                }
                catch (Exception e){
                    e.printStackTrace();
                    return 0;
                }
            }
        });

        for (int i = 0; i < response.body().getFiles().size(); i++) {
            GalleryItem galleryItem = itemsGallery.get(i);
            if(i != 0){
                //이전 것과 compare 했을 때, 날짜가 같지 않다면 header 생성해주기
                if(compareTime(itemsGallery.get(i-1), itemsGallery.get(i)) != 0){
                    mItemList.add(new HeaderItem(dateFormatting(galleryItem.getCreatedTime())));
                    mItemList.add(galleryItem);
                }
                else{ //같다면 사진만 추가해주기
                    mItemList.add(galleryItem);
                }
            }
            else {
                mItemList.add(new HeaderItem(dateFormatting(galleryItem.getCreatedTime())));
                mItemList.add(galleryItem);
            }
        }
        mAdapter = new CustomRecyclerViewAdapter(mItemList, gridLayoutManager, DEFAULT_SPAN_COUNT);

        mRecyclerView.setAdapter(mAdapter);

    }

    public int compareTime(GalleryItem o1, GalleryItem o2) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date d1 = format.parse(rfcToDate(o1.getCreatedTime()));
            Date d2 = format.parse(rfcToDate(o2.getCreatedTime()));
            return d2.compareTo(d1);
        }
        catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    public String rfcToDate(String originalTime){
        String newTime = "";

        try {
           SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
           Date date = format.parse(originalTime);
           SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
           newTime = s.format(date);
           return newTime;
       }
       catch (Exception e) {
           e.printStackTrace();
           return null;
       }

    }

    public String dateFormatting(String createdTime) {
        String newTime = "";

        try {

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            //   format.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = format.parse(createdTime);
            SimpleDateFormat s = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault());
            // s.setTimeZone(TimeZone.getDefault());
            newTime = s.format(date);
            return newTime;

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        }

    }


    public class CustomRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private int mDefaultSpanCount;
        private List<Item> itemObjects;

        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;

        public CustomRecyclerViewAdapter(List<Item> itemObjects, GridLayoutManager gridLayoutManager, int defaultSpanCount) {
            this.itemObjects = itemObjects;
            mDefaultSpanCount = defaultSpanCount;
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return isHeaderType(position) == TYPE_HEADER ? mDefaultSpanCount : 1;
                    //return position == 0 ? mDefaultSpanCount : 1; // 수정 예정
                }
            });
        }

        private int isHeaderType(int position) {
            int itemType = itemObjects.get(position).getItemType();
            Log.d(TAG, "ItemType = " + Integer.toString(itemType));
            return itemObjects.get(position).getItemType() == TYPE_HEADER ? 0 : 1;
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


            Item mObject = itemObjects.get(position);

            if (holder instanceof HeaderViewHolder) { //header 인 경우 binding
                ((HeaderViewHolder) holder).headerTitle.setText(((HeaderItem) mObject).getCreatedTime());

            } else if (holder instanceof ItemViewHolder) { //list item 인 경우 binding
                ImageView imageView = ((ItemViewHolder) holder).mPhotoImageView;

                Glide.with(MainActivity.this)
                        .load(((GalleryItem) mObject).getThumbnailLink())
                        .apply(new RequestOptions().placeholder(R.drawable.loading_img_small))
                        .into(imageView);
            }


        }

        private Item getItem(int position) {
            return itemObjects.get(position);
        }

        @Override
        public int getItemCount() {
            return itemObjects.size();
        }

        public void addItem(Item item) {
            mItemList.add(item);
            notifyDataSetChanged();
        }

        public void addAllItems(List<Item> items) {
            mItemList.addAll(items);
            notifyDataSetChanged();
        }


        @Override
        public int getItemViewType(int position) {
            return mItemList.get(position).getItemType();
        }


        public class HeaderViewHolder extends RecyclerView.ViewHolder {
            public TextView headerTitle;

            public HeaderViewHolder(View itemView) {
                super(itemView);
                headerTitle = (TextView) itemView.findViewById(R.id.headerTitle);
            }
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            public ImageView mPhotoImageView;

            public ItemViewHolder(View itemView) {
                super(itemView);
                mPhotoImageView = (ImageView) itemView.findViewById(R.id.fragment_gallery_image_view);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    GalleryItem galleryItem = (GalleryItem) itemObjects.get(position);
                    Intent intent = new Intent(view.getContext(), PhotoUploadActivity.class);
                    intent.putExtra(PhotoUploadActivity.EXTRA_SPACE_PHOTO, galleryItem);
                    startActivity(intent);
                }
            }

        }

    }


}

