package com.surexu.sesame;

import android.app.Application;

/**
 * 传统 Xposed API（≤93）产物中模块自身 App 进程的 Application。
 *
 * <p>传统框架（OPatch / TaiChi 等）没有 libxposed service 概念，不会把运行状态
 * 推送到模块自身进程；是否已激活由 UI 侧的 {@link com.surexu.sesame.data.ViewAppInfo#checkRunType()}
 * 主动探测，因此这里保持空实现即可。
 */
public class SesameApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
