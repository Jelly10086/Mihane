package rinn.rei.mihane.ui.screen.overrides

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.uwuaosp.compose.widget.expressive.SettingsActionButton
import org.uwuaosp.compose.widget.preference.CustomPreferenceRow
import org.uwuaosp.compose.widget.preference.PreferenceRow
import org.uwuaosp.compose.widget.preference.SettingsSectionHeader
import org.uwuaosp.compose.widget.scaffold.SettingsCollapsingAppBarScaffold
import rinn.rei.mihane.R
import rinn.rei.mihane.domain.model.OverrideFormat
import rinn.rei.mihane.platform.FilePickResult
import rinn.rei.mihane.ui.component.CardItem
import rinn.rei.mihane.ui.component.LocalPreferencePosition
import rinn.rei.mihane.ui.component.groupedCardItems
import rinn.rei.mihane.ui.component.material.FormField
import rinn.rei.mihane.ui.component.material.MenuPreference
import rinn.rei.mihane.ui.theme.StatusColors
import rinn.rei.mihane.ui.util.WideContentBox
import rinn.rei.mihane.ui.util.horizontalCutoutPadding
import rinn.rei.mihane.viewmodel.OverrideProfileViewModel

private enum class AddMethod {
    Remote,
    Blank,
    Local,
}

