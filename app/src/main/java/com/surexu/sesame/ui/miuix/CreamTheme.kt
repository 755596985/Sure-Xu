package com.surexu.sesame.ui.miuix

import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.theme.Colors
import top.yukonga.miuix.kmp.theme.lightColorScheme

/**
 * 奶白色（Cream）主题配色。
 *
 * 说明：项目中所有 miuix 页面都通过 [MiuixBaseActivity.setAppContent] 注入主题，
 * 且界面一律使用 MiuixTheme.colorScheme.* 取色，因此只需在此处集中定义配色，
 * 即可让主页 / 设置 / 日志 / 关于 / 扩展 / 好友统计 / 弹窗全部统一为奶白色。
 */
object CreamTheme {

    /* ---------- 底色：由浅到深，形成层次 ---------- */
    /** 窗口底色：温暖米白 */
    val background = Color(0xFFFAF5EC)
    /** 卡片/页面表面：近白的奶色 */
    val surface = Color(0xFFFFFDF8)
    /** 次级表面 */
    val surfaceVariant = Color(0xFFF7F1E6)
    /** 容器（列表项、日志行） */
    val surfaceContainer = Color(0xFFF4EDDF)
    val surfaceContainerHigh = Color(0xFFEEE6D6)
    val surfaceContainerHighest = Color(0xFFE6DCCA)

    /* ---------- 文字：暖调深灰棕，避免纯黑刺眼 ---------- */
    val onBackground = Color(0xFF2B261F)
    val onSurface = Color(0xFF3A332A)
    val onSurfaceSecondary = Color(0xFF6C6355)
    val onSurfaceVariantSummary = Color(0xFF8C8272)
    val onSurfaceVariantActions = Color(0xFFA79681)

    /* ---------- 主色：暖橙，延续支付宝 / 蚂蚁森林的暖色调 ---------- */
    val primary = Color(0xFFE07A3E)
    val onPrimary = Color(0xFFFFFDF8)
    val primaryVariant = Color(0xFFC75F26)
    val primaryContainer = Color(0xFFFCEADA)
    val onPrimaryContainer = Color(0xFF5C2F13)

    /* ---------- 辅色：抹茶绿 ---------- */
    val secondary = Color(0xFF7C9A6D)
    val onSecondary = Color(0xFFFFFDF8)
    val secondaryVariant = Color(0xFF64805A)
    val secondaryContainer = Color(0xFFEFF3E7)
    val onSecondaryContainer = Color(0xFF3B4A31)
    val secondaryContainerVariant = Color(0xFFE5EBDB)
    val onSecondaryContainerVariant = Color(0xFF4B5B40)

    /* ---------- 第三级容器 ---------- */
    val tertiaryContainer = Color(0xFFF8EFE0)
    val onTertiaryContainer = Color(0xFF4A3E2C)
    val tertiaryContainerVariant = Color(0xFFF1E6D3)

    /* ---------- 错误 / 危险 ---------- */
    val error = Color(0xFFD2543E)
    val onError = Color(0xFFFFFDF8)
    val errorContainer = Color(0xFFFBE4DE)
    val onErrorContainer = Color(0xFF5C2118)

    /* ---------- 禁用态 ---------- */
    val disabledPrimary = Color(0xFFF0E4D3)
    val disabledOnPrimary = Color(0xFFB9AC98)
    val disabledPrimaryButton = Color(0xFFF2EADA)
    val disabledOnPrimaryButton = Color(0xFFB0A694)
    val disabledPrimarySlider = Color(0xFFEADFCB)
    val disabledSecondary = Color(0xFFF1EFE4)
    val disabledOnSecondary = Color(0xFFAFAC9B)
    val disabledSecondaryVariant = Color(0xFFEAE7DA)
    val disabledOnSecondaryVariant = Color(0xFFA7A291)
    val disabledOnSurface = Color(0xFFBFB6A4)

    /* ---------- 描边 / 分隔 / 遮罩 ---------- */
    val outline = Color(0xFFE3D9C6)
    val dividerLine = Color(0xFFEBE2D3)
    val windowDimming = Color(0x66000000)

    /* ---------- 滑块 ---------- */
    val sliderKeyPoint = Color(0xFFFFFDF8)
    val sliderKeyPointForeground = Color(0xFFE9DFCC)
    val sliderBackground = Color(0xFFEFE6D5)

    /**
     * 生成奶白色配色实例。
     *
     * Miuix 的 [Colors] 中 background / surface / outline 等大量字段为 internal，
     * 跨模块无法直接赋值。但 [lightColorScheme] 返回的实例提供 public 的
     * `copy(...)` 方法（含全部字段的命名参数），通过它即可整体生成自定义配色，
     * 未显式覆盖的字段保持 lightColorScheme() 的浅色默认值。
     */
    fun colors(): Colors {
        return lightColorScheme().copy(
            // 主色（暖橙）
            primary = primary,
            onPrimary = onPrimary,
            primaryVariant = primaryVariant,
            onPrimaryVariant = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            // 辅色（抹茶绿）
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryVariant = secondaryVariant,
            onSecondaryVariant = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            secondaryContainerVariant = secondaryContainerVariant,
            onSecondaryContainerVariant = onSecondaryContainerVariant,
            // 第三级容器
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            tertiaryContainerVariant = tertiaryContainerVariant,
            // 错误 / 危险
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,
            // 禁用态
            disabledPrimary = disabledPrimary,
            disabledOnPrimary = disabledOnPrimary,
            disabledPrimaryButton = disabledPrimaryButton,
            disabledOnPrimaryButton = disabledOnPrimaryButton,
            disabledPrimarySlider = disabledPrimarySlider,
            disabledSecondary = disabledSecondary,
            disabledOnSecondary = disabledOnSecondary,
            disabledSecondaryVariant = disabledSecondaryVariant,
            disabledOnSecondaryVariant = disabledOnSecondaryVariant,
            disabledOnSurface = disabledOnSurface,
            // 底色（奶白）
            background = background,
            onBackground = onBackground,
            onBackgroundVariant = onSurfaceSecondary,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceSecondary = onSurfaceSecondary,
            onSurfaceVariantSummary = onSurfaceVariantSummary,
            onSurfaceVariantActions = Color(0xFFA79681),
            // 容器 / 描边 / 滑块（internal 字段，经 copy 一并覆盖）
            surfaceContainer = surfaceContainer,
            onSurfaceContainer = onSurface,
            onSurfaceContainerVariant = onSurfaceSecondary,
            surfaceContainerHigh = surfaceContainerHigh,
            onSurfaceContainerHigh = onSurface,
            surfaceContainerHighest = surfaceContainerHighest,
            onSurfaceContainerHighest = onSurface,
            outline = outline,
            dividerLine = dividerLine,
            windowDimming = windowDimming,
            sliderKeyPoint = sliderKeyPoint,
            sliderKeyPointForeground = sliderKeyPointForeground,
            sliderBackground = sliderBackground,
        )
    }
}
