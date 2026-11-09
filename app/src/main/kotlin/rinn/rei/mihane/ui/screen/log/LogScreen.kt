package rinn.rei.mihane.ui.screen.log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberContainedSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.uwuaosp.compose.widget.preference.CustomPreferenceRow
import org.uwuaosp.compose.widget.scaffold.SettingsToolbarActionButton
import rinn.rei.mihane.R
import rinn.rei.mihane.domain.model.LogMessage
import rinn.rei.mihane.ui.component.AppIcons
import rinn.rei.mihane.ui.component.SearchPageScaffold
import rinn.rei.mihane.ui.util.horizontalCutoutPadding
import rinn.rei.mihane.ui.util.readableContentPadding
import rinn.rei.mihane.viewmodel.LogViewModel

@Composable
fun LogScreen(viewModel: LogViewModel, onBack: () -> Unit = {}) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val density = LocalDensity.current

    DisposableEffect(Unit) {
        viewModel.connect()
        onDispose { viewModel.disconnect() }
    }

    val searchField = rememberTextFieldState()
    val searchBarState = rememberContainedSearchBarState()

    // 搜索词只作过滤的 key，不写回 ViewModel：回写会在组合期触发状态写
    val searchText by remember { derivedStateOf { searchField.text.toString() } }
    val filteredLogs = remember(searchText, logs) { viewModel.filteredLogs(searchText) }

    // 仅当用户已滚到底部时自动跟随新日志，否则不抢夺滚动位置
    val autoScrollEnabled by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val visible = info.visibleItemsInfo
            if (visible.isEmpty()) true
            else {
                val lastVisible = visible.last().index
                // bottom_spacer 或最后一条 log 在可视范围内即视为"在底部"
                lastVisible >= info.totalItemsCount - 2
            }
        }
    }

    // key 用单调递增的末条 id：缓冲写满后 size 恒定，不能再以 size 变化驱动自动跟随
    val lastLogId = logs.lastOrNull()?.id
    LaunchedEffect(lastLogId) {
        // 搜索态下主列表被弹出层盖住，滚它也看不见，收起后还会停在错位置
        if (
            lastLogId != null &&
                autoScrollEnabled &&
                searchBarState.currentValue == SearchBarValue.Collapsed
        ) {
            listState.animateScrollToItem(logs.lastIndex)
        }
    }

    SearchPageScaffold(
        title = stringResource(R.string.log_title),
        showBackButton = true,
        onNavigateUp = onBack,
        actions = {
            SettingsToolbarActionButton(
                onClick = { viewModel.clearLogs() },
                imageVector = AppIcons.Delete,
                contentDescription = stringResource(R.string.log_clear),
            )
        },
        query = searchField,
        searchBarState = searchBarState,
        placeholder = stringResource(R.string.log_search),
        results = {
            val imeBottomPadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item { Spacer(Modifier.height(12.dp)) }

                if (filteredLogs.isEmpty()) {
                    item(key = "search_empty") {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = stringResource(R.string.log_no_match),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    items(items = filteredLogs, key = { it.id }, contentType = { "log" }) {
                        indexedLog ->
                        LogCard(indexedLog.message)
                    }
                }

                item { Spacer(Modifier.height(imeBottomPadding).navigationBarsPadding()) }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().horizontalCutoutPadding(),
            state = listState,
            contentPadding = readableContentPadding(innerPadding),
        ) {
            if (logs.isEmpty()) {
                item(key = "empty", contentType = "empty") {
                    Column(
                        modifier = Modifier.fillParentMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text =
                                if (uiState.isConnected) {
                                    stringResource(R.string.log_waiting)
                                } else {
                                    stringResource(R.string.log_not_connected)
                                },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                item(key = "top_spacer", contentType = "spacer") { Spacer(Modifier.height(12.dp)) }
            }

            items(items = logs, key = { it.id }, contentType = { "log" }) { indexedLog ->
                LogCard(indexedLog.message)
            }

            item(key = "bottom_spacer", contentType = "spacer") {
                Spacer(Modifier.height(24.dp).navigationBarsPadding())
            }
        }
    }
}

/** 解析后的日志条目 */
private data class ParsedLog(
    val protocol: String = "",
    val source: String = "",
    val target: String = "",
    val rule: String = "",
    val proxy: String = "",
    val raw: String = "",
)

/**
 * 解析 mihomo 日志 payload 格式: [TCP] 198.18.0.1:45552 --> statusapi.micloud.xiaomi.com:443 match
 * GeoSite(cn) using 全球直连(DIRECT)
 */
private fun parsePayload(payload: String): ParsedLog {
    val raw = payload.trim()

    // 提取协议 [TCP] / [UDP]
    val protocolMatch = Regex("^\\[(\\w+)]").find(raw)
    val protocol = protocolMatch?.groupValues?.get(1) ?: ""
    val rest =
        if (protocolMatch != null) raw.substring(protocolMatch.range.last + 1).trim() else raw

    // 提取 source --> target
    val arrowIdx = rest.indexOf("-->")
    if (arrowIdx < 0) return ParsedLog(raw = raw)

    val source = rest.substring(0, arrowIdx).trim()
    val afterArrow = rest.substring(arrowIdx + 3).trim()

    // 提取 match ... using ...
    val matchIdx = afterArrow.indexOf(" match ")
    if (matchIdx < 0) {
        return ParsedLog(protocol = protocol, source = source, target = afterArrow, raw = raw)
    }

    val target = afterArrow.substring(0, matchIdx).trim()
    val matchPart = afterArrow.substring(matchIdx + 7).trim()

    val usingIdx = matchPart.indexOf(" using ")
    val rule: String
    val proxy: String
    if (usingIdx >= 0) {
        rule = matchPart.substring(0, usingIdx).trim()
        proxy = matchPart.substring(usingIdx + 7).trim()
    } else {
        rule = matchPart
        proxy = ""
    }

    return ParsedLog(
        protocol = protocol,
        source = source,
        target = target,
        rule = rule,
        proxy = proxy,
        raw = raw,
    )
}

@Composable
private fun LogCard(log: LogMessage) {
    val levelInfo = getLevelInfo(log.type)
    val parsed = remember(log.payload) { parsePayload(log.payload) }
    val isParsed = parsed.target.isNotEmpty()

    CustomPreferenceRow(
        modifier =
            Modifier.fillMaxWidth()
                .padding(
                    horizontal = org.uwuaosp.compose.framework.theme.SettingsDimension.pagePadding
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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // 顶行：级别标签 + 协议
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LevelBadge(levelInfo)

                    Spacer(Modifier.width(8.dp))

                    if (isParsed && parsed.protocol.isNotEmpty()) {
                        ProtocolBadge(parsed.protocol)
                        Spacer(Modifier.width(8.dp))
                    }

                    Text(
                        text = levelInfo.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = levelInfo.color,
                    )
                }

                if (isParsed) {
                    // 目标地址
                    Text(
                        text = parsed.target,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    // 规则 + 代理
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        if (parsed.rule.isNotEmpty()) {
                            Text(
                                text = parsed.rule,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (parsed.proxy.isNotEmpty()) {
                            Text(
                                text = parsed.proxy,
                                style = MaterialTheme.typography.labelMedium,
                                color = levelInfo.color.copy(alpha = 0.8f),
                            )
                        }
                    }
                } else {
                    // 无法解析，原样显示
                    Text(
                        text = log.payload,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelBadge(levelInfo: LevelInfo) {
    androidx.compose.material3.Badge(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Text(levelInfo.label)
    }
}

@Composable
private fun ProtocolBadge(protocol: String) {
    androidx.compose.material3.Badge(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Text(protocol)
    }
}

private data class LevelInfo(val label: String, val name: String, val color: Color)

private fun getLevelInfo(type: String): LevelInfo {
    return when (type.lowercase()) {
        "error" -> LevelInfo("E", "Error", Color(0xFFFF5252))
        "warning" -> LevelInfo("W", "Warning", Color(0xFFFFB74D))
        "info" -> LevelInfo("I", "Info", Color(0xFF69C0FF))
        "debug" -> LevelInfo("D", "Debug", Color(0xFF81C784))
        else -> LevelInfo("V", "Verbose", Color(0xFFBDBDBD))
    }
}
