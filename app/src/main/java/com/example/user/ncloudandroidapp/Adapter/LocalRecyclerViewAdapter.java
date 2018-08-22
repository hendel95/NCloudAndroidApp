package com.example.user.ncloudandroidapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.user.ncloudandroidapp.Controller.LocalDetailedImageActivity;
import com.example.user.ncloudandroidapp.Model.Item;
import com.example.user.ncloudandroidapp.Model.LocalGalleryItem;
import com.example.user.ncloudandroidapp.Model.LocalHeaderItem;
import com.example.user.ncloudandroidapp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocalRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "LocalRecyclerViewAdapter";
    private List<Item> mItemList;
    private int mDefaultSpanCount;
    private boolean isModeChanged;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;

    Context mContext;

    protected boolean isFooterAdded = false;

    // private SparseBooleanArray itemStateArray = new SparseBooleanArray();
    private HashMap<Integer, Boolean> itemCheckedStates = new HashMap<>();


    public LocalRecyclerViewAdapter(Context context, GridLayoutManager gridLayoutManager, int defaultSpanCount, boolean isModeChanged) {
        this.mContext = context;
        mItemList = new ArrayList<>();
        mDefaultSpanCount = defaultSpanCount;
        this.isModeChanged = isModeChanged;
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
            View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_item, parent, false);
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


        final Item item = mItemList.get(position);

        if (holder instanceof LocalRecyclerViewAdapter.HeaderViewHolder) {
            ((HeaderViewHolder) holder).headerTitle.setText(((LocalHeaderItem) item).getDateTakenTime());
        } else if (holder instanceof LocalRecyclerViewAdapter.ItemViewHolder) { //list item 인 경우 binding
            ImageView imageView = ((LocalRecyclerViewAdapter.ItemViewHolder) holder).mPhotoImageView;

            Log.i("URI=>", ((LocalGalleryItem) item).getPath());

            CheckBox checkBox = ((LocalRecyclerViewAdapter.ItemViewHolder) holder).mCheckBox;

            if(itemCheckedStates.get(position) != null){
                checkBox.setChecked(true);
            }else{
                checkBox.setChecked(false);
            }

            if(isModeChanged){
                checkBox.setVisibility(View.VISIBLE);
            }
            else{
                checkBox.setVisibility(View.GONE);
            }



            Glide.with(mContext)
                    .load(((LocalGalleryItem) item).getThumbnailPath())
                    .apply(new RequestOptions().placeholder(R.drawable.loading_img_small).error(R.drawable.error_img))
                    .into(imageView);

        }else if (holder instanceof LoadingViewHolder) {
        }

    }


    public void setModeChanged(boolean modeChanged){
        this.isModeChanged = modeChanged;
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

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView mPhotoImageView;
        public CheckBox mCheckBox;


        public ItemViewHolder(View itemView) {
            super(itemView);
            mPhotoImageView = (ImageView) itemView.findViewById(R.id.fragment_gallery_image_view);
            mCheckBox = (CheckBox) itemView.findViewById(R.id.check_box);

            itemView.setOnClickListener(this);

            mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int adapterPosition = getAdapterPosition();
                    if(isChecked == true) {
                        itemCheckedStates.put(adapterPosition, isChecked);
                    }else{
                        itemCheckedStates.remove(adapterPosition);
                    }
                }
            });


        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                if(isModeChanged){
                    int adapterPosition = getAdapterPosition();
                    if(itemCheckedStates.get(adapterPosition) != null){
                        itemCheckedStates.remove(adapterPosition);
                        mCheckBox.setChecked(false);

                        Log.d(TAG, "Item Removed" + adapterPosition);

                    }
                    else {
                        itemCheckedStates.put(adapterPosition, true);
                        mCheckBox.setChecked(true);
                        Log.d(TAG, "Item Checked" + adapterPosition);

                    }
                }else {
                    LocalGalleryItem localGalleryItem = (LocalGalleryItem) mItemList.get(position);
                    Intent intent = new Intent(mContext, LocalDetailedImageActivity.class);
                    intent.putExtra("class", TAG);
                    intent.putExtra(LocalDetailedImageActivity.EXTRA_LOCAL_PHOTO, localGalleryItem);
                    view.getContext().startActivity(intent);
                }
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
        //notifyDataSetChanged();
    }

    public void addAll(List<Item> items) {
        for (Item it : items) {
            add(it);
        }
    }


    public void clear() {
        //    isFooterAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    private void remove(Item item) {
        int position = mItemList.indexOf(item);
        if (position > -1) {
            mItemList.remove(position);
            notifyItemRemoved(position);
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

    public void removeItems(List<Item> itemList){

        for(Item item : itemList){
            remove(item);
        }
    }


    public void removeFooter() {
        isFooterAdded = false;

        int position = mItemList.size() - 1;
        Item item = getItem(position);

        if (item != null) {
            mItemList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public HashMap<Integer, Boolean> getItemStateArray() {
        return itemCheckedStates;
    }

    public void clearStateArray(){
        itemCheckedStates.clear();
    }



}