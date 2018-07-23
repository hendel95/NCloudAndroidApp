package com.example.user.ncloudandroidapp;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class AllImageAdapter extends BaseAdapter {

    Context c;
    Cursor cur;
    ImageView imageView;
    int columnIndex;

    public AllImageAdapter(Context c, Cursor cur, int columnIndex) {
        this.c = c;
        this.cur = cur;
        this.columnIndex = columnIndex;
    }

    @Override
    public int getCount() {
        return cur.getCount();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Set The Image in Image View.
        imageView = new ImageView(c);
        cur.moveToPosition(position);
        int imageID = cur.getInt(columnIndex);
        imageView.setImageURI(Uri.withAppendedPath(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, "" + imageID));

       // imageView.setLayoutParams(new ImageView().LayoutParams(330, 330));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setPadding(4, 4, 4, 4);
        //convertView.setTag(imageView);
        return imageView;
    }

}

