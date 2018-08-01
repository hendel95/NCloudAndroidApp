package com.example.user.ncloudandroidapp.Model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GalleryItem extends Item implements Parcelable {

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
  //  @SerializedName("imageMediaMetadata/time")
  //  private String dateTakenTime;

    public String cr = "\n";


    protected GalleryItem(Parcel in){
        this.id = in.readString();
        this.name = in.readString();
        this.mimeType = in.readString();
        this.thumbnailLink = in.readString();
  //      this.dateTakenTime = in.readString();

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
    //    dest.writeString(dateTakenTime);
    }

    public int getItemType() {
        return GRID_ITEM_TYPE;
    }

}
