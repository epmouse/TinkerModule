package com.imooc;

import android.app.Application;
import android.content.Intent;

import com.example.tikerlib.tinker.CustomTinkerLike;

/**
 * 在init中做app的初始化工作，也就是吧原来application中的逻辑移到这里
 */
public class MyAppLike extends CustomTinkerLike {
    public MyAppLike(Application application, int tinkerFlags, boolean tinkerLoadVerifyFlag, long applicationStartElapsedTime, long applicationStartMillisTime, Intent tinkerResultIntent) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime, tinkerResultIntent);
    }

    @Override
    public void init() {

    }
}
