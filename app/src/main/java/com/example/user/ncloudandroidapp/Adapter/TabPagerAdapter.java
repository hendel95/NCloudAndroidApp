package com.example.user.ncloudandroidapp.Adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.user.ncloudandroidapp.Controller.GDriveGalleryFragment;
import com.example.user.ncloudandroidapp.Controller.LocalGalleryFragment;

public class TabPagerAdapter extends FragmentStatePagerAdapter {

    // Count number of tabs
    private int tabCount;
    private static GDriveGalleryFragment gDriveGalleryFragment = new GDriveGalleryFragment();
    private static LocalGalleryFragment localGalleryFragment = new LocalGalleryFragment();


    public TabPagerAdapter(FragmentManager fm, int tabCount) {
        super(fm);
        this.tabCount = tabCount;
    }

    @Override
    public Fragment getItem(int position) {

        // Returning the current tabs
        switch (position) {
            case 0:
                //GDriveGalleryFragment gDriveGalleryFragment = new GDriveGalleryFragment();
                return gDriveGalleryFragment;
            case 1:
                //LocalGalleryFragment localGalleryFragment = new LocalGalleryFragment();
                return localGalleryFragment;


            default:
                return null;
        }
    }



    @Override
    public int getCount() {
        return tabCount;
    }
}
