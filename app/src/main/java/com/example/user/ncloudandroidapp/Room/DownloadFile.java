package com.example.user.ncloudandroidapp.Room;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(tableName = "download_file")
public class DownloadFile {

    @PrimaryKey(autoGenerate = true)
    private long mId;

    @ColumnInfo(name="name")
    private String name;

    @ColumnInfo(name="thumbnail_path")
    private String thumbnailPath;

    @ColumnInfo(name="date")
    private String date;

    @ColumnInfo(name="result")
    private int result;

    public DownloadFile(String name, String thumbnailPath , String date, int result){
        this.name = name;
        this.thumbnailPath = thumbnailPath;
        this.date = date;
        this.result = result;
    }

}