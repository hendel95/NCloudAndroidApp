package com.example.user.ncloudandroidapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.GridView;

import com.example.user.ncloudandroidapp.Adapter.GridViewAdapter;
import com.example.user.ncloudandroidapp.Adapter.LocalGalleryFolderAdapter;

/**
 * Created by deepshikha on 20/3/17.
 */

public class PhotosActivity extends AppCompatActivity {
    int int_position;
    private GridView gridView;
    GridViewAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_gallery);
        gridView = (GridView)findViewById(R.id.gv_folder);
        int_position = getIntent().getIntExtra("value", 0);
        adapter = new GridViewAdapter(this, LocalGalleryActivity.sLocalGalleryItems,int_position);
        gridView.setAdapter(adapter);
    }
}
