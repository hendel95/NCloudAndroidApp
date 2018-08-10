package com.example.user.ncloudandroidapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.RequestOptions;
import com.example.user.ncloudandroidapp.LocalDetailedImageActivity;
import com.example.user.ncloudandroidapp.Model.GalleryItem;
import com.example.user.ncloudandroidapp.Model.Item;
import com.example.user.ncloudandroidapp.Model.LocalGalleryItem;
import com.example.user.ncloudandroidapp.Model.LocalHeaderItem;
import com.example.user.ncloudandroidapp.R;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

            /*
            Bitmap bitmap = BitmapFactory.decodeFile(((LocalGalleryItem) item).getThumbnailPath());

            ExifInterface exif = null;
            try {
                exif = new ExifInterface(((LocalGalleryItem) item).getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            Log.i(TAG, "orientation =" + orientation);
            Bitmap bmRotated = rotateBitmap(bitmap, orientation);


            Glide.with(mContext)
                    .asBitmap()
                    .load(bmRotated)
                    .apply(new RequestOptions().placeholder(R.drawable.loading_img_small))
                    .into(imageView);

*/
            Glide.with(mContext)
                    .load(((LocalGalleryItem) item).getThumbnailPath())
                    .apply(new RequestOptions().placeholder(R.drawable.loading_img_small))
                    .into(imageView);

        }else if (holder instanceof LoadingViewHolder) {
        }

    }
    public class RotateTransformation extends BitmapTransformation {

        private float rotateRotationAngle = 0f;

        public RotateTransformation(Context context, float rotateRotationAngle) {
            //super(context);
            this.rotateRotationAngle = rotateRotationAngle;
        }

        @Override
        protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            Matrix matrix = new Matrix();

            matrix.postRotate(rotateRotationAngle);

            return Bitmap.createBitmap(toTransform, 0, 0, toTransform.getWidth(), toTransform.getHeight(), matrix, true);
        }

        @Override
        public void updateDiskCacheKey(MessageDigest messageDigest) {
            messageDigest.update(("rotate" + rotateRotationAngle).getBytes());
        }
    }


    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
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
                    itemCheckedStates.put(adapterPosition, true);
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
