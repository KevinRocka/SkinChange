package com.rocka.skinlibrary.core;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.rocka.skinlibrary.attr.core.AttrFactory;
import com.rocka.skinlibrary.attr.core.DynamicAttr;
import com.rocka.skinlibrary.attr.core.SkinAttr;
import com.rocka.skinlibrary.bean.SkinItem;
import com.rocka.skinlibrary.other.SkinConfig;
import com.rocka.skinlibrary.utils.SkinOperUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Rocka
 * @version: 1.0
 * @description: XML->View 核心类
 * @time:2018/5/10
 */
public class SkinInflaterFactory implements LayoutInflater.Factory2 {

    private final String TAG = SkinInflaterFactory.class.getName();

    private AppCompatActivity mAppCompatActivity;

    /**
     * 存储那些有皮肤更改需求的View及其对应的属性的集合
     */
    private Map<View, SkinItem> mSkinItemMap = new HashMap<>();

    public SkinInflaterFactory(AppCompatActivity appCompatActivity) {
        this.mAppCompatActivity = appCompatActivity;
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        boolean isSkinEnable = attrs.getAttributeBooleanValue(SkinConfig.NAME_SPACE, SkinConfig.ATTR_SKIN_ENABLE, false);
        AppCompatDelegate delegate = mAppCompatActivity.getDelegate();
        View view = delegate.createView(parent, name, context, attrs);
        if (view instanceof TextView && SkinConfig.isCanChangeFont()) {
            TextViewRepository.add(mAppCompatActivity, (TextView) view);
        }

        if (isSkinEnable || SkinConfig.isGlobalSkinApply()) {
            if (view == null) {
                view = createView(context, name, attrs);
            }
            if (view == null) {
                return null;
            }
            parseSkinAttr(context, attrs, view);
        }
        return view;
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return null;
    }

    /**
     * 搜集View中换肤的时候可以更改的属性
     *
     * @param context
     * @param attrs
     * @param view
     */
    private void parseSkinAttr(Context context, AttributeSet attrs, View view) {
        //存储View可更换皮肤的属性的合集
        List<SkinAttr> viewAttrs = new ArrayList<>();
        Log.i(TAG, "viewName:" + view.getClass().getSimpleName());
        for (int i = 0; i < attrs.getAttributeCount(); i++) {
            String attrName = attrs.getAttributeName(i);
            String attrValue = attrs.getAttributeValue(i);
            Log.i(TAG, "AttributeName:" + attrName + "|attrValue:" + attrValue);
            //处理下XML属性为style
            if ("style".equals(attrName)) {
                int[] skinAttrs = new int[]{android.R.attr.textColor, android.R.attr.background};
                TypedArray a = context.getTheme().obtainStyledAttributes(attrs, skinAttrs, 0, 0);
                int textColorId = a.getResourceId(0, -1);
                int backgroundId = a.getResourceId(1, -1);
                if (textColorId != -1) {
                    String entryName = context.getResources().getResourceEntryName(textColorId);
                    String typeName = context.getResources().getResourceTypeName(textColorId);
                    SkinAttr skinAttr = AttrFactory.get("textColor", textColorId, entryName, typeName);
                    Log.w(TAG, "    textColor in style is supported:" + "\n" +
                            "    resource id:" + textColorId + "\n" +
                            "    attrName:" + attrName + "\n" +
                            "    attrValue:" + attrValue + "\n" +
                            "    entryName:" + entryName + "\n" +
                            "    typeName:" + typeName);
                    if (skinAttr != null) {
                        viewAttrs.add(skinAttr);
                    }
                }
                if (backgroundId != -1) {
                    String entryName = context.getResources().getResourceEntryName(backgroundId);
                    String typeName = context.getResources().getResourceTypeName(backgroundId);
                    SkinAttr skinAttr = AttrFactory.get("background", backgroundId, entryName, typeName);
                    Log.w(TAG, "    background in style is supported:" + "\n" +
                            "    resource id:" + backgroundId + "\n" +
                            "    attrName:" + attrName + "\n" +
                            "    attrValue:" + attrValue + "\n" +
                            "    entryName:" + entryName + "\n" +
                            "    typeName:" + typeName);
                    if (skinAttr != null) {
                        viewAttrs.add(skinAttr);
                    }

                }
                a.recycle();
                continue;
            }
            //endregion
            //if attrValue is reference，eg:@color/red
            if (AttrFactory.isSupportedAttr(attrName) && attrValue.startsWith("@")) {
                try {
                    //resource id
                    int id = Integer.parseInt(attrValue.substring(1));
                    if (id == 0) {
                        continue;
                    }
                    //entryName，eg:text_color_selector
                    String entryName = context.getResources().getResourceEntryName(id);
                    //typeName，eg:color、drawable
                    String typeName = context.getResources().getResourceTypeName(id);
                    SkinAttr mSkinAttr = AttrFactory.get(attrName, id, entryName, typeName);
                    Log.w(TAG, "    " + attrName + " is supported:" + "\n" +
                            "    resource id:" + id + "\n" +
                            "    attrName:" + attrName + "\n" +
                            "    attrValue:" + attrValue + "\n" +
                            "    entryName:" + entryName + "\n" +
                            "    typeName:" + typeName
                    );
                    if (mSkinAttr != null) {
                        viewAttrs.add(mSkinAttr);
                    }
                } catch (NumberFormatException e) {
                    e.toString();
                }
            }
        }

        if (!SkinOperUtils.isEmpty(viewAttrs)) {
            SkinItem skinItem = new SkinItem();
            skinItem.view = view;
            skinItem.attrs = viewAttrs;
            mSkinItemMap.put(skinItem.view, skinItem);
            if (SkinManager.getInstance().isExternalSkin() ||
                    SkinManager.getInstance().isNightMode()) {//如果当前皮肤来自于外部或者是处于夜间模式
                skinItem.apply();
            }
        }
    }


