package rinn.rei.mihane.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.rememberTextFieldState
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
import org.uwuaosp.compose.widget.preference.CustomPreferenceRow
import org.uwuaosp.compose.widget.preference.PreferenceRow
import org.uwuaosp.compose.widget.preference.SettingsSectionHeader
import org.uwuaosp.compose.widget.preference.SwitchPreferenceRow
import org.uwuaosp.compose.widget.scaffold.SettingsCollapsingAppBarScaffold
import rinn.rei.mihane.R
import rinn.rei.mihane.platform.PlatformStorage
import rinn.rei.mihane.platform.StorageKeys
import rinn.rei.mihane.ui.component.CardItem
import rinn.rei.mihane.ui.component.LocalPreferencePosition
import rinn.rei.mihane.ui.component.RestartRequiredHint
import rinn.rei.mihane.ui.component.TetherInterfaceEditDialog
import rinn.rei.mihane.ui.component.groupedCardItems
import rinn.rei.mihane.ui.component.material.MenuPreference
import rinn.rei.mihane.ui.component.tetherInterfaceSummary
import rinn.rei.mihane.ui.util.horizontalCutoutPadding
import rinn.rei.mihane.ui.util.readableContentPadding

/**
 * ROOT 模式专属设置页。仅在 ROOT 模式下可见。
 *
 * 内容：
 * - TUN 设备名称（mihomo 创建的 TUN 接口名，代理运行时不可改）
 * - 热点客户端流量处置（绕过代理 / 走代理）
 * - 热点接口名列表（手填 + 扫描辅助）
 */
