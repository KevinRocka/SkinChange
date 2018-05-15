package com.rocka.skinlibrary.base;

import android.app.Application;

import com.rocka.skinlibrary.core.SkinManager;

/**
 * @author: Rocka
 * @version: 1.0
 * @description: TODO
 * @time:2018/5/10
 */
public class SkinBaseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SkinManager.getInstance().init(this);
    }
}
