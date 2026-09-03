package com.surexu.sesame.ui.miuix

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Sure-Xu 拟态（Neumorphism / Soft UI）设计系统。
 *
 * 核心思想：界面元素与背景使用**同一奶白基色**，
 * 完全依靠柔和的双向光照（左上高光 + 右下阴影）塑造体积感——
 * 凸起 [neuRaised] 像从背景里"长出来"，凹陷 [neuPressed] 像被"按进去"。
 *
 * 实现方式（Compose 官方能力，兼容 minSdk 26）：
 * - 凸起：`Modifier.shadow`（spot/ambient 用暖阴影色）+ 对角渐变描边（白→暖灰）
 * - 凹陷：深一档底色 + 反向渐变描边（暖灰→白），模拟内阴影
 */
object Neu {

    /** 拟态基色：与窗口底色一致的暖米白 */
    val base = Color(0xFFFAF5EC)

    /** 左上高光 */
    val highlight = Color(0xFFFFFFFF)

    /** 右下阴影（暖灰棕） */
    val shadow = Color(0xFFD6C9B2)

    /** 凹陷面底色（比基色深半档） */
    val pressed = Color(0xFFF2EBDC)

    /** 凹陷面深描边 */
    val pressedDark = Color(0xFFCFC1A8)

    /** 默认圆角 */
    val defaultCorner: Dp = 22.dp
}

/**
 * 凸起拟态表面：右下柔影 + 左上→右下渐变描边。
 *
 * @param shape 表面形状（决定阴影与描边轮廓）
 * @param elevation 阴影高度，越大越"浮"
 * @param base 表面底色，默认与背景同色（拟态精髓）
 */
fun Modifier.neuRaised(
    shape: Shape = RoundedCornerShape(Neu.defaultCorner),
    elevation: Dp = 6.dp,
    base: Color = Neu.base,
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = shape,
        clip = false,
        ambientColor = Neu.shadow,
        spotColor = Neu.shadow,
    )
    .background(base, shape)
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(listOf(Neu.highlight, Neu.shadow)),
        shape = shape,
    )

/**
 * 凹陷拟态表面：深一档底色 + 反向渐变描边（左上暗、右下亮），
 * 模拟光线射入凹槽的内阴影效果。常用于选中态、输入槽、图标槽。
 */
fun Modifier.neuPressed(
    shape: Shape = RoundedCornerShape(Neu.defaultCorner),
    base: Color = Neu.pressed,
): Modifier = this
    .background(base, shape)
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(listOf(Neu.pressedDark, Neu.highlight)),
        shape = shape,
    )
