# Sure-Xu

[![License](https://img.shields.io/github/license/755596985/Sure-Xu.svg)](LICENSE)
[![Release](https://img.shields.io/github/v/release/755596985/Sure-Xu)](https://github.com/755596985/Sure-Xu/releases)

> **拟态奶白 · 悬浮导航 · 温柔的芝麻粒**
>
> Sure-Xu 是芝麻粒系列的个人定制分支——在开源生态的能力之上，用一套完整的**拟态（Neumorphism / Soft UI）奶白设计语言**重新诠释界面：所有元素与背景同色，仅靠柔和的双向光照塑造体积，悬浮式底部导航栏像一枚浮在奶白底面上的胶囊。

## 界面特色

| 特性 | 说明 |
| --- | --- |
| 🥛 **拟态奶白主题** | 全局暖米白基色（`#FAF5EC`），凸起面左上高光、右下柔影；选中项以凹陷槽呈现"软按压"隐喻 |
| 🫧 **悬浮底部导航** | 四枚导航项（首页 / 日志 / 配置 / 设置）收进一枚两侧留边的拟态胶囊，选中项凹陷 + 暖橙高亮 |
| 🎨 **全新图标** | 奶白拟态容器 + 暖橙芝麻粒种子，与应用主题浑然一体 |
| 📖 **重写的关于页** | 项目介绍、版本下载、开源许可一目了然 |

## 下载

- **[apk/Sure-Xu-Normal-1.2.0.apk](apk/Sure-Xu-Normal-1.2.0.apk)**（[全部版本](https://github.com/755596985/Sure-Xu/releases)）
- 需要 **root + LSPosed** 框架方可生效；安装前请卸载同包名旧版本（如有）

## 快速上手

1. 安装 APK，在 LSPosed 中启用 Sure-Xu 模块，作用域勾选**支付宝**
2. 强制停止支付宝后重新打开，模块自动注入
3. 打开 Sure-Xu App：**首页**看运行状态与数据统计 → **配置**里调任务参数 → **日志**里查收执行记录

主要能力（继承自芝麻粒生态）：蚂蚁森林能量收取/浇水、庄园喂养与任务、海洋、果园、运动等自动化，全部任务可在配置页独立开关。

## 本地构建

```bash
# 环境：JDK 17+ / Android SDK（platforms;android-37, build-tools）
./gradlew assembleNormalRelease
# 产物：app/build/outputs/apk/normal/release/Sure-Xu-Normal-x.y.z.apk（已签名）
```

签名读取 `app/keystore.properties`（不入库，格式见下），首次克隆后需自备：

```properties
storeFile=your-release.jks
storePassword=你的口令
keyAlias=你的别名
keyPassword=你的口令
```

## 技术栈

- **模块框架**：[libxposed](https://github.com/libxposed/api) API 102（LSPosed 加载）
- **UI**：Jetpack Compose + [Miuix](https://github.com/compose-miuix-ui/miuix)，叠加自研拟态设计层（`Neumorphic.kt`）
- **核心逻辑**：沿用 Sesame-M 任务模型（`model/task/*`），JNI 桥接保留 `io.github.lazyimmortal.sesame.util.LibraryUtil`（libsesame.so 符号绑定，不可改名）
- **构建**：Gradle 9.4 / AGP 9.2 / Kotlin 2.4，仓库指向国内镜像，大陆网络可直连编译

## 使用条款（沿用上游）

1. 本项目为学习研究用途，不得用于任何形式的商业行为。
2. 使用者因违反本声明规定而触犯法律的，后果自负，作者不承担责任。
3. 本模块完全免费开源，请勿二次贩卖。

## 授权说明

本项目基于 [aw1y2z 版 Sesame-M](https://github.com/aw1y2z/Sesame-M) 修改而来，并沿其源流基于 [Dragon813 版 Sesame-GR](https://github.com/Dragon813/Sesame-GR)、[TKaxv-7S 版 Sesame](https://github.com/SenOffical/Sesame-TK)、[constanline 版 XQuickEnergy](https://github.com/constanline/XQuickEnergy) 与 [pansong291 版 XQuickEnergy](https://github.com/pansong291/XQuickEnergy) 开发。

采用 **GPL-3.0** 完整开源：**禁止**商业用途，**禁止**二次修改后闭源发布。第三方组件许可证原文见 [licenses/](licenses/) 目录。

## 特别感谢

- 芝麻粒生态的维护者与贡献者（TKaxv-7S、Dragon813、LazyImmortal、Fansirsqi、aw1y2z 等）
- [Miuix](https://github.com/compose-miuix-ui/miuix) 与 [libxposed](https://github.com/libxposed/api) 的开发者们
