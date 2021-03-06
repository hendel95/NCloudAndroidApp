package com.example.user.ncloudandroidapp.Model;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocalGalleryItem extends Item  implements Parcelable {

    @SerializedName("name")
    private String name;
    @SerializedName("path")
    private String path;
    @SerializedName("dateTakenTime")
    private String dateTakenTime;
    @SerializedName("thumbnailPath")
    private String thumbnailPath;
    @SerializedName("mimeType")
    private String mimeType;
    @SerializedName("uploadTime")
    private String uploadTime;
    @SerializedName("result")
    private int result;
    @SerializedName("progress")
    private int progress;

    private long id;
    //private Bitmap thumbnailBitmap;
    //private boolean isChecked;

    public LocalGalleryItem(){

    }

    public int getItemType() {
        return GRID_ITEM_TYPE;
    }

    public LocalGalleryItem(Parcel in){
        this.name = in.readString();
        this.path = in.readString();
        this.dateTakenTime = in.readString();
        this.thumbnailPath = in.readString();
        this.mimeType = in.readString();
        this.uploadTime = in.readString();
        this.result = in.readInt();


    }

    public static final Creator<LocalGalleryItem> CREATOR = new Creator<LocalGalleryItem>() {
        @Override
        public LocalGalleryItem createFromParcel(Parcel in) {
            return new LocalGalleryItem(in);
        }

        @Override
        public LocalGalleryItem[] newArray(int size) {

            return new LocalGalleryItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(path);
        dest.writeString(dateTakenTime);
        dest.writeString(thumbnailPath);
        dest.writeString(mimeType);
        dest.writeString(uploadTime);
        dest.writeInt(result);
    }

}
