package com.example.user.ncloudandroidapp.Room;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(tableName = "download_file")
public class DownloadFile {
    @PrimaryKey(autoGenerate = true)
    private long mId;

    @ColumnInfo(name="name")
    private String mName;

    @ColumnInfo(name="thumbnail_path")
    private String mThumbnailPath;

    @ColumnInfo(name="date")
    private String mDate;

/*    @ColumnInfo(name="mime_type")
    private String mMimeType;

    @ColumnInfo(name="date_taken_time")
    private String mDateTakenTime;

    @ColumnInfo(name="path")
    private String mPath;
*/

    public DownloadFile(String name, String thumbnailPath , String date){
        setMName(name);
        setMThumbnailPath(thumbnailPath);
        setMDate(date);
    }

}