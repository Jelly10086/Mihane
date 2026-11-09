package rinn.rei.mihane.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.uwuaosp.compose.widget.expressive.SettingsActionButton
import org.uwuaosp.compose.widget.preference.PreferenceRow
import org.uwuaosp.compose.widget.preference.SettingsSectionHeader
import org.uwuaosp.compose.widget.scaffold.SettingsCollapsingAppBarScaffold
import rinn.rei.mihane.R
import rinn.rei.mihane.domain.model.ConfigurationOverride
import rinn.rei.mihane.domain.model.DnsOverride
import rinn.rei.mihane.platform.showToast
import rinn.rei.mihane.ui.component.CardItem
import rinn.rei.mihane.ui.component.ListEditDialog
import rinn.rei.mihane.ui.component.LocalPreferencePosition
import rinn.rei.mihane.ui.component.RestartRequiredHint
import rinn.rei.mihane.ui.component.TriStatePreference
import rinn.rei.mihane.ui.component.groupedCardItems
import rinn.rei.mihane.ui.component.material.FormField
import rinn.rei.mihane.ui.component.material.MenuPreference
import rinn.rei.mihane.ui.util.horizontalCutoutPadding
import rinn.rei.mihane.ui.util.readableContentPadding
import rinn.rei.mihane.viewmodel.NetworkSettingsViewModel

