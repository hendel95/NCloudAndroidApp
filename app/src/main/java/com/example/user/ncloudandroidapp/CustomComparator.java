package com.example.user.ncloudandroidapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class CustomComparator implements Comparator<GalleryItem> {

    @Override
    public int compare(GalleryItem o1, GalleryItem o2) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        try {
            //Date d1 = o1.getDate();
            //Date d2 = o2.getDate();
            Date d1 = format.parse(o1.getCreatedTime());
            Date d2 = format.parse(o2.getCreatedTime());
            return d2.compareTo(d1);
        }
        catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }
}
