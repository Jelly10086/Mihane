package rinn.rei.mihane.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberContainedSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.uwuaosp.compose.widget.preference.PreferenceRow
import org.uwuaosp.compose.widget.preference.RadioPreferenceRow
import org.uwuaosp.compose.widget.preference.SettingsSectionHeader
import org.uwuaosp.compose.widget.scaffold.SettingsToolbarActionButton
import rinn.rei.mihane.R
import rinn.rei.mihane.ui.component.AppIcons
import rinn.rei.mihane.ui.component.LocalPreferencePosition
import rinn.rei.mihane.ui.component.SearchPageScaffold
import rinn.rei.mihane.ui.component.material.AnchoredMenu
import rinn.rei.mihane.ui.component.material.ChoiceMenuItem
import rinn.rei.mihane.ui.component.material.RefreshBox
import rinn.rei.mihane.ui.component.material.SelectionCheckbox
import rinn.rei.mihane.ui.platform.AppIcon
import rinn.rei.mihane.ui.platform.getPlainText
import rinn.rei.mihane.ui.platform.setPlainText
import rinn.rei.mihane.ui.util.horizontalCutoutPadding
import rinn.rei.mihane.ui.util.readableContentPadding
import rinn.rei.mihane.viewmodel.AppProxyMode
import rinn.rei.mihane.viewmodel.AppProxyViewModel

