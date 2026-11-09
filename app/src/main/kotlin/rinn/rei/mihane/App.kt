package rinn.rei.mihane

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import rinn.rei.mihane.platform.BootStartManager
import rinn.rei.mihane.platform.FilePicker
import rinn.rei.mihane.platform.PlatformStorage
import rinn.rei.mihane.platform.WifiPolicyController
import rinn.rei.mihane.ui.navigation.AppNavigation
import rinn.rei.mihane.ui.theme.LocalAppDarkMode
import rinn.rei.mihane.ui.theme.LocalAppMonetEnabled
import rinn.rei.mihane.ui.theme.LocalPlatformDensity
import rinn.rei.mihane.ui.theme.MishkaTheme
import rinn.rei.mihane.ui.theme.ThemeConfig
import rinn.rei.mihane.ui.theme.resolveIsDark
import rinn.rei.mihane.viewmodel.AppProxyViewModel
import rinn.rei.mihane.viewmodel.AutomationViewModel
import rinn.rei.mihane.viewmodel.BackupViewModel
import rinn.rei.mihane.viewmodel.ConnectionViewModel
import rinn.rei.mihane.viewmodel.DnsQueryViewModel
import rinn.rei.mihane.viewmodel.ExternalControlViewModel
import rinn.rei.mihane.viewmodel.HomeViewModel
import rinn.rei.mihane.viewmodel.LogViewModel
import rinn.rei.mihane.viewmodel.MetaSettingsViewModel
import rinn.rei.mihane.viewmodel.NetworkSettingsViewModel
import rinn.rei.mihane.viewmodel.OverrideProfileViewModel
import rinn.rei.mihane.viewmodel.ProviderViewModel
import rinn.rei.mihane.viewmodel.ProxyViewModel
import rinn.rei.mihane.viewmodel.SubscriptionViewModel

@Composable
fun App(
    themeConfig: ThemeConfig = ThemeConfig(),
    onThemeConfigChange: (ThemeConfig) -> Unit = {},
    homeViewModel: HomeViewModel? = null,
    subscriptionViewModel: SubscriptionViewModel? = null,
    overrideProfileViewModel: OverrideProfileViewModel? = null,
    proxyViewModel: ProxyViewModel? = null,
    logViewModel: LogViewModel? = null,
    providerViewModel: ProviderViewModel? = null,
    connectionViewModel: ConnectionViewModel? = null,
    dnsQueryViewModel: DnsQueryViewModel? = null,
    networkSettingsViewModel: NetworkSettingsViewModel? = null,
    metaSettingsViewModel: MetaSettingsViewModel? = null,
    externalControlViewModel: ExternalControlViewModel? = null,
    automationViewModel: AutomationViewModel? = null,
    appProxyViewModel: AppProxyViewModel? = null,
    filePicker: FilePicker? = null,
    storage: PlatformStorage? = null,
    bootStartManager: BootStartManager? = null,
    mihomoVersion: String = "",
    onScanQR: ((callback: (String?) -> Unit) -> Unit)? = null,
    wifiPolicyController: WifiPolicyController? = null,
    onRequestWifiPermission: (((Boolean) -> Unit) -> Unit)? = null,
    onPredictiveBackChange: ((Boolean) -> Unit)? = null,
    onHideTaskCardChange: ((Boolean) -> Unit)? = null,
    hasRootPermission: Boolean = false,
    deepLinkImport: DeepLinkImportRequest? = null,
    onDeepLinkImportConsumed: () -> Unit = {},
    backupViewModel: BackupViewModel? = null,
    onRestartApp: () -> Unit = {},
) {
    val isDark = themeConfig.resolveIsDark(isSystemInDarkTheme())
    MishkaTheme(themeConfig, isDark) {
        val currentDensity = LocalDensity.current
        val appDensity =
            remember(currentDensity, themeConfig.densityScale) {
                Density(
                    density = currentDensity.density * themeConfig.densityScale,
                    fontScale = currentDensity.fontScale,
                )
            }
        CompositionLocalProvider(
            LocalAppDarkMode provides isDark,
            LocalAppMonetEnabled provides themeConfig.useMonet,
            LocalPlatformDensity provides currentDensity,
            LocalDensity provides appDensity,
            LocalContentColor provides
                androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
        ) {
            AppNavigation(
                themeConfig = themeConfig,
                onThemeConfigChange = onThemeConfigChange,
                homeViewModel = homeViewModel,
                subscriptionViewModel = subscriptionViewModel,
                overrideProfileViewModel = overrideProfileViewModel,
                proxyViewModel = proxyViewModel,
                logViewModel = logViewModel,
                providerViewModel = providerViewModel,
                connectionViewModel = connectionViewModel,
                dnsQueryViewModel = dnsQueryViewModel,
                networkSettingsViewModel = networkSettingsViewModel,
                metaSettingsViewModel = metaSettingsViewModel,
                externalControlViewModel = externalControlViewModel,
                automationViewModel = automationViewModel,
                appProxyViewModel = appProxyViewModel,
                filePicker = filePicker,
                storage = storage,
                bootStartManager = bootStartManager,
                mihomoVersion = mihomoVersion,
                onScanQR = onScanQR,
                wifiPolicyController = wifiPolicyController,
                onRequestWifiPermission = onRequestWifiPermission,
                onPredictiveBackChange = onPredictiveBackChange,
                onHideTaskCardChange = onHideTaskCardChange,
                hasRootPermission = hasRootPermission,
                deepLinkImport = deepLinkImport,
                onDeepLinkImportConsumed = onDeepLinkImportConsumed,
                backupViewModel = backupViewModel,
                onRestartApp = onRestartApp,
            )
        }
    }
}
