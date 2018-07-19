package com.example.user.ncloudandroidapp.Model;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeaderItem extends Item {

    @SerializedName("createdTime")
    private String createdTime;

    @SerializedName("imageSize")
    private int imageSize;

    public HeaderItem(String createdTime){

        this.createdTime = createdTime;
    }

    public int getItemType(){
        return HEADER_ITEM_TYPE;
    }

}
