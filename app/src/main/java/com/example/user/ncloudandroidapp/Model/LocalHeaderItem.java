package com.example.user.ncloudandroidapp.Model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocalHeaderItem extends Item{
    @SerializedName("dateTakenTime")
    private String dateTakenTime;

    public LocalHeaderItem(String dateTakenTime){
        this.dateTakenTime = dateTakenTime;
    }

    public int getItemType(){
        return HEADER_ITEM_TYPE;
    }

}
