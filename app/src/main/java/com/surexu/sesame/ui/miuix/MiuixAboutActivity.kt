package com.surexu.sesame.ui.miuix

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.surexu.sesame.R
import com.surexu.sesame.data.ViewAppInfo
import com.surexu.sesame.util.ToastUtil
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** 关于页：拟态图标 + 项目介绍 + 源码链接 + 开源许可。 */
class MiuixAboutActivity : MiuixBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setAppContent {
            AboutScreen(this)
        }
    }
}

@Composable
fun AboutScreen(activity: MiuixAboutActivity) {
    Scaffold(
        topBar = {
            LogTopBar(
                title = "关于",
                onBack = { activity.finish() }
            )
        },
        containerColor = MiuixTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(top = 20.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 拟态图标容器：凹陷圆槽内浮起 logo
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .neuRaised(CircleShape, 8.dp)
                    .padding(10.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.size(88.dp).clip(CircleShape)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = ViewAppInfo.getAppTitle(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MiuixTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "版本 ${ViewAppInfo.getAppVersion()} · 拟态奶白版",
                fontSize = 13.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
            )
            Spacer(Modifier.height(20.dp))

            // 项目介绍卡
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .neuRaised(RoundedCornerShape(24.dp), 5.dp)
                    .padding(18.dp)
            ) {
                Text(
                    text = "一个把温暖握在手里的 Sure-Xu。",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Sure-Xu 是芝麻粒系列的个人定制分支：拟态（Soft UI）奶白界面、悬浮式底部导航，延续蚂蚁森林收能量与庄园打理的自动化能力。基于开源的 Sesame-M 演化而来，同样遵循 GPL-3.0 完整开源。",
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = MiuixTheme.colorScheme.onSurfaceSecondary,
                    textAlign = TextAlign.Justify
                )
            }
            Spacer(Modifier.height(16.dp))

            // 链接卡
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .neuRaised(RoundedCornerShape(24.dp), 5.dp)
                    .padding(vertical = 6.dp)
            ) {
                ArrowPreference(
                    title = "在 GitHub 查看源码",
                    onClick = { openWebUrl(activity, "https://github.com/755596985/Sure-Xu") }
                )
                ArrowPreference(
                    title = "下载最新版本",
                    onClick = { openWebUrl(activity, "https://github.com/755596985/Sure-Xu/releases") }
                )
            }
            Spacer(Modifier.height(16.dp))

            // 开源许可卡（GPL-3.0 合规声明）
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .neuRaised(RoundedCornerShape(24.dp), 5.dp)
                    .padding(18.dp)
            ) {
                Text(
                    text = "开源许可",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "本项目以 GPL-3.0 协议完整开源，禁止商业用途与闭源二次分发。源流致谢：Sesame-M（aw1y2z）、Sesame-GR（Dragon813）、Sesame（TKaxv-7S）、XQuickEnergy（constanline / pansong291），以及 Miuix 与 libxposed 的维护者们。",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }
        }
    }
}

private fun openWebUrl(activity: MiuixBaseActivity, url: String) {
    try {
        activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: Exception) {
        ToastUtil.show(activity, "无法打开链接")
    }
}