@Composable
fun OverrideEditScreen(
    overrideId: String?,
    viewModel: OverrideProfileViewModel,
    onBack: () -> Unit = {},
    onSaved: () -> Unit = {},
    onPickFile: ((FilePickResult?) -> Unit) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val profile = overrideId?.let { id -> state.profiles.find { it.id == id } }
    if (overrideId != null && profile == null) {
        rinn.rei.mihane.ui.component.material.LoadingPage(
            stringResource(R.string.override_edit),
            onBack,
        )
        return
    }

    ClearOverrideErrorOnExit(viewModel)
    var method by rememberSaveable { mutableStateOf(AddMethod.Remote) }
    var name by rememberSaveable(overrideId) { mutableStateOf(profile?.name.orEmpty()) }
    var url by
        rememberSaveable(overrideId) {
            mutableStateOf(profile?.takeIf { it.isRemote }?.sourceLocation.orEmpty())
        }
    var format by
        rememberSaveable(overrideId) { mutableStateOf(profile?.format ?: OverrideFormat.Yaml) }
    var picked by remember { mutableStateOf<FilePickResult?>(null) }
    val isRemote = profile?.isRemote ?: (method == AddMethod.Remote)
    val hasChanges =
        profile == null ||
            name != profile.name ||
            format != profile.format ||
            (profile.isRemote && url != profile.sourceLocation)
    val canSave =
        hasChanges &&
            !state.isLoading &&
            name.isNotBlank() &&
            (profile != null || method != AddMethod.Local || picked != null)

    SettingsCollapsingAppBarScaffold(
        title =
            stringResource(if (profile == null) R.string.override_add else R.string.override_edit),
        showBackButton = true,
        onNavigateUp = onBack,
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
                if (profile == null) {
                    item(key = "top_spacer") { Spacer(Modifier.height(12.dp)) }
                    groupedCardItems(
                        keyPrefix = "override_add_method",
                        outerBottomPadding = 6.dp,
                        items =
                            listOf(
                                CardItem("method") {
                                    MenuPreference(
                                        title = stringResource(R.string.override_add_method),
                                        items = AddMethod.entries.map { stringResource(it.label) },
                                        selectedIndex = method.ordinal,
                                        onSelectedIndexChange = { method = AddMethod.entries[it] },
                                    )
                                }
                            ),
                    )
                }

                item(key = "name") {
                    SettingsSectionHeader(title = stringResource(R.string.override_name))
                    FormField(
                        value = name,
                        onValueChange = { name = it },
                        label = stringResource(R.string.override_name_placeholder),
                        useLabelAsPlaceholder = true,
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(
                                    horizontal =
                                        org.uwuaosp.compose.framework.theme.SettingsDimension
                                            .pagePadding
                                )
                                .padding(bottom = 6.dp),
                    )
                }

                if (isRemote) {
                    item(key = "url") {
                        SettingsSectionHeader(title = stringResource(R.string.override_url))
                        FormField(
                            value = url,
                            onValueChange = { url = it },
                            label = stringResource(R.string.override_url_placeholder),
                            useLabelAsPlaceholder = true,
                            modifier =
                                Modifier.fillMaxWidth()
                                    .padding(
                                        horizontal =
                                            org.uwuaosp.compose.framework.theme.SettingsDimension
                                                .pagePadding
                                    )
                                    .padding(bottom = 6.dp),
                        )
                    }
                }

                if (profile == null && method == AddMethod.Local) {
                    groupedCardItems(
                        keyPrefix = "override_local_file",
                        outerTopPadding = 6.dp,
                        outerBottomPadding = 6.dp,
                        items =
                            listOf(
                                CardItem("file") {
                                    PreferenceRow(
                                        title = stringResource(R.string.override_local_file),
                                        summary =
                                            picked?.fileName
                                                ?: stringResource(
                                                    R.string.override_error_file_required
                                                ),
                                        onClick = {
                                            onPickFile { result ->
                                                if (result != null) {
                                                    picked = result
                                                    if (name.isBlank())
                                                        name =
                                                            result.fileName.substringBeforeLast('.')
                                                    format =
                                                        if (
                                                            result.fileName.endsWith(
                                                                ".js",
                                                                ignoreCase = true,
                                                            )
                                                        ) {
                                                            OverrideFormat.JavaScript
                                                        } else {
                                                            OverrideFormat.Yaml
                                                        }
                                                }
                                            }
                                        },
                                        index = LocalPreferencePosition.current.first,
                                        itemCount = LocalPreferencePosition.current.second,
                                    )
                                }
                            ),
                    )
                }

                groupedCardItems(
                    keyPrefix = "override_format",
                    outerTopPadding = 6.dp,
                    outerBottomPadding = 12.dp,
                    items =
                        listOf(
                            CardItem("format") {
                                MenuPreference(
                                    title = stringResource(R.string.override_format),
                                    items = OverrideFormat.entries.map { it.label },
                                    selectedIndex = format.ordinal,
                                    onSelectedIndexChange = { format = OverrideFormat.entries[it] },
                                )
                            }
                        ),
                )

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

                item(key = "save") {
                    SettingsActionButton(
                        label = stringResource(R.string.common_save),
                        enabled = canSave,
                        style = org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Filled,
                        onClick = {
                            when {
                                profile != null ->
                                    viewModel.edit(profile, name, url, format, onSaved)
                                method == AddMethod.Remote ->
                                    viewModel.addRemote(name, url, format, onSaved)
                                method == AddMethod.Local ->
                                    picked?.let {
                                        viewModel.addLocal(
                                            name,
                                            it.fileName,
                                            format,
                                            it.content,
                                            onSaved,
                                        )
                                    }

                                else -> viewModel.addBlank(name, format, onSaved)
                            }
                        },
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(
                                    horizontal =
                                        org.uwuaosp.compose.framework.theme.SettingsDimension
                                            .pagePadding
                                )
                                .padding(bottom = 12.dp),
                    )
                }

                item(key = "bottom_spacer") {
                    Spacer(Modifier.height(24.dp).navigationBarsPadding())
                }
            }
        }
    }
}

private val AddMethod.label: Int
    get() =
        when (this) {
            AddMethod.Remote -> R.string.override_method_remote
            AddMethod.Blank -> R.string.override_method_blank
            AddMethod.Local -> R.string.override_method_local
        }

private val OverrideFormat.label: String
    @Composable
    get() =
        when (this) {
            OverrideFormat.Yaml -> stringResource(R.string.override_format_yaml)
            OverrideFormat.JavaScript -> stringResource(R.string.override_format_javascript)
        }
