package com.example.user.ncloudandroidapp.Room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {UploadFile.class, DownloadFile.class}, version = 1)
public abstract class FileDatabase extends RoomDatabase {

    private static FileDatabase INSTANCE ;

    public abstract FileDao getFileDao();

    public static FileDatabase getDatabase(Context mContext) {
        if (INSTANCE  == null) {
            INSTANCE = Room.databaseBuilder(mContext.getApplicationContext(), FileDatabase.class, "CustomDatabase.db")
                    .allowMainThreadQueries()
                    .build();
        }

        return INSTANCE ;
    }

    public static void destroyInstance() {
        INSTANCE  = null;
    }
}

