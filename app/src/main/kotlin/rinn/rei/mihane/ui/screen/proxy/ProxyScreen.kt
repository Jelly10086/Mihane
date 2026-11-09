package rinn.rei.mihane.ui.screen.proxy

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.FloatState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.roundToInt
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.uwuaosp.compose.widget.scaffold.SettingsCollapsingAppBarScaffold
import org.uwuaosp.compose.widget.scaffold.SettingsToolbarActionButton
import rinn.rei.mihane.R
import rinn.rei.mihane.ui.component.AppIcons
import rinn.rei.mihane.ui.component.CardSegment
import rinn.rei.mihane.ui.component.material.AnchoredMenu
import rinn.rei.mihane.ui.component.material.ChoiceMenuItem
import rinn.rei.mihane.ui.component.material.shapeBackground
import rinn.rei.mihane.ui.component.material.shapeClip
import rinn.rei.mihane.ui.platform.IconLoader
import rinn.rei.mihane.ui.theme.LocalPlatformDensity
import rinn.rei.mihane.ui.theme.StatusColors
import rinn.rei.mihane.ui.util.WideContentBox
import rinn.rei.mihane.viewmodel.ProxyGroupUi
import rinn.rei.mihane.viewmodel.ProxyUiState
import rinn.rei.mihane.viewmodel.ProxyViewModel

private class GroupRowsCacheEntry(
    val all: ImmutableList<String>,
    val delays: ImmutableMap<String, Int>,
    val sortOption: Int,
    val rows: ImmutableList<ImmutableList<String>>,
)

private val SortResIds =
    listOf(R.string.proxy_sort_default, R.string.proxy_sort_name, R.string.proxy_sort_delay)

