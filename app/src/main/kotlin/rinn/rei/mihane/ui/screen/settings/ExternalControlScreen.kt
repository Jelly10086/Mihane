package rinn.rei.mihane.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.uwuaosp.compose.widget.expressive.SettingsActionButton
import org.uwuaosp.compose.widget.preference.CustomPreferenceRow
import org.uwuaosp.compose.widget.preference.PreferenceRow
import org.uwuaosp.compose.widget.scaffold.SettingsCollapsingAppBarScaffold
import rinn.rei.mihane.R
import rinn.rei.mihane.platform.showToast
import rinn.rei.mihane.ui.component.CardItem
import rinn.rei.mihane.ui.component.LocalPreferencePosition
import rinn.rei.mihane.ui.component.RestartRequiredHint
import rinn.rei.mihane.ui.component.groupedCardItems
import rinn.rei.mihane.ui.component.material.FormField
import rinn.rei.mihane.ui.util.horizontalCutoutPadding
import rinn.rei.mihane.ui.util.readableContentPadding
import rinn.rei.mihane.viewmodel.ExternalControlViewModel

@Composable
fun ExternalControlScreen(viewModel: ExternalControlViewModel, onBack: () -> Unit = {}) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val resetDoneMsg = stringResource(R.string.dialog_reset_done)
    val notModifiedStr = stringResource(R.string.common_not_modified)

    var showEditDialog by remember { mutableStateOf(false) }
    val controllerTextState = rememberTextFieldState()

    var showSecretDialog by remember { mutableStateOf(false) }
    val secretTextState = rememberTextFieldState()

    SettingsCollapsingAppBarScaffold(
        title = stringResource(R.string.external_control_title),
        showBackButton = true,
        onNavigateUp = onBack,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().horizontalCutoutPadding(),
            contentPadding = readableContentPadding(innerPadding),
        ) {
            item { RestartRequiredHint() }

            item { Spacer(Modifier.height(6.dp)) }

            groupedCardItems(
                keyPrefix = "external_control",
                outerBottomPadding = 12.dp,
                items =
                    listOf(
                        CardItem("controller") {
                            PreferenceRow(
                                title = stringResource(R.string.external_control_ip),
                                summary = uiState.externalController ?: notModifiedStr,
                                onClick = {
                                    controllerTextState.edit {
                                        replace(0, length, uiState.externalController ?: "")
                                    }
                                    showEditDialog = true
                                },
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                        },
                        CardItem("secret") {
                            PreferenceRow(
                                title = stringResource(R.string.external_control_secret),
                                summary = uiState.secret ?: notModifiedStr,
                                onClick = {
                                    secretTextState.edit {
                                        replace(0, length, uiState.secret ?: "")
                                    }
                                    showSecretDialog = true
                                },
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                        },
                    ),
            )

            item {
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
                            text = stringResource(R.string.external_control_controller_hint),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                        Text(
                            text = stringResource(R.string.external_control_secret_hint),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            confirmButton = {
                FlowRow(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SettingsActionButton(
                        label = stringResource(R.string.common_not_modified),
                        onClick = {
                            viewModel.setExternalController(null)
                            showToast(resetDoneMsg)
                            showEditDialog = false
                        },
                        style = org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Text,
                    )
                    SettingsActionButton(
                        label = stringResource(R.string.common_cancel),
                        onClick = { showEditDialog = false },
                        style = org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Text,
                    )
                    SettingsActionButton(
                        label = stringResource(R.string.common_confirm),
                        style = org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Text,
                        onClick = {
                            val value =
                                controllerTextState.text.toString().trim().takeIf {
                                    it.isNotEmpty()
                                }
                            viewModel.setExternalController(value)
                            showEditDialog = false
                        },
                    )
                }
            },
            title = { Text(stringResource(R.string.network_external_controller)) },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    FormField(
                        state = controllerTextState,
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(R.string.network_input_value),
                    )
                }
            },
        )
    }

    if (showSecretDialog) {
        AlertDialog(
            onDismissRequest = { showSecretDialog = false },
            confirmButton = {
                FlowRow(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SettingsActionButton(
                        label = stringResource(R.string.common_not_modified),
                        onClick = {
                            viewModel.setSecret(null)
                            showToast(resetDoneMsg)
                            showSecretDialog = false
                        },
                        style = org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Text,
                    )
                    SettingsActionButton(
                        label = stringResource(R.string.common_cancel),
                        onClick = { showSecretDialog = false },
                        style = org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Text,
                    )
                    SettingsActionButton(
                        label = stringResource(R.string.common_confirm),
                        style = org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Text,
                        onClick = {
                            val value =
                                secretTextState.text.toString().trim().takeIf { it.isNotEmpty() }
                            viewModel.setSecret(value)
                            showSecretDialog = false
                        },
                    )
                }
            },
            title = { Text(stringResource(R.string.external_control_secret)) },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    FormField(
                        state = secretTextState,
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(R.string.network_input_value),
                    )
                }
            },
        )
    }
}
