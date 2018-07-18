package com.example.user.ncloudandroidapp;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeaderItem extends Item{

    @SerializedName("createdTime")
    private String createdTime;

    public HeaderItem(String createdTime){
        this.createdTime = createdTime;
    }
    public int getItemType(){
        return HEADER_ITEM_TYPE;
    }

}
