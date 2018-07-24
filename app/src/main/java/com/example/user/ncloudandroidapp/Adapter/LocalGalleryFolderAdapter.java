package com.example.user.ncloudandroidapp.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.user.ncloudandroidapp.Model.LocalGalleryItem;
import com.example.user.ncloudandroidapp.R;

import java.util.ArrayList;

public class LocalGalleryFolderAdapter extends ArrayAdapter<LocalGalleryItem> {
    Context context;
    ViewHolder viewHolder;
    ArrayList<LocalGalleryItem> al_menu = new ArrayList<>();


    public LocalGalleryFolderAdapter(Context context, ArrayList<LocalGalleryItem> al_menu) {
        super(context, R.layout.adapter_photosfolder, al_menu);
        this.al_menu = al_menu;
        this.context = context;


    }

    @Override
    public int getCount() {

        Log.e("ADAPTER LIST SIZE", al_menu.size() + "");
        return al_menu.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        if (al_menu.size() > 0) {
            return al_menu.size();
        } else {
            return 1;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {

            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_photosfolder, parent, false);
            viewHolder.tv_foldern = (TextView) convertView.findViewById(R.id.tv_folder);
            viewHolder.tv_foldersize = (TextView) convertView.findViewById(R.id.tv_folder2);
            viewHolder.iv_image = (ImageView) convertView.findViewById(R.id.iv_image);


            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

       // viewHolder.tv_foldern.setText(al_menu.get(position).getFolder());
        viewHolder.tv_foldersize.setText(al_menu.get(position).getPath().size() + "");


        Glide.with(context).load("file://" + al_menu.get(position).getPath().get(0))
                .apply(RequestOptions.fitCenterTransform().error(R.drawable.error_img)
                        .placeholder(R.drawable.loading_img))
                .into(viewHolder.iv_image);


        return convertView;

    }

    private static class ViewHolder {
        TextView tv_foldern, tv_foldersize;
        ImageView iv_image;


    }
}
