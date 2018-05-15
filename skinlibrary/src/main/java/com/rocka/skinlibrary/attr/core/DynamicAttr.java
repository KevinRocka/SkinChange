package com.rocka.skinlibrary.attr.core;

import android.support.annotation.AnyRes;

public class DynamicAttr {
    /**
     * attr name,defined from {@link AttrFactory}
     */
    public String attrName;

    /**
     * resource id from default context,eg: "R.drawable.app_bg"
     */
    public int refResId;

    public DynamicAttr(String attrName, @AnyRes int refResId) {
        this.attrName = attrName;
        this.refResId = refResId;
    }
}