@Composable
fun AppProxyScreen(viewModel: AppProxyViewModel, onBack: () -> Unit = {}) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showPopup = remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val clipboard = LocalClipboard.current
    val clipboardScope = rememberCoroutineScope()

    val appliedMsg = stringResource(R.string.app_proxy_applied)
    val searchField = rememberTextFieldState()
    val searchBarState = rememberContainedSearchBarState()

    // 搜索过滤（直接传入 searchText，避免组合期间写入 ViewModel 状态）
    val searchText by remember { derivedStateOf { searchField.text.toString() } }

    // 同步搜索词到 ViewModel（selectAll/invertSelection 等操作需要）
    LaunchedEffect(searchText) { viewModel.setSearchQuery(searchText) }

    // 使用 ViewModel 缓存的 filteredAppsFlow（不依赖 selectedPackages，勾选不引起排序重算）
    val filteredApps by viewModel.filteredAppsFlow.collectAsStateWithLifecycle()

    SearchPageScaffold(
        title = stringResource(R.string.app_proxy_title),
        showBackButton = true,
        onNavigateUp = onBack,
        actions = {
            // 仅在非「允许所有应用」模式下显示菜单
            if (uiState.mode != AppProxyMode.AllowAll) {
                SettingsToolbarActionButton(
                    onClick = { showPopup.value = true },
                    imageVector = AppIcons.MoreCircle,
                    contentDescription = stringResource(R.string.common_more),
                )

                // 顶栏下拉菜单
                AnchoredMenu(
                    show = showPopup.value,
                    onDismissRequest = { showPopup.value = false },
                ) {
                    Column {
                        ChoiceMenuItem(
                            text = stringResource(R.string.app_proxy_select_all),
                            optionSize = 6,
                            isSelected = false,
                            index = 0,
                            onSelectedIndexChange = {
                                viewModel.selectAll()
                                showPopup.value = false
                            },
                        )
                        ChoiceMenuItem(
                            text = stringResource(R.string.app_proxy_deselect_all),
                            optionSize = 6,
                            isSelected = false,
                            index = 1,
                            onSelectedIndexChange = {
                                viewModel.deselectAll()
                                showPopup.value = false
                            },
                        )
                        ChoiceMenuItem(
                            text = stringResource(R.string.app_proxy_invert),
                            optionSize = 6,
                            isSelected = false,
                            index = 2,
                            onSelectedIndexChange = {
                                viewModel.invertSelection()
                                showPopup.value = false
                            },
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                        )
                        ChoiceMenuItem(
                            text =
                                if (uiState.showSystemApps)
                                    stringResource(R.string.app_proxy_hide_system)
                                else stringResource(R.string.app_proxy_show_system),
                            optionSize = 6,
                            isSelected = uiState.showSystemApps,
                            index = 3,
                            onSelectedIndexChange = {
                                viewModel.setShowSystemApps(!uiState.showSystemApps)
                                showPopup.value = false
                            },
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                        )
                        ChoiceMenuItem(
                            text = stringResource(R.string.app_proxy_import),
                            optionSize = 6,
                            isSelected = false,
                            index = 4,
                            onSelectedIndexChange = {
                                clipboardScope.launch {
                                    val text = clipboard.getPlainText()
                                    if (!text.isNullOrBlank()) {
                                        viewModel.importPackages(text)
                                    }
                                }
                                showPopup.value = false
                            },
                        )
                        ChoiceMenuItem(
                            text = stringResource(R.string.app_proxy_export),
                            optionSize = 6,
                            isSelected = false,
                            index = 5,
                            onSelectedIndexChange = {
                                val exported = viewModel.exportPackages()
                                if (exported.isNotEmpty()) {
                                    clipboardScope.launch { clipboard.setPlainText(exported) }
                                }
                                showPopup.value = false
                            },
                        )
                    }
                }
            }
        },
        query = searchField,
        searchBarState = searchBarState,
        placeholder = stringResource(R.string.app_proxy_search),
        results = {
            // 搜索结果列表
            LazyColumn(modifier = Modifier.fillMaxSize().imePadding()) {
                item { Spacer(Modifier.height(6.dp)) }

                if (filteredApps.isEmpty()) {
                    item(key = "search_empty") {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = stringResource(R.string.app_proxy_no_match),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    items(
                        items = filteredApps,
                        key = { it.packageName },
                        contentType = { "app" },
                    ) { app ->
                        AppItem(
                            appName = app.appName,
                            packageName = app.packageName,
                            isSelected = app.packageName in uiState.selectedPackages,
                            onToggle = { viewModel.toggleApp(app.packageName) },
                        )
                    }
                }

                item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
            }
        },
    ) { innerPadding ->
        val appList: @Composable () -> Unit = {
            LazyColumn(
                modifier = Modifier.fillMaxSize().horizontalCutoutPadding(),
                contentPadding = readableContentPadding(innerPadding),
            ) {
                item(key = "mode_card") {
                    Column(
                        modifier =
                            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(
                                org.uwuaosp.compose.framework.theme.SettingsDimension.groupSpacing
                            ),
                    ) {
                        RadioPreferenceRow(
                            title = stringResource(R.string.app_proxy_allow_all),
                            summary = stringResource(R.string.app_proxy_allow_all_summary),
                            selected = uiState.mode == AppProxyMode.AllowAll,
                            onSelect = { viewModel.setMode(AppProxyMode.AllowAll) },
                            index = 0,
                            itemCount = 3,
                        )
                        RadioPreferenceRow(
                            title = stringResource(R.string.app_proxy_allow_selected),
                            summary = stringResource(R.string.app_proxy_allow_selected_summary),
                            selected = uiState.mode == AppProxyMode.AllowSelected,
                            onSelect = { viewModel.setMode(AppProxyMode.AllowSelected) },
                            index = 1,
                            itemCount = 3,
                        )
                        RadioPreferenceRow(
                            title = stringResource(R.string.app_proxy_deny_selected),
                            summary = stringResource(R.string.app_proxy_deny_selected_summary),
                            selected = uiState.mode == AppProxyMode.DenySelected,
                            onSelect = { viewModel.setMode(AppProxyMode.DenySelected) },
                            index = 2,
                            itemCount = 3,
                        )
                    }
                }

                // 应用列表（AllowAll 模式下不显示）
                if (uiState.mode != AppProxyMode.AllowAll) {
                    if (uiState.isLoading) {
                        // 加载中 → 进度指示器
                        item(key = "loading") {
                            Box(
                                modifier = Modifier.fillParentMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                LoadingIndicator()
                            }
                        }
                    } else {
                        item(key = "apps_title") {
                            val selectedCount =
                                remember(filteredApps, uiState.selectedPackages) {
                                    filteredApps.count {
                                        it.packageName in uiState.selectedPackages
                                    }
                                }
                            SettingsSectionHeader(
                                title =
                                    stringResource(
                                        R.string.app_proxy_app_list,
                                        selectedCount,
                                        filteredApps.size,
                                    )
                            )
                        }
                        items(
                            items = filteredApps,
                            key = { it.packageName },
                            contentType = { "app" },
                        ) { app ->
                            AppItem(
                                appName = app.appName,
                                packageName = app.packageName,
                                isSelected = app.packageName in uiState.selectedPackages,
                                onToggle = { viewModel.toggleApp(app.packageName) },
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
            }
        }

        if (uiState.mode == AppProxyMode.AllowAll) {
            appList()
        } else {
            RefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = viewModel::refreshApps,
                modifier = Modifier.fillMaxSize(),
                content = appList,
            )
        }
    }
}

@Composable
private fun AppItem(
    appName: String,
    packageName: String,
    isSelected: Boolean,
    onToggle: () -> Unit,
) {
    Column(
        modifier =
            Modifier.padding(
                    horizontal = org.uwuaosp.compose.framework.theme.SettingsDimension.pagePadding
                )
                .padding(bottom = 12.dp)
    ) {
        PreferenceRow(
            title = appName,
            summary = packageName,
            iconContent = {
                AppIcon(
                    packageName = packageName,
                    modifier = Modifier.padding(end = 6.dp),
                    size = 40.dp,
                )
            },
            trailingContent = {
                SelectionCheckbox(
                    state = if (isSelected) ToggleableState.On else ToggleableState.Off,
                    onClick = null,
                )
            },
            onClick = onToggle,
            index = LocalPreferencePosition.current.first,
            itemCount = LocalPreferencePosition.current.second,
        )
    }
}
