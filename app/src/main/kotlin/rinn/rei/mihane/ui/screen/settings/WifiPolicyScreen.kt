package rinn.rei.mihane.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.uwuaosp.compose.widget.preference.PreferenceRow
import org.uwuaosp.compose.widget.preference.SettingsSectionHeader
import org.uwuaosp.compose.widget.preference.SwitchPreferenceRow
import org.uwuaosp.compose.widget.scaffold.SettingsCollapsingAppBarScaffold
import org.uwuaosp.compose.widget.scaffold.SettingsToolbarActionButton
import rinn.rei.mihane.R
import rinn.rei.mihane.platform.PlatformStorage
import rinn.rei.mihane.platform.StorageKeys
import rinn.rei.mihane.platform.WifiPolicyAction
import rinn.rei.mihane.platform.WifiPolicyController
import rinn.rei.mihane.platform.showToast
import rinn.rei.mihane.ui.component.AppIcons
import rinn.rei.mihane.ui.component.CardItem
import rinn.rei.mihane.ui.component.LocalPreferencePosition
import rinn.rei.mihane.ui.component.groupedCardItems
import rinn.rei.mihane.ui.component.material.FormField
import rinn.rei.mihane.ui.component.material.MenuPreference
import rinn.rei.mihane.ui.util.WideContentBox

