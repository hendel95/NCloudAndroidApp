package com.example.user.ncloudandroidapp;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GalleryItem implements Parcelable{

    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private String name;
    @SerializedName("mimeType")
    private String mimeType;
    @SerializedName("thumbnailLink")
    private String thumbnailLink;
    @SerializedName("createdTime")
    private String createdTime;

    private  int HEADER_ITEM_TYPE = 0;
    private int GRID_ITEM_TYPE = 1;

    public String cr = "\n";
    //다른 필드 더 추가하기 일단은 이렇게만!!

    protected GalleryItem(Parcel in){
        this.id = in.readString();
        this.name = in.readString();
        this.mimeType = in.readString();
        this.thumbnailLink = in.readString();

    }

    public static final Creator<GalleryItem> CREATOR = new Creator<GalleryItem>() {
        @Override
        public GalleryItem createFromParcel(Parcel in) {
            return new GalleryItem(in);
        }

        @Override
        public GalleryItem[] newArray(int size) {

            return new GalleryItem[size];
        }
    };

    public String toString(String indent) {
        final StringBuffer sb = new StringBuffer(indent+"GDriveFile{\r\n");
        sb.append(indent).append(indent).append("id='").append(id).append('\'').append(cr);
        sb.append(indent).append(indent).append(", name='").append(name).append('\'').append(cr);
        sb.append(indent).append(indent).append(", mimeType='").append(mimeType).append('\'').append(cr);
        sb.append(indent).append(indent).append(", thumbnailLink='").append(thumbnailLink).append('\'').append(cr);
        sb.append(indent).append(indent).append(", createdTime='").append(createdTime).append('\'').append(cr);
        sb.append(indent).append(indent).append('}').append("\r\n");
        return sb.toString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(mimeType);
        dest.writeString(thumbnailLink);
    }

    public Date getDate(String dateStr){
        SimpleDateFormat s;

        s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        s.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            return s.parse(dateStr);
        }catch (ParseException e){
            e.printStackTrace();
            return null;
        }
    }

    public int getItemType() {
        return GRID_ITEM_TYPE;
    }


}
