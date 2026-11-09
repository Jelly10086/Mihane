package rinn.rei.mihane.ui.screen.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.uwuaosp.compose.widget.preference.SettingsSectionHeader
import org.uwuaosp.compose.widget.preference.SwitchPreferenceRow
import org.uwuaosp.compose.widget.scaffold.SettingsCollapsingAppBarScaffold
import rinn.rei.mihane.R
import rinn.rei.mihane.platform.PlatformStorage
import rinn.rei.mihane.platform.StorageKeys
import rinn.rei.mihane.ui.component.CardItem
import rinn.rei.mihane.ui.component.LocalPreferencePosition
import rinn.rei.mihane.ui.component.groupedCardItems
import rinn.rei.mihane.ui.util.horizontalCutoutPadding
import rinn.rei.mihane.ui.util.readableContentPadding

@Composable
fun VpnSettingsScreen(
    storage: PlatformStorage,
    isSystemProxySupported: Boolean = false,
    onBack: () -> Unit = {},
) {

    var bypassPrivate by remember {
        mutableStateOf(storage.getString(StorageKeys.VPN_BYPASS_PRIVATE_NETWORK, "true") == "true")
    }
    var allowBypass by remember {
        mutableStateOf(storage.getString(StorageKeys.VPN_ALLOW_BYPASS, "true") == "true")
    }
    var dnsHijacking by remember {
        mutableStateOf(storage.getString(StorageKeys.VPN_DNS_HIJACKING, "true") != "false")
    }
    var systemProxy by remember {
        mutableStateOf(storage.getString(StorageKeys.VPN_SYSTEM_PROXY, "true") != "false")
    }
    var allowIpv6 by remember {
        mutableStateOf(storage.getString(StorageKeys.VPN_ALLOW_IPV6, "false") == "true")
    }

    SettingsCollapsingAppBarScaffold(
        title = stringResource(R.string.vpn_settings_title),
        showBackButton = true,
        onNavigateUp = onBack,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().horizontalCutoutPadding(),
            contentPadding = readableContentPadding(innerPadding),
        ) {
            item { SettingsSectionHeader(title = stringResource(R.string.vpn_tunnel)) }
            groupedCardItems(
                keyPrefix = "vpn_tunnel",
                outerBottomPadding = 0.dp,
                items =
                    buildList {
                        add(
                            CardItem("bypassPrivate") {
                                SwitchPreferenceRow(
                                    title = stringResource(R.string.vpn_bypass_private),
                                    summary = stringResource(R.string.vpn_bypass_private_summary),
                                    checked = bypassPrivate,
                                    onCheckedChange = {
                                        bypassPrivate = it
                                        storage.putString(
                                            StorageKeys.VPN_BYPASS_PRIVATE_NETWORK,
                                            it.toString(),
                                        )
                                    },
                                    index = LocalPreferencePosition.current.first,
                                    itemCount = LocalPreferencePosition.current.second,
                                )
                            }
                        )
                        add(
                            CardItem("allowBypass") {
                                SwitchPreferenceRow(
                                    title = stringResource(R.string.vpn_allow_bypass),
                                    summary = stringResource(R.string.vpn_allow_bypass_summary),
                                    checked = allowBypass,
                                    onCheckedChange = {
                                        allowBypass = it
                                        storage.putString(
                                            StorageKeys.VPN_ALLOW_BYPASS,
                                            it.toString(),
                                        )
                                    },
                                    index = LocalPreferencePosition.current.first,
                                    itemCount = LocalPreferencePosition.current.second,
                                )
                            }
                        )
                        add(
                            CardItem("dnsHijacking") {
                                SwitchPreferenceRow(
                                    title = stringResource(R.string.vpn_dns_hijacking),
                                    summary = stringResource(R.string.vpn_dns_hijacking_summary),
                                    checked = dnsHijacking,
                                    onCheckedChange = {
                                        dnsHijacking = it
                                        storage.putString(
                                            StorageKeys.VPN_DNS_HIJACKING,
                                            it.toString(),
                                        )
                                    },
                                    index = LocalPreferencePosition.current.first,
                                    itemCount = LocalPreferencePosition.current.second,
                                )
                            }
                        )
                        if (isSystemProxySupported) {
                            add(
                                CardItem("systemProxy") {
                                    SwitchPreferenceRow(
                                        title = stringResource(R.string.vpn_system_proxy),
                                        summary = stringResource(R.string.vpn_system_proxy_summary),
                                        checked = systemProxy,
                                        onCheckedChange = {
                                            systemProxy = it
                                            storage.putString(
                                                StorageKeys.VPN_SYSTEM_PROXY,
                                                it.toString(),
                                            )
                                        },
                                        index = LocalPreferencePosition.current.first,
                                        itemCount = LocalPreferencePosition.current.second,
                                    )
                                }
                            )
                        }
                        add(
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
                            }
                        )
                    },
            )
            item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
        }
    }
}
