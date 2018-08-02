package com.example.user.ncloudandroidapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.menu.MenuView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.user.ncloudandroidapp.GDriveDetailedImageActivity;
import com.example.user.ncloudandroidapp.Model.GalleryItem;
import com.example.user.ncloudandroidapp.Model.HeaderItem;
import com.example.user.ncloudandroidapp.Model.Item;
import com.example.user.ncloudandroidapp.R;

import java.util.ArrayList;
import java.util.List;

public class GDriveRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "GDriveRecyclerViewAdapter";
    private int mDefaultSpanCount;
    private List<Item> itemObjects;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;

    protected boolean isFooterAdded = false;


    Context mContext;

    public GDriveRecyclerViewAdapter(Context context, GridLayoutManager gridLayoutManager, int defaultSpanCount) {
        this.mContext = context;
        mDefaultSpanCount = defaultSpanCount;
        itemObjects = new ArrayList<>();
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
        return itemObjects.get(position).getItemType() == TYPE_ITEM ? 0 : 1;

    }


    public boolean isLastPosition(int position) {
        return (position == itemObjects.size() - 1);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_layout, parent, false);
            return new HeaderViewHolder(layoutView);
        } else if (viewType == TYPE_ITEM) {
            View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_item, parent, false);
            return new ItemViewHolder(layoutView);
        } else if (viewType == TYPE_FOOTER) {
            View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.progress_item, parent, false);
            return new LoadingViewHolder(layoutView);
        }
        throw new RuntimeException("No match for " + viewType + ".");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {


        final Item mObject = itemObjects.get(position);

        if (holder instanceof HeaderViewHolder) { //header 인 경우 binding
            ((HeaderViewHolder) holder).headerTitle.setText(((HeaderItem) mObject).getCreatedTime());

        } else if (holder instanceof ItemViewHolder) { //list item 인 경우 binding
            ImageView imageView = ((ItemViewHolder) holder).mPhotoImageView;

            CheckBox checkBox = ((ItemViewHolder) holder).mCheckBox;

            checkBox.setOnCheckedChangeListener(null);

            checkBox.setChecked(((GalleryItem) mObject).isChecked());

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ((GalleryItem) mObject).setChecked(isChecked);
                }
            });

            Glide.with(mContext)
                    .load(((GalleryItem) mObject).getThumbnailLink())
                    .apply(new RequestOptions().placeholder(R.drawable.loading_img_small))
                    .into(imageView);
        } else if (holder instanceof LoadingViewHolder) {
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
        public CheckBox mCheckBox;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mPhotoImageView = (ImageView) itemView.findViewById(R.id.fragment_gallery_image_view);
            mCheckBox = (CheckBox) itemView.findViewById(R.id.check_box);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();

            if (position != RecyclerView.NO_POSITION) {
                GalleryItem galleryItem = (GalleryItem) itemObjects.get(position);
                Intent intent = new Intent(mContext, GDriveDetailedImageActivity.class);
                intent.putExtra("class", TAG);
                intent.putExtra(GDriveDetailedImageActivity.EXTRA_GDRIVE_PHOTO, galleryItem);
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
        itemObjects.add(item);
        notifyItemInserted(itemObjects.size() - 1);
    }

    public void addAll(List<Item> items) {
        for (Item it : items) {
            add(it);
        }
    }

    public void addFooter() {
        isFooterAdded = true;
        add(new Item() {
            @Override
            public int getItemType() {
                return TYPE_FOOTER;
            }
        });
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

    public void removeFooter() {
        isFooterAdded = false;

        int position = itemObjects.size() - 1;
        Item item = getItem(position);

        if (item != null) {
            itemObjects.remove(position);
            notifyItemRemoved(position);
        }
    }

}