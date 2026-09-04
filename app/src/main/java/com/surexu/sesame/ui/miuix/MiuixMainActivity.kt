package com.surexu.sesame.ui.miuix

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.surexu.sesame.R
import com.surexu.sesame.data.AppConfig
import com.surexu.sesame.data.ModelGroup
import com.surexu.sesame.data.RunType
import com.surexu.sesame.data.ViewAppInfo
import com.surexu.sesame.util.FileUtil
import com.surexu.sesame.util.LanguageUtil
import com.surexu.sesame.util.Log
import com.surexu.sesame.util.PermissionUtil
import com.surexu.sesame.util.Statistics
import com.surexu.sesame.util.Statistics.DataType
import com.surexu.sesame.util.Statistics.TimeType
import com.surexu.sesame.util.ToastUtil
import com.surexu.sesame.util.idMap.UserIdMap
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.io.File
import java.util.Calendar

class MiuixMainActivity : MiuixBaseActivity() {

    var runTypeText by mutableStateOf("")
    var statisticsText by mutableStateOf("")
    var hasPermission by mutableStateOf(false)

    /** 配置页当前选中的账号 userId；null=默认配置。必须持久在 Activity 级别，否则切换底部 tab 后 ConfigTab 离开组合会丢失。 */
    var selectedUserId by mutableStateOf<String?>(null)

    private val handler = Handler(Looper.getMainLooper())
    private var isClick = false
    private val titleRunner = Runnable { updateSubTitle(RunType.DISABLE) }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            Log.i("view broadcast action:" + action + " intent:" + intent)
            if (action != null) {
                when (action) {
                    "com.surexu.sesame.status" -> {
                        // 模块已被 LSPosed 启用并注入支付宝，标记为已激活
                        ViewAppInfo.setRunTypeByCode(RunType.MODEL.getCode())
                        updateSubTitle(RunType.MODEL)
                        handler.removeCallbacks(titleRunner)
                        if (isClick) {
                            ToastUtil.show(context, "芝麻粒加载状态正常")
                            isClick = false
                        }
                    }

                    "com.surexu.sesame.update" -> {
                        Statistics.load()
                        statisticsText = Statistics.getText(this@MiuixMainActivity)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // runType 被模块置为 MODEL（onModuleLoaded）时立即刷新界面，无需手动加载配置
        ViewAppInfo.setRunTypeListener {
            runOnUiThread {
                updateSubTitle(ViewAppInfo.getRunType())
            }
        }
        ViewAppInfo.checkRunType()
        updateSubTitle(ViewAppInfo.getRunType())
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.surexu.sesame.status")
        intentFilter.addAction("com.surexu.sesame.update")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(broadcastReceiver, intentFilter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(broadcastReceiver, intentFilter)
        }
        setAppContent {
            MainScreen(this)
        }
    }

    override fun onResume() {
        super.onResume()
        // 每次回到前台都重估权限:从系统授权页返回时 DisposableEffect 不会重跑,这里负责把状态翻新
        hasPermission = PermissionUtil.checkFilePermissions(this)
        if (hasPermission) {
            if (RunType.DISABLE == ViewAppInfo.getRunType()) {
                handler.postDelayed(titleRunner, 3000)
                try {
                    sendBroadcast(Intent("com.eg.android.AlipayGphone.sesame.status"))
                } catch (th: Throwable) {
                    Log.i("view sendBroadcast status err:")
                    Log.printStackTrace(th)
                }
            }
            try {
                Statistics.load()
                Statistics.updateDay(Calendar.getInstance())
                statisticsText = Statistics.getText(this)
            } catch (e: Exception) {
                Log.printStackTrace(e)
            }
        }
    }

    fun updateSubTitle(runType: RunType) {
        runTypeText = when (runType) {
            RunType.DISABLE -> ViewAppInfo.getAppTitle() + "【" + getString(R.string.disable) + "】"
            RunType.MODEL -> ViewAppInfo.getAppTitle() + "【" + getString(R.string.activated) + "】"
            RunType.PACKAGE -> ViewAppInfo.getAppTitle() + "【" + getString(R.string.loading) + "】"
        }
    }

    fun sendStatus() {
        try {
            isClick = true
            sendBroadcast(Intent("com.eg.android.AlipayGphone.sesame.status"))
        } catch (th: Throwable) {
            Log.i("view sendBroadcast status err:")
            Log.printStackTrace(th)
        }
    }

    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            ToastUtil.show(this, "无法打开链接")
        }
    }

