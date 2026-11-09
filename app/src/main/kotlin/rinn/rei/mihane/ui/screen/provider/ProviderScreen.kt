package rinn.rei.mihane.ui.screen.provider

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.uwuaosp.compose.widget.preference.CustomPreferenceRow
import org.uwuaosp.compose.widget.preference.PreferenceRow
import org.uwuaosp.compose.widget.scaffold.SettingsCollapsingAppBarScaffold
import org.uwuaosp.compose.widget.scaffold.SettingsToolbarActionButton
import rinn.rei.mihane.R
import rinn.rei.mihane.ui.component.AppIcons
import rinn.rei.mihane.ui.component.LocalPreferencePosition
import rinn.rei.mihane.ui.component.material.PageTabs
import rinn.rei.mihane.ui.theme.StatusColors
import rinn.rei.mihane.ui.util.horizontalCutoutPadding
import rinn.rei.mihane.ui.util.readableContentPadding
import rinn.rei.mihane.util.formatIsoTimeAsLocalShort
import rinn.rei.mihane.viewmodel.ProviderErrorKey
import rinn.rei.mihane.viewmodel.ProviderItemUi
import rinn.rei.mihane.viewmodel.ProviderViewModel

@Composable
fun ProviderScreen(viewModel: ProviderViewModel, onBack: () -> Unit = {}) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIndex by
        rememberSaveable("provider_type_tab") { mutableIntStateOf(RULE_PROVIDERS_TAB) }
    val selectedIsRuleProvider = selectedTabIndex == RULE_PROVIDERS_TAB
    val visibleProviders = uiState.providers.filter { it.isRuleProvider == selectedIsRuleProvider }
    // 非当前 Tab 的更新错误没有行内 banner 可见，聚合到列表顶部的汇总卡展示
    val otherTabErrors =
        uiState.providerErrors.entries
            .filter { it.key.isRuleProvider != selectedIsRuleProvider }
            .sortedBy { it.key.name }
    val providerTabs =
        listOf(
            stringResource(R.string.provider_rule_providers),
            stringResource(R.string.provider_proxy_providers),
        )
    val providerTabsContent: @Composable () -> Unit = {
        PageTabs(
            tabs = providerTabs,
            selectedTabIndex = selectedTabIndex,
            onTabSelected = { selectedTabIndex = it },
            modifier =
                Modifier.fillMaxWidth()
                    .padding(
                        horizontal =
                            org.uwuaosp.compose.framework.theme.SettingsDimension.pagePadding
                    )
                    .padding(bottom = 12.dp),
        )
    }
    val otherTabErrorsContent: @Composable () -> Unit = {
        CustomPreferenceRow(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(
                        horizontal =
                            org.uwuaosp.compose.framework.theme.SettingsDimension.pagePadding
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
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    otherTabErrors.forEach { (errorKey, message) ->
                        Text(
                            text =
                                stringResource(
                                    R.string.provider_update_failed,
                                    errorKey.name,
                                    message,
                                ),
                            style = MaterialTheme.typography.bodySmall,
                            color = StatusColors.danger,
                        )
                    }
                }
            }
        }
    }

    SettingsCollapsingAppBarScaffold(
        title = stringResource(R.string.provider_title),
        showBackButton = true,
        onNavigateUp = onBack,
        actions = {
            SettingsToolbarActionButton(
                onClick = { viewModel.updateAll() },
                imageVector = AppIcons.Refresh,
                contentDescription = stringResource(R.string.subscription_update_all),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().horizontalCutoutPadding(),
            contentPadding = readableContentPadding(innerPadding),
        ) {
            if (visibleProviders.isEmpty() && !uiState.isLoading) {
                item(key = "empty", contentType = "empty") {
                    Column(modifier = Modifier.fillParentMaxSize()) {
                        Spacer(Modifier.height(12.dp))
                        providerTabsContent()
                        if (otherTabErrors.isNotEmpty()) {
                            otherTabErrorsContent()
                        }
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stringResource(R.string.provider_no_providers),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                // 仅当两个 Tab 都没有 provider 时才提示启动服务；
                                // 当前 Tab 为空但另一 Tab 有 provider 说明服务已在运行
                                if (uiState.providers.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.provider_start_first),
                                        modifier = Modifier.padding(top = 6.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                item(key = "top_spacer", contentType = "spacer") { Spacer(Modifier.height(12.dp)) }
                item(key = "provider_type_tabs", contentType = "tabs") { providerTabsContent() }
                if (otherTabErrors.isNotEmpty()) {
                    item(key = "other_tab_errors", contentType = "error_summary") {
                        otherTabErrorsContent()
                    }
                }
                items(
                    items = visibleProviders,
                    key = { "${it.isRuleProvider}:${it.name}" },
                    contentType = { "provider" },
                ) { provider ->
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
                            ProviderItem(
                                provider = provider,
                                error =
                                    uiState.providerErrors[
                                            ProviderErrorKey(
                                                provider.name,
                                                provider.isRuleProvider,
                                            )],
                                onUpdate = {
                                    viewModel.updateProvider(provider.name, provider.isRuleProvider)
                                },
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
        }
    }

    ProviderRefreshDialog(show = uiState.refresh != null, progress = uiState.refresh)
}

private const val RULE_PROVIDERS_TAB = 0

@Composable
private fun ProviderItem(provider: ProviderItemUi, error: String?, onUpdate: () -> Unit) {
    Column {
        PreferenceRow(
            title = provider.name,
            summary =
                stringResource(
                    if (provider.isRuleProvider) R.string.provider_kind_rule
                    else R.string.provider_kind_proxy,
                    provider.type,
                ),
            trailingContent = {
                if (provider.vehicleType != "Inline") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = formatIsoTimeAsLocalShort(provider.updatedAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Image(
                            modifier =
                                Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = LocalIndication.current,
                                    role = Role.Button,
                                    onClick = onUpdate,
                                ),
                            imageVector = AppIcons.Refresh,
                            contentDescription = stringResource(R.string.provider_update),
                            colorFilter =
                                ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                        )
                    }
                }
            },
            index = LocalPreferencePosition.current.first,
            itemCount = LocalPreferencePosition.current.second,
        )
        // 退出动画期间 error 已置 null，保留最后一条非空错误供动画帧渲染，
        // 避免闪现空原因文案并触发 liveRegion 播报
        var lastError by remember { mutableStateOf(error) }
        if (error != null) lastError = error
        AnimatedVisibility(
            visible = error != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Text(
                text =
                    stringResource(
                        R.string.provider_update_failed,
                        provider.name,
                        lastError.orEmpty(),
                    ),
                modifier =
                    Modifier.fillMaxWidth()
                        .semantics { liveRegion = LiveRegionMode.Polite }
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = StatusColors.danger,
            )
        }
    }
}
