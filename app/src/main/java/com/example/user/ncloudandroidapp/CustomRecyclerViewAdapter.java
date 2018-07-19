package com.example.user.ncloudandroidapp;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.user.ncloudandroidapp.Model.GalleryItem;
import com.example.user.ncloudandroidapp.Model.HeaderItem;
import com.example.user.ncloudandroidapp.Model.Item;

import java.util.ArrayList;
import java.util.List;

public class CustomRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "CustomRecyclerViewAdaper";
    private int mDefaultSpanCount;
    private List<Item> itemObjects =  new ArrayList<>();

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    Context mContext;

    public CustomRecyclerViewAdapter(Context context, List<Item> itemObjects, GridLayoutManager gridLayoutManager, int defaultSpanCount) {
        this.mContext = context;
        this.itemObjects = itemObjects;
        mDefaultSpanCount = defaultSpanCount;
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return isHeaderType(position) == TYPE_HEADER ? mDefaultSpanCount : 1;
            }
        });
    }

    private int isHeaderType(int position) {
        int itemType = itemObjects.get(position).getItemType();
       // Log.d(TAG, "ItemType = " + Integer.toString(itemType));
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

            Glide.with(mContext)
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


    @Override
    public int getItemViewType(int position) {
        return itemObjects.get(position).getItemType();
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
                Intent intent = new Intent(mContext, PhotoUploadActivity.class);
                intent.putExtra(PhotoUploadActivity.EXTRA_SPACE_PHOTO, galleryItem);
                view.getContext().startActivity(intent);
            }
        }

    }

}