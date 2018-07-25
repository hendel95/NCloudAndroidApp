package com.example.user.ncloudandroidapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import com.example.user.ncloudandroidapp.Model.Item;
import com.example.user.ncloudandroidapp.Model.LocalGalleryItem;
import com.example.user.ncloudandroidapp.Model.LocalHeaderItem;
import com.example.user.ncloudandroidapp.PhotoUploadActivity;
import com.example.user.ncloudandroidapp.R;

import java.util.ArrayList;
import java.util.List;

public class LocalRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "LocalRecyclerViewAdapter";
    private List<Item> mItemList;
    private int mDefaultSpanCount;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    Context mContext;

    public LocalRecyclerViewAdapter(Context context, GridLayoutManager gridLayoutManager, int defaultSpanCount) {
        this.mContext = context;
        mItemList = new ArrayList<>();
        mDefaultSpanCount = defaultSpanCount;

        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return isItemType(position) == TYPE_ITEM ? mDefaultSpanCount : 1;
            }
        });
    }


    private int isItemType(int position) {
        //int itemType = itemObjects.get(position).getItemType();
        // Log.d(TAG, "ItemType = " + Integer.toString(itemType));
       /* if(isLastPosition(position) && isFooterAdded){
            return TYPE_FOOTER;
        }*/
        //  return mLocalGalleryItemList.get(position).getItemType() == TYPE_ITEM ? 0 : 1;
        return mItemList.get(position).getItemType() == TYPE_ITEM ? 0 : 1;
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


        Item item = mItemList.get(position);

        if (holder instanceof LocalRecyclerViewAdapter.HeaderViewHolder) {
            ((HeaderViewHolder) holder).headerTitle.setText(((LocalHeaderItem) item).getDateTakenTime());
        }
        else if (holder instanceof LocalRecyclerViewAdapter.ItemViewHolder) { //list item 인 경우 binding
            ImageView imageView = ((LocalRecyclerViewAdapter.ItemViewHolder) holder).mPhotoImageView;

            Log.i("URI=>",  ((LocalGalleryItem)item).getPath());
            Glide.with(mContext)
                    .load("file://" + ((LocalGalleryItem)item).getThumbnailPath())
                    .apply(new RequestOptions().placeholder(R.drawable.loading_img_small))
                    .into(imageView);
        }

    }

    public Item getItem(int position) {
        return mItemList.get(position);
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
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

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

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
                LocalGalleryItem localGalleryItem = (LocalGalleryItem) mItemList.get(position);
                Intent intent = new Intent(mContext, PhotoUploadActivity.class);
                intent.putExtra("class", TAG);
                intent.putExtra(PhotoUploadActivity.EXTRA_LOCAL_PHOTO, localGalleryItem);
                view.getContext().startActivity(intent);
            }
        }

    }

    public class LoadingViewHolder extends RecyclerView.ViewHolder {
        public LoadingViewHolder(View itemView) {
            super(itemView);
        }
    }

    public void add(Item item) {
        mItemList.add(item);
        notifyItemInserted(mItemList.size() - 1);
    }

    public void addAll(List<Item> items) {
        for (Item it : items) {
            add(it);
        }
    }

    /*
    public void clear() {
        isFooterAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }*/

    private void remove(Item item) {
        int position = mItemList.indexOf(item);
        if (position > -1) {
            mItemList.remove(position);
            notifyItemRemoved(position);
        }
    }
}
