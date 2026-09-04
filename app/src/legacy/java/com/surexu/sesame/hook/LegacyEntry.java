package com.surexu.sesame.hook;

import com.surexu.sesame.util.XHelpers;
import com.surexu.sesame.util.compat.XC_LoadPackage;

import de.robv.android.xposed.IXposedHookLoadPackage;

/**
 * 传统 Xposed API（≤93）模块入口。
 *
 * <p>仅存在于 legacy flavor；由 {@code assets/xposed_init} 声明，供 OPatch / TaiChi /
 * 旧版 LSPosed(≤93) 等框架发现。框架会把本类实例化并调用
 * {@link #handleLoadPackage(de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam)}，
 * 这里桥接到与 modern 版完全一致的核心逻辑
 * {@link ApplicationHook#handleLoadPackage(com.surexu.sesame.util.compat.XC_LoadPackage.LoadPackageParam)}。
 */
public class LegacyEntry implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam lpparam) {
        XHelpers.setBackend(new LegacyXposedHookBackend());

        XC_LoadPackage.LoadPackageParam p = new XC_LoadPackage.LoadPackageParam();
        p.packageName = lpparam.packageName;
        p.processName = lpparam.processName;
        p.classLoader = lpparam.classLoader;
        new ApplicationHook().handleLoadPackage(p);
    }
}
