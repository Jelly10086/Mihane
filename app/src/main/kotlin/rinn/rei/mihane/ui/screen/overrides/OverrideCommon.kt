package rinn.rei.mihane.ui.screen.overrides

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import org.uwuaosp.compose.widget.scaffold.SettingsToolbarActionButton
import rinn.rei.mihane.R
import rinn.rei.mihane.domain.model.OverrideFormat
import rinn.rei.mihane.domain.model.OverrideProfile
import rinn.rei.mihane.ui.component.AppIcons
import rinn.rei.mihane.viewmodel.OverrideProfileViewModel

@Composable
internal fun ClearOverrideErrorOnExit(viewModel: OverrideProfileViewModel) {
    DisposableEffect(viewModel) { onDispose { viewModel.clearError() } }
}

@Composable
internal fun OverrideBackButton(onBack: () -> Unit) {
    val layoutDirection = LocalLayoutDirection.current
    SettingsToolbarActionButton(
        onClick = onBack,
        imageVector = AppIcons.Back,
        contentDescription = stringResource(R.string.common_back),
    )
}

@Composable
internal fun overrideSummary(profile: OverrideProfile): String =
    stringResource(
        if (profile.isRemote) R.string.override_source_remote else R.string.override_source_local
    ) +
        " · " +
        stringResource(
            if (profile.format == OverrideFormat.Yaml) {
                R.string.override_format_yaml
            } else {
                R.string.override_format_javascript
            }
        )
