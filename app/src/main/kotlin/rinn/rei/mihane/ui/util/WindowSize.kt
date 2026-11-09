package rinn.rei.mihane.ui.util

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import rinn.rei.mihane.ui.theme.LocalPlatformDensity

/** 宽屏阈值：窗口宽度达到此值时启用侧边导航栏与内容居中（平板、横屏、展开态折叠屏、桌面）。 */
private val WideScreenMinWidth = 600.dp

/** 宽屏下内容的最大宽度，超出后两侧留白居中，避免超宽窗口下卡片被过度拉伸。 */
val MaxContentWidth: Dp = org.uwuaosp.compose.framework.theme.SettingsDimension.contentMaxWidth

/** 当前窗口是否达到宽屏尺寸。宽屏切换到 Expressive 侧栏与 SDK 页面骨架。 */
@Composable
fun rememberIsWideScreen(): Boolean {
    val containerSize = LocalWindowInfo.current.containerSize
    val density = LocalPlatformDensity.current ?: LocalDensity.current
    return with(density) { containerSize.width.toDp() >= WideScreenMinWidth }
}

/** 内容宽度由SDK按实际窗口约束限制，列表保留自己的滚动状态 */
@Composable
fun WideContentBox(modifier: Modifier = Modifier, content: @Composable (sidePadding: Dp) -> Unit) {
    org.uwuaosp.compose.widget.ui.SettingsContent(modifier = modifier.fillMaxSize()) {
        content(0.dp)
    }
}

/**
 * 为内容补上水平方向（起始 + 末尾）的屏幕缺口 / 侧边导航栏 inset padding。二级页 Scaffold 内容默认 只吃了顶部 inset，横屏遇到侧边刘海 / 挖孔 /
 * 手势条时内容会压到缺口下——在内容根容器加此 modifier 让内容避开。竖屏或无侧边缺口时 inset 为 0、等同无操作。顶栏由 `TopAppBar` 自己的
 * `defaultWindowInsetsPadding` 处理，与此互不重叠。
 */
@Composable
fun Modifier.horizontalCutoutPadding(): Modifier =
    windowInsetsPadding(
        WindowInsets.displayCutout
            .union(WindowInsets.navigationBars)
            .only(WindowInsetsSides.Horizontal)
    )

@Composable
fun Modifier.sheetContentSafePadding(): Modifier =
    windowInsetsPadding(
        WindowInsets.systemBars.union(WindowInsets.displayCutout).only(WindowInsetsSides.Bottom)
    )

@Composable
fun Modifier.sheetHeightTransition(): Modifier =
    animateContentSize(androidx.compose.material3.MaterialTheme.motionScheme.defaultSpatialSpec())

/** Secondary pages have no navigation rail; keep their rows inside a readable window width. */
@Composable
fun readableContentPadding(
    insets: androidx.compose.foundation.layout.PaddingValues
): androidx.compose.foundation.layout.PaddingValues {
    val width = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.width.toDp() }
    val side = ((width - MaxContentWidth) / 2).coerceAtLeast(0.dp)
    val direction = androidx.compose.ui.platform.LocalLayoutDirection.current
    return androidx.compose.foundation.layout.PaddingValues(
        start = maxOf(side, insets.calculateStartPadding(direction)),
        end = maxOf(side, insets.calculateEndPadding(direction)),
        top = insets.calculateTopPadding(),
    )
}
