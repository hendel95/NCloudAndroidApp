package com.example.user.ncloudandroidapp.Model;

public abstract class Item {

    public static final int HEADER_ITEM_TYPE = 0;
    public static final int GRID_ITEM_TYPE = 1;

    public abstract int getItemType();

}
