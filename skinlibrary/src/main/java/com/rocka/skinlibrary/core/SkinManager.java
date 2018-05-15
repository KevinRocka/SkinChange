package com.rocka.skinlibrary.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.RestrictTo;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.rocka.skinlibrary.listener.ISkinLoader;
import com.rocka.skinlibrary.listener.ISkinUpdate;
import com.rocka.skinlibrary.listener.SkinLoadListener;
import com.rocka.skinlibrary.other.SkinConfig;
import com.rocka.skinlibrary.utils.ResourcesCompat;
import com.rocka.skinlibrary.utils.SkinOperUtils;
import com.rocka.skinlibrary.utils.TypefaceUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Rocka
 * @version: 1.0
 * @description: TODO
 * @time:2018/5/10
 */
public class SkinManager implements ISkinLoader {

    private final String TAG = SkinManager.class.getName();

    private static volatile SkinManager mInstance;

    private Context mContext;

    private Resources mResources;

    private boolean isDefaultSkin = false;

    private String skinPackageName;

    private List<ISkinUpdate> mSkinObservers;

    private SkinManager() {
        //do nothing
    }

    public static SkinManager getInstance() {
        if (mInstance == null) {
            synchronized (SkinManager.class) {
                if (mInstance == null) {
                    mInstance = new SkinManager();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context) {
        mContext = context.getApplicationContext();
        setUpSkinFile(context);
        setUpFont();
        if (SkinConfig.isInNightMode(context)) {
            SkinManager.getInstance().nightMode();
        } else {
            String skin = SkinConfig.getCustomSkinPath(context);
            if (SkinConfig.isDefaultSkin(context)) {
                return;
            }
            loadSkin(skin, null);
        }
    }

    private void setUpFont(){
        TypefaceUtils.mCurrentTypeface = TypefaceUtils.getTypeface(mContext);
    }

    /**
     * 安装皮肤文件，从Assets路径下拷贝到指定目录
     *
     * @param context
     */
    private void setUpSkinFile(Context context) {
        try {
            String[] skinFiles = context.getAssets().list(SkinConfig.SKIN_DIR_NAME);
            for (String fileName : skinFiles) {
                File file = new File(SkinOperUtils.getSkinDir(context), fileName);
                if (!file.exists()) {
                    SkinOperUtils.copySkinAssetsToDir(context, fileName, SkinOperUtils.getSkinDir(context));
                }
            }
        } catch (Exception e) {
            e.toString();
        }
    }

    /**
     * 打开夜间模式
     */
    public void nightMode() {
        if (!isDefaultSkin) {
            restoreDefaultTheme();
        }
        SkinConfig.setNightMode(mContext, true);
        notifySkinUpdate();
    }

    /**
     * 恢复到默认的皮肤
     */
    public void restoreDefaultTheme() {
        SkinConfig.saveSkinPath(mContext, SkinConfig.DEFAULT_SKIN);
        isDefaultSkin = true;
        SkinConfig.setNightMode(mContext, false);
        mResources = mContext.getResources();
        skinPackageName = mContext.getPackageName();
        notifySkinUpdate();
    }

    /**
     * 加载皮肤
     * todo 处理AsyncTask带来的内存泄露问题，需要优化
     *
     * @param skinName
     * @param callback
     */
    @SuppressLint("StaticFieldLeak")
    public void loadSkin(String skinName, final SkinLoadListener callback) {
        new AsyncTask<String, Void, Resources>() {

            @Override
            protected void onPreExecute() {
                if (callback != null) {
                    callback.onStart();
                }
            }


            @Override
            protected Resources doInBackground(String... strings) {
                try {
                    if (strings.length == 1 && !TextUtils.isEmpty(strings[0])) {
                        String skinPath = SkinOperUtils.getSkinDir(mContext) + File.separator + strings[0];
                        File file = new File(skinPath);
                        if (!file.exists()) {
                            return null;
                        }

                        PackageManager mPackManager = mContext.getPackageManager();
                        PackageInfo mInfo = mPackManager.getPackageArchiveInfo(skinPath, PackageManager.GET_ACTIVITIES);
                        skinPackageName = mInfo.packageName;

                        //利用反射获取皮肤包的Resources
                        AssetManager assetManager = AssetManager.class.newInstance();
                        //动态构造的Method对象invoke委托动态构造的InvokeTest对象，执行对应形参的addAssetPath方法
                        Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
                        addAssetPath.invoke(assetManager, skinPath);

                        Resources resources = mContext.getResources();
                        Resources skinResources = ResourcesCompat.getResources(assetManager, resources.getDisplayMetrics(), resources.getConfiguration());
                        SkinConfig.saveSkinPath(mContext, strings[0]);

                        isDefaultSkin = false;
                        return skinResources;
                    }
                } catch (Exception e) {
                    e.toString();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Resources resources) {
                mResources = resources;

                if (mResources != null) {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                    SkinConfig.setNightMode(mContext, false);
                    notifySkinUpdate();
                } else {
                    isDefaultSkin = true;
                    if (callback != null) {
                        callback.onFailed("没有获取到资源");
                    }
                }
            }

        }.execute(skinName);
    }

    /**
     * 加载字体
     *
     * @param fontName
     */
    public void loadFont(String fontName) {
        Typeface tf = TypefaceUtils.createTypeface(mContext, fontName);
        TextViewRepository.applyFont(tf);
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public int getColor(int resId) {
        int originColor = ContextCompat.getColor(mContext, resId);
        if (mResources == null || isDefaultSkin) {
            return originColor;
        }

        String resName = mContext.getResources().getResourceEntryName(resId);
        int trueResId = mResources.getIdentifier(resName, "color", skinPackageName);
        int trueColor;
        if (trueResId == 0) {
            trueColor = originColor;
        } else {
            trueColor = mResources.getColor(trueResId);
        }
        return trueColor;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public int getNightColor(int resId) {
        String resName = mResources.getResourceEntryName(resId);
        String resNameNight = resName + "_night";
        int nightResId = mResources.getIdentifier(resNameNight, "color", skinPackageName);
        if (nightResId == 0) {
            return ContextCompat.getColor(mContext, resId);
        } else {
            return ContextCompat.getColor(mContext, nightResId);
        }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public Drawable getDrawable(int resId) {
        Drawable originDrawable = ContextCompat.getDrawable(mContext, resId);
        if (mResources == null || isDefaultSkin) {
            return originDrawable;
        }

        String resName = mContext.getResources().getResourceEntryName(resId);
        int trueResId = mResources.getIdentifier(resName, "drawable", skinPackageName);
        Drawable trueDrawable;
        if (trueResId == 0) {
            trueResId = mResources.getIdentifier(resName, "mipmap", skinPackageName);
        }
        if (trueResId == 0) {
            trueDrawable = originDrawable;
        } else {
            if (android.os.Build.VERSION.SDK_INT < 22) {
                trueDrawable = mResources.getDrawable(trueResId);
            } else {
                trueDrawable = mResources.getDrawable(trueResId, null);
            }
        }
        return trueDrawable;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public Drawable getDrawable(int resId, String dir) {
        Drawable originDrawable = ContextCompat.getDrawable(mContext, resId);
        if (mResources == null || isDefaultSkin) {
            return originDrawable;
        }
        String resName = mContext.getResources().getResourceEntryName(resId);
        int trueResId = mResources.getIdentifier(resName, dir, skinPackageName);
        Drawable trueDrawable;
        if (trueResId == 0) {
            trueDrawable = originDrawable;
        } else {
            if (android.os.Build.VERSION.SDK_INT < 22) {
                trueDrawable = mResources.getDrawable(trueResId);
            } else {
                trueDrawable = mResources.getDrawable(trueResId, null);
            }
        }
        return trueDrawable;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public Drawable getNightDrawable(String resName) {

        String resNameNight = resName + "_night";

        int nightResId = mResources.getIdentifier(resNameNight, "drawable", skinPackageName);
        if (nightResId == 0) {
            nightResId = mResources.getIdentifier(resNameNight, "mipmap", skinPackageName);
        }
        Drawable color;
        if (nightResId == 0) {
            int resId = mResources.getIdentifier(resName, "drawable", skinPackageName);
            if (resId == 0) {
                resId = mResources.getIdentifier(resName, "mipmap", skinPackageName);
            }
            color = mResources.getDrawable(resId);
        } else {
            color = mResources.getDrawable(nightResId);
        }
        return color;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public ColorStateList getColorStateList(int resId) {
        boolean isExternalSkin = true;
        if (mResources == null || isDefaultSkin) {
            isExternalSkin = false;
        }

        String resName = mContext.getResources().getResourceEntryName(resId);
        if (isExternalSkin) {
            int trueResId = mResources.getIdentifier(resName, "color", skinPackageName);
            ColorStateList trueColorList;
            if (trueResId == 0) {
                return ContextCompat.getColorStateList(mContext, resId);
            } else {
                trueColorList = mResources.getColorStateList(trueResId);
                return trueColorList;
            }
        } else {
            return ContextCompat.getColorStateList(mContext, resId);
        }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public ColorStateList getNightColorStateList(int resId) {

        String resName = mResources.getResourceEntryName(resId);
        String resNameNight = resName + "_night";
        int nightResId = mResources.getIdentifier(resNameNight, "color", skinPackageName);
        if (nightResId == 0) {
            return ContextCompat.getColorStateList(mContext, resId);
        } else {
            return ContextCompat.getColorStateList(mContext, nightResId);
        }
    }

    @Override
    public void attach(ISkinUpdate observer) {
        if (mSkinObservers == null) {
            mSkinObservers = new ArrayList<>();
        }
        if (!mSkinObservers.contains(observer)) {
            mSkinObservers.add(observer);
        }
    }

    @Override
    public void detach(ISkinUpdate observer) {
        if (mSkinObservers != null && mSkinObservers.contains(observer)) {
            mSkinObservers.remove(observer);
        }
    }

    @Override
    public void notifySkinUpdate() {
        if (mSkinObservers != null) {
            for (ISkinUpdate observer : mSkinObservers) {
                observer.onThemeUpdate();
            }
        }
    }

    /**
     * 是否在夜间模式状态下
     *
     * @return
     */
    public boolean isNightMode() {
        return SkinConfig.isInNightMode(mContext);
    }

    public Resources getResources() {
        return mResources;
    }

    public String getCurSkinPackageName() {
        return skinPackageName;
    }

    boolean isExternalSkin() {
        return !isDefaultSkin && mResources != null;
    }
}
