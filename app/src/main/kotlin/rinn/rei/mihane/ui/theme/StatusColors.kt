package rinn.rei.mihane.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/** 状态语义色 token。集中管理运行状态、延迟、操作按钮的色谱并支持深浅色， 调用方一律走 token，避免散落的 `Color(0xFF...)`。 */
object StatusColors {

    val neutral: Color
        @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant

    val danger: Color
        @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.error

    val warning: Color
        @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.tertiary

    val healthy: Color
        @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary

    /** 延迟语义：null=未测、<0=超时、<200=优、<500=一般、>=500=差 */
    @Composable
    @ReadOnlyComposable
    fun delay(value: Int?): Color =
        when {
            value == null -> neutral
            value < 0 -> danger
            value < 200 -> healthy
            value < 500 -> warning
            else -> danger
        }

    @Composable
    @ReadOnlyComposable
    fun runState(state: RunState): Color =
        when (state) {
            RunState.Running -> healthy
            RunState.Pending -> warning
            RunState.Stopped -> danger
        }

    @Composable
    @ReadOnlyComposable
    fun runStateContainer(state: RunState): Color =
        when (state) {
            RunState.Running -> MaterialTheme.colorScheme.primaryContainer
            RunState.Pending -> MaterialTheme.colorScheme.tertiaryContainer
            RunState.Stopped -> MaterialTheme.colorScheme.errorContainer
        }

    /** 三色按钮（restart/stop/reload）的容器与文字色 */
    @Composable
    @ReadOnlyComposable
    fun actionButton(action: ActionKind): ActionPalette {
        val c = MaterialTheme.colorScheme
        return when (action) {
            ActionKind.Restart -> ActionPalette(c.secondaryContainer, c.onSecondaryContainer)
            ActionKind.Stop -> ActionPalette(c.errorContainer, c.onErrorContainer)
            ActionKind.Reload -> ActionPalette(c.tertiaryContainer, c.onTertiaryContainer)
        }
    }

    val trafficUpload: Color
        @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary

    val trafficDownload: Color
        @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.tertiary

    /** 订阅配额用量：逼近耗尽时转警示色，其余跟随主题 primary */
    @Composable
    @ReadOnlyComposable
    fun usage(progress: Float): Color =
        when {
            progress >= 0.9f -> danger
            progress >= 0.75f -> warning
            else -> MaterialTheme.colorScheme.primary
        }

    /** Proxy 节点选中态背景 */
    val selectedNodeContainer: Color
        @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.secondaryContainer

    /** Proxy 节点固定态背景（URLTest / Fallback 被钉住），暖调以区别于冷调选中态 */
    val fixedNodeContainer: Color
        @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.tertiaryContainer
}

enum class RunState {
    Running,
    Pending,
    Stopped,
}

enum class ActionKind {
    Restart,
    Stop,
    Reload,
}

data class ActionPalette(val container: Color, val content: Color)
