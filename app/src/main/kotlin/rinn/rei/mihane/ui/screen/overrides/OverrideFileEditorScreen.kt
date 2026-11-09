package rinn.rei.mihane.ui.screen.overrides

import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.uwuaosp.compose.widget.preference.CustomPreferenceRow
import org.uwuaosp.compose.widget.scaffold.SettingsCollapsingAppBarScaffold
import rinn.rei.mihane.R
import rinn.rei.mihane.domain.model.OverrideFormat
import rinn.rei.mihane.platform.showToast
import rinn.rei.mihane.ui.component.AppIcons
import rinn.rei.mihane.ui.theme.LocalAppDarkMode
import rinn.rei.mihane.ui.theme.StatusColors
import rinn.rei.mihane.ui.util.horizontalCutoutPadding
import rinn.rei.mihane.viewmodel.OverrideProfileViewModel
import top.yukonga.scripta.editor.CodeEditor
import top.yukonga.scripta.editor.EditorColors
import top.yukonga.scripta.editor.EditorLanguage
import top.yukonga.scripta.editor.rememberSaveableCodeEditorController

@Composable
fun OverrideFileEditorScreen(
    overrideId: String,
    viewModel: OverrideProfileViewModel,
    onBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val profile = state.profiles.find { it.id == overrideId }
    if (profile == null) {
        rinn.rei.mihane.ui.component.material.LoadingPage(
            stringResource(R.string.override_edit),
            onBack,
        )
        return
    }

    val controller = rememberSaveableCodeEditorController()
    var documentLoaded by rememberSaveable { mutableStateOf(false) }
    val savedMessage = stringResource(R.string.file_manager_saved)
    var saving by remember { mutableStateOf(false) }
    ClearOverrideErrorOnExit(viewModel)

    LaunchedEffect(overrideId) {
        viewModel.clearError()
        if (!documentLoaded) {
            controller.setDocument(viewModel.readContent(overrideId))
            documentLoaded = true
        }
    }
    LaunchedEffect(state.isLoading) { if (!state.isLoading) saving = false }

    SettingsCollapsingAppBarScaffold(
        title = profile.name,
        showBackButton = true,
        onNavigateUp = onBack,
        actions = {
            val canSave = controller.isModified && !state.isLoading && !saving
            FilledTonalIconButton(
                enabled = canSave,
                onClick = {
                    val version = controller.documentVersion
                    val content = controller.getText(controller.lineEnding)
                    saving = true
                    viewModel.saveContent(overrideId, content) {
                        controller.markSaved(version)
                        saving = false
                        showToast(savedMessage)
                    }
                },
                shapes = androidx.compose.material3.IconButtonDefaults.shapes(),
            ) {
                if (saving || state.isLoading) {
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
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .horizontalCutoutPadding()
                    .padding(top = innerPadding.calculateTopPadding())
        ) {
            if (state.error.isNotEmpty()) {
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
                            text = state.error,
                            color = StatusColors.danger,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            CompositionLocalProvider(LocalOverscrollFactory provides null) {
                CodeEditor(
                    controller = controller,
                    language =
                        if (profile.format == OverrideFormat.Yaml) EditorLanguage.Yaml
                        else EditorLanguage.JavaScript,
                    colors =
                        (if (LocalAppDarkMode.current) EditorColors.Default else EditorColors.Light)
                            .copy(
                                background = MaterialTheme.colorScheme.surface,
                                foreground = MaterialTheme.colorScheme.onSurface,
                                gutterBackground = MaterialTheme.colorScheme.surfaceContainer,
                                gutterForeground = MaterialTheme.colorScheme.onSurfaceVariant,
                                cursor = MaterialTheme.colorScheme.primary,
                                selection = MaterialTheme.colorScheme.primaryContainer,
                                handle = MaterialTheme.colorScheme.primary,
                                symbolBarBackground =
                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                symbolBarForeground = MaterialTheme.colorScheme.onSurface,
                                symbolBarPressed = MaterialTheme.colorScheme.secondaryContainer,
                                currentLine = MaterialTheme.colorScheme.surfaceContainerLow,
                            ),
                    modifier = Modifier.fillMaxWidth().weight(1f),
                )
            }
        }
    }
}
