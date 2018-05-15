package com.rocka.skinlibrary.other;

import android.content.Context;

import com.rocka.skinlibrary.attr.core.AttrFactory;
import com.rocka.skinlibrary.attr.core.SkinAttr;
import com.rocka.skinlibrary.utils.SkinPreferencesUtils;

/**
 * @author: Rocka
 * @version: 1.0
 * @description: TODO
 * @time:2018/5/10
 */
public class SkinConfig {

    /**
     * 命名空间
     */
    public static final String NAME_SPACE = "http://schemas.android.com/android/skin";

    /**
     * 开启皮肤标志
     */
    public static final String ATTR_SKIN_ENABLE = "enable";

    /**
     * 皮肤目录名
     */
    public static final String SKIN_DIR_NAME = "skin";

    /**
     * 字体目录名
     */
    public static final String FONT_DIR_NAME = "fonts";

    /**
     * 夜间模式——首选项
     */
    public static final String PREF_NIGHT_MODE = "night_mode";

    /**
     * 皮肤路径——首选项
     */
    public static final String PREF_CUSTOM_SKIN_PATH = "skin_custom_path";

    /**
     * 默认皮肤
     */
    public static final String DEFAULT_SKIN = "skin_default";

    /**
     * 是否设置状态栏颜色
     */
    private static boolean isCanChangeStatusColor = false;

    /**
     * 设置了和这个值之后就不用在xml中设置skin:enable="true"
     */
    private static boolean isGlobalSkinApply = false;

    /**
     * 字体目录——首选项
     */
    public static final String PREF_FONT_PATH = "skin_font_path";

    /**
     * 是否改变字体
     */
    private static boolean isCanChangeFont = false;

    public static void setNightMode(Context context, boolean isEnableNightMode) {
        SkinPreferencesUtils.putBoolean(context, PREF_NIGHT_MODE, isEnableNightMode);
    }

    public static boolean isInNightMode(Context context) {
        return SkinPreferencesUtils.getBoolean(context, PREF_NIGHT_MODE, false);
    }

    public static void saveSkinPath(Context context, String path) {
        SkinPreferencesUtils.putString(context, PREF_CUSTOM_SKIN_PATH, path);
    }

    public static String getCustomSkinPath(Context context) {
        return SkinPreferencesUtils.getString(context, PREF_CUSTOM_SKIN_PATH, DEFAULT_SKIN);
    }

    public static boolean isDefaultSkin(Context context) {
        return DEFAULT_SKIN.equals(getCustomSkinPath(context));
    }

    public static void setCanChangeStatusColor(boolean isCan) {
        isCanChangeStatusColor = isCan;
    }

    public static boolean isCanChangeStatusColor() {
        return isCanChangeStatusColor;
    }

    public static boolean isGlobalSkinApply() {
        return isGlobalSkinApply;
    }

    public static void enableGlobalSkinApply() {
        isGlobalSkinApply = true;
    }

    public static void addSupportAttr(String attrName, SkinAttr skinAttr) {
        AttrFactory.addSupportAttr(attrName, skinAttr);
    }

    public static boolean isCanChangeFont() {
        return isCanChangeFont;
    }

    public static void setCanChangeFont(boolean isCan) {
        isCanChangeFont = isCan;
    }
}
