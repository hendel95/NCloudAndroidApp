package com.example.user.ncloudandroidapp.Room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface FileDao {
    @Query("SELECT * FROM upload_file ORDER BY mId DESC")
    List<UploadFile> getAllUploadedFiles();

    @Query("DELETE FROM upload_file")
    void clearAllUploadedFiles();

    @Query("SELECT * FROM download_file ORDER BY mId DESC")
    List<DownloadFile> getAllDownloadedFiles();

    @Query("DELETE FROM download_file")
    void clearAllDownloadedFiles();

    @Update
    void updateUploadFile(UploadFile uploadFile);

    @Delete
    void deleteUploadFile(UploadFile uploadFile);

    @Insert
    void insertUploadFile(UploadFile uploadFile);

    @Insert
    void insertDownloadFile(DownloadFile downloadFile);

}
