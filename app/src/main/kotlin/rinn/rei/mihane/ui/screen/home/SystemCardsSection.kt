package rinn.rei.mihane.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlin.time.Instant
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.uwuaosp.compose.framework.theme.SettingsDimension
import org.uwuaosp.compose.widget.preference.CustomPreferenceRow
import org.uwuaosp.compose.widget.scaffold.SettingsToolbarActionButton
import rinn.rei.mihane.R
import rinn.rei.mihane.platform.NetworkAddressType
import rinn.rei.mihane.ui.component.AppIcons
import rinn.rei.mihane.ui.component.material.ContentSheet
import rinn.rei.mihane.ui.component.material.LocalSheetDismiss
import rinn.rei.mihane.ui.component.material.shapeBackground
import rinn.rei.mihane.ui.util.sheetContentSafePadding
import rinn.rei.mihane.ui.util.sheetHeightTransition
import rinn.rei.mihane.util.FormatUtils
import rinn.rei.mihane.util.formatIsoTimeRelative
import rinn.rei.mihane.viewmodel.MemorySnapshot
import rinn.rei.mihane.viewmodel.ProviderTrafficInfo
import rinn.rei.mihane.viewmodel.SystemInfoSnapshot

/** 「概览」分组第二行：本机 IP / 网卡 + 系统占用；标题由 [overviewCardsSection] 的第一行统一给出 */
fun LazyListScope.systemCardsSection(
    memory: MemorySnapshot = MemorySnapshot(),
    systemInfo: SystemInfoSnapshot = SystemInfoSnapshot(),
) {
    item(key = "system_cards") {
        HomeCardPair(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(
                        horizontal = HomeContentPadding,
                        vertical = SettingsDimension.sectionSpacing,
                    ),
            first = { cardModifier ->
                CustomPreferenceRow(modifier = cardModifier) {
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
                            Text(
                                text = "IP",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            BadgeLabel(
                                when (systemInfo.addressType) {
                                    NetworkAddressType.Unknown -> "--"
                                    NetworkAddressType.Tun -> "TUN"
                                    NetworkAddressType.Lan -> "LAN"
                                    NetworkAddressType.Wan -> "WAN"
                                }
                            )
                        }
                        InfoRow(
                            stringResource(R.string.home_address),
                            systemInfo.localIp,
                            Modifier.padding(top = 8.dp),
                        )
                        InfoRow(
                            stringResource(R.string.home_interface),
                            systemInfo.interfaceName,
                            Modifier.padding(top = 4.dp),
                        )
                    }
                }
            },
            second = { cardModifier ->
                CustomPreferenceRow(modifier = cardModifier) {
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
                            Text(
                                text = stringResource(R.string.home_system),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            BadgeLabel("SYS")
                        }
                        InfoRow("CPU", systemInfo.cpuUsage, Modifier.padding(top = 8.dp))
                        InfoRow("RAM", memory.ramUsage, Modifier.padding(top = 4.dp))
                    }
                }
            },
        )
    }
}

@Composable
internal fun SubscriptionTrafficDialog(
    show: Boolean,
    providers: ImmutableList<ProviderTrafficInfo>,
    isLoading: Boolean,
    loadFailed: Boolean,
    onUpdateAll: () -> Unit,
    onDismiss: () -> Unit,
) {
    ContentSheet(
        show = show,
        title = stringResource(R.string.home_subscription_provider_traffic),
        onDismissRequest = onDismiss,
        startAction = {
            // 打开弹窗时已自动 GET 重读快照；此按钮触发真实更新（会消耗订阅服务器配额）
            FilledTonalIconButton(
                onClick = onUpdateAll,
                enabled = !isLoading,
                shapes = androidx.compose.material3.IconButtonDefaults.shapes(),
            ) {
                Icon(
                    imageVector = AppIcons.Refresh,
                    contentDescription = stringResource(R.string.home_provider_update_all),
                )
            }
        },
        endAction = {
            val dismiss = LocalSheetDismiss.current
            SettingsToolbarActionButton(
                onClick = { dismiss?.invoke() },
                imageVector = AppIcons.Close,
                contentDescription = stringResource(R.string.common_close),
            )
        },
    ) {
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .sheetContentSafePadding()
                    .heightIn(min = 200.dp)
                    .sheetHeightTransition()
        ) {
            when {
                // 刷新期间隐藏旧数据，只显示加载指示器，成功后再展示新内容
                isLoading -> LoadingIndicator(modifier = Modifier.align(Alignment.Center))

                loadFailed ->
                    ProviderTrafficMessage(
                        text =
                            stringResource(R.string.home_subscription_provider_traffic_load_failed),
                        modifier = Modifier.align(Alignment.Center),
                    )

                providers.isEmpty() ->
                    ProviderTrafficMessage(
                        text = stringResource(R.string.home_subscription_no_provider_traffic),
                        modifier = Modifier.align(Alignment.Center),
                    )

                else ->
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(items = providers, key = { it.id }) { provider ->
                            ProviderTrafficCard(provider)
                        }
                    }
            }
        }
    }
}

