package com.surexu.sesame.hook;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.surexu.sesame.data.AppConfig;
import com.surexu.sesame.data.RunType;
import com.surexu.sesame.data.ViewAppInfo;
import com.surexu.sesame.util.XHelpers;
import com.surexu.sesame.util.compat.XC_LoadPackage;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface;

/**
 * libxposed API 102（现代版）模块入口。
 *
 * <p>入口由 {@code META-INF/xposed/java_init.list} 声明，仅在 normal / compatible
 * 两个现代 flavor 中参与编译与打包；legacy 产物不包含本类与任何 libxposed 类型。
 */
public class LibXposedEntry extends XposedModule {

    private static final String TAG = "LibXposedEntry";

    @Override
    public void onModuleLoaded(@NonNull XposedModuleInterface.ModuleLoadedParam param) {
        // 初始化框架无关的 hook 后端，供后续 onPackageReady / handleLoadPackage 使用
        XHelpers.setBackend(new LibXposedHookBackend(this));
        log(4, TAG, "event=module_loaded api=" + getApiVersion()
                + " framework=" + getFrameworkName() + " version=" + getFrameworkVersion());
        markFile("/sdcard/sesame_diag.txt", "onModuleLoaded " + getFrameworkName() + " api=" + getApiVersion());
        try {
            // 读取与 App 共享的日志开关配置，使各分项开关在本进程真正生效
            AppConfig.load();
            // 模块已在 LSPosed 中启用：onModuleLoaded 被调用即代表已启用，
            // 直接标记为已激活（与是否打开 / hook 支付宝无关）
            ViewAppInfo.setRunTypeByCode(RunType.MODEL.getCode());
            // 若 UI 已启动，发同进程广播实时刷新界面
            Application app = (Application) Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication").invoke(null);
            if (app != null) {
                app.sendBroadcast(new Intent("com.surexu.sesame.status"));
            }
        } catch (Throwable ignored) {
        }
    }

    private static void markFile(String path, String line) {
        try {
            java.io.FileWriter fw = new java.io.FileWriter(path, true);
            fw.write(line + " @ " + new java.util.Date() + "\n");
            fw.close();
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void onPackageReady(@NonNull XposedModuleInterface.PackageReadyParam param) {
        // 部分框架可能跳过 onModuleLoaded，兜底保证后端已注入
        XHelpers.setBackend(new LibXposedHookBackend(this));

        XC_LoadPackage.LoadPackageParam lpparam = new XC_LoadPackage.LoadPackageParam();
        lpparam.packageName = param.getPackageName();
        lpparam.processName = param.getPackageName();
        lpparam.classLoader = param.getClassLoader();
        new ApplicationHook().handleLoadPackage(lpparam);
    }
}
