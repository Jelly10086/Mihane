package rinn.rei.mihane.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import rinn.rei.mihane.DeepLinkImportRequest
import rinn.rei.mihane.R
import rinn.rei.mihane.platform.BootStartManager
import rinn.rei.mihane.platform.FilePicker
import rinn.rei.mihane.platform.PlatformStorage
import rinn.rei.mihane.platform.WifiPolicyController
import rinn.rei.mihane.ui.component.AppIcons
import rinn.rei.mihane.ui.screen.connection.ConnectionScreen
import rinn.rei.mihane.ui.screen.dns.DnsQueryScreen
import rinn.rei.mihane.ui.screen.home.HomeScreen
import rinn.rei.mihane.ui.screen.log.LogScreen
import rinn.rei.mihane.ui.screen.overrides.OverrideEditScreen
import rinn.rei.mihane.ui.screen.overrides.OverrideFileEditorScreen
import rinn.rei.mihane.ui.screen.overrides.OverrideListScreen
import rinn.rei.mihane.ui.screen.overrides.SubscriptionOverridesScreen
import rinn.rei.mihane.ui.screen.provider.ProviderScreen
import rinn.rei.mihane.ui.screen.proxy.ProxyScreen
import rinn.rei.mihane.ui.screen.settings.AboutScreen
import rinn.rei.mihane.ui.screen.settings.AppProxyScreen
import rinn.rei.mihane.ui.screen.settings.AutomationScreen
import rinn.rei.mihane.ui.screen.settings.BackupRestoreScreen
import rinn.rei.mihane.ui.screen.settings.ExternalControlScreen
import rinn.rei.mihane.ui.screen.settings.FileManagerEditorScreen
import rinn.rei.mihane.ui.screen.settings.FileManagerScreen
import rinn.rei.mihane.ui.screen.settings.MetaSettingsScreen
import rinn.rei.mihane.ui.screen.settings.NetworkSettingsScreen
import rinn.rei.mihane.ui.screen.settings.RootSettingsScreen
import rinn.rei.mihane.ui.screen.settings.SettingsScreen
import rinn.rei.mihane.ui.screen.settings.ThemeSettingsScreen
import rinn.rei.mihane.ui.screen.settings.VpnSettingsScreen
import rinn.rei.mihane.ui.screen.settings.WifiPolicyScreen
import rinn.rei.mihane.ui.screen.subscription.SubscriptionAddScreen
import rinn.rei.mihane.ui.screen.subscription.SubscriptionAddUrlScreen
import rinn.rei.mihane.ui.screen.subscription.SubscriptionEditScreen
import rinn.rei.mihane.ui.screen.subscription.SubscriptionScreen
import rinn.rei.mihane.ui.theme.ThemeConfig
import rinn.rei.mihane.viewmodel.AppProxyViewModel
import rinn.rei.mihane.viewmodel.AutomationViewModel
import rinn.rei.mihane.viewmodel.BackupViewModel
import rinn.rei.mihane.viewmodel.ConnectionViewModel
import rinn.rei.mihane.viewmodel.DnsQueryViewModel
import rinn.rei.mihane.viewmodel.ExternalControlViewModel
import rinn.rei.mihane.viewmodel.HomeUiState
import rinn.rei.mihane.viewmodel.HomeViewModel
import rinn.rei.mihane.viewmodel.LogViewModel
import rinn.rei.mihane.viewmodel.MetaSettingsViewModel
import rinn.rei.mihane.viewmodel.NetworkSettingsViewModel
import rinn.rei.mihane.viewmodel.OverrideProfileViewModel
import rinn.rei.mihane.viewmodel.ProviderViewModel
import rinn.rei.mihane.viewmodel.ProxyViewModel
import rinn.rei.mihane.viewmodel.SubscriptionViewModel

// Route 是 @Serializable sealed 层级，直接多态编码整个栈；新增路由自动获得持久化能力，无需手工注册
private val NavBackStackSaver =
    Saver<SnapshotStateList<Route>, List<String>>(
        save = { backStack ->
            backStack
                .map { Json.encodeToString<Route>(it) }
                .ifEmpty { listOf(Json.encodeToString<Route>(Route.Main)) }
        },
        restore = { savedRoutes ->
            val restoredRoutes =
                savedRoutes.mapNotNull { value ->
                    runCatching { Json.decodeFromString<Route>(value) }.getOrNull()
                }
            mutableStateListOf<Route>().apply {
                if (restoredRoutes.isEmpty()) {
                    add(Route.Main)
                } else {
                    if (restoredRoutes.first() !is Route.Main) add(Route.Main)
                    addAll(restoredRoutes)
                }
            }
        },
    )

