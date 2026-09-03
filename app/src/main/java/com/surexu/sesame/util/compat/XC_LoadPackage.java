package com.surexu.sesame.util.compat;

/**
 * API 102 临时迁移兼容层：模拟旧 Xposed {@code XC_LoadPackage.LoadPackageParam}。
 * 由 {@link com.surexu.sesame.hook.ApplicationHook#onPackageReady} 填充并传给原 handleLoadPackage 逻辑。
 */
public class XC_LoadPackage {

    public static class LoadPackageParam {
        public String packageName;
        public String processName;
        public ClassLoader classLoader;
    }
}
