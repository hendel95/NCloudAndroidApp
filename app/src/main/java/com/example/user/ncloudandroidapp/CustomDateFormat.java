package com.example.user.ncloudandroidapp;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;

import com.example.user.ncloudandroidapp.Model.GalleryItem;
import com.example.user.ncloudandroidapp.Model.Item;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CustomDateFormat{

    //RFC3339 (String) -> Date Type formatting !
    public Date StringToDate(String time){
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            Date date = format.parse(time);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Date Type -> String (Header / Grid)
    public String DateToString(Date date, int type){
        String newTime = "";
        SimpleDateFormat s;
        //yyyy년 MM월 dd일
        if(type == Item.HEADER_ITEM_TYPE){
            s = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault());
            newTime = s.format(date);
            return newTime;
        }
        else{//yyyy-MM-dd
            s = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            newTime = s.format(date);
            return newTime;
        }
    }


    public int compareTime(String t1, String t2) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date d1 = format.parse(dateFormatting(t1, Item.GRID_ITEM_TYPE));
            Date d2 = format.parse(dateFormatting(t2, Item.GRID_ITEM_TYPE));
            return d2.compareTo(d1);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int compareTime(Date t1, Date t2) {
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());

        return compareTime(s.format(t1), s.format(t2));
    }

/*
    public int compareDate(String t1,String t2) {
        Date d1 = StringToDate(t1);
        Date d2 = StringToDate(t2);
        return d2.compareTo(d1);
    }
*/

    public String dateFormatting(String createdTime, int type) {
        String newTime = DateToString(StringToDate(createdTime), type);
        return newTime;
    }


    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

}
