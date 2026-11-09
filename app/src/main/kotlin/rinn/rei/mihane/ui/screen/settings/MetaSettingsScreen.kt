package rinn.rei.mihane.ui.screen.settings

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.uwuaosp.compose.widget.expressive.SettingsActionButton
import org.uwuaosp.compose.widget.preference.PreferenceRow
import org.uwuaosp.compose.widget.preference.SettingsSectionHeader
import org.uwuaosp.compose.widget.scaffold.SettingsCollapsingAppBarScaffold
import rinn.rei.mihane.R
import rinn.rei.mihane.data.bridge.AgeKeyPair
import rinn.rei.mihane.data.bridge.MishkaCoreBridge
import rinn.rei.mihane.domain.model.ConfigurationOverride
import rinn.rei.mihane.domain.model.SnifferOverride
import rinn.rei.mihane.platform.showToast
import rinn.rei.mihane.ui.component.CardItem
import rinn.rei.mihane.ui.component.ListEditDialog
import rinn.rei.mihane.ui.component.LocalPreferencePosition
import rinn.rei.mihane.ui.component.RestartRequiredHint
import rinn.rei.mihane.ui.component.TriStatePreference
import rinn.rei.mihane.ui.component.groupedCardItems
import rinn.rei.mihane.ui.component.material.MenuPreference
import rinn.rei.mihane.ui.platform.setPlainText
import rinn.rei.mihane.ui.util.horizontalCutoutPadding
import rinn.rei.mihane.ui.util.readableContentPadding
import rinn.rei.mihane.viewmodel.MetaSettingsViewModel

