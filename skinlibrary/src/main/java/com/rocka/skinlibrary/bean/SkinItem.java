package com.rocka.skinlibrary.bean;

import android.view.View;

import com.rocka.skinlibrary.attr.core.SkinAttr;
import com.rocka.skinlibrary.utils.SkinOperUtils;

import java.util.ArrayList;
import java.util.List;



public class SkinItem {

    public View view;

    public List<SkinAttr> attrs;

    public SkinItem() {
        attrs = new ArrayList<>();
    }

    public void apply() {
        if (SkinOperUtils.isEmpty(attrs)) {
            return;
        }
        for (SkinAttr at : attrs) {
            at.apply(view);
        }
    }

    public void clean() {
        if (!SkinOperUtils.isEmpty(attrs)) {
            attrs.clear();
        }
    }

    @Override
    public String toString() {
        return "SkinItem [view=" + view.getClass().getSimpleName() + ", attrs=" + attrs + "]";
    }
}
