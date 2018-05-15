package com.rocka.skinchange.utils;

import android.content.Context;
import android.content.res.Resources;
import android.widget.Toast;

import java.util.HashMap;

/**
 * @author: Rocka
 * @version: 1.0
 * @description: Toast封装，可避免5秒之内重复出现。
 * @time:2018/5/11
 */

public class ToastUtil {


    private HashMap<Object, Long> map = new HashMap<Object, Long>();

    private static ToastUtil toast;

    Context context;

    private Toast currentToast;

    private ToastUtil(Context context) {
        this.context = context;
    }

    public static ToastUtil getInstance(Context context) {
        if (toast == null) {
            toast = new ToastUtil(context);
        }
        return toast;
    }

    /**
     * 显示Toast,显示时间为{@link Toast#LENGTH_SHORT}
     *
     * @param res 　显示字符串的resourceId
     * @param
     */
    public void makeText(int res) {
        makeText(res, Toast.LENGTH_SHORT);
    }

    /**
     * 显示toast,显示时间为{@link Toast#LENGTH_SHORT}
     *
     * @param
     */
    public void makeText(String str) {
        makeText(str, Toast.LENGTH_SHORT);
    }

    /**
     * 显示Toast
     *
     * @param str  显示文字
     * @param type 需要显示的时间{@link Toast#LENGTH_SHORT}|{@link Toast#LENGTH_LONG}
     * @param
     */
    public void makeText(String str, int type) {
        try {
            if (map.get(str) == null || System.currentTimeMillis() - map.get(str) > 2000) {
                currentToast = Toast.makeText(context, str, type);
                currentToast.show();
                map.put(str, System.currentTimeMillis());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示Toast
     *
     * @param res  显示字符串的资源id
     * @param type 需要显示的时间{@link Toast#LENGTH_SHORT}|{@link Toast#LENGTH_LONG}
     * @param
     */
    public void makeText(int res, int type) {
        try {
            if (map.get(res) == null || System.currentTimeMillis() - map.get(res) > 2000) {
                Toast.makeText(context, res, type).show();
                map.put(res, System.currentTimeMillis());
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        try {
            if (currentToast != null) {
                currentToast.cancel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

