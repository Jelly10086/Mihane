package rinn.rei.mihane.ui.screen.connection

import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberContainedSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.uwuaosp.compose.widget.expressive.SettingsActionButton
import org.uwuaosp.compose.widget.preference.CustomPreferenceRow
import org.uwuaosp.compose.widget.preference.SettingsSectionHeader
import org.uwuaosp.compose.widget.scaffold.SettingsToolbarActionButton
import rinn.rei.mihane.R
import rinn.rei.mihane.domain.model.ConnectionInfo
import rinn.rei.mihane.ui.component.AppIcons
import rinn.rei.mihane.ui.component.SearchPageScaffold
import rinn.rei.mihane.ui.component.material.shapeBackground
import rinn.rei.mihane.ui.util.horizontalCutoutPadding
import rinn.rei.mihane.ui.util.readableContentPadding
import rinn.rei.mihane.util.FormatUtils
import rinn.rei.mihane.viewmodel.ConnectionViewModel

@Composable
fun ConnectionScreen(viewModel: ConnectionViewModel, onBack: () -> Unit = {}) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCloseAllDialog by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    // WS 订阅只在本页存续，理由见 startObserving
    DisposableEffect(viewModel) {
        viewModel.startObserving()
        onDispose { viewModel.stopObserving() }
    }

    val searchField = rememberTextFieldState()
    val searchBarState = rememberContainedSearchBarState()

    // 搜索过滤（直接传入 searchText，避免组合期间写入 ViewModel 状态）
    val searchText by remember { derivedStateOf { searchField.text.toString() } }
    val filteredConnections =
        remember(searchText, uiState.connections) { viewModel.filteredConnections(searchText) }

    SearchPageScaffold(
        title = stringResource(R.string.connection_title),
        showBackButton = true,
        onNavigateUp = onBack,
        actions = {
            SettingsToolbarActionButton(
                onClick = { showCloseAllDialog = true },
                imageVector = AppIcons.Delete,
                contentDescription = stringResource(R.string.connection_close_all),
            )
        },
        query = searchField,
        searchBarState = searchBarState,
        placeholder = stringResource(R.string.connection_search),
        results = {
            // 搜索结果列表
            val imeBottomPadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item { Spacer(Modifier.height(6.dp)) }

                if (filteredConnections.isEmpty()) {
                    item(key = "search_empty") {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = stringResource(R.string.connection_no_match),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    items(filteredConnections, key = { it.id }) { conn ->
                        ConnectionItem(
                            connection = conn,
                            onClose = { viewModel.closeConnection(conn.id) },
                        )
                    }
                }

                item { Spacer(Modifier.height(imeBottomPadding).navigationBarsPadding()) }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().horizontalCutoutPadding(),
            contentPadding = readableContentPadding(innerPadding),
        ) {
            if (uiState.connections.isEmpty() && !uiState.isConnected) {
                item(key = "empty") {
                    Column(
                        modifier = Modifier.fillParentMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.connection_no_active),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = stringResource(R.string.connection_start_first),
                            modifier = Modifier.padding(top = 6.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                item(key = "stats", contentType = "stats") {
                    StatsCard(
                        connectionCount = uiState.connections.size,
                        uploadTotal = uiState.uploadTotal,
                        downloadTotal = uiState.downloadTotal,
                    )
                }

                item(key = "connection_title", contentType = "title") {
                    SettingsSectionHeader(title = stringResource(R.string.connection_list))
                }

                items(
                    items = uiState.connections,
                    key = { it.id },
                    contentType = { "connection" },
                ) { conn ->
                    ConnectionItem(
                        connection = conn,
                        onClose = { viewModel.closeConnection(conn.id) },
                    )
                }

                item(key = "bottom_spacer", contentType = "spacer") {
                    Spacer(Modifier.navigationBarsPadding())
                }
            }
        }
    }

    // 关闭全部确认 Dialog
    if (showCloseAllDialog) {
        AlertDialog(
            onDismissRequest = { showCloseAllDialog = false },
            confirmButton = {
                FlowRow(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SettingsActionButton(
                        label = stringResource(R.string.common_cancel),
                        onClick = { showCloseAllDialog = false },
                        style = org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Text,
                    )
                    SettingsActionButton(
                        label = stringResource(R.string.common_confirm),
                        style = org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Text,
                        onClick = {
                            viewModel.closeAllConnections()
                            showCloseAllDialog = false
                        },
                    )
                }
            },
            title = { Text(stringResource(R.string.connection_close_all_title)) },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(stringResource(R.string.connection_close_all_summary))
                }
            },
        )
    }
}

@Composable
private fun StatsCard(connectionCount: Int, uploadTotal: Long, downloadTotal: Long) {
    CustomPreferenceRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
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
            ) {
                StatItem(
                    label = stringResource(R.string.connection_active),
                    value = "$connectionCount",
                )
                StatItem(
                    label = stringResource(R.string.connection_upload_total),
                    value = FormatUtils.formatBytes(uploadTotal),
                )
                StatItem(
                    label = stringResource(R.string.connection_download_total),
                    value = FormatUtils.formatBytes(downloadTotal),
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ConnectionItem(connection: ConnectionInfo, onClose: () -> Unit) {
    val meta = connection.metadata

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
            // 标题行：Host + 关闭按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier =
                        Modifier.shapeBackground(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                3.dp,
                            )
                            .padding(horizontal = 5.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = meta.network.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = meta.host.ifEmpty { "${meta.destinationIP}:${meta.destinationPort}" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Image(
                    modifier =
                        Modifier.size(14.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = LocalIndication.current,
                                role = Role.Button,
                                onClick = onClose,
                            ),
                    imageVector = AppIcons.Close,
                    contentDescription = stringResource(R.string.common_close),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                )
            }

            if (connection.chains.isNotEmpty()) {
                Text(
                    text = connection.chains.joinToString(" → "),
                    modifier = Modifier.padding(top = 2.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            val ruleText = buildString {
                append(connection.rule)
                if (connection.rulePayload.isNotEmpty()) {
                    append("(${connection.rulePayload})")
                }
            }
            Text(
                text = ruleText,
                modifier = Modifier.padding(top = 2.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // 底部信息行：流量 + 进程名
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text =
                        "↑ ${FormatUtils.formatBytes(connection.upload)}  ↓ ${FormatUtils.formatBytes(connection.download)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (meta.process.isNotEmpty()) {
                    Text(
                        text = meta.process,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
