---
layout:     post
title:      "Android 插件化动态加载机制换肤"
subtitle:   "Android 插件化动态加载机制换肤"
date:       2018.5.9 16:54
author:     "Rocka"
categories: "Android进阶"
tags:
    - Android进阶
---

前段时间被问到Android 动态换肤机制被问懵逼了，只是知道插件化换肤是可以不需要重新启动Activity，皮肤包和主APP应该是相互分离，皮肤包可以动态下载，皮肤包是一个普通的Android项目，只有简单的资源文件，没有类文件，其中做的最好的还是网易云音乐的换肤，当问到具体实现流程与内部实现方式时，还是一脸的懵逼。

先来看看成品的效果吧：



下面是具体插件化动态换肤机制要用到的知识点思维导图 : 




## LayoutInflater.Factory

这个是官方对这个类的最好诠释

> Inflating your own custom views, instead of letting the system do it

LayoutInflater其实是将XML转化为View的一个工具，举个栗子LayoutInflater都被设置了一个默认的Factory，Activity 是实现了LayoutInflater.Factory2接口的,因此在你的Activity中直接重写onCreateView就可以自定义View的填充了。

```java
public class Activity extends ContextThemeWrapper
        implements LayoutInflater.Factory2,
        Window.Callback, KeyEvent.Callback,
        OnCreateContextMenuListener, ComponentCallbacks2,
        Window.OnWindowDismissedCallback, WindowControllerCallback,
        AutofillManager.AutofillClient {
        // more
        }
```

在自定义换肤中，LayoutInflater提供了setFactory和setFactory2两个方法让你自定义布局的填充(有点类似于过滤器)，在这里面你完全可以自己去定义去创建你所想要的View，SkinInflaterFactory的作用就是去搜集那些有需要响应皮肤更改的View。

```java

public class SkinInflaterFactory implements LayoutInflater.Factory2 {
	@Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return null;
    }
    
    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        boolean isSkinEnable = attrs.getAttributeBooleanValue(SkinConfig.NAME_SPACE, SkinConfig.ATTR_SKIN_ENABLE, false);
        AppCompatDelegate delegate = mAppCompatActivity.getDelegate();
        View view = delegate.createView(parent, name, context, attrs);
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

}


public class SkinBaseActivity extends AppCompatActivity{
	private SkinInflaterFactory mSkinInflaterFactory;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        mSkinInflaterFactory = new SkinInflaterFactory(this);
        LayoutInflaterCompat.setFactory2(getLayoutInflater(), mSkinInflaterFactory);
        super.onCreate(savedInstanceState);
    }
}
```

## Resource替换

动态换肤的核心就是动态的获取皮肤包的Resources，当获取到皮肤包的Resources后就通知界面去更改界面的皮肤，如果获取失败，就使用应用默认的Resources。

1. 实例化 AssetManager 对象，并通过反射调用 addAssetPath(String) 方法加载目标 apk（或与 apk 文件架构一致的目录）
2. 通过第一步得到的 AssetManager 实例化 Resource 对象

```java

@SuppressLint("StaticFieldLeak")
public void loadSkin(String skinName, final SkinLoaderListener callback) {

        new AsyncTask<String, Void, Resources>() {

            @Override
            protected void onPreExecute() {
                if (callback != null) {
                    callback.onStart();
                }
            }

            @Override
            protected Resources doInBackground(String... params) {
                try {
                    if (params.length == 1) {
                        String skinPkgPath = SkinFileUtils.getSkinDir(context) + File.separator + params[0];
                        SkinL.i(TAG, "skinPackagePath:" + skinPkgPath);
                        File file = new File(skinPkgPath);
                        if (!file.exists()) {
                            return null;
                        }
                        PackageManager mPm = context.getPackageManager();
                        PackageInfo mInfo = mPm.getPackageArchiveInfo(skinPkgPath, PackageManager.GET_ACTIVITIES);
                        skinPackageName = mInfo.packageName;
                        //利用反射获取皮肤包的Resources
                        AssetManager assetManager = AssetManager.class.newInstance();
                        //动态构造的Method对象invoke委托动态构造的InvokeTest对象，执行对应形参的addAssetPath方法
                        Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
                        addAssetPath.invoke(assetManager, skinPkgPath);


                        Resources superRes = context.getResources();
                        Resources skinResource = ResourcesCompat.getResources(assetManager, superRes.getDisplayMetrics(), superRes.getConfiguration());
                        SkinConfig.saveSkinPath(context, params[0]);

                        isDefaultSkin = false;
                        return skinResource;
                    }
                    return null;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Resources result) {
                mResources = result;

                if (mResources != null) {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                    SkinConfig.setNightMode(context, false);
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
```

## 动态更新
* 所有需要换肤的界面都实现ISkinUpdate这个接口，SkinManager里维护了一个List<ISkinUpdate> mSkinObservers，当需要换肤的时候就遍历这个观察类，然后通知各个界面皮肤更新，最后让各个页面的BackgroundAttr或者TextViewAttr来重新加载替换了皮肤的Resources的资源，就实现了换肤。


## 自定义属性

* 因为所涉及的需要自定义换肤的属性很多，所以这个是一个功能性的lib，不建议做成远程仓库，因为这个框架是一直在拓展的，我就单独举一个例子，因为项目下RadioButton用到了drawableTop，所以就自定义了DrawableTop来使用。

```java
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
```

## 输出皮肤工程
* 皮肤工程其实也是一个apk，只是没有源代码，只有资源文件，资源文件与主项目的资源文件的名称相同。
* 可以制作一个皮肤工具，方便输出皮肤工程。
* 后缀名为.skin，目的是防止用户点击安装，可以用gradle build，也可以直接用打包的方式生成，最终输出格式是theme-皮肤名-时间.skin。

```java
android {
	applicationVariants.all { variant ->
	        variant.outputs.all { output ->
	            def buildType = variant.buildType.name
	            def outputFile = output.outputFile
	            def date = releaseTime()
	            if (outputFile != null && buildType.contains("release") && outputFile.name.endsWith('.apk')) {
	                def fileName = "theme-blue-${date}.skin"
	                outputFileName=fileName
	                //output.outputFile = new File(outputFile.parent, fileName)
	            }
	        }
	    }
}
def releaseTime() {
    return new Date().format("yyyyMMdd")
}
```


## 其他

### 夜间模式
* 夜间模式可以单独作为一种皮肤工程来供食用，也可以用我项目中使用到的方法，拷贝一份values/colors为colors-night。然后所有属性后面加上”_night“，然后再更改里面的颜色属性为夜间模式想要的值，因为library工程有相应获取“xx_night”资源的方法。

### 字体功能

* 首先在SkinInflaterFactory中判断哪些view要使用到字体功能，然后将其加入到该View的仓库。其次字体切换主要用到Typeface，从assets/fonts目录下加载了ttf字体后，通过textView.setTypeface(tf)来设置字体;

* 如果发现java.lang.RuntimeException: Font asset not found fonts/fonts_1.ttf，而你的assets路径下还是有该字体，可能是拷贝过来后，字体的实际大小变为0，重新拷贝字体即可;

### 总结

* 其实动态加载机制进行换肤就是通过反射机制加载.skin皮肤包的Resources文件来替换本地Resources文件，再进行界面的刷新。