package com.rocka.skinlibrary.attr.normal;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.RadioButton;

import com.rocka.skinlibrary.attr.core.SkinAttr;
import com.rocka.skinlibrary.utils.SkinResourcesUtils;

/**
 * @author: Rocka
 * @version: 1.0
 * @description: TODO
 * @time:2018/5/11
 */
public class RadioButtonAttr extends SkinAttr {

    @Override
    protected void applySkin(View view) {
        if (view instanceof RadioButton) {
            RadioButton radioButton = (RadioButton) view;
            Drawable drawable = SkinResourcesUtils.getDrawable(attrValueRefId);
            radioButton.setButtonDrawable(drawable);
        }
    }
}
