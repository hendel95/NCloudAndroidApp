package com.example.user.ncloudandroidapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.user.ncloudandroidapp.Model.GalleryItem;
import com.example.user.ncloudandroidapp.Model.HeaderItem;
import com.example.user.ncloudandroidapp.Model.Item;
import com.example.user.ncloudandroidapp.Model.LocalGalleryItem;
import com.example.user.ncloudandroidapp.PhotoUploadActivity;
import com.example.user.ncloudandroidapp.R;

import java.util.ArrayList;
import java.util.List;

public class LocalRecyclerViewAdapter extends ArrayAdapter<LocalGalleryItem> {
    private static final String TAG = "LocalRecyclerViewAdapter";
    private List<LocalGalleryItem> mLocalGalleryItemList;

    Context mContext;

    public LocalRecyclerViewAdapter(Context context){
        this.mContext = context;
        mLocalGalleryItemList = new ArrayList<>();
    }



    private int isItemType(int position) {
        //int itemType = itemObjects.get(position).getItemType();
        // Log.d(TAG, "ItemType = " + Integer.toString(itemType));
       /* if(isLastPosition(position) && isFooterAdded){
            return TYPE_FOOTER;
        }*/
        return itemObjects.get(position).getItemType() == TYPE_ITEM ? 0 : 1;

    }



    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {


        Item mObject = itemObjects.get(position);

        if (holder instanceof CustomRecyclerViewAdapter.HeaderViewHolder) { //header 인 경우 binding
            ((CustomRecyclerViewAdapter.HeaderViewHolder) holder).headerTitle.setText(((HeaderItem) mObject).getCreatedTime());

        } else if (holder instanceof CustomRecyclerViewAdapter.ItemViewHolder) { //list item 인 경우 binding
            ImageView imageView = ((CustomRecyclerViewAdapter.ItemViewHolder) holder).mPhotoImageView;

            Glide.with(mContext)
                    .load(((GalleryItem) mObject).getThumbnailLink())
                    .apply(new RequestOptions().placeholder(R.drawable.loading_img_small))
                    .into(imageView);
        }
        else if(holder instanceof CustomRecyclerViewAdapter.LoadingViewHolder){
            //  ((LoadingViewHolder)holder)
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

    public class LoadingViewHolder extends RecyclerView.ViewHolder {
        public LoadingViewHolder(View itemView){
            super(itemView);
        }
    }

    public void add(Item item) {
        itemObjects.add(item);
        notifyItemInserted(itemObjects.size() - 1);
    }

    public void addAll(List<Item> items) {
        for (Item it : items) {
            add(it);
        }
    }


    public void clear() {
        isFooterAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    private void remove(Item item) {
        int position = itemObjects.indexOf(item);
        if (position > -1) {
            itemObjects.remove(position);
            notifyItemRemoved(position);
        }
    }


}