@Composable
fun WifiPolicyScreen(
    storage: PlatformStorage,
    controller: WifiPolicyController?,
    onRequestPermission: (((Boolean) -> Unit) -> Unit)? = null,
    onBack: () -> Unit = {},
) {
    var enabled by remember {
        mutableStateOf(storage.getString(StorageKeys.WIFI_POLICY_ENABLED, "false") == "true")
    }
    var action by remember {
        mutableStateOf(
            WifiPolicyAction.fromStorage(storage.getString(StorageKeys.WIFI_POLICY_ACTION, ""))
        )
    }
    var notifySwitch by remember {
        mutableStateOf(storage.getString(StorageKeys.WIFI_POLICY_NOTIFY_SWITCH, "true") != "false")
    }
    var hideMonitorNotification by remember {
        mutableStateOf(
            storage.getString(StorageKeys.WIFI_POLICY_HIDE_MONITOR_NOTIFICATION, "false") == "true"
        )
    }
    var ssids by remember {
        mutableStateOf(
            storage.getStringSet(StorageKeys.WIFI_POLICY_SSIDS, emptySet()).toList().sorted()
        )
    }
    var permissionGranted by remember {
        mutableStateOf(controller?.hasRequiredPermission() == true)
    }
    var currentSsid by remember { mutableStateOf(controller?.currentSsid()) }
    var newSsid by rememberSaveable { mutableStateOf("") }

    val permissionRequired = stringResource(R.string.wifi_policy_permission_required)
    val ssidEmpty = stringResource(R.string.wifi_policy_ssid_empty)
    val ssidDuplicate = stringResource(R.string.wifi_policy_ssid_duplicate)
    val currentWifiEmpty = stringResource(R.string.wifi_policy_ssid_scan_empty)

    fun refreshStatus() {
        permissionGranted = controller?.hasRequiredPermission() == true
        currentSsid = controller?.currentSsid()
    }

    fun persistEnabled(value: Boolean) {
        storage.putString(StorageKeys.WIFI_POLICY_ENABLED, value.toString())
        enabled = value
        if (value) controller?.startMonitor() else controller?.stopMonitor()
    }

    fun requestPermissionThenEnable() {
        if (controller == null || onRequestPermission == null) {
            showToast(permissionRequired, long = true)
            return
        }
        if (controller.hasRequiredPermission()) {
            refreshStatus()
            persistEnabled(true)
            return
        }
        onRequestPermission { granted ->
            permissionGranted = granted
            currentSsid = controller.currentSsid()
            if (granted) {
                persistEnabled(true)
            } else {
                persistEnabled(false)
                showToast(permissionRequired, long = true)
            }
        }
    }

    fun requestPermissionOnly(onGranted: () -> Unit) {
        if (controller == null || onRequestPermission == null) {
            showToast(permissionRequired, long = true)
            return
        }
        if (controller.hasRequiredPermission()) {
            refreshStatus()
            onGranted()
            return
        }
        onRequestPermission { granted ->
            permissionGranted = granted
            currentSsid = controller.currentSsid()
            if (granted) {
                onGranted()
            } else {
                showToast(permissionRequired, long = true)
            }
        }
    }

    fun persistSsids(value: List<String>) {
        ssids = value.distinct().sorted()
        storage.putStringSet(StorageKeys.WIFI_POLICY_SSIDS, ssids.toSet())
        if (enabled) controller?.evaluateNow()
    }

    fun normalizeInputSsid(value: String): String {
        val trimmed = value.trim()
        return if (trimmed.length >= 2 && trimmed.first() == '"' && trimmed.last() == '"') {
                trimmed.substring(1, trimmed.length - 1)
            } else {
                trimmed
            }
            .trim()
    }

    fun addSsid() {
        val value = normalizeInputSsid(newSsid)
        when {
            value.isEmpty() -> showToast(ssidEmpty)
            value in ssids -> showToast(ssidDuplicate)
            else -> {
                persistSsids(ssids + value)
                newSsid = ""
            }
        }
    }

    fun fillCurrentSsid() {
        requestPermissionOnly {
            val candidate = controller?.currentSsid()
            if (candidate == null) {
                showToast(currentWifiEmpty)
            } else {
                newSsid = candidate
                refreshStatus()
            }
        }
    }

    val actionItems =
        listOf(
            stringResource(R.string.wifi_policy_action_stop),
            stringResource(R.string.wifi_policy_action_direct),
        )
    val actionSummary =
        when (action) {
            WifiPolicyAction.StopService -> stringResource(R.string.wifi_policy_action_stop_summary)
            WifiPolicyAction.DirectMode ->
                stringResource(R.string.wifi_policy_action_direct_summary)
        }
    val ssidCardItems =
        listOf(
            CardItem("ssidInput") {
                SsidInputRow(
                    value = newSsid,
                    onValueChange = { newSsid = it },
                    onScan = { fillCurrentSsid() },
                    onAdd = { addSsid() },
                )
            }
        ) +
            ssids.map { ssid ->
                CardItem("ssid:$ssid") {
                    SsidListItem(ssid = ssid, onDelete = { persistSsids(ssids - ssid) })
                }
            } +
            listOf(
                CardItem("current") {
                    PreferenceRow(
                        title = stringResource(R.string.wifi_policy_current),
                        summary = currentSsid ?: stringResource(R.string.wifi_policy_unknown_ssid),
                        onClick = { refreshStatus() },
                        index = LocalPreferencePosition.current.first,
                        itemCount = LocalPreferencePosition.current.second,
                    )
                },
                CardItem("permission") {
                    PreferenceRow(
                        title = stringResource(R.string.wifi_policy_permission),
                        summary =
                            stringResource(
                                if (permissionGranted) R.string.wifi_policy_permission_granted
                                else R.string.wifi_policy_permission_missing
                            ),
                        onClick = {
                            if (!permissionGranted) requestPermissionThenEnable()
                            else refreshStatus()
                        },
                        index = LocalPreferencePosition.current.first,
                        itemCount = LocalPreferencePosition.current.second,
                    )
                },
            )

    SettingsCollapsingAppBarScaffold(
        title = stringResource(R.string.wifi_policy_title),
        showBackButton = true,
        onNavigateUp = onBack,
    ) { innerPadding ->
        WideContentBox { sidePadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().imePadding(),
                contentPadding =
                    PaddingValues(
                        top = innerPadding.calculateTopPadding(),
                        start = sidePadding,
                        end = sidePadding,
                    ),
            ) {
                item { SettingsSectionHeader(title = stringResource(R.string.wifi_policy_basic)) }
                groupedCardItems(
                    keyPrefix = "wifi_policy_control",
                    items =
                        listOf(
                            CardItem("enable") {
                                SwitchPreferenceRow(
                                    title = stringResource(R.string.wifi_policy_enable),
                                    summary = stringResource(R.string.wifi_policy_enable_summary),
                                    checked = enabled,
                                    onCheckedChange = { checked ->
                                        if (checked) requestPermissionThenEnable()
                                        else persistEnabled(false)
                                    },
                                    index = LocalPreferencePosition.current.first,
                                    itemCount = LocalPreferencePosition.current.second,
                                )
                            },
                            CardItem("notifySwitch") {
                                SwitchPreferenceRow(
                                    title = stringResource(R.string.wifi_policy_notify_switch),
                                    summary =
                                        stringResource(R.string.wifi_policy_notify_switch_summary),
                                    checked = notifySwitch,
                                    onCheckedChange = { checked ->
                                        notifySwitch = checked
                                        storage.putString(
                                            StorageKeys.WIFI_POLICY_NOTIFY_SWITCH,
                                            checked.toString(),
                                        )
                                    },
                                    index = LocalPreferencePosition.current.first,
                                    itemCount = LocalPreferencePosition.current.second,
                                )
                            },
                            CardItem("hideMonitorNotification") {
                                SwitchPreferenceRow(
                                    title =
                                        stringResource(
                                            R.string.wifi_policy_hide_monitor_notification
                                        ),
                                    summary =
                                        stringResource(
                                            R.string.wifi_policy_hide_monitor_notification_summary
                                        ),
                                    checked = hideMonitorNotification,
                                    onCheckedChange = { checked ->
                                        hideMonitorNotification = checked
                                        storage.putString(
                                            StorageKeys.WIFI_POLICY_HIDE_MONITOR_NOTIFICATION,
                                            checked.toString(),
                                        )
                                        if (enabled) controller?.startMonitor()
                                    },
                                    index = LocalPreferencePosition.current.first,
                                    itemCount = LocalPreferencePosition.current.second,
                                )
                            },
                            CardItem("action") {
                                MenuPreference(
                                    title = stringResource(R.string.wifi_policy_action),
                                    summary = actionSummary,
                                    items = actionItems,
                                    selectedIndex =
                                        if (action == WifiPolicyAction.DirectMode) 1 else 0,
                                    onSelectedIndexChange = { index ->
                                        action =
                                            if (index == 1) WifiPolicyAction.DirectMode
                                            else WifiPolicyAction.StopService
                                        storage.putString(
                                            StorageKeys.WIFI_POLICY_ACTION,
                                            action.storageValue,
                                        )
                                        if (enabled) controller?.evaluateNow()
                                    },
                                    enabled = enabled,
                                )
                            },
                        ),
                )

                item {
                    SettingsSectionHeader(title = stringResource(R.string.wifi_policy_ssids_title))
                }
                groupedCardItems(keyPrefix = "wifi_policy_ssids", items = ssidCardItems)
                item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
            }
        }
    }
}

@Composable
private fun SsidInputRow(
    value: String,
    onValueChange: (String) -> Unit,
    onScan: () -> Unit,
    onAdd: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SettingsToolbarActionButton(
            onClick = onScan,
            imageVector = AppIcons.Wifi,
            contentDescription = stringResource(R.string.wifi_policy_ssid_scan),
        )
        FormField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.wifi_policy_ssid_add_placeholder),
            useLabelAsPlaceholder = true,
        )
        SettingsToolbarActionButton(
            onClick = onAdd,
            imageVector = AppIcons.Add,
            contentDescription = stringResource(R.string.wifi_policy_ssid_add),
        )
    }
}

@Composable
private fun SsidListItem(ssid: String, onDelete: () -> Unit) {
    PreferenceRow(
        title = ssid,
        trailingContent = {
            SettingsToolbarActionButton(
                onClick = onDelete,
                imageVector = AppIcons.Delete,
                contentDescription = stringResource(R.string.common_delete),
            )
        },
        index = LocalPreferencePosition.current.first,
        itemCount = LocalPreferencePosition.current.second,
    )
}
