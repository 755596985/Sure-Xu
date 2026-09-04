package com.surexu.sesame.util.compat;

/**
 * 框架无关的加载包回调参数模型：模拟旧 Xposed {@code XC_LoadPackage.LoadPackageParam}。
 * <p>modern 入口（{@code LibXposedEntry}）与 legacy 入口（{@code LegacyEntry}）都会把
 * 各自框架给出的参数填入本对象，再交给核心业务 {@code ApplicationHook.handleLoadPackage}。
 */
public class XC_LoadPackage {

    public static class LoadPackageParam {
        public String packageName;
        public String processName;
        public ClassLoader classLoader;
    }
}
