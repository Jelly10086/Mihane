package rinn.rei.mihane.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.uwuaosp.compose.widget.preference.SettingsPreferenceGroup
import org.uwuaosp.compose.widget.preference.SwitchPreferenceRow
import org.uwuaosp.compose.widget.scaffold.SettingsCollapsingAppBarScaffold
import rinn.rei.mihane.R
import rinn.rei.mihane.platform.PlatformStorage
import rinn.rei.mihane.ui.component.LocalPreferencePosition
import rinn.rei.mihane.ui.component.material.*
import rinn.rei.mihane.ui.theme.*

@Composable
fun ThemeSettingsScreen(
    storage: PlatformStorage,
    themeConfig: ThemeConfig,
    onThemeConfigChange: (ThemeConfig) -> Unit,
    onPredictiveBackChange: ((Boolean) -> Unit)? = null,
    onBack: () -> Unit = {},
) {
    var predictiveBack by remember {
        mutableStateOf(
            storage.getString(rinn.rei.mihane.platform.StorageKeys.PREDICTIVE_BACK, "false") ==
                "true"
        )
    }
    fun update(config: ThemeConfig) {
        writeThemeConfig(storage, config)
        onThemeConfigChange(config)
    }
    SettingsCollapsingAppBarScaffold(
        title = stringResource(R.string.settings_theme_title),
        showBackButton = true,
        onNavigateUp = onBack,
    ) { insets ->
        org.uwuaosp.compose.widget.ui.SettingsContent {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = insets) {
                item {
                    SettingsPreferenceGroup {
                        item {
                            MenuPreference(
                                stringResource(R.string.settings_theme_mode),
                                ThemeColorModes.map { themeColorModeLabel(it) },
                                themeConfig.colorMode,
                                { update(themeConfig.copy(colorMode = it)) },
                            )
                        }
                        item {
                            SwitchPreferenceRow(
                                title = stringResource(R.string.settings_theme_monet),
                                summary = "",
                                showSummary = false,
                                checked = themeConfig.useMonet,
                                onCheckedChange = { update(themeConfig.copy(useMonet = it)) },
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                        }
                        item {
                            MenuPreference(
                                stringResource(R.string.settings_theme_accent_title),
                                ThemeAccentColor.entries.map { it.label() },
                                themeConfig.accentColor.ordinal,
                                {
                                    update(
                                        themeConfig.copy(accentColor = ThemeAccentColor.entries[it])
                                    )
                                },
                                enabled = !themeConfig.useMonet,
                            )
                        }
                        item {
                            SwitchPreferenceRow(
                                title = stringResource(R.string.settings_theme_pure_black),
                                summary = "",
                                showSummary = false,
                                checked = themeConfig.pureBlack,
                                onCheckedChange = { update(themeConfig.copy(pureBlack = it)) },
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                        }
                        item(visible = onPredictiveBackChange != null) {
                            SwitchPreferenceRow(
                                title = stringResource(R.string.settings_predictive_back),
                                checked = predictiveBack,
                                onCheckedChange = { enabled ->
                                    predictiveBack = enabled
                                    storage.putString(
                                        rinn.rei.mihane.platform.StorageKeys.PREDICTIVE_BACK,
                                        enabled.toString(),
                                    )
                                    onPredictiveBackChange?.invoke(enabled)
                                },
                                summary = stringResource(R.string.settings_predictive_back_summary),
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                        }
                    }
                }
                item {
                    Column(Modifier.padding(24.dp)) {
                        Text(
                            stringResource(R.string.settings_theme_density_scale),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        var draft by
                            remember(themeConfig.densityScale) {
                                mutableFloatStateOf(themeConfig.densityScale)
                            }
                        Slider(
                            draft,
                            { draft = it },
                            valueRange = MinDensityScale..MaxDensityScale,
                            onValueChangeFinished = {
                                update(
                                    themeConfig.copy(densityScale = normalizeDensityScale(draft))
                                )
                            },
                        )
                        Text(
                            "${(draft * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}
