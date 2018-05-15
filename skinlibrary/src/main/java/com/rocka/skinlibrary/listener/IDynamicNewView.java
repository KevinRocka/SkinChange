package com.rocka.skinlibrary.listener;

import android.view.View;

import com.rocka.skinlibrary.attr.core.DynamicAttr;

import java.util.List;

/**
 * @author: Rocka
 * @version: 1.0
 * @description: TODO
 * @time:2018/5/10
 */
public interface IDynamicNewView {

    void dynamicAddView(View view, List<DynamicAttr> pDAttrs);

    void dynamicAddView(View view, String attrName, int attrValueResId);
}
