package rinn.rei.mihane.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.uwuaosp.compose.widget.preference.CustomPreferenceRow
import org.uwuaosp.compose.widget.preference.SettingsSectionHeader
import rinn.rei.mihane.R
import rinn.rei.mihane.ui.component.AppIcons
import rinn.rei.mihane.ui.theme.StatusColors
import rinn.rei.mihane.util.FormatUtils
import rinn.rei.mihane.viewmodel.HomeUiState

fun LazyListScope.latencySection(
    state: HomeUiState = HomeUiState(),
    onTestLatency: () -> Unit = {},
) {
    item(key = "latency_title") { LatencyHeader(state, onTestLatency) }
    item(key = "latency") {
        CustomPreferenceRow(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(horizontal = HomeContentPadding)
                    .padding(bottom = 6.dp)
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LatencyItem(
                        "Baidu",
                        state.latencyBaidu,
                        state.hasTestedLatency,
                        Modifier.weight(1f),
                    )
                    LatencyItem(
                        "Cloudflare",
                        state.latencyCloudflare,
                        state.hasTestedLatency,
                        Modifier.weight(1f),
                    )
                    LatencyItem(
                        "Google",
                        state.latencyGoogle,
                        state.hasTestedLatency,
                        Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun LatencyHeader(state: HomeUiState, onTestLatency: () -> Unit) {
    val tested = state.hasTestedLatency
    val delays = listOf(state.latencyBaidu, state.latencyCloudflare, state.latencyGoogle)
    val worst = if (delays.any { it < 0 }) -1 else delays.max()
    val statusColor =
        if (tested && !state.isTestingLatency) StatusColors.delay(worst) else StatusColors.neutral
    val statusText =
        stringResource(
            when {
                state.isTestingLatency -> R.string.home_latency_testing
                !tested -> R.string.home_latency_untested
                delays.all { it < 0 } -> R.string.home_latency_abnormal
                worst < 0 -> R.string.home_latency_partial
                worst >= 500 -> R.string.home_latency_high
                worst >= 200 -> R.string.home_latency_slow
                else -> R.string.home_latency_normal
            }
        )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsSectionHeader(
            title = stringResource(R.string.home_latency),
            modifier = Modifier.padding(start = HomeContentPadding - 8.dp),
        )
        Row(
            modifier = Modifier.padding(end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // 本轮未经规则引擎，明确标注而非静默给数字
            if (state.isRunning && !state.latencyViaRules) {
                Text(
                    text = stringResource(R.string.home_latency_not_via_rules),
                    style = MaterialTheme.typography.bodySmall,
                    color = StatusColors.warning,
                )
            }
            if (state.isRunning) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor,
                )
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(statusColor))
                if (state.isTestingLatency) {
                    LoadingIndicator(Modifier.size(40.dp))
                } else {
                    FilledTonalIconButton(
                        onClick = onTestLatency,
                        shapes = IconButtonDefaults.shapes(),
                    ) {
                        Icon(AppIcons.Refresh, stringResource(R.string.common_refresh))
                    }
                }
            }
        }
    }
}

@Composable
private fun LatencyItem(name: String, delay: Int, tested: Boolean, modifier: Modifier) {
    // -1 视为"未测/不可达"，由 token 映射到 neutral 灰
    val color = StatusColors.delay(if (delay < 0 && !tested) null else delay)

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        }
        Text(
            text = FormatUtils.formatLatency(delay),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
