package rinn.rei.mihane.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.uwuaosp.compose.widget.expressive.SettingsActionButton
import org.uwuaosp.compose.widget.preference.PreferenceRow
import rinn.rei.mihane.R
import rinn.rei.mihane.platform.scanTetherInterfacesAsRoot
import rinn.rei.mihane.platform.showToast
import rinn.rei.mihane.ui.component.material.FormField
import rinn.rei.mihane.ui.component.material.SelectionCheckbox

/**
 * 热点接口名编辑对话框。多行 FormField（每行一个接口名）+ [检测当前接口] 辅助按钮。 扫描按钮通过 `su ip -o addr show` 解析当前持有热点私网 IP
 * 的接口，弹二级勾选 Dialog 后覆盖输入框。
 */
@Composable
fun TetherInterfaceEditDialog(
    show: Boolean,
    title: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val textState = rememberTextFieldState()
    val scanningMsg = stringResource(R.string.root_tether_scan_scanning)
    val emptyMsg = stringResource(R.string.root_tether_scan_empty)
    val scope = rememberCoroutineScope()

    var showScanDialog by remember { mutableStateOf(false) }
    var scanResult by remember { mutableStateOf<List<String>>(emptyList()) }
    val selectedMap = remember { androidx.compose.runtime.mutableStateMapOf<String, Boolean>() }

    // 每次 Dialog 打开时把 storage 的逗号分隔值回填成多行文本
    LaunchedEffect(show, initialValue) {
        if (show) {
            textState.setTextAndPlaceCursorAtEnd(
                initialValue
                    .split(',')
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .joinToString("\n")
            )
        }
    }

    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                FlowRow(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SettingsActionButton(
                        label = stringResource(R.string.root_tether_scan),
                        onClick = {
                            scope.launch {
                                showToast(scanningMsg)
                                val found =
                                    withContext(Dispatchers.IO) { scanTetherInterfacesAsRoot() }
                                if (found.isEmpty()) {
                                    showToast(emptyMsg)
                                } else {
                                    scanResult = found
                                    selectedMap.clear()
                                    // 默认只勾选"像热点"的接口：wlan0 是主 STA 客户端，不勾；
                                    // ap*/wlan[1-9]/swlan*/wlan0_AP/rndis*/usb*/bt-pan 视为候选热点
                                    found.forEach { selectedMap[it] = isLikelyTetherInterface(it) }
                                    showScanDialog = true
                                }
                            }
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
                            val csv =
                                textState.text
                                    .toString()
                                    .lines()
                                    .map { it.trim() }
                                    .filter { it.isNotEmpty() }
                                    .distinct()
                                    .joinToString(",")
                            onConfirm(csv)
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
                    Text(stringResource(R.string.root_tether_ifaces_hint))

                    FormField(state = textState, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                }
            },
        )
    }

    ScanResultDialog(
        show = showScanDialog,
        candidates = scanResult,
        selected = selectedMap,
        onDismiss = { showScanDialog = false },
        onApply = { picked ->
            if (picked.isNotEmpty()) {
                textState.setTextAndPlaceCursorAtEnd(picked.joinToString("\n"))
            }
            showScanDialog = false
        },
    )
}

@Composable
private fun ScanResultDialog(
    show: Boolean,
    candidates: List<String>,
    selected: SnapshotStateMap<String, Boolean>,
    onDismiss: () -> Unit,
    onApply: (List<String>) -> Unit,
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
                        label = stringResource(R.string.common_cancel),
                        onClick = onDismiss,
                        style = org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Text,
                    )
                    SettingsActionButton(
                        label = stringResource(R.string.common_confirm),
                        style = org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Text,
                        onClick = {
                            val picked = candidates.filter { selected[it] == true }
                            onApply(picked)
                        },
                    )
                }
            },
            title = { Text(stringResource(R.string.root_tether_scan_result_title)) },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(candidates) { iface ->
                            val checked = selected[iface] == true
                            PreferenceRow(
                                title = iface,
                                trailingContent = {
                                    SelectionCheckbox(
                                        state =
                                            if (checked) ToggleableState.On
                                            else ToggleableState.Off,
                                        onClick = { selected[iface] = !checked },
                                    )
                                },
                                onClick = { selected[iface] = !checked },
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                        }
                    }
                }
            },
        )
    }
}

@Composable
fun tetherInterfaceSummary(value: String): String {
    val items = value.split(',').map { it.trim() }.filter { it.isNotEmpty() }
    return if (items.isEmpty()) "—" else items.joinToString(", ")
}

/**
 * 判断接口名是否"像热点接口"，用于扫描结果默认勾选。
 *
 * 明确排除：`wlan0`（主 STA Wi-Fi 客户端，几乎所有 Android 设备的主 Wi-Fi 都用这个名字）。 明确纳入：`ap*` / `wlan[1-9]` / `swlan*`
 * / `wlan0_AP` / `rndis*` / `usb*` / `bt-pan`。 其他未识别名保守视为"非热点"，不默认勾选（用户可手动勾）。
 */
private fun isLikelyTetherInterface(name: String): Boolean {
    if (name == "wlan0") return false
    if (name.startsWith("ap")) return true
    if (name == "wlan0_AP") return true
    if (name.startsWith("wlan") && name.length > 4 && name[4].isDigit() && name[4] != '0')
        return true
    if (name.startsWith("swlan")) return true
    if (name.startsWith("rndis")) return true
    if (name.startsWith("usb")) return true
    if (name == "bt-pan") return true
    return false
}
