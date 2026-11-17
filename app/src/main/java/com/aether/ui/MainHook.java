package com.aether.ui;

import com.aether.ui.hooks.EdgeGestureHook;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class MainHook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!"com.android.systemui".equals(lpparam.packageName)) {
            return;
        }

        XposedBridge.log("AETHER UI: SystemUI loaded");
        try {
            EdgeGestureHook.install(lpparam.classLoader);
        } catch (Throwable t) {
            XposedBridge.log("AETHER UI: hook install failed: " + t);
        }
    }
}