@Composable
fun MetaSettingsScreen(viewModel: MetaSettingsViewModel, onBack: () -> Unit = {}) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val resetDoneMsg = stringResource(R.string.dialog_reset_done)

    fun updateTop(transform: (ConfigurationOverride) -> ConfigurationOverride) {
        viewModel.update(transform)
    }

    fun updateSniffer(transform: (SnifferOverride) -> SnifferOverride) {
        viewModel.updateSniffer(transform)
    }

    var showAgeDialog by remember { mutableStateOf(false) }
    var ageKeyPair by remember { mutableStateOf<AgeKeyPair?>(null) }
    var showListDialog by remember { mutableStateOf(false) }
    var editingListTitle by remember { mutableStateOf("") }
    var editingListSetter by remember { mutableStateOf<(List<String>?) -> Unit>({}) }
    val listTextState = rememberTextFieldState()

    fun openListDialog(title: String, value: List<String>?, setter: (List<String>?) -> Unit) {
        editingListTitle = title
        editingListSetter = setter
        listTextState.edit { replace(0, length, value?.joinToString("\n") ?: "") }
        showListDialog = true
    }

    val sniffer = uiState.sniffer

    val failedMsg = stringResource(R.string.meta_age_keygen_failed)
    val genKey: (Boolean) -> Unit = { hybrid ->
        val pair = MishkaCoreBridge.generateAgeKeyPair(hybrid)
        if (pair != null) {
            ageKeyPair = pair
            showAgeDialog = true
        } else {
            showToast(failedMsg)
        }
    }

    SettingsCollapsingAppBarScaffold(
        title = stringResource(R.string.meta_settings_title),
        showBackButton = true,
        onNavigateUp = onBack,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().horizontalCutoutPadding(),
            contentPadding = readableContentPadding(innerPadding),
        ) {
            item { RestartRequiredHint() }

            item { SettingsSectionHeader(title = stringResource(R.string.meta_basic)) }
            groupedCardItems(
                keyPrefix = "meta_basic",
                items =
                    listOf(
                        CardItem("unifiedDelay") {
                            TriStatePreference(
                                title = stringResource(R.string.meta_unified_delay),
                                value = uiState.unifiedDelay,
                                onValueChange = { v -> updateTop { it.copy(unifiedDelay = v) } },
                            )
                        },
                        CardItem("geodataMode") {
                            TriStatePreference(
                                title = stringResource(R.string.meta_geodata_mode),
                                value = uiState.geodataMode,
                                onValueChange = { v -> updateTop { it.copy(geodataMode = v) } },
                            )
                        },
                        CardItem("tcpConcurrent") {
                            TriStatePreference(
                                title = stringResource(R.string.meta_tcp_concurrent),
                                value = uiState.tcpConcurrent,
                                onValueChange = { v -> updateTop { it.copy(tcpConcurrent = v) } },
                            )
                        },
                        CardItem("findProcessMode") {
                            FindProcessModePreference(
                                value = uiState.findProcessMode,
                                onValueChange = { v -> updateTop { it.copy(findProcessMode = v) } },
                            )
                        },
                    ),
            )

            item { SettingsSectionHeader(title = stringResource(R.string.meta_sniffer)) }
            groupedCardItems(
                keyPrefix = "meta_sniffer",
                items =
                    listOf(
                        CardItem("enable") {
                            TriStatePreference(
                                title = stringResource(R.string.meta_sniffer_enable),
                                value = sniffer?.enable,
                                onValueChange = { v -> updateSniffer { it.copy(enable = v) } },
                            )
                        },
                        CardItem("forceDnsMapping") {
                            TriStatePreference(
                                title = stringResource(R.string.meta_sniffer_force_dns_mapping),
                                value = sniffer?.forceDnsMapping,
                                onValueChange = { v ->
                                    updateSniffer { it.copy(forceDnsMapping = v) }
                                },
                            )
                        },
                        CardItem("parsePureIp") {
                            TriStatePreference(
                                title = stringResource(R.string.meta_sniffer_parse_pure_ip),
                                value = sniffer?.parsePureIp,
                                onValueChange = { v -> updateSniffer { it.copy(parsePureIp = v) } },
                            )
                        },
                        CardItem("overrideDest") {
                            TriStatePreference(
                                title = stringResource(R.string.meta_sniffer_override_dest),
                                value = sniffer?.overrideDestination,
                                onValueChange = { v ->
                                    updateSniffer { it.copy(overrideDestination = v) }
                                },
                            )
                        },
                        CardItem("forceDomain") {
                            val forceDomainTitle =
                                stringResource(R.string.meta_sniffer_force_domain)
                            PreferenceRow(
                                title = forceDomainTitle,
                                summary = listSummary(sniffer?.forceDomain),
                                onClick = {
                                    openListDialog(forceDomainTitle, sniffer?.forceDomain) { v ->
                                        updateSniffer { it.copy(forceDomain = v) }
                                    }
                                },
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                        },
                        CardItem("skipDomain") {
                            val skipDomainTitle = stringResource(R.string.meta_sniffer_skip_domain)
                            PreferenceRow(
                                title = skipDomainTitle,
                                summary = listSummary(sniffer?.skipDomain),
                                onClick = {
                                    openListDialog(skipDomainTitle, sniffer?.skipDomain) { v ->
                                        updateSniffer { it.copy(skipDomain = v) }
                                    }
                                },
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                        },
                    ),
            )

            item { SettingsSectionHeader(title = stringResource(R.string.meta_age)) }
            groupedCardItems(
                keyPrefix = "meta_age",
                items =
                    listOf(
                        CardItem("generate") {
                            PreferenceRow(
                                title = stringResource(R.string.meta_age_generate),
                                summary = stringResource(R.string.meta_age_generate_summary),
                                onClick = { genKey(false) },
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                        },
                        CardItem("generateHybrid") {
                            PreferenceRow(
                                title = stringResource(R.string.meta_age_generate_hybrid),
                                summary = stringResource(R.string.meta_age_generate_hybrid_summary),
                                onClick = { genKey(true) },
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                        },
                    ),
            )

            item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
        }
    }

    ListEditDialog(
        show = showListDialog,
        title = editingListTitle,
        textState = listTextState,
        onDismiss = { showListDialog = false },
        onConfirm = { list -> editingListSetter(list) },
        onReset = {
            editingListSetter(null)
            showToast(resetDoneMsg)
        },
    )

    val clipboard = LocalClipboard.current
    val clipboardScope = rememberCoroutineScope()
    val copiedMsg = stringResource(R.string.common_copied)
    val agePair = ageKeyPair
    if (showAgeDialog && agePair != null) {
        AlertDialog(
            onDismissRequest = { showAgeDialog = false },
            confirmButton = {
                FlowRow(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {}
            },
            title = { Text(stringResource(R.string.meta_age_keypair_title)) },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (agePair != null) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // 密钥内容可滚动，按钮固定在底部
                            Column(
                                modifier =
                                    Modifier.fillMaxWidth()
                                        .weight(1f, fill = false)
                                        .verticalScroll(rememberScrollState())
                            ) {
                                SettingsSectionHeader(
                                    title = stringResource(R.string.meta_age_secret_key),
                                    modifier = Modifier.padding(PaddingValues(vertical = 8.dp)),
                                )
                                Text(
                                    text = agePair.secretKey,
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                )
                                SettingsSectionHeader(
                                    title = stringResource(R.string.meta_age_public_key),
                                    modifier = Modifier.padding(PaddingValues(vertical = 8.dp)),
                                )
                                Text(
                                    text = agePair.publicKey,
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                SettingsActionButton(
                                    label = stringResource(R.string.meta_age_copy_secret),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        clipboardScope.launch {
                                            clipboard.setPlainText(agePair.secretKey)
                                        }
                                        showToast(copiedMsg)
                                    },
                                    style =
                                        org.uwuaosp.compose.widget.expressive.SettingsButtonStyle
                                            .Tonal,
                                )
                                SettingsActionButton(
                                    label = stringResource(R.string.meta_age_copy_public),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        clipboardScope.launch {
                                            clipboard.setPlainText(agePair.publicKey)
                                        }
                                        showToast(copiedMsg)
                                    },
                                    style =
                                        org.uwuaosp.compose.widget.expressive.SettingsButtonStyle
                                            .Tonal,
                                )
                            }
                            SettingsActionButton(
                                label = stringResource(R.string.common_close),
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                onClick = { showAgeDialog = false },
                                style =
                                    org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Tonal,
                            )
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun FindProcessModePreference(value: String?, onValueChange: (String?) -> Unit) {
    val notModifiedStr = stringResource(R.string.common_not_modified)
    val items = listOf(notModifiedStr, "Off", "Strict", "Always")
    val values = listOf(null, "off", "strict", "always")
    val selectedIndex = values.indexOf(value).coerceAtLeast(0)

    MenuPreference(
        title = stringResource(R.string.meta_find_process_mode),
        summary = items[selectedIndex],
        items = items,
        selectedIndex = selectedIndex,
        onSelectedIndexChange = { index -> onValueChange(values[index]) },
    )
}

@Composable
private fun listSummary(list: List<String>?): String {
    if (list == null) return stringResource(R.string.common_not_modified)
    if (list.isEmpty()) return stringResource(R.string.common_cleared)
    return pluralStringResource(R.plurals.common_items_count, list.size, list.size)
}
