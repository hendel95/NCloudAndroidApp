package com.example.user.ncloudandroidapp;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

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
    @SerializedName("thumbnailLink")
    private String thumbnailLink;
    @SerializedName("mimeType")
    private String mimeType;

    public String cr = "\n";
    //다른 필드 더 추가하기 일단은 이렇게만!!


    protected GalleryItem(Parcel in) {
        id = in.readString();
        name = in.readString();
        thumbnailLink = in.readString();
        mimeType = in.readString();
        cr = in.readString();
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
        sb.append(indent).append(indent).append('}').append("\r\n");
        return sb.toString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(thumbnailLink);
    }


}
