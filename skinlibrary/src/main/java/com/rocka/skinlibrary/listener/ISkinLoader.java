package com.rocka.skinlibrary.listener;

/**
 * @author: Rocka
 * @version: 1.0
 * @description: 用来添加皮肤更新，删除皮肤更新以及通知皮肤更新
 * @time:2018/5/10
 */
public interface ISkinLoader {
    void attach(ISkinUpdate observer);

    void detach(ISkinUpdate observer);

    void notifySkinUpdate();
}
