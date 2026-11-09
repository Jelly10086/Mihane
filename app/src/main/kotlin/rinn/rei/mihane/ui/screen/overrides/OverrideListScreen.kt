package rinn.rei.mihane.ui.screen.overrides

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.uwuaosp.compose.widget.expressive.SettingsActionButton
import org.uwuaosp.compose.widget.preference.CustomPreferenceRow
import org.uwuaosp.compose.widget.scaffold.SettingsCollapsingAppBarScaffold
import rinn.rei.mihane.R
import rinn.rei.mihane.domain.model.OverrideProfile
import rinn.rei.mihane.ui.component.AppIcons
import rinn.rei.mihane.ui.component.HintCard
import rinn.rei.mihane.ui.theme.StatusColors
import rinn.rei.mihane.ui.util.WideContentBox
import rinn.rei.mihane.ui.util.horizontalCutoutPadding
import rinn.rei.mihane.util.formatEpochMillisAsLocal
import rinn.rei.mihane.viewmodel.OverrideProfileViewModel

@Composable
fun OverrideListScreen(
    viewModel: OverrideProfileViewModel,
    onBack: () -> Unit = {},
    onAdd: () -> Unit = {},
    onEdit: (String) -> Unit = {},
    onEditFile: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ClearOverrideErrorOnExit(viewModel)

    SettingsCollapsingAppBarScaffold(
        title = stringResource(R.string.settings_overrides),
        showBackButton = true,
        onNavigateUp = onBack,
        actions = {
            if (state.profiles.any { it.isRemote }) {
                FilledTonalIconButton(
                    onClick = viewModel::updateAll,
                    enabled = !state.isLoading,
                    shapes = androidx.compose.material3.IconButtonDefaults.shapes(),
                ) {
                    Icon(
                        imageVector = AppIcons.Refresh,
                        contentDescription = stringResource(R.string.subscription_update_all),
                    )
                }
            }
            FilledTonalIconButton(
                onClick = {
                    viewModel.clearError()
                    onAdd()
                },
                enabled = !state.isLoading,
                shapes = androidx.compose.material3.IconButtonDefaults.shapes(),
            ) {
                Icon(
                    imageVector = AppIcons.Add,
                    contentDescription = stringResource(R.string.override_add),
                )
            }
        },
    ) { innerPadding ->
        WideContentBox { sidePadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().horizontalCutoutPadding(),
                contentPadding =
                    PaddingValues(
                        top = innerPadding.calculateTopPadding(),
                        start = sidePadding,
                        end = sidePadding,
                    ),
            ) {
                item(key = "top_spacer") { Spacer(Modifier.height(12.dp)) }

                if (state.error.isNotEmpty()) {
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
                                Text(text = state.error, color = StatusColors.danger)
                            }
                        }
                    }
                }

                if (state.profiles.isEmpty()) {
                    item(key = "empty") { HintCard(text = stringResource(R.string.override_empty)) }
                }

                items(state.profiles, key = { it.id }) { profile ->
                    OverrideItem(
                        profile = profile,
                        isLoading = state.isLoading,
                        onOpen = {
                            viewModel.clearError()
                            onEditFile(profile.id)
                        },
                        onEdit = {
                            viewModel.clearError()
                            onEdit(profile.id)
                        },
                        onUpdate = { viewModel.update(profile.id) },
                        onDelete = { viewModel.delete(profile.id) },
                        modifier = Modifier.animateItem(),
                    )
                }

                item(key = "bottom_spacer") {
                    Spacer(Modifier.height(24.dp).navigationBarsPadding())
                }
            }
        }
    }
}

@Composable
private fun OverrideItem(
    profile: OverrideProfile,
    isLoading: Boolean,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onUpdate: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    CustomPreferenceRow(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    horizontal = org.uwuaosp.compose.framework.theme.SettingsDimension.pagePadding
                )
                .padding(bottom = 12.dp),
        onClick = { if (!isLoading) onOpen() },
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
                text = profile.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = overrideSummary(profile),
                modifier = Modifier.padding(top = 2.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    if (profile.sourceLocation.isNotBlank()) {
                        Text(
                            text = profile.sourceLocation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    profile.lastUpdatedAt?.let {
                        Text(
                            text =
                                stringResource(
                                    R.string.subscription_updated_at,
                                    formatEpochMillisAsLocal(it),
                                ),
                            modifier = Modifier.padding(top = 2.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                ItemAction(
                    icon = AppIcons.MoreCircle,
                    contentDescription = stringResource(R.string.common_edit),
                    enabled = !isLoading,
                    onClick = onEdit,
                )
                if (profile.isRemote) {
                    Spacer(Modifier.width(8.dp))
                    ItemAction(
                        icon = AppIcons.Refresh,
                        contentDescription = stringResource(R.string.common_update),
                        enabled = !isLoading,
                        onClick = onUpdate,
                    )
                }
                Spacer(Modifier.width(8.dp))
                ItemAction(
                    icon = AppIcons.Delete,
                    contentDescription = stringResource(R.string.common_delete),
                    enabled = !isLoading,
                    onClick = { showDeleteDialog = true },
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
            title = { Text(stringResource(R.string.override_delete_title)) },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(stringResource(R.string.override_delete_summary, profile.name))
                }
            },
        )
    }
}

@Composable
private fun ItemAction(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    FilledTonalIconButton(
        onClick = onClick,
        enabled = enabled,
        shapes = androidx.compose.material3.IconButtonDefaults.shapes(),
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}
