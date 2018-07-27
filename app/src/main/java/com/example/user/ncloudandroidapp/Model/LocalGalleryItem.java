package com.example.user.ncloudandroidapp.Model;

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

    @SerializedName("path")
    private String path;
    @SerializedName("dateTakenTime")
    private String dateTakenTime;
    @SerializedName("thumbnailPath")
    private String thumbnailPath;

    public LocalGalleryItem(){

    }

    public int getItemType() {
        return GRID_ITEM_TYPE;
    }
    public LocalGalleryItem(Parcel in){
        this.path = in.readString();
        this.dateTakenTime = in.readString();
        this.thumbnailPath = in.readString();
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
        dest.writeString(path);
        dest.writeString(dateTakenTime);
        dest.writeValue(thumbnailPath);
    }
}
