package com.example.user.ncloudandroidapp.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.user.ncloudandroidapp.Model.GalleryItem;
import com.example.user.ncloudandroidapp.Model.HeaderItem;
import com.example.user.ncloudandroidapp.Model.Item;
import com.example.user.ncloudandroidapp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DownloadResultRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "DownloadResultRecyclerViewAdapter";
    private List<Item> itemObjects;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;


    protected boolean isFooterAdded = false;


    // private SparseBooleanArray itemStateArray = new SparseBooleanArray();
    private HashMap<Integer, Boolean> itemCheckedStates = new HashMap<>();

    Context mContext;

    public DownloadResultRecyclerViewAdapter(Context context) {
        this.mContext = context;
        itemObjects = new ArrayList<>();

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
            View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.room_header_item, parent, false);
            return new HeaderViewHolder(layoutView);
        } else if (viewType == TYPE_ITEM) {
            View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.room_item, parent, false);
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
            ((HeaderViewHolder) holder).mHeaderTitle.setText(((HeaderItem) mObject).getCreatedTime());
        } else if (holder instanceof ItemViewHolder) { //list item 인 경우 binding
            ImageView imageView = ((ItemViewHolder) holder).mImageView;
            TextView titleText = ((ItemViewHolder) holder).mTitleText;
            TextView dateText = ((ItemViewHolder) holder).mDateText;
          //  ProgressBar progressBar = ((ItemViewHolder)holder).mProgressBar;

            Glide.with(mContext)
                    .load(((GalleryItem) mObject).getThumbnailLink())
                    .apply(new RequestOptions().placeholder(R.drawable.loading_img_small).centerCrop())
                    .into(imageView);

            titleText.setText(((GalleryItem) mObject).getName());

            switch (((GalleryItem)mObject).getResult()){
                case Item.DOWNLOAD_SUCCESS:
                    dateText.setText(((GalleryItem) mObject).getDownloadTime());
                    break;

                case Item.DOWNLOAD_DUPLICATED:
                    dateText.setText(R.string.duplicated_file_name);
                    break;

                case Item.DOWNLOAD_FAILED:
                    dateText.setText(R.string.download_failed);
            }

        }

    }



    public Item getItem(int position) {
        return itemObjects.get(position);
    }

    @Override
    public int getItemCount() {
        return itemObjects.size();
    }

    public HashMap<Integer, Boolean> getItemStateArray() {
        return itemCheckedStates;
    }

    public void clearStateArray() {
        itemCheckedStates.clear();
    }

    @Override
    public int getItemViewType(int position) {
        return itemObjects.get(position).getItemType();
    }


    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.room_header_text)
        TextView mHeaderTitle;
        @BindView(R.id.room_header_flag_text)
        TextView mFlagTextView;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mFlagTextView.setText("LOCAL:");
        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {


        @BindView(R.id.room_result_image_view)
        ImageView mImageView;

        @BindView(R.id.room_result_title_text)
        TextView mTitleText;

        @BindView(R.id.room_result_date_text)
        TextView mDateText;

       // @BindView(R.id.room_result_progressbar)
       // ProgressBar mProgressBar;

        public ItemViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
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

    public void addGdriveItem(GalleryItem item){
        itemObjects.add((Item)item);
        notifyItemInserted(itemObjects.size() - 1);
    }

    public void addAllGdriveItems(List<GalleryItem> items){
        for(GalleryItem item : items){
            addGdriveItem(item);
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