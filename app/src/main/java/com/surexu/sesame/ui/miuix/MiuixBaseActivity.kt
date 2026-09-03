package com.surexu.sesame.ui.miuix

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import com.surexu.sesame.data.AppConfig
import com.surexu.sesame.data.ViewAppInfo
import com.surexu.sesame.util.LanguageUtil
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

open class MiuixBaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageUtil.setLocal(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppConfig.load()
        ViewAppInfo.init(applicationContext)
        setupSystemBars()
    }

    /**
     * 所有 miuix 页面统一用此方法设置内容。
     *
     * 全量奶白色主题：明暗两套配色都注入 [CreamTheme]，并固定为 Light 模式，
     * 因此无论系统是否处于深色模式，界面始终保持奶白色。
     */
    protected fun setAppContent(content: @Composable () -> Unit) {
        setContent {
            val controller = remember {
                // 位置参数：colorSchemeMode / lightColors / darkColors
                ThemeController(ColorSchemeMode.Light, CreamTheme.colors(), CreamTheme.colors())
            }
            MiuixTheme(controller) {
                content()
            }
        }
    }

    private fun setupSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        // 奶白底 → 状态栏/导航栏图标一律用深色，保证始终可见
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
    }
}
