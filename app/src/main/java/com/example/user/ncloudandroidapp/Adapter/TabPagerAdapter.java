package com.example.user.ncloudandroidapp.Adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.user.ncloudandroidapp.GDriveGalleryFragment;
import com.example.user.ncloudandroidapp.LocalGalleryFragment;

public class TabPagerAdapter extends FragmentStatePagerAdapter {

    // Count number of tabs
    private int tabCount;

    public TabPagerAdapter(FragmentManager fm, int tabCount) {
        super(fm);
        this.tabCount = tabCount;
    }

    @Override
    public Fragment getItem(int position) {

        // Returning the current tabs
        switch (position) {
            case 0:
                GDriveGalleryFragment gDriveGalleryFragment = new GDriveGalleryFragment();
                return gDriveGalleryFragment;
            case 1:
                LocalGalleryFragment localGalleryFragment = new LocalGalleryFragment();
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
