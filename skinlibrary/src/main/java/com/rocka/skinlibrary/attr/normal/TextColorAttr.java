package com.rocka.skinlibrary.attr.normal;

import android.view.View;
import android.widget.TextView;

import com.rocka.skinlibrary.attr.core.SkinAttr;
import com.rocka.skinlibrary.utils.SkinResourcesUtils;



public class TextColorAttr extends SkinAttr {

    @Override
    protected void applySkin(View view) {
        if (view instanceof TextView) {
            TextView tv = (TextView) view;
            if (isColor()) {
                tv.setTextColor(SkinResourcesUtils.getColorStateList(attrValueRefId));
            }
        }
    }

    @Override
    protected void applyNightMode(View view) {
        if (view instanceof TextView) {
            TextView tv = (TextView) view;
            if (isColor()) {
                tv.setTextColor(SkinResourcesUtils.getNightColorStateList(attrValueRefId));
            }
        }
    }
}