@Composable
fun ProxyScreen(
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 0.dp,
    viewModel: ProxyViewModel? = null,
) {
    val uiState = viewModel?.uiState?.collectAsStateWithLifecycle()?.value ?: ProxyUiState()
    val sortOption = viewModel?.sortOption?.collectAsStateWithLifecycle()?.value ?: 0
    val singleColumn = viewModel?.singleColumn?.collectAsStateWithLifecycle()?.value == true
    val showGroupIcons = viewModel?.showGroupIcons?.collectAsStateWithLifecycle()?.value ?: true
    // 0 = 行内两列并排，1 = 行内竖排
    val singleColumnProgress =
        animateFloatAsState(
            targetValue = if (singleColumn) 1f else 0f,
            animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
            label = "singleColumnProgress",
        )
    val groupMotion = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val groups = uiState.groups
    val containerSize = LocalWindowInfo.current.containerSize
    val density = LocalPlatformDensity.current ?: LocalDensity.current
    val maxAnimRows =
        remember(containerSize.height, density) {
            val windowHeightDp = with(density) { containerSize.height.toDp() }
            // 覆盖 1.8 倍视口高度作为滑动冗余缓冲，防展开中途手势下滑露底
            ((windowHeightDp * 1.8f / 68.dp).toInt()).coerceIn(16, 36)
        }
    val listState = rememberLazyListState()

    val modeHintRes =
        when (uiState.mode) {
            ProxyViewModel.MODE_DIRECT -> R.string.proxy_mode_direct_hint
            ProxyViewModel.MODE_GLOBAL -> R.string.proxy_mode_global_hint
            else -> null
        }

    val showPopup = remember { mutableStateOf(false) }
    val showSortPopup = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // 节点行动态增删，展开态须存屏幕级可保存列表
    val expandedGroups =
        rememberSaveable(
            saver = listSaver(save = { it.toList() }, restore = { it.toMutableStateList() })
        ) {
            mutableStateListOf<String>()
        }

    // retainedGroups 在收缩动画期间保住 item 防脱落穿插；普通 Map 靠随后状态写入驱动重测
    val expandProgress = remember { mutableMapOf<String, MutableFloatState>() }
    val animatingGroups = remember { mutableStateListOf<String>() }
    val retainedGroups = remember { mutableStateListOf<String>() }
    val expandJobs = remember { mutableMapOf<String, Job>() }

    fun toggleGroup(name: String) {
        val progress =
            expandProgress.getOrPut(name) {
                mutableFloatStateOf(if (name in expandedGroups) 1f else 0f)
            }
        val expanding = name !in expandedGroups
        expandJobs[name]?.cancel()
        if (expanding) {
            retainedGroups.remove(name)
            expandedGroups.add(name)
        } else {
            if (name !in retainedGroups) retainedGroups.add(name)
        }
        if (name !in animatingGroups) animatingGroups.add(name)
        expandJobs[name] =
            coroutineScope.launch {
                animate(
                    initialValue = progress.floatValue,
                    targetValue = if (expanding) 1f else 0f,
                    animationSpec = groupMotion,
                ) { value, _ ->
                    progress.floatValue = value
                }
                // 清理交给接手的新动画
                if (!expanding) {
                    expandedGroups.remove(name)
                    retainedGroups.remove(name)
                }
                animatingGroups.remove(name)
            }
    }

    // 手势介入滑动时立即收敛动画至终态，释放全量节点
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collect { isScrolling ->
                if (isScrolling && animatingGroups.isNotEmpty()) {
                    animatingGroups.toList().forEach { name ->
                        val isExpanding = name in expandedGroups
                        expandJobs[name]?.cancel()
                        expandProgress[name]?.floatValue = if (isExpanding) 1f else 0f
                        if (!isExpanding) {
                            expandedGroups.remove(name)
                            retainedGroups.remove(name)
                        }
                        animatingGroups.remove(name)
                    }
                }
            }
    }

    // 分组缓存排序分块，防单节点延迟变动触发全量重排
    val rowsCache = remember { mutableMapOf<String, GroupRowsCacheEntry>() }

    fun getGroupRows(group: ProxyGroupUi, option: Int): ImmutableList<ImmutableList<String>> {
        val cached = rowsCache[group.name]
        if (
            cached != null &&
                cached.all === group.all &&
                cached.delays === group.delays &&
                cached.sortOption == option
        ) {
            return cached.rows
        }
        val sorted = sortNodes(group.all, group.delays, option)
        val chunked = sorted.chunked(2).map { it.toPersistentList() }.toPersistentList()
        rowsCache[group.name] = GroupRowsCacheEntry(group.all, group.delays, option, chunked)
        return chunked
    }

    SettingsCollapsingAppBarScaffold(
        title = stringResource(R.string.proxy_title),
        modifier = modifier,
        actions = {
            if (groups.isNotEmpty()) {
                Box {
                    SettingsToolbarActionButton(
                        onClick = { showSortPopup.value = true },
                        imageVector =
                            when (sortOption / 2) {
                                1 -> AppIcons.SortName
                                2 -> AppIcons.Clock
                                else -> AppIcons.Sort
                            },
                        contentDescription = stringResource(R.string.proxy_sort_title),
                    )

                    AnchoredMenu(
                        show = showSortPopup.value,
                        onDismissRequest = { showSortPopup.value = false },
                    ) {
                        Column {
                            val sortResIds = SortResIds
                            val currentKey = sortOption / 2
                            val isReverse = sortOption % 2 != 0
                            val groupSize = sortResIds.size + 1

                            sortResIds.forEachIndexed { index, resId ->
                                ChoiceMenuItem(
                                    text = stringResource(resId),
                                    optionSize = groupSize,
                                    isSelected = currentKey == index,
                                    index = index,
                                    onSelectedIndexChange = {
                                        viewModel?.updateSortOption(
                                            index * 2 + if (isReverse) 1 else 0
                                        )
                                        showSortPopup.value = false
                                    },
                                )
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                            )
                            ChoiceMenuItem(
                                text = stringResource(R.string.proxy_sort_reverse),
                                optionSize = groupSize,
                                isSelected = isReverse,
                                index = sortResIds.size,
                                onSelectedIndexChange = {
                                    viewModel?.updateSortOption(
                                        currentKey * 2 + if (!isReverse) 1 else 0
                                    )
                                    showSortPopup.value = false
                                },
                            )
                        }
                    }
                }

                Box {
                    SettingsToolbarActionButton(
                        onClick = { showPopup.value = true },
                        imageVector = AppIcons.MoreCircle,
                        contentDescription = stringResource(R.string.common_more),
                    )

                    AnchoredMenu(
                        show = showPopup.value,
                        onDismissRequest = { showPopup.value = false },
                    ) {
                        Column {
                            ChoiceMenuItem(
                                text = stringResource(R.string.proxy_single_column),
                                optionSize = 2,
                                isSelected = singleColumn,
                                index = 0,
                                onSelectedIndexChange = {
                                    viewModel?.updateSingleColumn(!singleColumn)
                                    showPopup.value = false
                                },
                            )
                            ChoiceMenuItem(
                                text = stringResource(R.string.proxy_show_group_icons),
                                optionSize = 2,
                                isSelected = showGroupIcons,
                                index = 1,
                                onSelectedIndexChange = {
                                    viewModel?.updateShowGroupIcons(!showGroupIcons)
                                    showPopup.value = false
                                },
                            )
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        WideContentBox(modifier = Modifier) { sidePadding ->
            if (groups.isEmpty()) {
                Column(
                    modifier =
                        Modifier.fillMaxSize()
                            .padding(
                                top = innerPadding.calculateTopPadding(),
                                bottom = bottomPadding,
                            ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(R.string.proxy_no_groups),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.proxy_start_first),
                        modifier = Modifier.padding(top = 6.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                val onTestNodeDelay: (String) -> Unit =
                    remember(viewModel) { { nodeName -> viewModel?.testNodeDelay(nodeName) } }
                val onSelectProxy: (String, String) -> Unit =
                    remember(viewModel) {
                        { groupName, proxyName -> viewModel?.selectProxy(groupName, proxyName) }
                    }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding =
                        PaddingValues(
                            top = innerPadding.calculateTopPadding(),
                            bottom = bottomPadding,
                            start = sidePadding,
                            end = sidePadding,
                        ),
                ) {
                    if (modeHintRes != null) {
                        item(key = "mode_hint", contentType = "proxy_mode_hint") {
                            CardSegment(
                                isFirst = true,
                                isLast = true,
                                modifier = Modifier.animateItem(placementSpec = null),
                                outerTopPadding = 12.dp,
                            ) {
                                Text(
                                    text = stringResource(modeHintRes),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }

                    // 展平为独立 item，仅组合可见行
                    groups.forEach { group ->
                        val isExpanded = group.name in expandedGroups
                        val rowsPresent = isExpanded || group.name in retainedGroups
                        val isAnimating = group.name in animatingGroups
                        val allRows =
                            if (rowsPresent) {
                                getGroupRows(group, sortOption)
                            } else {
                                persistentListOf()
                            }
                        // 动画期间限制最大渲染行数防卡死，收尾后恢复全量虚拟化
                        val rows =
                            if (isAnimating && allRows.size > maxAnimRows) {
                                allRows.take(maxAnimRows)
                            } else {
                                allRows
                            }
                        val groupProgress = expandProgress[group.name]
                        val currentFraction =
                            groupProgress?.floatValue ?: if (isExpanded) 1f else 0f

                        item(key = "group:${group.name}", contentType = "proxy_group_header") {
                            CardSegment(
                                isFirst = true,
                                isLast = currentFraction <= 0.01f,
                                modifier = Modifier.animateItem(placementSpec = null),
                                outerTopPadding = 12.dp,
                            ) {
                                ProxyGroupHeader(
                                    group = group,
                                    showIcon = showGroupIcons,
                                    isExpanded = isExpanded,
                                    expandProgress = groupProgress,
                                    isTesting = group.name in uiState.testingGroups,
                                    onTestDelay = { viewModel?.testGroupDelay(group.name) },
                                    onUnfix = { viewModel?.unfixProxy(group.name) },
                                    onToggle = { toggleGroup(group.name) },
                                )
                            }
                        }

                        if (rows.isNotEmpty()) {
                            val lastRowIndex = rows.lastIndex
                            val rowCount = rows.size
                            val groupExpandProgress = expandProgress[group.name]
                            val onSelectThisGroup: (String) -> Unit = { proxyName ->
                                onSelectProxy(group.name, proxyName)
                            }

                            // 动态计算当前露出的最底行，赋予 16dp 底圆角
                            val activeBottomIndex =
                                when {
                                    currentFraction >= 0.99f -> lastRowIndex
                                    currentFraction <= 0.01f -> -1
                                    else ->
                                        (rowCount * currentFraction)
                                            .toInt()
                                            .coerceIn(0, lastRowIndex)
                                }

                            rows.forEachIndexed { rowIndex, row ->
                                val isCurrentBottom = rowIndex == activeBottomIndex

                                item(
                                    key = "nodes:${group.name}:$rowIndex",
                                    contentType = "proxy_node_row",
                                ) {
                                    Column(
                                        modifier =
                                            Modifier.animateItem(placementSpec = null)
                                                .clipToBounds()
                                                .padding(
                                                    horizontal =
                                                        org.uwuaosp.compose.framework.theme
                                                            .SettingsDimension
                                                            .pagePadding
                                                )
                                    ) {
                                        ProxyNodeRow(
                                            row = row,
                                            group = group,
                                            singleColumnProgress = singleColumnProgress,
                                            expandProgress = groupExpandProgress,
                                            rowIndex = rowIndex,
                                            rowCount = rowCount,
                                            testingNodes = uiState.testingNodes,
                                            onTestNodeDelay = onTestNodeDelay,
                                            onSelect = onSelectThisGroup,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item(key = "bottom_spacer") { Spacer(Modifier.padding(bottom = 12.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ProxyGroupHeader(
    group: ProxyGroupUi,
    showIcon: Boolean,
    isExpanded: Boolean,
    expandProgress: FloatState? = null,
    isTesting: Boolean,
    onTestDelay: () -> Unit,
    onUnfix: () -> Unit,
    onToggle: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current

    Row(
        modifier =
            Modifier.fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggle()
                    },
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedVisibility(
            visible = showIcon,
            enter =
                slideInHorizontally(MaterialTheme.motionScheme.defaultSpatialSpec()) { -it } +
                    expandHorizontally(
                        MaterialTheme.motionScheme.defaultSpatialSpec(),
                        expandFrom = Alignment.Start,
                    ),
            exit =
                slideOutHorizontally(MaterialTheme.motionScheme.defaultSpatialSpec()) { -it } +
                    shrinkHorizontally(
                        MaterialTheme.motionScheme.defaultSpatialSpec(),
                        shrinkTowards = Alignment.Start,
                    ),
            label = "groupIconVisibility",
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GroupIcon(icon = group.icon, name = group.name)
                Spacer(Modifier.width(12.dp))
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = group.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (group.now.isNotEmpty()) {
                Text(
                    text = group.now,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        val nowDelay = group.delays[group.now]
        if (nowDelay != null) {
            val timeoutText = stringResource(R.string.proxy_timeout)
            val delayText = if (nowDelay < 0) timeoutText else "${nowDelay}ms"
            Text(
                text = delayText,
                style = MaterialTheme.typography.bodySmall,
                color = StatusColors.delay(nowDelay),
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = "${group.all.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        // 固定是组级属性，放组头才能在收起态下操作
        if (group.isFixed) {
            Spacer(Modifier.width(8.dp))
            SettingsToolbarActionButton(
                onClick = onUnfix,
                modifier = Modifier,
                imageVector = AppIcons.Unpin,
                contentDescription = stringResource(R.string.proxy_unfix),
            )
        }
        Spacer(Modifier.width(8.dp))
        FilledTonalIconButton(
            onClick = onTestDelay,
            enabled = !isTesting,
            modifier = Modifier.size(24.dp),
            shapes = androidx.compose.material3.IconButtonDefaults.shapes(),
        ) {
            if (isTesting) {
                LoadingIndicator(modifier = Modifier.size(14.dp))
            } else {
                Icon(
                    imageVector = AppIcons.Refresh,
                    contentDescription = stringResource(R.string.proxy_test_group_delay),
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        val layoutDirection = LocalLayoutDirection.current
        Image(
            imageVector = AppIcons.ArrowRight,
            contentDescription = null,
            modifier =
                Modifier.size(15.dp).graphicsLayer {
                    scaleX = if (layoutDirection == LayoutDirection.Rtl) -1f else 1f
                    val p = expandProgress?.floatValue ?: if (isExpanded) 1f else 0f
                    rotationZ = lerp(0f, 90f, p)
                },
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
        )
    }
}

@Composable
private fun GroupIcon(icon: String, name: String) {
    if (icon.isNotEmpty()) {
        val iconVersion by IconLoader.iconVersion.collectAsStateWithLifecycle()
        var bitmap by remember(icon) { mutableStateOf<ImageBitmap?>(null) }

        LaunchedEffect(icon, iconVersion) {
            if (bitmap == null) {
                bitmap = IconLoader.loadIcon(icon)
            }
        }
        val current = bitmap
        if (current != null) {
            Image(
                bitmap = current,
                contentDescription = name,
                modifier = Modifier.size(30.dp).shapeClip(8.dp),
            )
        } else {
            DefaultGroupIcon(name)
        }
    } else {
        DefaultGroupIcon(name)
    }
}

@Composable
private fun DefaultGroupIcon(name: String) {
    Box(
        modifier =
            Modifier.size(30.dp)
                .shapeBackground(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text =
                if (name.isEmpty()) ""
                else {
                    val first = name[0]
                    if (first.isHighSurrogate() && name.length > 1 && name[1].isLowSurrogate()) {
                        name.substring(0, 2)
                    } else {
                        first.toString()
                    }
                },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary.copy(0.8f),
        )
    }
}

private val NodeRowHorizontalGap = 8.dp
private val NodeRowVerticalGap = 2.dp
private val NodeRowBottomPadding = 2.dp

// 顶底距由行内 Layout 承担，否则高度收缩后残留段底色
private val NodeRowTopPadding = 12.dp

// 恒定分行，单双列由 Layout 连续插值，保证同 item 内平滑过渡
@Composable
private fun ProxyNodeRow(
    row: ImmutableList<String>,
    group: ProxyGroupUi,
    singleColumnProgress: State<Float>,
    expandProgress: FloatState?,
    rowIndex: Int,
    rowCount: Int,
    testingNodes: ImmutableSet<String> = persistentSetOf(),
    onTestNodeDelay: (String) -> Unit = {},
    onSelect: (String) -> Unit,
) {
    Layout(
        content = {
            row.forEach { proxyName ->
                val isSelected = proxyName == group.now
                val delay = group.delays[proxyName]
                val nodeType = group.nodeTypes[proxyName] ?: ""

                ProxyNodeCard(
                    name = proxyName,
                    type = nodeType,
                    delay = delay,
                    isSelected = isSelected,
                    isFixed = proxyName == group.fixed,
                    isSelectable = group.isSelectable,
                    isTesting = proxyName in testingNodes,
                    onTestDelay = onTestNodeDelay,
                    onClick = onSelect,
                )
            }
        }
    ) { measurables, constraints ->
        // measure 阶段读进度，动画帧只重测不重组
        val fraction = singleColumnProgress.value
        val rowWidth = constraints.maxWidth
        val horizontalGap = NodeRowHorizontalGap.roundToPx()
        val verticalGap = NodeRowVerticalGap.roundToPx()
        val halfWidth = (rowWidth - horizontalGap) / 2
        val cellWidth = lerp(halfWidth, rowWidth, fraction)

        val cellConstraints = constraints.copy(minWidth = cellWidth, maxWidth = cellWidth)
        val first = if (measurables.isNotEmpty()) measurables[0].measure(cellConstraints) else null
        val second = if (measurables.size > 1) measurables[1].measure(cellConstraints) else null

        // 竖排基准取首项实际高度，两节点不等高时不留多余空隙
        val secondX = lerp(halfWidth + horizontalGap, 0, fraction)
        val secondY = lerp(0, (first?.height ?: 0) + verticalGap, fraction)
        val topPadding = if (rowIndex == 0) NodeRowTopPadding.roundToPx() else 0
        val contentHeight =
            topPadding +
                maxOf(first?.height ?: 0, if (second != null) secondY + second.height else 0) +
                NodeRowBottomPadding.roundToPx()

        val expand = expandProgress?.floatValue ?: 1f
        val visibleFraction =
            if (expand >= 1f) {
                1f
            } else if (expand <= 0f) {
                0f
            } else {
                val targetRows = rowCount * expand
                (targetRows - rowIndex.toFloat()).coerceIn(0f, 1f)
            }
        val rowHeight = (contentHeight * visibleFraction).roundToInt()

        // 满高测量顶对齐，超出部分由段裁切（卷起而非变形）
        layout(rowWidth, rowHeight) {
            if (visibleFraction > 0f) {
                first?.placeRelative(0, topPadding)
                second?.placeRelative(secondX, secondY + topPadding)
            }
        }
    }
}

@Composable
private fun ProxyNodeCard(
    name: String,
    type: String,
    delay: Int?,
    isSelected: Boolean,
    isFixed: Boolean,
    isSelectable: Boolean,
    isTesting: Boolean = false,
    onTestDelay: (String) -> Unit = {},
    onClick: (String) -> Unit,
) {
    val timeoutStr = stringResource(R.string.proxy_timeout)
    val delayText =
        when {
            delay == null -> null
            delay < 0 -> timeoutStr
            else -> "$delay"
        }
    val delayColor = StatusColors.delay(delay)
    val testNodeDelayLabel = stringResource(R.string.proxy_test_node_delay)

    androidx.compose.material3.ListItem(
        selected = isSelected,
        onClick = { if (isSelectable) onClick(name) },
        leadingContent =
            if (isFixed) {
                { Icon(AppIcons.Pin, stringResource(R.string.proxy_fixed)) }
            } else null,
        supportingContent =
            if (type.isNotEmpty()) {
                { Text(type, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            } else null,
        trailingContent = {
            androidx.compose.material3.TextButton(
                onClick = { onTestDelay(name) },
                enabled = !isTesting,
                shapes = androidx.compose.material3.ButtonDefaults.shapes(),
            ) {
                if (isTesting) LoadingIndicator(Modifier.size(24.dp))
                else Text(delayText ?: "--", color = delayColor)
            }
        },
    ) {
        Text(name, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

// 延迟排序超时与未测沉底，倒序仅翻转有效部分
private fun sortNodes(
    names: List<String>,
    delays: Map<String, Int>,
    sortOption: Int,
): List<String> {
    val key = sortOption / 2
    val reverse = sortOption % 2 != 0
    return when (key) {
        1 -> {
            val comparator =
                if (reverse) String.CASE_INSENSITIVE_ORDER.reversed()
                else String.CASE_INSENSITIVE_ORDER
            names.sortedWith(comparator)
        }

        2 -> {
            val valid = ArrayList<String>(names.size)
            val invalid = ArrayList<String>()
            for (name in names) {
                val d = delays[name]
                if (d != null && d > 0) valid.add(name) else invalid.add(name)
            }
            if (reverse) {
                valid.sortByDescending { delays[it] ?: Int.MAX_VALUE }
            } else {
                valid.sortBy { delays[it] ?: Int.MAX_VALUE }
            }
            valid.addAll(invalid)
            valid
        }

        else -> if (reverse) names.reversed() else names
    }
}
