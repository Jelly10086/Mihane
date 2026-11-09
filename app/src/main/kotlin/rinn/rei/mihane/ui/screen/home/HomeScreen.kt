package rinn.rei.mihane.ui.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.uwuaosp.compose.framework.theme.SettingsDimension
import org.uwuaosp.compose.widget.scaffold.SettingsCollapsingAppBarScaffold
import org.uwuaosp.compose.widget.ui.SettingsContent
import rinn.rei.mihane.R
import rinn.rei.mihane.util.formatUptime
import rinn.rei.mihane.viewmodel.HomeUiState
import rinn.rei.mihane.viewmodel.HomeViewModel
import rinn.rei.mihane.viewmodel.MemorySnapshot
import rinn.rei.mihane.viewmodel.SpeedSnapshot
import rinn.rei.mihane.viewmodel.SystemInfoSnapshot

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 0.dp,
    uiState: HomeUiState = HomeUiState(),
    viewModel: HomeViewModel? = null,
    onRestart: () -> Unit = {},
    onStop: () -> Unit = {},
    onReload: () -> Unit = {},
    onTestLatency: () -> Unit = {},
    onNavigateLog: () -> Unit = {},
    onNavigateProvider: () -> Unit = {},
    onNavigateConnection: () -> Unit = {},
    onNavigateDnsQuery: () -> Unit = {},
    onStartProxy: () -> Unit = {},
    onSwitchMode: (String) -> Unit = {},
    onSwitchTunStack: (String) -> Unit = {},
) {

    // 高频字段拆成独立 Flow：1Hz 推送仍会重组 HomeScreen 本体与 LazyColumn 的 content lambda，
    // 但只有拿到新值的那个 Section 参数变了，其余全部 skip，实际重绘收在对应 Section 内
    val speed by
        (viewModel?.speedState?.collectAsStateWithLifecycle()
            ?: remember { mutableStateOf(SpeedSnapshot()) })
    val memory by
        (viewModel?.memoryState?.collectAsStateWithLifecycle()
            ?: remember { mutableStateOf(MemorySnapshot()) })
    val systemInfo by
        (viewModel?.systemInfoState?.collectAsStateWithLifecycle()
            ?: remember { mutableStateOf(SystemInfoSnapshot()) })
    val uptimeSeconds by
        (viewModel?.uptimeState?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(-1L) })
    val uptime = formatUptime(uptimeSeconds)

    var showSubscriptionTraffic by rememberSaveable { mutableStateOf(false) }
    var showSpeedDetail by rememberSaveable { mutableStateOf(false) }
    val topConnectionRates by
        (viewModel?.topConnectionRates?.collectAsStateWithLifecycle()
            ?: remember { mutableStateOf(null) })

    // WS 订阅只在详情打开期间存续，理由见 startConnectionRateTracking
    if (showSpeedDetail) {
        DisposableEffect(viewModel) {
            viewModel?.startConnectionRateTracking()
            onDispose { viewModel?.stopConnectionRateTracking() }
        }
    }
    // 代理停止后不会再有快照可差分，速率被重置回「未就绪」，留着详情只会无限转圈
    LaunchedEffect(uiState.isRunning) { if (!uiState.isRunning) showSpeedDetail = false }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val useSideGutter =
            LocalConfiguration.current.smallestScreenWidthDp >= 600 && maxWidth >= 600.dp
        val compactHeight = maxHeight < 480.dp
        val sideGutter = if (useSideGutter) 88.dp else 0.dp
        // SettingsContent centers its capped column inside the remaining width.
        val trailingContentSpace =
            ((maxWidth - sideGutter - SettingsDimension.contentMaxWidth) / 2).coerceAtLeast(0.dp)
        val tabletMenuBottom =
            (bottomPadding - FloatingToolbarDefaults.ContainerSize + 16.dp).coerceAtLeast(0.dp)
        val powerMenu: @Composable (Modifier) -> Unit = { menuModifier ->
            ProxyPowerMenu(
                onRestart,
                onStop,
                onReload,
                onStartProxy,
                uiState.isRunning,
                uiState.isStarting,
                uiState.isStopping,
                menuModifier,
            )
        }
        SettingsContent(Modifier.fillMaxSize().padding(end = sideGutter)) {
            Box(Modifier.fillMaxSize()) {
                SettingsCollapsingAppBarScaffold(
                    title = stringResource(R.string.home_title),
                    startCollapsed = compactHeight,
                    modifier = Modifier.fillMaxSize(),
                ) { innerPadding ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding =
                            PaddingValues(
                                top = innerPadding.calculateTopPadding(),
                                bottom = bottomPadding + 88.dp,
                                start = 0.dp,
                                end = 0.dp,
                            ),
                    ) {
                        statusSection(
                            state = uiState,
                            uptime = uptime,
                            onSwitchMode = onSwitchMode,
                            onSwitchTunStack = onSwitchTunStack,
                            isTabletLayout = useSideGutter,
                        )
                        overviewCardsSection(
                            speed = speed,
                            state = uiState,
                            onSpeedClick = { showSpeedDetail = true },
                            onSubscriptionClick = {
                                showSubscriptionTraffic = true
                                viewModel?.refreshProviderTraffic()
                            },
                        )
                        systemCardsSection(memory = memory, systemInfo = systemInfo)
                        latencySection(uiState, onTestLatency)
                        quickEntriesSection(
                            isRunning = uiState.isRunning,
                            onNavigateLog = onNavigateLog,
                            onNavigateProvider = onNavigateProvider,
                            onNavigateConnection = onNavigateConnection,
                            onNavigateDnsQuery = onNavigateDnsQuery,
                        )
                    }
                }

                if (!useSideGutter) {
                    powerMenu(
                        Modifier.align(Alignment.BottomEnd)
                            .padding(end = HomeContentPadding - 16.dp, bottom = bottomPadding)
                    )
                }
            }
        }
        if (useSideGutter) {
            powerMenu(
                Modifier.align(Alignment.BottomEnd)
                    .padding(
                        end = trailingContentSpace + HomeContentPadding - 16.dp,
                        bottom = tabletMenuBottom,
                    )
            )
        }
    }

    SpeedDetailSheet(
        show = showSpeedDetail,
        rates = topConnectionRates,
        onDismiss = { showSpeedDetail = false },
    )

    SubscriptionTrafficDialog(
        show = showSubscriptionTraffic,
        providers = uiState.providerTraffic,
        isLoading = uiState.isProviderTrafficLoading,
        loadFailed = uiState.providerTrafficLoadFailed,
        onUpdateAll = { viewModel?.updateAllProviders() },
        onDismiss = { showSubscriptionTraffic = false },
    )
}
