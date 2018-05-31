package com.example.tikerlib.tinker;

import android.content.Context;
import android.support.multidex.MultiDex;

import com.example.tikerlib.network.HttpConstant;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.loader.app.ApplicationLike;

/**
 * Created by renzhiqiang on 17/4/27.
 *
 * @functon 对Tinker的所有api做一层封装
 */
public class TinkerManager {

    private static volatile boolean isInstalled = false;
    private boolean baseUrlIsSet = false;
    private boolean updateCheckPatchUrlIsSet = false;
    private boolean downloadPatchUrlIsSet = false;

    private ApplicationLike mAppLike;

    private TinkerManager() {
    }

    private static class TinkerFactory {
        private static final TinkerManager tinkerManager = new TinkerManager();
    }

    public static TinkerManager getInstance() {
        return TinkerFactory.tinkerManager;
    }

    /**
     * 完成Tinker的初始化
     *
     * @param applicationLike
     */
    public TinkerManager installTinker(ApplicationLike applicationLike) {
        mAppLike = applicationLike;
        if (isInstalled) {
            return this;
        }
        MultiDex.install(getApplicationContext());//使应用支持分包
        TinkerInstaller.install(mAppLike); //完成tinker初始化
        isInstalled = true;
        return this;
    }


    public TinkerManager setUpdateCheckPatchUrl(String checkPatchUrl) {
        if (checkPatchUrl != null) {
            HttpConstant.UPDATE_PATCH_URL = checkPatchUrl;
            updateCheckPatchUrlIsSet = true;
        }
        return this;
    }

    public TinkerManager setBaseUrl(String baseUrl) {
        if (baseUrl != null) {
            HttpConstant.ROOT_URL = baseUrl;
            baseUrlIsSet = true;
        }
        return this;
    }

    public TinkerManager setDownloadPatchUrl(String downloadPatchUrl) {
        if (downloadPatchUrl != null) {
            HttpConstant.DOWNLOAD_PATCH_URL = downloadPatchUrl;
            downloadPatchUrlIsSet = true;
        }
        return this;
    }

    //完成Patch文件的加载
    public void loadPatch(String path) {
        checkUrls();
        if (Tinker.isTinkerInstalled()) {
            TinkerInstaller.onReceiveUpgradePatch(getApplicationContext(), path);
        }
    }

    private void checkUrls() {
        if (!baseUrlIsSet)
            throw new RuntimeException("请在初始化的地方设置baseUrl，调用TinkerManager的setBaseUrl(String baseUrl)");
        if (!updateCheckPatchUrlIsSet)
            throw new RuntimeException("请在初始化的地方设置updateCheckPatchUrlIsSet，调用TinkerManager的setUpdateCheckPatchUrl(String checkPatchUrl)");
        if (!downloadPatchUrlIsSet)
            throw new RuntimeException("请在初始化的地方设置downloadPatchUrlIsSet，调用TinkerManager的setDownloadPatchUrl(String downloadPatchUrl) ");
    }

    //通过ApplicationLike获取Context
    private Context getApplicationContext() {
        if (mAppLike != null) {
            return mAppLike.getApplication().getApplicationContext();
        }
        return null;
    }
}
