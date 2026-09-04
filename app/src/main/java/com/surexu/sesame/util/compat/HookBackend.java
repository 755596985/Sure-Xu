package com.surexu.sesame.util.compat;

import java.lang.reflect.Member;

/**
 * Hook 后端抽象：屏蔽 libxposed(API 102) 与传统 Xposed(≤93) 两套 hook 实现差异。
 * <p>
 * 业务代码只面向 {@link XC_MethodHook} 这一套自研兼容回调模型，
 * 由不同运行时各自注册的 {@link HookBackend} 把 hook 落到真实框架。
 */
public interface HookBackend {

    /**
     * Hook 一个方法或构造器。
     *
     * @param member   被 hook 的 Method / Constructor
     * @param callback 兼容层回调（before/after 模型）
     * @return 可用于解除 hook 的句柄
     */
    XC_MethodHook.Unhook hook(Member member, XC_MethodHook callback);
}
