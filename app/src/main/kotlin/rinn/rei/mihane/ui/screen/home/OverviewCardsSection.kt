package rinn.rei.mihane.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import org.uwuaosp.compose.framework.theme.SettingsDimension
import org.uwuaosp.compose.widget.preference.CustomPreferenceRow
import org.uwuaosp.compose.widget.preference.SettingsSectionHeader
import rinn.rei.mihane.R
import rinn.rei.mihane.domain.model.SubscriptionInfo
import rinn.rei.mihane.ui.theme.StatusColors
import rinn.rei.mihane.util.FormatUtils
import rinn.rei.mihane.viewmodel.HomeUiState
import rinn.rei.mihane.viewmodel.SpeedSnapshot

/** 「概览」分组第一行：实时网速（带流量折线图）+ 订阅用量（可点开代理集合弹窗）。 本行的标题同时统辖 [systemCardsSection] 的第二行，四张卡共处一个分组。 */
fun LazyListScope.overviewCardsSection(
    speed: SpeedSnapshot = SpeedSnapshot(),
    state: HomeUiState = HomeUiState(),
    onSpeedClick: () -> Unit = {},
    onSubscriptionClick: () -> Unit = {},
) {
    item(key = "overview_title") {
        SettingsSectionHeader(
            title = stringResource(R.string.home_overview),
            modifier = Modifier.padding(start = HomeContentPadding - 8.dp),
        )
    }
    item(key = "overview_cards") {
        HomeCardPair(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(
                        horizontal = HomeContentPadding,
                        vertical = SettingsDimension.sectionSpacing,
                    ),
            first = { cardModifier ->
                SpeedCard(cardModifier, speed, state.isRunning, onSpeedClick)
            },
            second = { cardModifier ->
                SubscriptionCard(
                    cardModifier,
                    state.subscription,
                    state.isRunning,
                    onSubscriptionClick,
                )
            },
        )
    }
}

@Composable
private fun SpeedCard(
    modifier: Modifier = Modifier,
    speed: SpeedSnapshot,
    isRunning: Boolean,
    onClick: () -> Unit,
) {
    // 曲线铺满卡片背景，文字层独立消耗内边距
    CustomPreferenceRow(modifier = modifier, onClick = if (isRunning) onClick else null) {
        Column(
            Modifier.fillMaxWidth()
                .padding(
                    vertical =
                        org.uwuaosp.compose.framework.theme.SettingsDimension
                            .preferenceVerticalPadding
                )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                TrafficSparkline(
                    // matchParentSize 不参与测量，卡片高度仍由文字内容决定，同行的订阅卡不被拉高
                    modifier = Modifier.matchParentSize(),
                    history = speed.history,
                    uploadColor = StatusColors.trafficUpload,
                    downloadColor = StatusColors.trafficDownload,
                )
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.home_speed),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        BadgeLabel("NET")
                    }
                    InfoRow(
                        stringResource(R.string.home_upload),
                        speed.uploadSpeed,
                        Modifier.padding(top = 8.dp),
                    )
                    InfoRow(
                        stringResource(R.string.home_download),
                        speed.downloadSpeed,
                        Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SubscriptionCard(
    modifier: Modifier = Modifier,
    subscription: SubscriptionInfo?,
    isRunning: Boolean,
    onClick: () -> Unit,
) {
    val total = subscription?.Total?.coerceAtLeast(0) ?: 0
    val used = subscription?.let { (it.Upload + it.Download).coerceAtLeast(0) } ?: 0
    // total<=0 的语义见 HomeViewModel.activeSubscriptionInfo：不画用量水印、badge 退回 "SUB"
    val hasQuota = total > 0
    val progress = if (hasQuota) (used.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f

    CustomPreferenceRow(modifier = modifier, onClick = if (isRunning) onClick else null) {
        Column(
            Modifier.fillMaxWidth()
                .padding(
                    vertical =
                        org.uwuaosp.compose.framework.theme.SettingsDimension
                            .preferenceVerticalPadding
                )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.home_subscription),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        BadgeLabel(if (hasQuota) "${(progress * 100).roundToInt()}%" else "SUB")
                    }
                    InfoRow(
                        stringResource(R.string.home_used),
                        FormatUtils.formatBytes(used),
                        Modifier.padding(top = 8.dp),
                    )
                    InfoRow(
                        stringResource(R.string.home_total),
                        if (hasQuota) FormatUtils.formatBytes(total) else "--",
                        Modifier.padding(top = 4.dp),
                    )
                    if (hasQuota) {
                        SubscriptionUsageBar(
                            Modifier.fillMaxWidth().padding(top = 16.dp),
                            progress,
                            StatusColors.usage(progress),
                        )
                    }
                }
            }
        }
    }
}