@Composable
fun NetworkSettingsScreen(viewModel: NetworkSettingsViewModel, onBack: () -> Unit = {}) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val resetDoneMsg = stringResource(R.string.dialog_reset_done)

    fun updateTop(transform: (ConfigurationOverride) -> ConfigurationOverride) {
        viewModel.update(transform)
    }

    fun updateDns(transform: (DnsOverride) -> DnsOverride) {
        viewModel.updateDns(transform)
    }

    // 端口编辑 Dialog 状态（setter 在 open 时绑定）
    var showPortDialog by remember { mutableStateOf(false) }
    var editingPortTitle by remember { mutableStateOf("") }
    var editingPortSetter by remember { mutableStateOf<(Int?) -> Unit>({}) }
    val portTextState = rememberTextFieldState()

    // 字符串编辑 Dialog 状态
    var showStringDialog by remember { mutableStateOf(false) }
    var editingStringTitle by remember { mutableStateOf("") }
    var editingStringSetter by remember { mutableStateOf<(String?) -> Unit>({}) }
    val stringTextState = rememberTextFieldState()

    // 列表编辑 Dialog 状态
    var showListDialog by remember { mutableStateOf(false) }
    var editingListTitle by remember { mutableStateOf("") }
    var editingListSetter by remember { mutableStateOf<(List<String>?) -> Unit>({}) }
    val listTextState = rememberTextFieldState()

    fun openPortDialog(title: String, value: Int?, setter: (Int?) -> Unit) {
        editingPortTitle = title
        editingPortSetter = setter
        portTextState.edit { replace(0, length, value?.toString() ?: "") }
        showPortDialog = true
    }

    fun openStringDialog(title: String, value: String?, setter: (String?) -> Unit) {
        editingStringTitle = title
        editingStringSetter = setter
        stringTextState.edit { replace(0, length, value ?: "") }
        showStringDialog = true
    }

    fun openListDialog(title: String, value: List<String>?, setter: (List<String>?) -> Unit) {
        editingListTitle = title
        editingListSetter = setter
        listTextState.edit { replace(0, length, value?.joinToString("\n") ?: "") }
        showListDialog = true
    }

    val dns = uiState.dns

    SettingsCollapsingAppBarScaffold(
        title = stringResource(R.string.network_settings_title),
        showBackButton = true,
        onNavigateUp = onBack,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().horizontalCutoutPadding(),
            contentPadding = readableContentPadding(innerPadding),
        ) {
            item { RestartRequiredHint() }

            item { SettingsSectionHeader(title = stringResource(R.string.network_proxy_ports)) }
            groupedCardItems(
                keyPrefix = "network_ports",
                items =
                    listOf(
                        CardItem("http") {
                            val httpPortTitle = stringResource(R.string.network_http_port)
                            PreferenceRow(
                                title = httpPortTitle,
                                summary = portSummary(uiState.httpPort),
                                onClick = {
                                    openPortDialog(httpPortTitle, uiState.httpPort) { v ->
                                        updateTop { it.copy(httpPort = v) }
                                    }
                                },
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                        },
                        CardItem("socks") {
                            val socksPortTitle = stringResource(R.string.network_socks_port)
                            PreferenceRow(
                                title = socksPortTitle,
                                summary = portSummary(uiState.socksPort),
                                onClick = {
                                    openPortDialog(socksPortTitle, uiState.socksPort) { v ->
                                        updateTop { it.copy(socksPort = v) }
                                    }
                                },
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                        },
                        CardItem("redir") {
                            val redirPortTitle = stringResource(R.string.network_redir_port)
                            PreferenceRow(
                                title = redirPortTitle,
                                summary = portSummary(uiState.redirPort),
                                onClick = {
                                    openPortDialog(redirPortTitle, uiState.redirPort) { v ->
                                        updateTop { it.copy(redirPort = v) }
                                    }
                                },
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                        },
                        CardItem("tproxy") {
                            val tproxyPortTitle = stringResource(R.string.network_tproxy_port)
                            PreferenceRow(
                                title = tproxyPortTitle,
                                summary = portSummary(uiState.tproxyPort),
                                onClick = {
                                    openPortDialog(tproxyPortTitle, uiState.tproxyPort) { v ->
                                        updateTop { it.copy(tproxyPort = v) }
                                    }
                                },
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                        },
                        CardItem("mixed") {
                            val mixedPortTitle = stringResource(R.string.network_mixed_port)
                            PreferenceRow(
                                title = mixedPortTitle,
                                summary = portSummary(uiState.mixedPort),
                                onClick = {
                                    openPortDialog(mixedPortTitle, uiState.mixedPort) { v ->
                                        updateTop { it.copy(mixedPort = v) }
                                    }
                                },
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                        },
                    ),
            )

            item { SettingsSectionHeader(title = stringResource(R.string.network_options)) }
            groupedCardItems(
                keyPrefix = "network_options",
                items =
                    listOf(
                        CardItem("allowLan") {
                            TriStatePreference(
                                title = stringResource(R.string.network_allow_lan),
                                value = uiState.allowLan,
                                onValueChange = { v -> updateTop { it.copy(allowLan = v) } },
                            )
                        },
                        CardItem("ipv6") {
                            TriStatePreference(
                                title = "IPv6",
                                value = uiState.ipv6,
                                onValueChange = { v -> updateTop { it.copy(ipv6 = v) } },
                            )
                        },
                        CardItem("bindAddress") {
                            val bindAddrTitle = stringResource(R.string.network_bind_address)
                            PreferenceRow(
                                title = bindAddrTitle,
                                summary =
                                    uiState.bindAddress
                                        ?: stringResource(R.string.common_not_modified),
                                onClick = {
                                    openStringDialog(bindAddrTitle, uiState.bindAddress) { v ->
                                        updateTop { it.copy(bindAddress = v) }
                                    }
                                },
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                        },
                        CardItem("logLevel") {
                            LogLevelPreference(
                                value = uiState.logLevel,
                                onValueChange = { v -> updateTop { it.copy(logLevel = v) } },
                            )
                        },
                    ),
            )

            // DNS 显式关闭时禁用子项；null（不修改）仍保留可编辑，允许用户预配置子覆写
            val dnsSubEnabled = dns?.enable != false
            item { SettingsSectionHeader(title = stringResource(R.string.network_dns)) }
            groupedCardItems(
                keyPrefix = "network_dns",
                items =
                    listOf(
                        CardItem("enable") {
                            TriStatePreference(
                                title = stringResource(R.string.network_dns_enable),
                                value = dns?.enable,
                                onValueChange = { v -> updateDns { it.copy(enable = v) } },
                            )
                        },
                        CardItem("listen") {
                            val dnsListenTitle = stringResource(R.string.network_dns_listen_title)
                            PreferenceRow(
                                title = stringResource(R.string.network_dns_listen),
                                summary =
                                    dns?.listen ?: stringResource(R.string.common_not_modified),
                                onClick = {
                                    openStringDialog(dnsListenTitle, dns?.listen) { v ->
                                        updateDns { it.copy(listen = v) }
                                    }
                                },
                                enabled = dnsSubEnabled,
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                        },
                        CardItem("ipv6") {
                            TriStatePreference(
                                title = "DNS IPv6",
                                value = dns?.ipv6,
                                onValueChange = { v -> updateDns { it.copy(ipv6 = v) } },
                                enabled = dnsSubEnabled,
                            )
                        },
                        CardItem("preferH3") {
                            TriStatePreference(
                                title = stringResource(R.string.network_dns_prefer_h3),
                                value = dns?.preferH3,
                                onValueChange = { v -> updateDns { it.copy(preferH3 = v) } },
                                enabled = dnsSubEnabled,
                            )
                        },
                        CardItem("useHosts") {
                            TriStatePreference(
                                title = stringResource(R.string.network_dns_use_hosts),
                                value = dns?.useHosts,
                                onValueChange = { v -> updateDns { it.copy(useHosts = v) } },
                                enabled = dnsSubEnabled,
                            )
                        },
                        CardItem("enhancedMode") {
                            DnsEnhancedModePreference(
                                value = dns?.enhancedMode,
                                onValueChange = { v -> updateDns { it.copy(enhancedMode = v) } },
                                enabled = dnsSubEnabled,
                            )
                        },
                        CardItem("nameserver") {
                            PreferenceRow(
                                title = "Nameserver",
                                summary = listSummary(dns?.nameserver),
                                onClick = {
                                    openListDialog("Nameserver", dns?.nameserver) { v ->
                                        updateDns { it.copy(nameserver = v) }
                                    }
                                },
                                enabled = dnsSubEnabled,
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                        },
                        CardItem("fallback") {
                            PreferenceRow(
                                title = "Fallback",
                                summary = listSummary(dns?.fallback),
                                onClick = {
                                    openListDialog("Fallback", dns?.fallback) { v ->
                                        updateDns { it.copy(fallback = v) }
                                    }
                                },
                                enabled = dnsSubEnabled,
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                        },
                        CardItem("defaultNameserver") {
                            val defaultNsTitle =
                                stringResource(R.string.network_dns_default_nameserver)
                            PreferenceRow(
                                title = defaultNsTitle,
                                summary = listSummary(dns?.defaultNameserver),
                                onClick = {
                                    openListDialog(defaultNsTitle, dns?.defaultNameserver) { v ->
                                        updateDns { it.copy(defaultNameserver = v) }
                                    }
                                },
                                enabled = dnsSubEnabled,
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                        },
                        CardItem("fakeipFilter") {
                            val fakeipFilterTitle =
                                stringResource(R.string.network_dns_fakeip_filter)
                            PreferenceRow(
                                title = fakeipFilterTitle,
                                summary = listSummary(dns?.fakeIpFilter),
                                onClick = {
                                    openListDialog(fakeipFilterTitle, dns?.fakeIpFilter) { v ->
                                        updateDns { it.copy(fakeIpFilter = v) }
                                    }
                                },
                                enabled = dnsSubEnabled,
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                        },
                    ),
            )

            item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
        }
    }

    PortEditDialog(
        show = showPortDialog,
        title = editingPortTitle,
        textState = portTextState,
        onDismiss = { showPortDialog = false },
        onConfirm = { port -> editingPortSetter(port) },
        onReset = {
            editingPortSetter(null)
            showToast(resetDoneMsg)
        },
    )

    StringEditDialog(
        show = showStringDialog,
        title = editingStringTitle,
        textState = stringTextState,
        onDismiss = { showStringDialog = false },
        onConfirm = { value -> editingStringSetter(value) },
        onReset = {
            editingStringSetter(null)
            showToast(resetDoneMsg)
        },
    )

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
}

// === 私有组件 ===

@Composable
private fun LogLevelPreference(value: String?, onValueChange: (String?) -> Unit) {
    val notModifiedStr = stringResource(R.string.common_not_modified)
    val items = listOf(notModifiedStr, "Info", "Warning", "Error", "Debug", "Silent")
    val values = listOf(null, "info", "warning", "error", "debug", "silent")
    val selectedIndex = values.indexOf(value).coerceAtLeast(0)

    MenuPreference(
        title = stringResource(R.string.network_log_level),
        summary = items[selectedIndex],
        items = items,
        selectedIndex = selectedIndex,
        onSelectedIndexChange = { index -> onValueChange(values[index]) },
    )
}

@Composable
private fun DnsEnhancedModePreference(
    value: String?,
    onValueChange: (String?) -> Unit,
    enabled: Boolean = true,
) {
    val notModifiedStr2 = stringResource(R.string.common_not_modified)
    val items = listOf(notModifiedStr2, "Normal", "FakeIP", "Redir-Host")
    val values = listOf(null, "normal", "fake-ip", "redir-host")
    val selectedIndex = values.indexOf(value).coerceAtLeast(0)

    MenuPreference(
        title = stringResource(R.string.network_dns_enhanced_mode),
        summary = items[selectedIndex],
        items = items,
        selectedIndex = selectedIndex,
        onSelectedIndexChange = { index -> onValueChange(values[index]) },
        enabled = enabled,
    )
}

@Composable
private fun PortEditDialog(
    show: Boolean,
    title: String,
    textState: TextFieldState,
    onDismiss: () -> Unit,
    onConfirm: (Int?) -> Unit,
    onReset: () -> Unit,
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                FlowRow(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SettingsActionButton(
                        label = stringResource(R.string.common_not_modified),
                        onClick = {
                            onReset()
                            onDismiss()
                        },
                        style = org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Text,
                    )
                    SettingsActionButton(
                        label = stringResource(R.string.common_cancel),
                        onClick = onDismiss,
                        style = org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Text,
                    )
                    SettingsActionButton(
                        label = stringResource(R.string.common_confirm),
                        style = org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Text,
                        onClick = {
                            val port = textState.text.toString().toIntOrNull()
                            if (port != null && port in 0..65535) {
                                onConfirm(port)
                            }
                            onDismiss()
                        },
                    )
                }
            },
            title = { Text(title) },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(stringResource(R.string.network_port_zero_hint))

                    FormField(
                        state = textState,
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(R.string.network_port_label),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
            },
        )
    }
}

@Composable
private fun StringEditDialog(
    show: Boolean,
    title: String,
    textState: TextFieldState,
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit,
    onReset: () -> Unit,
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                FlowRow(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SettingsActionButton(
                        label = stringResource(R.string.common_not_modified),
                        onClick = {
                            onReset()
                            onDismiss()
                        },
                        style = org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Text,
                    )
                    SettingsActionButton(
                        label = stringResource(R.string.common_cancel),
                        onClick = onDismiss,
                        style = org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Text,
                    )
                    SettingsActionButton(
                        label = stringResource(R.string.common_confirm),
                        style = org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Text,
                        onClick = {
                            val value = textState.text.toString().trim()
                            onConfirm(value.ifEmpty { null })
                            onDismiss()
                        },
                    )
                }
            },
            title = { Text(title) },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    FormField(
                        state = textState,
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(R.string.network_input_value),
                    )
                }
            },
        )
    }
}

@Composable
private fun portSummary(port: Int?): String =
    if (port == null) stringResource(R.string.common_not_modified) else "$port"

@Composable
private fun listSummary(list: List<String>?): String {
    if (list == null) return stringResource(R.string.common_not_modified)
    if (list.isEmpty()) return stringResource(R.string.common_cleared)
    return pluralStringResource(R.plurals.common_items_count, list.size, list.size)
}