val LocalMainPagerState =
    staticCompositionLocalOf<MainPagerState> { error("LocalMainPagerState not provided") }

@Composable
fun AppNavigation(
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
    val backStack =
        rememberSaveable(saver = NavBackStackSaver) { mutableStateListOf<Route>(Route.Main) }
    val navigator = remember(backStack) { Navigator(backStack) }
    val pagerState = rememberPagerState(pageCount = { 4 })
    val mainPagerState = rememberMainPagerState(pagerState = pagerState, animatePageChanges = true)

    // 深链导入：回 Main 并把 pager 切到订阅 Tab 后直接压预填添加页（订阅管理是 pager Tab，无二级路由）
    LaunchedEffect(deepLinkImport) {
        if (deepLinkImport != null) {
            navigator.popUntil { key -> key is Route.Main }
            mainPagerState.animateToPage(2)
            navigator.push(
                Route.SubscriptionAddUrl(
                    initialUrl = deepLinkImport.url,
                    initialName = deepLinkImport.name,
                    initialIntervalMinutes = deepLinkImport.intervalMinutes,
                )
            )
            onDeepLinkImportConsumed()
        }
    }

    LaunchedEffect(mainPagerState.pagerState.currentPage) {
        mainPagerState.syncPage()
        // 切到代理 Tab 时刷新 mihomo 缓存的 history.delay；URL-Test 组的周期测试结果靠这里同步到 UI
        if (mainPagerState.pagerState.currentPage == 1) proxyViewModel?.loadProxies()
    }

    MainScreenBackHandler(mainPagerState, navigator)

    CompositionLocalProvider(
        LocalNavigator provides navigator,
        LocalMainPagerState provides mainPagerState,
    ) {
        NavDisplay(
            backStack = backStack,
            entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
            onBack = { navigator.pop() },
            transitionSpec = { settingsTransition(back = false) },
            popTransitionSpec = { settingsTransition(back = true) },
            predictivePopTransitionSpec = { _ -> settingsTransition(back = true) },
            entryProvider =
                entryProvider {
                    entry<Route.Main>() {
                        MainPage(
                            homeViewModel,
                            proxyViewModel,
                            subscriptionViewModel,
                            navigator,
                            mainPagerState,
                            bootStartManager,
                            themeConfig,
                            storage,
                            onHideTaskCardChange,
                            hasRootPermission,
                        )
                    }
                    entry<Route.SubscriptionAdd>() {
                        SubscriptionAddScreen(
                            viewModel = subscriptionViewModel,
                            onBack = { navigator.pop() },
                            onPickFile = {
                                filePicker?.pickYamlFile { result ->
                                    if (result != null && subscriptionViewModel != null) {
                                        subscriptionViewModel.addFromFile(
                                            fileName = result.fileName,
                                            content = result.content,
                                            onComplete = {
                                                navigator.popUntil { key -> key is Route.Main }
                                            },
                                        )
                                    }
                                }
                            },
                            onNavigateUrl = { navigator.push(Route.SubscriptionAddUrl()) },
                            onScanQR =
                                if (onScanQR != null) {
                                    {
                                        onScanQR { url ->
                                            if (url != null) {
                                                navigator.push(
                                                    Route.SubscriptionAddUrl(initialUrl = url)
                                                )
                                            }
                                        }
                                    }
                                } else null,
                        )
                    }
                    entry<Route.SubscriptionAddUrl>() { route ->
                        subscriptionViewModel?.let {
                            SubscriptionAddUrlScreen(
                                viewModel = it,
                                initialUrl = route.initialUrl,
                                initialName = route.initialName,
                                initialIntervalMinutes = route.initialIntervalMinutes,
                                onBack = { navigator.pop() },
                                onSaved = { navigator.popUntil { key -> key is Route.Main } },
                            )
                        }
                    }
                    entry<Route.SubscriptionEdit>() { route ->
                        subscriptionViewModel?.let {
                            SubscriptionEditScreen(
                                uuid = route.uuid,
                                viewModel = it,
                                onBack = { navigator.pop() },
                                onSaved = { navigator.pop() },
                                onNavigateOverrides = {
                                    navigator.push(Route.SubscriptionOverrides(route.uuid))
                                },
                            )
                        }
                    }
                    entry<Route.SubscriptionOverrides>() { route ->
                        if (subscriptionViewModel != null && overrideProfileViewModel != null) {
                            SubscriptionOverridesScreen(
                                subscriptionId = route.uuid,
                                subscriptionViewModel = subscriptionViewModel,
                                overrideViewModel = overrideProfileViewModel,
                                onBack = { navigator.pop() },
                            )
                        }
                    }
                    entry<Route.OverrideList>() {
                        overrideProfileViewModel?.let { vm ->
                            OverrideListScreen(
                                viewModel = vm,
                                onBack = { navigator.pop() },
                                onAdd = { navigator.push(Route.OverrideEdit()) },
                                onEdit = { id -> navigator.push(Route.OverrideEdit(id)) },
                                onEditFile = { id -> navigator.push(Route.OverrideFileEditor(id)) },
                            )
                        }
                    }
                    entry<Route.OverrideEdit>() { route ->
                        overrideProfileViewModel?.let { vm ->
                            OverrideEditScreen(
                                overrideId = route.id,
                                viewModel = vm,
                                onBack = { navigator.pop() },
                                onSaved = { navigator.pop() },
                                onPickFile = { callback -> filePicker?.pickYamlFile(callback) },
                            )
                        }
                    }
                    entry<Route.OverrideFileEditor>() { route ->
                        overrideProfileViewModel?.let { vm ->
                            OverrideFileEditorScreen(route.id, vm, onBack = { navigator.pop() })
                        }
                    }
                    entry<Route.Log>() {
                        logViewModel?.let {
                            LogScreen(viewModel = it, onBack = { navigator.pop() })
                        }
                    }
                    entry<Route.Provider>() {
                        providerViewModel?.let {
                            ProviderScreen(viewModel = it, onBack = { navigator.pop() })
                        }
                    }
                    entry<Route.Connection>() {
                        connectionViewModel?.let {
                            ConnectionScreen(viewModel = it, onBack = { navigator.pop() })
                        }
                    }
                    entry<Route.DnsQuery>() {
                        dnsQueryViewModel?.let {
                            DnsQueryScreen(viewModel = it, onBack = { navigator.pop() })
                        }
                    }
                    entry<Route.VpnSettings>() {
                        storage?.let {
                            VpnSettingsScreen(
                                storage = it,
                                isSystemProxySupported = true,
                                onBack = { navigator.pop() },
                            )
                        }
                    }
                    entry<Route.RootSettings>() {
                        storage?.let {
                            val homeState =
                                homeViewModel?.uiState?.collectAsStateWithLifecycle()?.value
                            RootSettingsScreen(
                                storage = it,
                                isProxyRunning =
                                    homeState?.isRunning == true || homeState?.isStarting == true,
                                onBack = { navigator.pop() },
                            )
                        }
                    }
                    entry<Route.NetworkSettings>() {
                        networkSettingsViewModel?.let {
                            NetworkSettingsScreen(viewModel = it, onBack = { navigator.pop() })
                        }
                    }
                    entry<Route.MetaSettings>() {
                        metaSettingsViewModel?.let {
                            MetaSettingsScreen(viewModel = it, onBack = { navigator.pop() })
                        }
                    }
                    entry<Route.AppProxy>() {
                        appProxyViewModel?.let {
                            AppProxyScreen(viewModel = it, onBack = { navigator.pop() })
                        }
                    }
                    entry<Route.WifiPolicy>() {
                        storage?.let {
                            WifiPolicyScreen(
                                storage = it,
                                controller = wifiPolicyController,
                                onRequestPermission = onRequestWifiPermission,
                                onBack = { navigator.pop() },
                            )
                        }
                    }
                    entry<Route.ThemeSettings>() {
                        storage?.let {
                            ThemeSettingsScreen(
                                storage = it,
                                themeConfig = themeConfig,
                                onThemeConfigChange = onThemeConfigChange,
                                onPredictiveBackChange = onPredictiveBackChange,
                                onBack = { navigator.pop() },
                            )
                        }
                    }
                    entry<Route.ExternalControl>() {
                        externalControlViewModel?.let {
                            ExternalControlScreen(viewModel = it, onBack = { navigator.pop() })
                        }
                    }
                    entry<Route.Automation>() {
                        automationViewModel?.let {
                            AutomationScreen(viewModel = it, onBack = { navigator.pop() })
                        }
                    }
                    entry<Route.BackupRestore>() {
                        if (backupViewModel != null && storage != null) {
                            BackupRestoreScreen(
                                viewModel = backupViewModel,
                                storage = storage,
                                filePicker = filePicker,
                                onRestartApp = onRestartApp,
                                onBack = { navigator.pop() },
                            )
                        }
                    }
                    entry<Route.FileManager>() {
                        FileManagerScreen(
                            subscriptionViewModel = subscriptionViewModel,
                            onBack = { navigator.pop() },
                            onOpenFile = { uuid, relPath ->
                                navigator.push(Route.FileManagerEditor(uuid, relPath))
                            },
                        )
                    }
                    entry<Route.FileManagerEditor>() { route ->
                        FileManagerEditorScreen(
                            uuid = route.uuid,
                            relativePath = route.relativePath,
                            subscriptionViewModel = subscriptionViewModel,
                            onBack = { navigator.pop() },
                        )
                    }
                    entry<Route.About>() {
                        val uriHandler = LocalUriHandler.current
                        AboutScreen(
                            onBack = { navigator.pop() },
                            mihomoVersion = mihomoVersion,
                            onOpenUrl = { url -> uriHandler.openUri(url) },
                        )
                    }
                },
        )
    }
}

