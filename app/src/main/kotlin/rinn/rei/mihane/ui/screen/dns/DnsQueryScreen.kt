package rinn.rei.mihane.ui.screen.dns

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.uwuaosp.compose.widget.expressive.SettingsActionButton
import org.uwuaosp.compose.widget.preference.CustomPreferenceRow
import org.uwuaosp.compose.widget.preference.PreferenceRow
import org.uwuaosp.compose.widget.preference.SettingsSectionHeader
import org.uwuaosp.compose.widget.scaffold.SettingsCollapsingAppBarScaffold
import org.uwuaosp.compose.widget.scaffold.SettingsToolbarActionButton
import rinn.rei.mihane.R
import rinn.rei.mihane.domain.model.DnsAnswer
import rinn.rei.mihane.ui.component.AppIcons
import rinn.rei.mihane.ui.component.CardItem
import rinn.rei.mihane.ui.component.LocalPreferencePosition
import rinn.rei.mihane.ui.component.groupedCardItems
import rinn.rei.mihane.ui.component.material.FormField
import rinn.rei.mihane.ui.component.material.shapeBackground
import rinn.rei.mihane.ui.theme.StatusColors
import rinn.rei.mihane.ui.util.horizontalCutoutPadding
import rinn.rei.mihane.ui.util.readableContentPadding
import rinn.rei.mihane.viewmodel.DnsQueryViewModel

@Composable
fun DnsQueryScreen(viewModel: DnsQueryViewModel, onBack: () -> Unit = {}) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val textFieldState = rememberTextFieldState()
    var showCacheDialog by remember { mutableStateOf(false) }

    SettingsCollapsingAppBarScaffold(
        title = stringResource(R.string.dns_title),
        showBackButton = true,
        onNavigateUp = onBack,
        actions = {
            SettingsToolbarActionButton(
                onClick = { showCacheDialog = true },
                imageVector = AppIcons.Delete,
                contentDescription = stringResource(R.string.dns_clear_cache),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().horizontalCutoutPadding(),
            contentPadding = readableContentPadding(innerPadding),
        ) {
            // 输入区域
            item(key = "input") {
                Column(modifier = Modifier.padding(all = 12.dp)) {
                    FormField(
                        state = textFieldState,
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(R.string.dns_domain),
                        useLabelAsPlaceholder = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        onKeyboardAction = {
                            viewModel.setQueryName(textFieldState.text.toString())
                            viewModel.queryDns()
                        },
                    )

                    Spacer(Modifier.height(12.dp))

                    // 查询类型选择（3+3 两行全宽）
                    val types = listOf("A", "AAAA", "CNAME", "MX", "TXT", "NS")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        types.take(3).forEach { type ->
                            SettingsActionButton(
                                label = type,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.setQueryType(type) },
                                style =
                                    org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Filled,
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        types.drop(3).forEach { type ->
                            SettingsActionButton(
                                label = type,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.setQueryType(type) },
                                style =
                                    org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Filled,
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    SettingsActionButton(
                        label =
                            if (uiState.isQuerying) stringResource(R.string.dns_querying)
                            else stringResource(R.string.dns_query),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = textFieldState.text.isNotBlank() && !uiState.isQuerying,
                        style = org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Filled,
                        onClick = {
                            viewModel.setQueryName(textFieldState.text.toString())
                            viewModel.queryDns()
                        },
                    )
                }
            }

            if (uiState.error.isNotEmpty()) {
                item(key = "error") {
                    CustomPreferenceRow(
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(
                                    horizontal =
                                        org.uwuaosp.compose.framework.theme.SettingsDimension
                                            .pagePadding
                                )
                                .padding(top = 12.dp, bottom = 6.dp)
                    ) {
                        Column(
                            Modifier.fillMaxWidth()
                                .padding(
                                    vertical =
                                        org.uwuaosp.compose.framework.theme.SettingsDimension
                                            .preferenceVerticalPadding
                                )
                        ) {
                            Text(
                                text = stringResource(R.string.dns_query_failed, uiState.error),
                                color = StatusColors.danger,
                            )
                        }
                    }
                }
            }

            // 结果区域
            if (uiState.status != null) {
                item(key = "result_title") {
                    SettingsSectionHeader(title = stringResource(R.string.dns_results))
                }

                if (uiState.answers.isEmpty()) {
                    item(key = "no_result") {
                        CustomPreferenceRow(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .padding(
                                        horizontal =
                                            org.uwuaosp.compose.framework.theme.SettingsDimension
                                                .pagePadding
                                    )
                                    .padding(bottom = 12.dp)
                        ) {
                            Column(
                                Modifier.fillMaxWidth()
                                    .padding(
                                        vertical =
                                            org.uwuaosp.compose.framework.theme.SettingsDimension
                                                .preferenceVerticalPadding
                                    )
                            ) {
                                Text(
                                    text = stringResource(R.string.dns_no_record),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                } else {
                    // 每条 DNS 记录独立成段，视觉上仍拼为一张连续卡片；记录多时只组合可见段
                    groupedCardItems(
                        keyPrefix = "dns_result",
                        outerBottomPadding = 12.dp,
                        items =
                            uiState.answers.mapIndexed { index, answer ->
                                CardItem("$index:${answer.data}") { DnsAnswerItem(answer) }
                            },
                    )
                }
            }

            item(key = "bottom_spacer") { Spacer(Modifier.navigationBarsPadding()) }
        }
    }

    // 缓存清除 Dialog
    if (showCacheDialog) {
        AlertDialog(
            onDismissRequest = { showCacheDialog = false },
            confirmButton = {
                FlowRow(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {}
            },
            title = { Text(stringResource(R.string.dns_clear_cache)) },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingsActionButton(
                            label = stringResource(R.string.dns_clear_dns),
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                viewModel.flushDnsCache()
                                showCacheDialog = false
                            },
                            style = org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Tonal,
                        )
                        SettingsActionButton(
                            label = stringResource(R.string.dns_clear_fakeip),
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                viewModel.flushFakeIp()
                                showCacheDialog = false
                            },
                            style = org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Tonal,
                        )
                    }
                }
            },
        )
    }
}

@Composable
private fun DnsAnswerItem(answer: DnsAnswer) {
    PreferenceRow(
        title = answer.data,
        summary = "TTL: ${answer.TTL}s",
        iconContent = {
            Box(
                modifier =
                    Modifier.padding(end = 10.dp)
                        .shapeBackground(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            3.dp,
                        )
                        .padding(horizontal = 5.dp, vertical = 1.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = dnsTypeToString(answer.type),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        index = LocalPreferencePosition.current.first,
        itemCount = LocalPreferencePosition.current.second,
    )
}

private fun dnsTypeToString(type: Int): String =
    when (type) {
        1 -> "A"
        2 -> "NS"
        5 -> "CNAME"
        6 -> "SOA"
        15 -> "MX"
        16 -> "TXT"
        28 -> "AAAA"
        33 -> "SRV"
        else -> "TYPE$type"
    }
