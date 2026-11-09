package rinn.rei.mihane.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.uwuaosp.compose.widget.preference.CustomPreferenceRow
import rinn.rei.mihane.R
import rinn.rei.mihane.platform.TunMode
import rinn.rei.mihane.ui.component.AppIcons
import rinn.rei.mihane.ui.theme.RunState
import rinn.rei.mihane.ui.theme.StatusColors
import rinn.rei.mihane.viewmodel.HomeUiState

fun LazyListScope.statusSection(
    state: HomeUiState = HomeUiState(),
    uptime: String = "",
    onSwitchMode: (String) -> Unit = {},
    onSwitchTunStack: (String) -> Unit = {},
    isTabletLayout: Boolean = false,
) {
    item(key = "status") {
        StatusContent(state, uptime, onSwitchMode, onSwitchTunStack, isTabletLayout)
    }
}

@Composable
private fun StatusContent(
    state: HomeUiState,
    uptime: String,
    onSwitchMode: (String) -> Unit,
    onSwitchTunStack: (String) -> Unit,
    isTabletLayout: Boolean,
) {
    val isRunning = state.isRunning
    val isStarting = state.isStarting
    val isStopping = state.isStopping

    val statusIcon =
        if (isRunning) {
            AppIcons.CheckCircleOutline
        } else {
            AppIcons.RemoveCircleOutline
        }
    val runState =
        when {
            isStarting || isStopping -> RunState.Pending
            isRunning -> RunState.Running
            else -> RunState.Stopped
        }
    val statusTint = StatusColors.runState(runState)

    var showModeDialog by remember { mutableStateOf(false) }
    var showTunStackDialog by remember { mutableStateOf(false) }

    CustomPreferenceRow(
        modifier =
            Modifier.fillMaxWidth().padding(horizontal = HomeContentPadding, vertical = 12.dp)
    ) {
        Column(
            Modifier.fillMaxWidth()
                .padding(
                    vertical =
                        org.uwuaosp.compose.framework.theme.SettingsDimension
                            .preferenceVerticalPadding
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(statusIcon, null, Modifier.size(48.dp), tint = statusTint)
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(
                            when {
                                isStopping -> R.string.home_stopping
                                isStarting -> R.string.home_starting
                                isRunning -> R.string.home_running
                                else -> R.string.home_stopped
                            }
                        ),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    val runtimeSummary =
                        listOf(
                                state.version,
                                uptime
                                    .takeIf { it.isNotEmpty() }
                                    ?.let { stringResource(R.string.home_uptime_label, it) },
                            )
                            .filterNotNull()
                            .filter { it.isNotBlank() }
                            .joinToString(" - ")
                    if (!isTabletLayout || (isRunning && runtimeSummary.isNotBlank())) {
                        Text(
                            runtimeSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (isStarting || isStopping)
                    androidx.compose.material3.LoadingIndicator(Modifier.size(40.dp))
            }
        }
    }
    HomeCardPair(
        modifier =
            Modifier.fillMaxWidth()
                .padding(horizontal = HomeContentPadding)
                .padding(bottom = 12.dp),
        first = { cardModifier ->
            ModeCard(
                stringResource(R.string.home_mode),
                modeLabel(state.mode),
                cardModifier,
                enabled = !isStarting && !isStopping,
                onClick = { showModeDialog = true },
            )
        },
        second = { cardModifier ->
            val isTproxy = state.tunMode == TunMode.RootTproxy
            ModeCard(
                stringResource(R.string.home_tun),
                if (isTproxy) stringResource(R.string.home_inbound_tproxy, TPROXY_INBOUND_PORT)
                else tunStackLabel(state.tunStack),
                cardModifier,
                enabled = isRunning && !isTproxy,
                onClick = { showTunStackDialog = true },
            )
        },
    )

    OptionSelectDialog(
        show = showModeDialog,
        title = stringResource(R.string.home_switch_mode),
        options = MODE_OPTIONS,
        current = state.mode,
        onSelect = onSwitchMode,
        onDismiss = { showModeDialog = false },
    )

    OptionSelectDialog(
        show = showTunStackDialog,
        title = stringResource(R.string.home_switch_tun_stack),
        options = TUN_STACK_OPTIONS,
        current = state.tunStack,
        onSelect = onSwitchTunStack,
        onDismiss = { showTunStackDialog = false },
    )
}

@Composable
private fun ModeCard(
    title: String,
    value: String,
    modifier: Modifier,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    CustomPreferenceRow(modifier, enabled = enabled, onClick = onClick) {
        Column(Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    }
}

// ROOT TPROXY 模式 mihomo tproxy-port（与 RootTproxyApplier.TPROXY_PORT 对齐，UI 仅展示用）
private const val TPROXY_INBOUND_PORT: Int = 7895

// value（mihomo 侧小写标识）to label（展示名），同时供卡片取值与选择弹窗列举
private val MODE_OPTIONS = listOf("rule" to "Rule", "global" to "Global", "direct" to "Direct")

private val TUN_STACK_OPTIONS =
    listOf("mixed" to "Mixed", "gvisor" to "gVisor", "system" to "System", "mips" to "MIPS")

private fun modeLabel(mode: String): String =
    MODE_OPTIONS.firstOrNull { it.first == mode.lowercase() }?.second ?: mode.ifEmpty { "--" }

private fun tunStackLabel(stack: String): String =
    TUN_STACK_OPTIONS.firstOrNull { it.first == stack.lowercase() }?.second
        ?: stack.ifEmpty { "--" }

@Composable
private fun OptionSelectDialog(
    show: Boolean,
    title: String,
    options: List<Pair<String, String>>,
    current: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    if (show) {
        org.uwuaosp.compose.widget.editor.SettingsChoiceDialog(
            title = title,
            options = options.map { it.second },
            selectedIndex = options.indexOfFirst { it.first == current.lowercase() },
            onSelect = { index ->
                onSelect(options[index].first)
                onDismiss()
            },
            onDismissRequest = onDismiss,
        )
    }
}
