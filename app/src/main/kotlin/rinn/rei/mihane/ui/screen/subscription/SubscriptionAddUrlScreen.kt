package rinn.rei.mihane.ui.screen.subscription

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.uwuaosp.compose.widget.expressive.SettingsActionButton
import org.uwuaosp.compose.widget.preference.CustomPreferenceRow
import org.uwuaosp.compose.widget.preference.SettingsSectionHeader
import org.uwuaosp.compose.widget.scaffold.SettingsCollapsingAppBarScaffold
import rinn.rei.mihane.R
import rinn.rei.mihane.ui.component.AppIcons
import rinn.rei.mihane.ui.component.material.FormField
import rinn.rei.mihane.ui.util.horizontalCutoutPadding
import rinn.rei.mihane.ui.util.readableContentPadding
import rinn.rei.mihane.viewmodel.SubscriptionViewModel

/** URL 导入配置页面 */
@Composable
fun SubscriptionAddUrlScreen(
    viewModel: SubscriptionViewModel,
    initialUrl: String = "",
    initialName: String = "",
    initialIntervalMinutes: Long = 0,
    onBack: () -> Unit = {},
    onSaved: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var inputName by rememberSaveable { mutableStateOf(initialName) }
    var inputUrl by rememberSaveable { mutableStateOf(initialUrl) }
    var userAgent by rememberSaveable { mutableStateOf("") }
    var ageSecretKey by rememberSaveable { mutableStateOf("") }
    var intervalMinutes by rememberSaveable {
        mutableStateOf(if (initialIntervalMinutes > 0) initialIntervalMinutes.toString() else "")
    }

    SettingsCollapsingAppBarScaffold(
        title = stringResource(R.string.subscription_config),
        showBackButton = true,
        onNavigateUp = onBack,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().horizontalCutoutPadding(),
            contentPadding = readableContentPadding(innerPadding),
        ) {
            item(key = "hint") {
                CustomPreferenceRow(
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(
                                horizontal =
                                    org.uwuaosp.compose.framework.theme.SettingsDimension
                                        .pagePadding
                            )
                            .padding(top = 12.dp, bottom = 6.dp)
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = AppIcons.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = stringResource(R.string.subscription_url_hint),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            item(key = "name_title") {
                SettingsSectionHeader(title = stringResource(R.string.subscription_name))
            }
            item(key = "name_field") {
                FormField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(
                                horizontal =
                                    org.uwuaosp.compose.framework.theme.SettingsDimension
                                        .pagePadding
                            )
                            .padding(bottom = 6.dp),
                    label = stringResource(R.string.subscription_name_auto_placeholder),
                    useLabelAsPlaceholder = true,
                )
            }
            item(key = "url_title") {
                SettingsSectionHeader(title = stringResource(R.string.subscription_url_label))
            }
            item(key = "url_field") {
                FormField(
                    value = inputUrl,
                    onValueChange = { inputUrl = it },
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(
                                horizontal =
                                    org.uwuaosp.compose.framework.theme.SettingsDimension
                                        .pagePadding
                            )
                            .padding(bottom = 6.dp),
                    label = stringResource(R.string.subscription_url_placeholder),
                    useLabelAsPlaceholder = true,
                )
            }
            item(key = "user_agent_title") {
                SettingsSectionHeader(title = stringResource(R.string.subscription_user_agent))
            }
            item(key = "user_agent_field") {
                FormField(
                    value = userAgent,
                    onValueChange = { userAgent = it },
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(
                                horizontal =
                                    org.uwuaosp.compose.framework.theme.SettingsDimension
                                        .pagePadding
                            )
                            .padding(bottom = 6.dp),
                    label = stringResource(R.string.subscription_user_agent_placeholder),
                    useLabelAsPlaceholder = true,
                )
            }
            item(key = "age_secret_key_title") {
                SettingsSectionHeader(title = stringResource(R.string.subscription_age_secret_key))
            }
            item(key = "age_secret_key_field") {
                FormField(
                    value = ageSecretKey,
                    onValueChange = { ageSecretKey = it },
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(
                                horizontal =
                                    org.uwuaosp.compose.framework.theme.SettingsDimension
                                        .pagePadding
                            )
                            .padding(bottom = 6.dp),
                    label = stringResource(R.string.subscription_age_secret_key_placeholder),
                    useLabelAsPlaceholder = true,
                )
            }
            item(key = "interval_field") {
                SettingsSectionHeader(title = stringResource(R.string.subscription_auto_update))
                FormField(
                    value = intervalMinutes,
                    onValueChange = { intervalMinutes = it.filter { c -> c.isDigit() } },
                    label = stringResource(R.string.subscription_auto_update_placeholder),
                    useLabelAsPlaceholder = true,
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
            item(key = "save") {
                SettingsActionButton(
                    label = stringResource(R.string.common_save),
                    onClick = {
                        val intervalMs = (intervalMinutes.toLongOrNull() ?: 0) * 60000
                        viewModel.addSubscription(
                            name = inputName.trim(),
                            url = inputUrl,
                            interval = intervalMs,
                            userAgent = userAgent.trim(),
                            ageSecretKey = ageSecretKey.trim(),
                            onComplete = onSaved,
                        )
                    },
                    enabled = inputUrl.isNotBlank() && !uiState.isLoading,
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(
                                horizontal =
                                    org.uwuaosp.compose.framework.theme.SettingsDimension
                                        .pagePadding
                            )
                            .padding(bottom = 12.dp),
                    style = org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Filled,
                )
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
                                text = uiState.error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
            item(key = "bottom_spacer") { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
        }
    }

    ImportProgressDialog(
        show = uiState.isLoading,
        step =
            uiState.importProgress?.let { importStepLabel(it) }
                ?: stringResource(R.string.common_processing),
        onCancel = { viewModel.cancelCurrentUpdate() },
    )
}
