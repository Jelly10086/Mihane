package rinn.rei.mihane.ui.screen.subscription

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.uwuaosp.compose.widget.expressive.SettingsActionButton
import org.uwuaosp.compose.widget.preference.CustomPreferenceRow
import org.uwuaosp.compose.widget.scaffold.SettingsCollapsingAppBarScaffold
import org.uwuaosp.compose.widget.scaffold.SettingsToolbarActionButton
import rinn.rei.mihane.R
import rinn.rei.mihane.domain.model.Subscription
import rinn.rei.mihane.ui.component.AppIcons
import rinn.rei.mihane.ui.component.material.shapeBackground
import rinn.rei.mihane.ui.screen.home.SubscriptionQuotaBar
import rinn.rei.mihane.ui.theme.StatusColors
import rinn.rei.mihane.ui.util.WideContentBox
import rinn.rei.mihane.util.FormatUtils
import rinn.rei.mihane.util.formatEpochMillisAsLocal
import rinn.rei.mihane.viewmodel.ProfileOperation
import rinn.rei.mihane.viewmodel.SubscriptionViewModel

/** 订阅列表页面 */
@Composable
fun SubscriptionScreen(
    viewModel: SubscriptionViewModel,
    bottomPadding: Dp = 0.dp,
    onNavigateAdd: () -> Unit = {},
    onNavigateEdit: (uuid: String) -> Unit = {},
    onActiveChanged: (() -> Unit)? = null,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsCollapsingAppBarScaffold(
        title = stringResource(R.string.subscription_title),
        actions = {
            if (uiState.subscriptions.any { it.url.isNotBlank() }) {
                FilledTonalIconButton(
                    onClick = { viewModel.updateAllSubscriptions() },
                    enabled = !uiState.isLoading,
                    shapes = androidx.compose.material3.IconButtonDefaults.shapes(),
                ) {
                    Icon(
                        imageVector = AppIcons.Refresh,
                        contentDescription = stringResource(R.string.subscription_update_all),
                    )
                }
            }
            SettingsToolbarActionButton(
                onClick = {
                    viewModel.clearError()
                    onNavigateAdd()
                },
                imageVector = AppIcons.Add,
                contentDescription = stringResource(R.string.subscription_add),
            )
        },
    ) { innerPadding ->
        WideContentBox { sidePadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding =
                    PaddingValues(
                        top = innerPadding.calculateTopPadding(),
                        bottom = bottomPadding,
                        start = sidePadding,
                        end = sidePadding,
                    ),
            ) {
                if (uiState.subscriptions.isNotEmpty()) {
                    item(key = "top_padding") { Spacer(modifier = Modifier.height(12.dp)) }
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
                                Text(text = uiState.error, color = StatusColors.danger)
                            }
                        }
                    }
                }

                if (uiState.subscriptions.isEmpty()) {
                    item(key = "empty") {
                        Column(
                            modifier = Modifier.fillParentMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = stringResource(R.string.subscription_no_config),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = stringResource(R.string.subscription_tap_add),
                                modifier = Modifier.padding(top = 6.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                items(uiState.subscriptions, key = { it.id }) { sub ->
                    SubscriptionItem(
                        subscription = sub,
                        isLoading = uiState.isLoading,
                        onSelect = {
                            viewModel.setActive(sub.id)
                            onActiveChanged?.invoke()
                        },
                        onRefresh = { viewModel.fetchSubscription(sub.id) },
                        onDelete = {
                            viewModel.removeSubscription(sub.id) { onActiveChanged?.invoke() }
                        },
                        onEdit = { onNavigateEdit(sub.id) },
                    )
                }
            }
        }
    }

    // 批量更新优先展示（含 currentName + (done/total) + step）；
    // 单条走 importProgress，标题由 operation 区分（列表页触发的是 Update）。
    val updateAll = uiState.updateAll
    if (updateAll != null) {
        val progressText =
            stringResource(
                R.string.subscription_updating_progress,
                updateAll.currentName,
                updateAll.completed + 1,
                updateAll.total,
            )
        val step =
            updateAll.currentStep?.let { "$progressText\n${importStepLabel(it)}" } ?: progressText
        ImportProgressDialog(
            show = true,
            step = step,
            title = stringResource(R.string.subscription_updating_title),
            onCancel = { viewModel.cancelCurrentUpdate() },
        )
    } else {
        val title =
            when (uiState.operation) {
                ProfileOperation.Update -> stringResource(R.string.subscription_update_config)
                else -> stringResource(R.string.subscription_import_config)
            }
        ImportProgressDialog(
            show = uiState.importProgress != null,
            step =
                uiState.importProgress?.let { importStepLabel(it) }
                    ?: stringResource(R.string.common_processing),
            title = title,
            onCancel = { viewModel.cancelCurrentUpdate() },
        )
    }
}

