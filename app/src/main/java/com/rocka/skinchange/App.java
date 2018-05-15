package com.rocka.skinchange;

import com.rocka.skinlibrary.attr.normal.RadioButtonAttr;
import com.rocka.skinlibrary.base.SkinBaseApp;
import com.rocka.skinlibrary.other.SkinConfig;

/**
 * @author: Rocka
 * @version: 1.0
 * @description: TODO
 * @time:2018/5/11
 */
public class App extends SkinBaseApp{

    @Override
    public void onCreate() {
        super.onCreate();

        SkinConfig.setCanChangeStatusColor(true);
        SkinConfig.setCanChangeFont(true);
        SkinConfig.addSupportAttr("button", new RadioButtonAttr());
        SkinConfig.enableGlobalSkinApply();
    }
}
