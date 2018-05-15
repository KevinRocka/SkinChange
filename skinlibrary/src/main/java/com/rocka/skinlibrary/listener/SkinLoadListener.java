package com.rocka.skinlibrary.listener;

/**
 * @author: Rocka
 * @version: 1.0
 * @description: TODO
 * @time:2018/5/10
 */
public interface SkinLoadListener {
    void onStart();

    void onSuccess();

    void onFailed(String errMsg);
}
