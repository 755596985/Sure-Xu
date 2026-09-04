package com.surexu.sesame.hook;

import com.surexu.sesame.util.compat.HookBackend;
import com.surexu.sesame.util.compat.XC_MethodHook;

import java.lang.reflect.Member;

import de.robv.android.xposed.XposedBridge;

/**
 * 传统 Xposed API（≤93）的 {@link HookBackend} 实现。
 *
 * <p>由于自研兼容层 {@link XC_MethodHook.MethodHookParam} 与
 * {@code de.robv.android.xposed.XC_MethodHook.MethodHookParam} 字段几乎一一对应，
 * 适配器只做字段双向拷贝：before/after 前把框架参数填入兼容对象，
 * 回调后再把 setResult / setThrowable 结果同步回框架。
 */
public class LegacyXposedHookBackend implements HookBackend {

    @Override
    public XC_MethodHook.Unhook hook(Member member, XC_MethodHook callback) {
        de.robv.android.xposed.XC_MethodHook.Unhook unhook = XposedBridge.hookMethod(member,
                new de.robv.android.xposed.XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XC_MethodHook.MethodHookParam p = new XC_MethodHook.MethodHookParam();
                        p.thisObject = param.thisObject;
                        p.args = param.args;
                        callback.callBefore(p);
                        if (p.hasThrowable()) {
                            param.setThrowable(p.getThrowable());
                        }
                        if (p.hasResult) {
                            param.setResult(p.result);
                        }
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XC_MethodHook.MethodHookParam p = new XC_MethodHook.MethodHookParam();
                        p.thisObject = param.thisObject;
                        p.args = param.args;
                        p.result = param.getResult();
                        p.hasResult = true;
                        if (param.hasThrowable()) {
                            p.exception = param.getThrowable();
                        }
                        callback.callAfter(p);
                        if (p.hasThrowable()) {
                            param.setThrowable(p.getThrowable());
                            return;
                        }
                        if (p.hasResult) {
                            param.setResult(p.result);
                        }
                    }
                });
        return unhook::unhook;
    }
}
