package com.rocka.skinlibrary.attr.normal;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import com.rocka.skinlibrary.attr.core.SkinAttr;
import com.rocka.skinlibrary.utils.SkinResourcesUtils;

public class DrawableTopAttr extends SkinAttr {

    @Override
    protected void applySkin(View view) {
        if (view instanceof TextView) {
            TextView tv = (TextView) view;
            if (isDrawable()) {
                Drawable bg = SkinResourcesUtils.getDrawable(attrValueRefId);
                bg.setBounds(0, 0, bg.getMinimumWidth(), bg.getMinimumHeight());
                tv.setCompoundDrawablesWithIntrinsicBounds(null, bg, null, null);
            }
        }
    }
}
