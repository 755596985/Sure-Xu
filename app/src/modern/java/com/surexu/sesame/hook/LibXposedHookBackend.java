package com.surexu.sesame.hook;

import com.surexu.sesame.util.compat.HookBackend;
import com.surexu.sesame.util.compat.XC_MethodHook;

import java.lang.reflect.Executable;
import java.lang.reflect.Member;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;

/**
 * libxposed API 102 的 {@link HookBackend} 实现。
 *
 * <p>把自研兼容层 {@link XC_MethodHook}（before/after 模型）适配到 libxposed 的
 * OkHttp 风格拦截链（{@link XposedInterface.Hooker#intercept(XposedInterface.Chain)}）。
 * 语义对齐旧版 Xposed：
 * <ul>
 *     <li>before 中 {@code setResult} → 短路原方法直接返回；</li>
 *     <li>before 中改 {@code args} → 以修改后的参数继续执行原方法；</li>
 *     <li>原方法正常返回后才回调 after，after 中 {@code setResult} 覆盖返回值；</li>
 *     <li>{@code setThrowable} 会让异常向外传播。</li>
 * </ul>
 */
public class LibXposedHookBackend implements HookBackend {

    private final XposedModule module;

    public LibXposedHookBackend(XposedModule module) {
        this.module = module;
    }

    @Override
    public XC_MethodHook.Unhook hook(Member member, XC_MethodHook callback) {
        if (!(member instanceof Executable)) {
            throw new IllegalArgumentException("libxposed 仅支持 hook 方法/构造器: " + member);
        }
        XposedInterface.HookHandle handle = module.hook((Executable) member)
                .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                .intercept(chain -> {
                    XC_MethodHook.MethodHookParam p = new XC_MethodHook.MethodHookParam();
                    p.thisObject = chain.getThisObject();
                    p.args = chain.getArgs().toArray();

                    // ---- before 阶段 ----
                    callback.callBefore(p);
                    if (p.hasThrowable()) {
                        throw p.getThrowable();
                    }
                    if (p.hasResult) {
                        return p.result; // before 中 setResult，短路原方法
                    }

                    // ---- 调用原方法（沿用 before 可能修改过的参数）----
                    Object result;
                    try {
                        result = chain.proceed(p.args);
                    } catch (Throwable t) {
                        p.exception = t;
                        throw t; // 原方法抛异常：不进入 after，保持旧 Xposed 语义
                    }
                    p.result = result;
                    p.hasResult = true;

                    // ---- after 阶段 ----
                    callback.callAfter(p);
                    if (p.hasThrowable()) {
                        throw p.getThrowable();
                    }
                    return p.hasResult ? p.result : result;
                });
        return new XC_MethodHook.Unhook() {
            @Override
            public void unhook() {
                handle.unhook();
            }
        };
    }
}
