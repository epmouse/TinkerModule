package com.imooc.tinker;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.example.tikerlib.tinker.TinkerManager;
import com.tencent.tinker.anno.DefaultLifeCycle;
import com.tencent.tinker.loader.app.ApplicationLike;
import com.tencent.tinker.loader.shareutil.ShareConstants;

/**
 * Created by renzhiqiang on 17/4/27.
 */
@DefaultLifeCycle(application = ".MyTinkerApplication",
        flags = ShareConstants.TINKER_ENABLE_ALL,
        loadVerifyFlag = false)
public abstract class CustomTinkerLike extends ApplicationLike {

    public CustomTinkerLike(Application application, int tinkerFlags,
                            boolean tinkerLoadVerifyFlag,
                            long applicationStartElapsedTime,
                            long applicationStartMillisTime,
                            Intent tinkerResultIntent) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag,
                applicationStartElapsedTime, applicationStartMillisTime,
                tinkerResultIntent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TinkerManager.getInstance()
                .installTinker(this)
                .setBaseUrl("http://xxxxxx/")
                .setUpdateCheckPatchUrl("xx/xx")
                .setDownloadPatchUrl("xx/xx");
        init();
    }
    public abstract void init();
    @Override
    public void onBaseContextAttached(Context base) {
        super.onBaseContextAttached(base);

    }
}
