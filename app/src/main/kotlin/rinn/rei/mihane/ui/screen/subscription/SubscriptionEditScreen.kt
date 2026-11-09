package rinn.rei.mihane.ui.screen.subscription

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.uwuaosp.compose.widget.expressive.SettingsActionButton
import org.uwuaosp.compose.widget.preference.CustomPreferenceRow
import org.uwuaosp.compose.widget.preference.PreferenceRow
import org.uwuaosp.compose.widget.preference.SettingsSectionHeader
import org.uwuaosp.compose.widget.scaffold.SettingsCollapsingAppBarScaffold
import rinn.rei.mihane.R
import rinn.rei.mihane.domain.model.ProfileType
import rinn.rei.mihane.ui.component.CardItem
import rinn.rei.mihane.ui.component.LocalPreferencePosition
import rinn.rei.mihane.ui.component.groupedCardItems
import rinn.rei.mihane.ui.component.material.FormField
import rinn.rei.mihane.ui.util.horizontalCutoutPadding
import rinn.rei.mihane.ui.util.readableContentPadding
import rinn.rei.mihane.viewmodel.SubscriptionViewModel

/** 订阅编辑页面 —— 编辑名称、URL、自动更新间隔 */
@Composable
fun SubscriptionEditScreen(
    uuid: String,
    viewModel: SubscriptionViewModel,
    onBack: () -> Unit = {},
    onSaved: () -> Unit = {},
    onNavigateOverrides: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val subscription = uiState.subscriptions.find { it.id == uuid }
    if (subscription == null) {
        rinn.rei.mihane.ui.component.material.LoadingPage(
            stringResource(R.string.subscription_edit),
            onBack,
        )
        return
    }

    var name by rememberSaveable(uuid) { mutableStateOf(subscription.name) }
    var url by rememberSaveable(uuid) { mutableStateOf(subscription.url) }
    var userAgent by rememberSaveable(uuid) { mutableStateOf(subscription.userAgent) }
    var ageSecretKey by rememberSaveable(uuid) { mutableStateOf(subscription.ageSecretKey) }
    var intervalMinutes by
        rememberSaveable(uuid) {
            mutableStateOf(
                if (subscription.interval > 0) (subscription.interval / 60000).toString() else ""
            )
        }

    val isFile = subscription.type == ProfileType.File
    val hasChanges =
        name != subscription.name ||
            url != subscription.url ||
            userAgent.trim() != subscription.userAgent ||
            ageSecretKey.trim() != subscription.ageSecretKey ||
            intervalMinutes !=
                (if (subscription.interval > 0) (subscription.interval / 60000).toString() else "")

    SettingsCollapsingAppBarScaffold(
        title = stringResource(R.string.subscription_edit),
        showBackButton = true,
        onNavigateUp = onBack,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().horizontalCutoutPadding(),
            contentPadding = readableContentPadding(innerPadding),
        ) {
            item {
                SettingsSectionHeader(title = stringResource(R.string.subscription_config_name))
                FormField(
                    value = name,
                    onValueChange = { name = it },
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(
                                horizontal =
                                    org.uwuaosp.compose.framework.theme.SettingsDimension
                                        .pagePadding
                            )
                            .padding(bottom = 6.dp),
                )
                if (!isFile) {
                    SettingsSectionHeader(title = stringResource(R.string.subscription_sub_url))
                    FormField(
                        value = url,
                        onValueChange = { url = it },
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(
                                    horizontal =
                                        org.uwuaosp.compose.framework.theme.SettingsDimension
                                            .pagePadding
                                )
                                .padding(bottom = 6.dp),
                    )
                    SettingsSectionHeader(title = stringResource(R.string.subscription_user_agent))
                    FormField(
                        value = userAgent,
                        onValueChange = { userAgent = it },
                        label = stringResource(R.string.subscription_user_agent_placeholder),
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
                    SettingsSectionHeader(
                        title = stringResource(R.string.subscription_age_secret_key)
                    )
                    FormField(
                        value = ageSecretKey,
                        onValueChange = { ageSecretKey = it },
                        label = stringResource(R.string.subscription_age_secret_key_placeholder),
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
                                .padding(bottom = 6.dp),
                    )
                }
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
                                .padding(bottom = 6.dp)
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

            groupedCardItems(
                keyPrefix = "subscription_overrides",
                outerTopPadding = 6.dp,
                outerBottomPadding = 12.dp,
                items =
                    listOf(
                        CardItem("open") {
                            PreferenceRow(
                                title = stringResource(R.string.subscription_overrides),
                                summary =
                                    if (subscription.overrideIds.isEmpty()) {
                                        stringResource(R.string.subscription_override_none)
                                    } else {
                                        pluralStringResource(
                                            R.plurals.subscription_override_count,
                                            subscription.overrideIds.size,
                                            subscription.overrideIds.size,
                                        )
                                    },
                                onClick = onNavigateOverrides,
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                        }
                    ),
            )

            item {
                SettingsActionButton(
                    label = stringResource(R.string.common_save),
                    onClick = {
                        val intervalMs = (intervalMinutes.toLongOrNull() ?: 0) * 60000
                        viewModel.editSubscription(
                            uuid = uuid,
                            name = name,
                            source = url,
                            interval = intervalMs,
                            userAgent = userAgent.trim(),
                            ageSecretKey = ageSecretKey.trim(),
                            onComplete = onSaved,
                        )
                    },
                    enabled = hasChanges && !uiState.isLoading && name.isNotBlank(),
                    style = org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Filled,
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

            item(key = "bottom_spacer") { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
        }
    }
}