    /**
     * 通过name去实例化一个view，其实就是动态的去创建View
     *
     * @param context
     * @param name    被实例化的全称
     * @param attrs   View在布局文件中的XML属性
     * @return 返回实例化成功的view或者返回null.
     */
    private View createView(Context context, String name, AttributeSet attrs) {
        View view = null;
        try {
            if (-1 == name.indexOf('.')) {
                if ("View".equals(name)) {
                    view = LayoutInflater.from(context).createView(name, "android.view.", attrs);
                }
                if (view == null) {
                    view = LayoutInflater.from(context).createView(name, "android.widget.", attrs);
                }
                if (view == null) {
                    view = LayoutInflater.from(context).createView(name, "android.webkit.", attrs);
                }
            } else {
                view = LayoutInflater.from(context).createView(name, null, attrs);
            }

            Log.i(TAG, "about to create " + name);

        } catch (Exception e) {
            Log.e(TAG, "error while create 【" + name + "】 : " + e.getMessage());
            view = null;
        }
        return view;
    }

    public void clean() {
        for (View view : mSkinItemMap.keySet()) {
            if (view == null) {
                continue;
            }
            mSkinItemMap.get(view).clean();
        }
        TextViewRepository.remove(mAppCompatActivity);
        mSkinItemMap.clear();
        mSkinItemMap = null;
    }

    public void dynamicAddSkinEnableView(Context context, View view, List<DynamicAttr> attrs) {
        List<SkinAttr> viewAttrs = new ArrayList<>();
        SkinItem skinItem = new SkinItem();
        skinItem.view = view;

        for (DynamicAttr dAttr : attrs) {
            int id = dAttr.refResId;
            String entryName = context.getResources().getResourceEntryName(id);
            String typeName = context.getResources().getResourceTypeName(id);
            SkinAttr mSkinAttr = AttrFactory.get(dAttr.attrName, id, entryName, typeName);
            viewAttrs.add(mSkinAttr);
        }

        skinItem.attrs = viewAttrs;
        skinItem.apply();
        addSkinView(skinItem);
    }

    public void dynamicAddSkinEnableView(Context context, View view, String attrName, int attrValueResId) {
        String entryName = context.getResources().getResourceEntryName(attrValueResId);
        String typeName = context.getResources().getResourceTypeName(attrValueResId);
        SkinAttr mSkinAttr = AttrFactory.get(attrName, attrValueResId, entryName, typeName);
        SkinItem skinItem = new SkinItem();
        skinItem.view = view;
        List<SkinAttr> viewAttrs = new ArrayList<>();
        viewAttrs.add(mSkinAttr);
        skinItem.attrs = viewAttrs;
        skinItem.apply();
        addSkinView(skinItem);
    }

    public void removeSkinView(View view) {
        SkinItem skinItem = mSkinItemMap.remove(view);
        if (skinItem != null) {
        }
        if (view instanceof TextView) {
            TextViewRepository.remove(mAppCompatActivity, (TextView) view);
        }

        if (SkinConfig.isCanChangeFont() && view instanceof TextView) {
            TextViewRepository.remove(mAppCompatActivity, (TextView) view);
        }
    }

    public void applySkin() {
        if (mSkinItemMap.isEmpty()) {
            return;
        }
        for (View view : mSkinItemMap.keySet()) {
            if (view == null) {
                continue;
            }
            mSkinItemMap.get(view).apply();
        }
    }

    private void addSkinView(SkinItem item) {
        if (mSkinItemMap.get(item.view) != null) {
            mSkinItemMap.get(item.view).attrs.addAll(item.attrs);
        } else {
            mSkinItemMap.put(item.view, item);
        }
    }
}