@Composable
fun RootSettingsScreen(storage: PlatformStorage, isProxyRunning: Boolean, onBack: () -> Unit = {}) {

    var tunDevice by remember {
        mutableStateOf(storage.getString(StorageKeys.ROOT_TUN_DEVICE, DEFAULT_TUN_DEVICE))
    }
    var showTunDeviceDialog by remember { mutableStateOf(false) }
    val tunDeviceTextState = rememberTextFieldState()

    var jumboMtu by remember {
        mutableStateOf(storage.getString(StorageKeys.ROOT_TUN_JUMBO_MTU, "true") == "true")
    }

    var allowIpv6 by remember {
        mutableStateOf(storage.getString(StorageKeys.VPN_ALLOW_IPV6, "false") == "true")
    }

    var forceReapply by remember {
        mutableStateOf(storage.getString(StorageKeys.ROOT_ATTACH_FORCE_REAPPLY, "false") == "true")
    }

    val bypassLabel = stringResource(R.string.root_tether_mode_bypass)
    val proxyLabel = stringResource(R.string.root_tether_mode_proxy)
    val tetherModeValues = listOf("bypass", "proxy")
    var tetherMode by remember {
        mutableStateOf(storage.getString(StorageKeys.ROOT_TETHER_MODE, "bypass"))
    }
    var tetherIfaces by remember {
        mutableStateOf(storage.getString(StorageKeys.ROOT_TETHER_IFACES, DEFAULT_TETHER_IFACES))
    }
    var showTetherDialog by remember { mutableStateOf(false) }

    // 读一次 submode：TPROXY 下隐藏 tether mode 下拉，显示说明文案
    // （用户改 TUN_MODE 必须先停代理，不需要热更新）
    val isTproxy = storage.getString(StorageKeys.TUN_MODE, "vpn") == "root_tproxy"

    // xt_TPROXY 内核能力探测结果，仅在 TPROXY submode 或 PROXY 热点模式下显示告警
    val tproxyCapable = storage.getString(StorageKeys.ROOT_TPROXY_KERNEL_CAPABLE, "")
    val showTproxyWarning = tproxyCapable == "false" && (isTproxy || tetherMode == "proxy")

    SettingsCollapsingAppBarScaffold(
        title = stringResource(R.string.root_settings_title),
        showBackButton = true,
        onNavigateUp = onBack,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().horizontalCutoutPadding(),
            contentPadding = readableContentPadding(innerPadding),
        ) {
            item { RestartRequiredHint() }
            item { SettingsSectionHeader(title = stringResource(R.string.root_section_device)) }
            groupedCardItems(
                keyPrefix = "root_device",
                outerBottomPadding = 12.dp,
                items =
                    listOf(
                        CardItem("tunDevice") {
                            PreferenceRow(
                                title = stringResource(R.string.settings_tun_device),
                                summary = tunDevice,
                                onClick = {
                                    tunDeviceTextState.edit { replace(0, length, tunDevice) }
                                    showTunDeviceDialog = true
                                },
                                enabled = !isProxyRunning,
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                        },
                        CardItem("jumboMtu") {
                            SwitchPreferenceRow(
                                title = stringResource(R.string.root_tun_jumbo_mtu_title),
                                summary = stringResource(R.string.root_tun_jumbo_mtu_summary),
                                checked = jumboMtu,
                                onCheckedChange = {
                                    jumboMtu = it
                                    storage.putString(StorageKeys.ROOT_TUN_JUMBO_MTU, it.toString())
                                },
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                        },
                        CardItem("allowIpv6") {
                            SwitchPreferenceRow(
                                title = stringResource(R.string.vpn_allow_ipv6),
                                summary = stringResource(R.string.vpn_allow_ipv6_summary),
                                checked = allowIpv6,
                                onCheckedChange = {
                                    allowIpv6 = it
                                    storage.putString(StorageKeys.VPN_ALLOW_IPV6, it.toString())
                                },
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                        },
                    ),
            )
            item { SettingsSectionHeader(title = stringResource(R.string.root_section_tether)) }
            if (showTproxyWarning) {
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
                                text =
                                    stringResource(R.string.root_tether_tproxy_unavailable_warning),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            )
                        }
                    }
                }
            }
            groupedCardItems(
                keyPrefix = "root_tether",
                outerBottomPadding = 12.dp,
                items =
                    listOf(
                        CardItem("mode") {
                            if (isTproxy) {
                                // TPROXY 模式：整条链路都由 iptables 透明代理，不需要 BYPASS/PROXY 切换
                                Text(
                                    text = stringResource(R.string.root_tproxy_tether_note),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier =
                                        Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                )
                            } else {
                                val tetherItems = listOf(bypassLabel, proxyLabel)
                                val selectedIndex =
                                    tetherModeValues.indexOf(tetherMode).coerceAtLeast(0)
                                MenuPreference(
                                    title = stringResource(R.string.root_tether_mode_title),
                                    summary = tetherItems[selectedIndex],
                                    items = tetherItems,
                                    selectedIndex = selectedIndex,
                                    onSelectedIndexChange = { index ->
                                        val value = tetherModeValues[index]
                                        tetherMode = value
                                        storage.putString(StorageKeys.ROOT_TETHER_MODE, value)
                                    },
                                )
                            }
                        },
                        CardItem("ifaces") {
                            PreferenceRow(
                                title = stringResource(R.string.root_tether_ifaces_title),
                                summary = tetherInterfaceSummary(tetherIfaces),
                                onClick = { showTetherDialog = true },
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                        },
                        CardItem("forceReapply") {
                            SwitchPreferenceRow(
                                title = stringResource(R.string.root_attach_force_reapply_title),
                                summary =
                                    stringResource(R.string.root_attach_force_reapply_summary),
                                checked = forceReapply,
                                onCheckedChange = {
                                    forceReapply = it
                                    storage.putString(
                                        StorageKeys.ROOT_ATTACH_FORCE_REAPPLY,
                                        it.toString(),
                                    )
                                },
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                        },
                    ),
            )
            item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
        }
    }

    if (showTunDeviceDialog) {
        org.uwuaosp.compose.widget.editor.TextInputPreferenceDialog(
            title = stringResource(R.string.settings_tun_device),
            value = tunDeviceTextState.text.toString(),
            confirmText = stringResource(R.string.common_confirm),
            dismissText = stringResource(R.string.common_cancel),
            onDismissRequest = { showTunDeviceDialog = false },
            onConfirm = { input ->
                val value = input.trim().ifEmpty { DEFAULT_TUN_DEVICE }
                storage.putString(StorageKeys.ROOT_TUN_DEVICE, value)
                tunDevice = value
                showTunDeviceDialog = false
            },
        )
    }

    TetherInterfaceEditDialog(
        show = showTetherDialog,
        title = stringResource(R.string.root_tether_ifaces_title),
        initialValue = tetherIfaces,
        onDismiss = { showTetherDialog = false },
        onConfirm = { csv ->
            tetherIfaces = csv
            storage.putString(StorageKeys.ROOT_TETHER_IFACES, csv)
        },
    )
}

private const val DEFAULT_TUN_DEVICE = "Mishka"

private const val DEFAULT_TETHER_IFACES = "wlan1,wlan2"