    fun toggleLanguage() {
        val appConfig = AppConfig.INSTANCE
        appConfig.languageSimplifiedChinese = !appConfig.languageSimplifiedChinese
        if (AppConfig.save()) {
            LanguageUtil.setLocal(this)
            recreate()
        }
    }

    fun isIconHidden(): Boolean {
        val alias = ComponentName(this, "com.surexu.sesame.ui.MainActivityAlias")
        return packageManager.getComponentEnabledSetting(alias) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    }

    fun toggleHideIcon() {
        val alias = ComponentName(this, "com.surexu.sesame.ui.MainActivityAlias")
        val state = packageManager.getComponentEnabledSetting(alias)
        val newState = if (state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
        }
        packageManager.setComponentEnabledSetting(alias, newState, PackageManager.DONT_KILL_APP)
    }

    fun exportStatistics(): Uri? {
        return FileUtil.getExportedStatisticsFile()?.let { Uri.fromFile(it) }
    }

    fun importStatistics(): Boolean {
        val src = FileUtil.getExportedStatisticsFile()
        if (src != null && FileUtil.copyTo(src, FileUtil.getStatisticsFile())) {
            statisticsText = Statistics.getText(this)
            return true
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(broadcastReceiver)
        } catch (_: Exception) {
        }
    }
}

@Composable
fun MainScreen(activity: MiuixMainActivity) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        if (!PermissionUtil.checkOrRequestFilePermissions(activity)) {
            activity.hasPermission = false
        } else {
            activity.hasPermission = true
        }
        onDispose { }
    }

    Scaffold(
        bottomBar = {
            FloatingNavBar(
                items = listOf(
                    NavBarTab("首页", Icons.Filled.Home),
                    NavBarTab("日志", Icons.Filled.Description),
                    NavBarTab("配置", Icons.Filled.Tune),
                    NavBarTab("设置", Icons.Filled.Settings),
                ),
                selected = selectedTab,
                onSelect = { selectedTab = it },
            )
        },
        containerColor = MiuixTheme.colorScheme.surface
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            when (selectedTab) {
                0 -> HomeTab(activity)
                1 -> LogsTab(activity)
                2 -> ConfigTab(activity)
                3 -> SettingsTab(activity)
            }
        }
    }
}

/** 悬浮导航栏的条目定义 */
data class NavBarTab(val label: String, val icon: ImageVector)

/**
 * 悬浮拟态底部导航栏。
 *
 * 与背景同色的拟态胶囊从奶白底面上"浮起"，两侧留边、底部悬空，
 * 选中项以凹陷槽 + 暖橙高亮呈现，形成软按压的交互隐喻。
 */
@Composable
fun FloatingNavBar(
    items: List<NavBarTab>,
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 10.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .neuRaised(RoundedCornerShape(30.dp), 10.dp)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, tab ->
                val isSelected = index == selected
                Column(
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .then(
                            if (isSelected) Modifier.neuPressed(RoundedCornerShape(20.dp))
                            else Modifier
                        )
                        .clickable { onSelect(index) }
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val tint =
                        if (isSelected) MiuixTheme.colorScheme.primary
                        else MiuixTheme.colorScheme.onSurfaceVariantSummary
                    Image(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        modifier = Modifier.size(23.dp),
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(tint)
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = tab.label,
                        fontSize = 11.sp,
                        color = tint
                    )
                }
            }
        }
    }
}

