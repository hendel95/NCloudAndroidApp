package com.example.user.ncloudandroidapp.Model;

import com.example.user.ncloudandroidapp.Model.GalleryItem;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GalleryItems {

    @SerializedName("kind")
    private String kind;

    @SerializedName("nextPageToken")
    private String nextPageToken;

    @SerializedName("files")
    private List<GalleryItem> files;

    @SerializedName("length")

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("GDriveFiles{");
        sb.append(",\r\n kind='").append(kind).append('\'');
        sb.append(",\r\n nextPageToken='").append(nextPageToken).append('\'');
        sb.append("\r\nfiles=\r\n");
        for (GalleryItem file : files) {
            sb.append(file.toString("\t"));
        }
        sb.append('}');
        return sb.toString();
    }

}
