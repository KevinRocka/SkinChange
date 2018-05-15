package com.rocka.skinlibrary.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;

import com.rocka.skinlibrary.other.SkinConfig;

import java.io.File;

/**
 * @author: Rocka
 * @version: 1.0
 * @description: 字体工具类
 * @time:2018/5/15
 */
public class TypefaceUtils {

    public static Typeface mCurrentTypeface;

    public static Typeface createTypeface(Context context, String fontName) {
        Typeface tf;
        if (!TextUtils.isEmpty(fontName)) {
            tf = Typeface.createFromAsset(context.getAssets(), getFontPath(fontName));
            SkinPreferencesUtils.putString(context, SkinConfig.PREF_FONT_PATH, getFontPath(fontName));
        } else {
            tf = Typeface.DEFAULT;
            SkinPreferencesUtils.putString(context, SkinConfig.PREF_FONT_PATH, "");
        }
        TypefaceUtils.mCurrentTypeface = tf;
        return tf;
    }

    public static Typeface getTypeface(Context context) {
        String fontPath = SkinPreferencesUtils.getString(context, SkinConfig.PREF_FONT_PATH);
        Typeface tf;
        if (!TextUtils.isEmpty(fontPath)) {
            tf = Typeface.createFromAsset(context.getAssets(), fontPath);
        } else {
            tf = Typeface.DEFAULT;
            SkinPreferencesUtils.putString(context, SkinConfig.PREF_FONT_PATH, "");
        }
        return tf;
    }

    private static String getFontPath(String fontName) {
        return SkinConfig.FONT_DIR_NAME + File.separator + fontName;
    }
}