@Composable
private fun SubscriptionItem(
    subscription: Subscription,
    isLoading: Boolean,
    onSelect: () -> Unit,
    onRefresh: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val displayName = subscription.name.ifBlank { stringResource(R.string.subscription_config) }

    CustomPreferenceRow(
        modifier =
            Modifier.fillMaxWidth()
                .padding(
                    horizontal = org.uwuaosp.compose.framework.theme.SettingsDimension.pagePadding
                )
                .padding(bottom = 12.dp),
        onClick = onSelect,
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
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text =
                        subscription.name.ifBlank { stringResource(R.string.subscription_config) },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (subscription.isActive) {
                    val activeColor = StatusColors.healthy
                    Text(
                        text = stringResource(R.string.subscription_in_use),
                        style = MaterialTheme.typography.bodySmall,
                        color = activeColor,
                        modifier =
                            Modifier.shapeBackground(activeColor.copy(alpha = 0.15f), 6.dp)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            if (subscription.total > 0) {
                val used =
                    subscription.upload.coerceAtLeast(0) + subscription.download.coerceAtLeast(0)
                val remaining = (subscription.total - used).coerceAtLeast(0)
                val progress = (used.toDouble() / subscription.total).toFloat().coerceIn(0f, 1f)
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        stringResource(
                            R.string.subscription_used_traffic,
                            FormatUtils.formatBytes(used),
                            FormatUtils.formatBytes(subscription.total),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "${stringResource(R.string.home_remaining)} ${FormatUtils.formatBytes(remaining)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                SubscriptionQuotaBar(
                    Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    progress,
                    StatusColors.usage(progress),
                )
            } else {
                Text(
                    stringResource(R.string.subscription_no_traffic),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (subscription.updatedAt > 0) {
                Text(
                    stringResource(
                        R.string.subscription_updated_at,
                        formatEpochMillisAsLocal(subscription.updatedAt),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SettingsToolbarActionButton(
                    onClick = onEdit,
                    imageVector = AppIcons.MoreCircle,
                    contentDescription = stringResource(R.string.common_edit),
                )
                Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                    FilledTonalIconButton(
                        onClick = onRefresh,
                        enabled = !isLoading && subscription.url.isNotBlank(),
                        modifier = Modifier.size(40.dp),
                        shapes = androidx.compose.material3.IconButtonDefaults.shapes(),
                        colors =
                            androidx.compose.material3.IconButtonDefaults
                                .filledTonalIconButtonColors(
                                    containerColor =
                                        MaterialTheme.colorScheme.surfaceContainerHighest,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                    ) {
                        Icon(
                            AppIcons.Refresh,
                            stringResource(R.string.common_update),
                            Modifier.size(24.dp),
                        )
                    }
                }
                SettingsToolbarActionButton(
                    onClick = { showDeleteDialog = true },
                    imageVector = AppIcons.Delete,
                    contentDescription = stringResource(R.string.common_delete),
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                FlowRow(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SettingsActionButton(
                        label = stringResource(R.string.common_cancel),
                        onClick = { showDeleteDialog = false },
                        style = org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Text,
                    )
                    SettingsActionButton(
                        label = stringResource(R.string.common_confirm),
                        style = org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Text,
                        onClick = {
                            showDeleteDialog = false
                            onDelete()
                        },
                    )
                }
            },
            title = { Text(stringResource(R.string.subscription_delete_title)) },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(stringResource(R.string.subscription_delete_summary, displayName))
                }
            },
        )
    }
}
