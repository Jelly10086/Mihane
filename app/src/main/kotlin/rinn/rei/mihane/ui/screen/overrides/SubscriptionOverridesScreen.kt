package rinn.rei.mihane.ui.screen.overrides

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.uwuaosp.compose.widget.preference.CustomPreferenceRow
import org.uwuaosp.compose.widget.preference.PreferenceRow
import org.uwuaosp.compose.widget.scaffold.SettingsCollapsingAppBarScaffold
import rinn.rei.mihane.R
import rinn.rei.mihane.domain.model.OverrideProfile
import rinn.rei.mihane.domain.model.orderedOverrideIds
import rinn.rei.mihane.ui.component.AppIcons
import rinn.rei.mihane.ui.component.HintCard
import rinn.rei.mihane.ui.component.LocalPreferencePosition
import rinn.rei.mihane.ui.component.material.SelectionCheckbox
import rinn.rei.mihane.ui.theme.StatusColors
import rinn.rei.mihane.ui.util.WideContentBox
import rinn.rei.mihane.ui.util.horizontalCutoutPadding
import rinn.rei.mihane.viewmodel.OverrideProfileViewModel
import rinn.rei.mihane.viewmodel.SubscriptionViewModel

@Composable
fun SubscriptionOverridesScreen(
    subscriptionId: String,
    subscriptionViewModel: SubscriptionViewModel,
    overrideViewModel: OverrideProfileViewModel,
    onBack: () -> Unit = {},
) {
    val subscriptionState by subscriptionViewModel.uiState.collectAsStateWithLifecycle()
    val overrideState by overrideViewModel.uiState.collectAsStateWithLifecycle()
    val subscription = subscriptionState.subscriptions.find { it.id == subscriptionId }
    if (subscription == null) {
        LaunchedEffect(subscriptionId) { onBack() }
        return
    }
    LaunchedEffect(subscriptionId) { overrideViewModel.clearError() }
    ClearOverrideErrorOnExit(overrideViewModel)

    val profiles = overrideState.profiles
    val profilesById = remember(profiles) { profiles.associateBy { it.id } }
    val initialOrder =
        remember(profiles, subscription.overrideSortPreference) {
            val rank =
                subscription.overrideSortPreference.withIndex().associate { it.value to it.index }
            profiles
                .sortedWith(
                    compareBy<OverrideProfile> { rank[it.id] ?: Int.MAX_VALUE }.thenBy { it.name }
                )
                .map { it.id }
        }
    val initialSelected =
        remember(profiles, subscription.orderedOverrideIds) {
            subscription.orderedOverrideIds.filter { it in profilesById }
        }
    var order by rememberSaveable(subscriptionId) { mutableStateOf(initialOrder) }
    var selected by rememberSaveable(subscriptionId) { mutableStateOf(initialSelected) }
    val displayed = order.filter { it in profilesById } + initialOrder.filter { it !in order }
    val selectedIds = displayed.filter { it in selected }
    val hasChanges = displayed != initialOrder || selectedIds != initialSelected

    SettingsCollapsingAppBarScaffold(
        title = stringResource(R.string.subscription_overrides),
        showBackButton = true,
        onNavigateUp = onBack,
        actions = {
            val canSave = hasChanges && !overrideState.isLoading
            FilledTonalIconButton(
                enabled = canSave,
                onClick = {
                    overrideViewModel.setSelection(subscriptionId, selectedIds, displayed, onBack)
                },
                shapes = androidx.compose.material3.IconButtonDefaults.shapes(),
            ) {
                if (overrideState.isLoading) {
                    LoadingIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Icon(
                        imageVector = AppIcons.Ok,
                        contentDescription = stringResource(R.string.common_save),
                        tint =
                            if (canSave) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
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

                if (overrideState.error.isNotEmpty()) {
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
                                Text(text = overrideState.error, color = StatusColors.danger)
                            }
                        }
                    }
                }

                if (displayed.isEmpty()) {
                    item(key = "empty") { HintCard(text = stringResource(R.string.override_empty)) }
                }

                itemsIndexed(displayed, key = { _, id -> id }) { index, id ->
                    val profile = profilesById[id] ?: return@itemsIndexed
                    val checked = id in selected
                    OverrideSelectionItem(
                        profile = profile,
                        checked = checked,
                        canMoveUp = index > 0,
                        canMoveDown = index < displayed.lastIndex,
                        onToggle = { selected = if (checked) selected - id else selected + id },
                        onMoveUp = { order = displayed.swap(index, index - 1) },
                        onMoveDown = { order = displayed.swap(index, index + 1) },
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
private fun OverrideSelectionItem(
    profile: OverrideProfile,
    checked: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onToggle: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    horizontal = org.uwuaosp.compose.framework.theme.SettingsDimension.pagePadding
                )
                .padding(bottom = 12.dp)
    ) {
        PreferenceRow(
            title = profile.name,
            summary = overrideSummary(profile),
            iconContent = {
                SelectionCheckbox(
                    state = if (checked) ToggleableState.On else ToggleableState.Off,
                    onClick = onToggle,
                    modifier = Modifier.padding(end = 12.dp),
                )
            },
            trailingContent = {
                MoveButton(
                    icon = AppIcons.KeyboardArrowUp,
                    contentDescription = stringResource(R.string.override_move_up),
                    enabled = canMoveUp,
                    onClick = onMoveUp,
                )
                Spacer(Modifier.width(8.dp))
                MoveButton(
                    icon = AppIcons.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.override_move_down),
                    enabled = canMoveDown,
                    onClick = onMoveDown,
                )
            },
            onClick = onToggle,
            index = LocalPreferencePosition.current.first,
            itemCount = LocalPreferencePosition.current.second,
        )
    }
}

@Composable
private fun MoveButton(
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

private fun List<String>.swap(from: Int, to: Int): List<String> =
    toMutableList().also {
        it[from] = this[to]
        it[to] = this[from]
    }