@Composable
private fun ProviderTrafficMessage(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ProviderTrafficCard(provider: ProviderTrafficInfo) {
    val upload = provider.upload.coerceAtLeast(0)
    val download = provider.download.coerceAtLeast(0)
    val total = provider.total.coerceAtLeast(0)
    val used = if (Long.MAX_VALUE - upload < download) Long.MAX_VALUE else upload + download
    val remaining = (total - used).coerceAtLeast(0)
    // total<=0 的语义见 HomeViewModel.activeSubscriptionInfo：剩余/总量/进度显示 "--"
    val hasQuota = total > 0
    val progress =
        if (hasQuota) {
            (used.toFloat() / total.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
    val percent = if (hasQuota) "${(progress * 100).roundToInt()}%" else "--"
    val updatedText = remember(provider.updatedAt) { formatIsoTimeRelative(provider.updatedAt) }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier.weight(1f).padding(start = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = provider.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (provider.nodeCount > 0) {
                    Spacer(Modifier.width(6.dp))
                    NodeCountBadge(provider.nodeCount)
                }
            }
            Spacer(Modifier.width(8.dp))
            if (updatedText.isNotEmpty()) {
                Text(
                    text = updatedText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            if (provider.hasTraffic) {
                Spacer(Modifier.width(6.dp))
                UsageBadge(percent)
            }
        }

        if (provider.hasTraffic) {
            TrafficDetailPanel(
                progress = progress.takeIf { hasQuota },
                used = FormatUtils.formatBytes(used),
                remaining = if (hasQuota) FormatUtils.formatBytes(remaining) else "--",
                total = if (hasQuota) FormatUtils.formatBytes(total) else "--",
                expire = expireText(provider.expire),
            )
        }
    }
}

@Composable
private fun NodeCountBadge(count: Int) {
    Text(
        text = count.toString(),
        modifier =
            Modifier.shapeBackground(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), 3.dp)
                .padding(horizontal = 4.dp, vertical = 1.dp),
        style = MaterialTheme.typography.labelSmall,
        fontFamily = FontFamily.Monospace,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun TrafficDetailPanel(
    progress: Float?,
    used: String,
    remaining: String,
    total: String,
    expire: String,
) {
    CustomPreferenceRow(modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth()
                .padding(
                    vertical =
                        org.uwuaosp.compose.framework.theme.SettingsDimension
                            .preferenceVerticalPadding
                )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (progress != null) {
                    TrafficProgressBar(progress)
                }
                UsedRemainingRow(used = used, remaining = remaining)
                TotalExpireRow(total = total, expire = expire)
            }
        }
    }
}

@Composable
private fun UsedRemainingRow(used: String, remaining: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Row(
            modifier = Modifier.weight(1f).padding(end = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TrafficMetaLabel(stringResource(R.string.home_used))
            TrafficMetaValue(used)
        }
        Box(
            modifier =
                Modifier.width(1.dp)
                    .height(20.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
        )
        Row(
            modifier = Modifier.weight(1f).padding(start = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TrafficMetaLabel(stringResource(R.string.home_remaining))
            TrafficMetaValue(remaining)
        }
    }
}

@Composable
private fun TotalExpireRow(total: String, expire: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "${stringResource(R.string.home_total)} $total",
            modifier = Modifier.weight(0.42f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = expire,
            modifier = Modifier.weight(0.58f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TrafficMetaLabel(text: String) {
    Text(
        text = text,
        maxLines = 1,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun TrafficMetaValue(text: String) {
    Text(
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.End,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun UsageBadge(text: String) {
    androidx.compose.material3.Badge(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Text(text)
    }
}

@Composable
private fun TrafficProgressBar(progress: Float) {
    androidx.compose.material3.LinearWavyProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = Modifier.fillMaxWidth(),
    )
}

/** 完整的到期展示文本：无到期时间时不带「到期」前缀，有日期时为「到期 yyyy-MM-dd HH:mm」 */
@Composable
private fun expireText(expire: Long): String {
    if (expire <= 0) return stringResource(R.string.home_no_expire)
    val formatted =
        runCatching {
                Instant.fromEpochSeconds(expire)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .formatDateTime()
            }
            .getOrNull() ?: stringResource(R.string.home_expire_unknown)
    return "${stringResource(R.string.home_expire)} $formatted"
}

private fun LocalDateTime.formatDateTime(): String = buildString {
    append(year.toString().padStart(4, '0'))
    append('-')
    append(month.number.toString().padStart(2, '0'))
    append('-')
    append(day.toString().padStart(2, '0'))
    append(' ')
    append(hour.toString().padStart(2, '0'))
    append(':')
    append(minute.toString().padStart(2, '0'))
}