@Composable
fun HomeTab(activity: MiuixMainActivity) {
    val context = LocalContext.current
    val activated = ViewAppInfo.getRunType() == RunType.MODEL
    val appTitle = ViewAppInfo.getAppTitle()
    val version = ViewAppInfo.getAppVersion()

    Text(
        text = "Sure-Xu",
        fontSize = 34.sp,
        fontWeight = FontWeight.Bold,
        color = MiuixTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
    Spacer(Modifier.height(14.dp))

    // 能量统计卡：顶部状态条（激活状态 + 标题），下方 4×4 数据网格
    EnergyStatsCard(activated = activated, version = version)
    Spacer(Modifier.height(16.dp))

    // 随机一言：点击整卡换一句
    HitokotoCard()
    Spacer(Modifier.height(16.dp))
}

/**
 * 能量统计卡（拟态）：顶部状态条 + 4×4 数据网格。
 * 顶部左侧红点 + "已激活/未加载"状态 + 版本号，右侧"能量统计"标题；
 * 下方 4 行 × 4 列：行=总量/今年/本月/今日，列=收取/帮收/浇水。
 */
@Composable
fun EnergyStatsCard(activated: Boolean, version: String) {
    val timeTypes = listOf(TimeType.TOTAL, TimeType.YEAR, TimeType.MONTH, TimeType.DAY)
    val dataTypes = listOf(DataType.COLLECTED, DataType.HELPED, DataType.WATERED)
    val colHeaders = listOf("收取", "帮收", "浇水")
    val rowHeaders = listOf("总量", "今年", "本月", "今日")

    Column(
        Modifier
            .fillMaxWidth()
            .neuRaised(RoundedCornerShape(24.dp), 5.dp)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        // 顶部状态条
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 状态红点
                Box(
                    Modifier
                        .size(10.dp)
                        .background(
                            if (activated) Color(0xFF4CAF50) else Color(0xFFE0532C),
                            CircleShape
                        )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (activated) "已激活" else "未加载",
                    fontSize = 15.sp,
                    color = if (activated) MiuixTheme.colorScheme.secondary
                    else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "· $version",
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }
            Text(
                text = "能量统计",
                fontSize = 14.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
            )
        }

        Spacer(Modifier.height(8.dp))

        // 表头
        Row(Modifier.fillMaxWidth()) {
            Box(Modifier.weight(1f))
            colHeaders.forEach { h ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = h,
                        fontSize = 14.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))

        // 数据行
        rowHeaders.forEachIndexed { rowIdx, rowLabel ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.weight(1f)) {
                    Text(
                        text = rowLabel,
                        fontSize = 14.sp,
                        color = if (rowIdx == 3) MiuixTheme.colorScheme.primary
                        else MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                }
                dataTypes.forEach { dt ->
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = "%,d".format(Statistics.getData(timeTypes[rowIdx], dt)),
                            fontSize = 15.sp,
                            color = MiuixTheme.colorScheme.onBackground,
                            fontWeight = if (rowIdx == 3) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

/** 一言（Hitokoto）客户端：短超时，避免阻塞首页 */
private val hitokotoClient by lazy {
    okhttp3.OkHttpClient.Builder()
        .connectTimeout(java.util.concurrent.TimeUnit.SECONDS.toMillis(5), java.util.concurrent.TimeUnit.MILLISECONDS)
        .readTimeout(java.util.concurrent.TimeUnit.SECONDS.toMillis(5), java.util.concurrent.TimeUnit.MILLISECONDS)
        .build()
}

private const val HITOKOTO_URL = "https://v1.hitokoto.cn/?encode=json"

/**
 * 随机一言卡片：拟态表面 + 点击刷新。
 * 数据来自 hitokoto.cn；网络不可用时静默保留上一句（首次为内置句）。
 */
@Composable
fun HitokotoCard() {
    var sentence by remember { mutableStateOf("慢慢来，比较快。") }
    var source by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(refreshKey) {
        loading = true
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val request = okhttp3.Request.Builder().url(HITOKOTO_URL).build()
                hitokotoClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string().orEmpty()
                        if (body.isNotBlank()) {
                            val node = com.fasterxml.jackson.databind.ObjectMapper().readTree(body)
                            sentence = node.path("hitokoto").asText(sentence)
                            val from = node.path("from").asText("")
                            val fromWho = node.path("from_who").asText("")
                            source = when {
                                from.isNotBlank() && fromWho.isNotBlank() -> "—— $fromWho「$from」"
                                from.isNotBlank() -> "——「$from」"
                                fromWho.isNotBlank() -> "—— $fromWho"
                                else -> ""
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // 网络不可用：保留当前句子，不打扰用户
            }
        }
        loading = false
    }

    Column(
        Modifier
            .fillMaxWidth()
            .neuRaised(RoundedCornerShape(24.dp), 5.dp)
            .clickable(enabled = !loading) { refreshKey++ }
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "「 一言 」",
                fontSize = 12.sp,
                color = MiuixTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            // 刷新按钮：凹陷圆槽
            Box(
                Modifier
                    .size(30.dp)
                    .neuPressed(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "换一句",
                    modifier = Modifier.size(15.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                        if (loading) MiuixTheme.colorScheme.primary
                        else MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = sentence,
            fontSize = 15.sp,
            lineHeight = 23.sp,
            color = MiuixTheme.colorScheme.onBackground
        )
        if (source.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = source,
                fontSize = 12.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun LogsTab(activity: MiuixMainActivity) {
    Text(
        text = "日志",
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = MiuixTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
    )

    SmallTitle(text = "分类记录")
    CardColumn {
        var forest by remember { mutableStateOf(AppConfig.INSTANCE.enableForestLog ?: true) }
        LogSwitchRow("森林记录", forest, onClick = { openLog(activity, LogType.FOREST) }) {
            forest = it
            AppConfig.INSTANCE.enableForestLog = it
            AppConfig.save()
            if (!it) FileUtil.clearLog("forest")
        }
        var farm by remember { mutableStateOf(AppConfig.INSTANCE.enableFarmLog ?: true) }
        LogSwitchRow("庄园记录", farm, onClick = { openLog(activity, LogType.FARM) }) {
            farm = it
            AppConfig.INSTANCE.enableFarmLog = it
            AppConfig.save()
            if (!it) FileUtil.clearLog("farm")
        }
        var other by remember { mutableStateOf(AppConfig.INSTANCE.enableOtherLog ?: true) }
        LogSwitchRow("其他记录", other, onClick = { openLog(activity, LogType.OTHER) }) {
            other = it
            AppConfig.INSTANCE.enableOtherLog = it
            AppConfig.save()
            if (!it) FileUtil.clearLog("other")
        }
    }
    Spacer(Modifier.height(16.dp))

    SmallTitle(text = "系统记录")
    CardColumn {
        var debug by remember { mutableStateOf(AppConfig.INSTANCE.enableDebugLog ?: false) }
        LogSwitchRow("抓包记录", debug, onClick = { openLog(activity, LogType.DEBUG) }) {
            debug = it
            AppConfig.INSTANCE.enableDebugLog = it
            AppConfig.save()
            if (!it) FileUtil.clearLog("debug")
        }
        var error by remember { mutableStateOf(AppConfig.INSTANCE.enableViewErrorLog ?: true) }
        LogSwitchRow("查看异常日志", error, onClick = { openLog(activity, LogType.ERROR) }) {
            error = it
            AppConfig.INSTANCE.enableViewErrorLog = it
            AppConfig.save()
            if (!it) FileUtil.clearLog("error")
        }
        var runtime by remember { mutableStateOf(AppConfig.INSTANCE.enableViewRuntimeLog ?: true) }
        LogSwitchRow("查看运行日志", runtime, onClick = { openLog(activity, LogType.RUNTIME) }) {
            runtime = it
            AppConfig.INSTANCE.enableViewRuntimeLog = it
            AppConfig.save()
            if (!it) FileUtil.clearLog("runtime")
        }
    }
    Spacer(Modifier.height(16.dp))
}

/** 日志条目行：点按整行进入对应日志详情；右侧开关控制是否记录 */
@Composable
fun LogSwitchRow(title: String, checked: Boolean, onClick: () -> Unit, onCheckedChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
            color = MiuixTheme.colorScheme.onBackground
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/** 打开日志查看器(显示指定日志类型的全部条目) */
fun openLog(activity: MiuixMainActivity, logType: LogType) {
    try {
        activity.startActivity(
            Intent(activity, MiuixLogViewerActivity::class.java)
                .putExtra(LogType.EXTRA_LOG_TYPE, logType.name)
        )
    } catch (t: Throwable) {
        Log.printStackTrace(t)
    }
}

/** 配置分组入口页(图2)的图标与描述映射:全部 ModelGroup 均展示,不隐藏空分组 */
internal val GROUP_EMOJI: Map<ModelGroup, String> = mapOf(
    ModelGroup.BASE to "⚙️",
    ModelGroup.FOREST to "🌳",
    ModelGroup.FARM to "🐔",
    ModelGroup.STALL to "🏪",
    ModelGroup.ORCHARD to "🍎",
    ModelGroup.SPORTS to "🏃",
    ModelGroup.MEMBER to "👑",
    ModelGroup.OTHER to "📦"
)

private val GROUP_DESC: Map<ModelGroup, String> = mapOf(
    ModelGroup.BASE to "应用与通用设置",
    ModelGroup.FOREST to "能量森林收取设置",
    ModelGroup.FARM to "蚂蚁庄园收取设置",
    ModelGroup.STALL to "新村摆摊相关设置",
    ModelGroup.ORCHARD to "农场果树相关设置",
    ModelGroup.SPORTS to "运动与步数设置",
    ModelGroup.MEMBER to "会员权益相关设置",
    ModelGroup.OTHER to "扩展与杂项设置"
)

/**
 * 配置分组入口条目(图2):左侧拟态凸起圆形图标槽 + 组名与描述 + 右侧 › 箭头。
 */
@Composable
fun GroupEntryRow(emoji: String, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(46.dp)
                .neuRaised(CircleShape, 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 21.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MiuixTheme.colorScheme.onBackground
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }
        }
        Text(
            text = "›",
            fontSize = 22.sp,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.padding(horizontal = 6.dp)
        )
    }
}

@Composable
fun ConfigTab(activity: MiuixMainActivity) {
    val context = LocalContext.current
    // selectedUserId 上提到 Activity 级别，切换底部 tab 后 ConfigTab 离开组合也不会重置
    var selectedUserId by activity.selectedUserId
    val items = remember {
        val list = ArrayList<Pair<String?, String>>()
        list.add(null to "默认")
        try {
            val dir = FileUtil.CONFIG_DIRECTORY_FILE
            dir.listFiles()?.forEach { configDir ->
                if (configDir.isDirectory) {
                    val userId = configDir.name
                    UserIdMap.loadSelf(userId)
                    val userEntity = UserIdMap.get(userId)
                    val name = userEntity?.let { it.showName + ": " + it.account } ?: userId
                    list.add(userId to name)
                }
            }
        } catch (e: Exception) {
            Log.printStackTrace(e)
        }
        list
    }

    Text(
        text = "配置",
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = MiuixTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
    )

    SmallTitle(text = "配置管理")
    CardColumn {
        items.forEach { (userId, name) ->
            val selected = selectedUserId == userId
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .then(
                        if (selected) Modifier.neuPressed(RoundedCornerShape(14.dp))
                        else Modifier
                    )
                    .clickable { selectedUserId = userId }
                    .padding(horizontal = 12.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    modifier = Modifier.weight(1f),
                    fontSize = 16.sp,
                    fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                    color = if (selected) MiuixTheme.colorScheme.primary
                    else MiuixTheme.colorScheme.onBackground
                )
                if (selected) {
                    Image(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "当前配置",
                        modifier = Modifier.size(20.dp),
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                            MiuixTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(16.dp))

    val currentName = items.firstOrNull { it.first == selectedUserId }?.second ?: "默认"
    SmallTitle(text = "配置分组 · $currentName")
    CardColumn {
        ModelGroup.values().forEach { g ->
            GroupEntryRow(
                emoji = GROUP_EMOJI[g] ?: "📦",
                title = g.getName(),
                subtitle = GROUP_DESC[g] ?: "",
                onClick = {
                    val intent = Intent(context, MiuixSettingsActivity::class.java)
                    if (selectedUserId != null) intent.putExtra("userId", selectedUserId)
                    intent.putExtra("group", g.code)
                    context.startActivity(intent)
                }
            )
        }
    }
    Spacer(Modifier.height(16.dp))
}

@Composable
fun SettingsTab(activity: MiuixMainActivity) {
    val context = LocalContext.current

    Text(
        text = "设置",
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = MiuixTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
    )

    SmallTitle(text = "功能设置")
    CardColumn {
        ArrowPreference(
            title = "好友统计",
            onClick = { context.startActivity(Intent(context, MiuixFriendStatsActivity::class.java)) }
        )
        ArrowPreference(
            title = "扩展功能",
            onClick = { context.startActivity(Intent(context, MiuixExtensionsActivity::class.java)) }
        )
    }
    Spacer(Modifier.height(16.dp))

    SmallTitle(text = "系统设置")
    CardColumn {
        var iconHidden by remember { mutableStateOf(activity.isIconHidden()) }
        BooleanSwitch("隐藏图标", iconHidden) {
            activity.toggleHideIcon()
            iconHidden = activity.isIconHidden()
        }
        // 界面已固定为奶白色主题（见 MiuixBaseActivity / CreamTheme），
        // 原「深色模式」「跟随系统设置」开关不再生效，故移除以免误导。
    }
    Spacer(Modifier.height(16.dp))

    SmallTitle(text = "关于")
    CardColumn {
        ArrowPreference(
            title = "关于应用",
            onClick = { context.startActivity(Intent(context, MiuixAboutActivity::class.java)) }
        )
    }
    Spacer(Modifier.height(16.dp))

}

@Composable
fun BooleanSwitch(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    SwitchPreference(
        title = title,
        checked = checked,
        onCheckedChange = onCheckedChange
    )
}

@Composable
fun CardColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .neuRaised(RoundedCornerShape(24.dp), 5.dp)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        content()
    }
}
