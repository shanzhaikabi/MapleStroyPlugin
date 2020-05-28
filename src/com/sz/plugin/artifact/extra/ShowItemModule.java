package com.sz.plugin.artifact.extra;

import com.sz.plugin.artifact.ExtraModule;

public class ShowItemModule implements ExtraModule {

    int itemId;

    public ShowItemModule(String itemId){
        this(Integer.valueOf(itemId));
    }

    private ShowItemModule(int itemId){
        this.itemId = itemId;
    }

    public int getItemId(){
        return this.itemId;
    }
}