@Composable
private fun MainPage(
    homeViewModel: HomeViewModel?,
    proxyViewModel: ProxyViewModel?,
    subscriptionViewModel: SubscriptionViewModel?,
    navigator: Navigator,
    mainPagerState: MainPagerState,
    bootStartManager: BootStartManager? = null,
    themeConfig: ThemeConfig = ThemeConfig(),
    storage: PlatformStorage? = null,
    onHideTaskCardChange: ((Boolean) -> Unit)? = null,
    hasRootPermission: Boolean = false,
) {
    val homeUiState = homeViewModel?.uiState?.collectAsStateWithLifecycle()?.value ?: HomeUiState()
    val selectedPage = mainPagerState.selectedPage
    // 页面主体：手机与宽屏两套外壳共用，仅传入不同的容器 modifier 与底部留白
    val pagerContent: @Composable (Modifier, Dp) -> Unit = { pagerModifier, bottomPadding ->
        HorizontalPager(
            modifier = pagerModifier,
            state = mainPagerState.pagerState,
            verticalAlignment = Alignment.Top,
        ) { page ->
            when (page) {
                0 ->
                    HomeScreen(
                        bottomPadding = bottomPadding,
                        uiState = homeUiState,
                        viewModel = homeViewModel,
                        onRestart = { homeViewModel?.restartProxy() },
                        onStop = { homeViewModel?.stopProxy() },
                        onReload = { homeViewModel?.reloadConfig() },
                        onTestLatency = { homeViewModel?.testLatency() },
                        onNavigateLog = { navigator.push(Route.Log) },
                        onNavigateProvider = { navigator.push(Route.Provider) },
                        onNavigateConnection = { navigator.push(Route.Connection) },
                        onNavigateDnsQuery = { navigator.push(Route.DnsQuery) },
                        onStartProxy = { homeViewModel?.startProxy() },
                        onSwitchMode = { homeViewModel?.switchMode(it) },
                        onSwitchTunStack = { homeViewModel?.switchTunStack(it) },
                    )

                1 -> ProxyScreen(bottomPadding = bottomPadding, viewModel = proxyViewModel)
                2 ->
                    subscriptionViewModel?.let {
                        SubscriptionScreen(
                            viewModel = it,
                            bottomPadding = bottomPadding,
                            onNavigateAdd = { navigator.push(Route.SubscriptionAdd) },
                            onNavigateEdit = { uuid ->
                                navigator.push(Route.SubscriptionEdit(uuid))
                            },
                            onActiveChanged = { homeViewModel?.onActiveSubscriptionChanged() },
                        )
                    }

                3 ->
                    SettingsScreen(
                        bottomPadding = bottomPadding,
                        onNavigateVpnSettings = { navigator.push(Route.VpnSettings) },
                        onNavigateRootSettings = { navigator.push(Route.RootSettings) },
                        onNavigateNetworkSettings = { navigator.push(Route.NetworkSettings) },
                        onNavigateMetaSettings = { navigator.push(Route.MetaSettings) },
                        onNavigateExternalControl = { navigator.push(Route.ExternalControl) },
                        onNavigateAutomation = { navigator.push(Route.Automation) },
                        onNavigateAppProxy = { navigator.push(Route.AppProxy) },
                        onNavigateWifiPolicy = { navigator.push(Route.WifiPolicy) },
                        onNavigateThemeSettings = { navigator.push(Route.ThemeSettings) },
                        onNavigateFileManager = { navigator.push(Route.FileManager) },
                        onNavigateOverrides = { navigator.push(Route.OverrideList) },
                        onNavigateBackup = { navigator.push(Route.BackupRestore) },
                        onNavigateAbout = { navigator.push(Route.About) },
                        bootStartManager = bootStartManager,
                        storage = storage,
                        onHideTaskCardChange = onHideTaskCardChange,
                        hasRootPermission = hasRootPermission,
                        isProxyRunning = homeUiState.isRunning || homeUiState.isStarting,
                    )
            }
        }
    }

    val labels =
        listOf(
                R.string.nav_home,
                R.string.nav_proxy,
                R.string.nav_subscription,
                R.string.nav_settings,
            )
            .map { stringResource(it) }
    val icons = listOf(AppIcons.Home, AppIcons.Tune, AppIcons.UploadCloud, AppIcons.Settings)
    val navigationSizeMotion = MaterialTheme.motionScheme.defaultSpatialSpec<IntSize>()
    val navigationFadeMotion = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        bottomBar = {
            Box(
                Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                HorizontalFloatingToolbar(
                    expanded = true,
                    modifier = Modifier.selectableGroup(),
                    expandedShadowElevation = 6.dp,
                    collapsedShadowElevation = 6.dp,
                ) {
                    labels.forEachIndexed { index, label ->
                        val selected = selectedPage == index
                        val itemModifier =
                            Modifier.semantics {
                                this.selected = selected
                                role = Role.Tab
                            }
                        AnimatedContent(
                            targetState = selected,
                            modifier = itemModifier,
                            label = "navigation-$index",
                            transitionSpec = {
                                (fadeIn(navigationFadeMotion) togetherWith
                                        fadeOut(navigationFadeMotion))
                                    .using(SizeTransform { _, _ -> navigationSizeMotion })
                            },
                        ) { isSelected ->
                            if (isSelected) {
                                FilledTonalButton(
                                    onClick = { mainPagerState.animateToPage(index) },
                                    modifier = Modifier,
                                    contentPadding = PaddingValues(horizontal = 12.dp),
                                ) {
                                    Icon(icons[index], null, Modifier.size(24.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            } else {
                                IconButton(
                                    onClick = { mainPagerState.animateToPage(index) },
                                    modifier = Modifier,
                                ) {
                                    Icon(icons[index], label)
                                }
                            }
                        }
                    }
                }
            }
        },
    ) { padding ->
        pagerContent(Modifier.fillMaxSize(), padding.calculateBottomPadding())
    }
}

// === MainPagerState ===

@Stable
class MainPagerState(
    val pagerState: PagerState,
    private val coroutineScope: CoroutineScope,
    private val animatePageChanges: Boolean,
) {
    var selectedPage by mutableIntStateOf(pagerState.currentPage)
        private set

    var isNavigating by mutableStateOf(false)
        private set

    private var navJob: Job? = null

    fun animateToPage(targetIndex: Int) {
        if (targetIndex == selectedPage) return

        navJob?.cancel()
        selectedPage = targetIndex
        isNavigating = true

        navJob =
            coroutineScope.launch {
                val myJob = coroutineContext.job
                try {
                    if (animatePageChanges) {
                        pagerState.animateScrollToPage(targetIndex)
                    } else {
                        pagerState.scrollToPage(targetIndex)
                    }
                } finally {
                    if (navJob == myJob) {
                        isNavigating = false
                        if (pagerState.currentPage != targetIndex) {
                            selectedPage = pagerState.currentPage
                        }
                    }
                }
            }
    }

    fun syncPage() {
        if (!isNavigating && selectedPage != pagerState.currentPage) {
            selectedPage = pagerState.currentPage
        }
    }
}

@Composable
fun rememberMainPagerState(
    pagerState: PagerState,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    animatePageChanges: Boolean = true,
): MainPagerState =
    remember(pagerState, coroutineScope, animatePageChanges) {
        MainPagerState(pagerState, coroutineScope, animatePageChanges)
    }

// === 返回键处理 ===

@Composable
private fun MainScreenBackHandler(mainState: MainPagerState, navigator: Navigator) {
    val isPagerBackHandlerEnabled by remember {
        derivedStateOf {
            navigator.current() is Route.Main &&
                navigator.backStackSize() == 1 &&
                mainState.selectedPage != 0
        }
    }

    val navEventState = rememberNavigationEventState(NavigationEventInfo.None)

    NavigationBackHandler(
        state = navEventState,
        isBackEnabled = isPagerBackHandlerEnabled,
        onBackCompleted = { mainState.animateToPage(0) },
    )
}
