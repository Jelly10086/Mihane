package rinn.rei.mihane.ui.screen.provider

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import rinn.rei.mihane.R
import rinn.rei.mihane.viewmodel.RefreshProgress

/**
 * Provider 刷新进度对话框。不可手动关闭，调用方根据 refresh == null 控制 show。
 * - `singleName != null`：显示 "正在更新 xxx…"（单条刷新）
 * - `singleName == null`：显示 "completed / total"（updateAll 并发刷新，每完成一条计数 +1）
 */
@Composable
fun ProviderRefreshDialog(show: Boolean, progress: RefreshProgress?) {
    if (show) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                FlowRow(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {}
            },
            properties =
                DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
            title = { Text(stringResource(R.string.provider_updating_title)) },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    val text =
                        when {
                            progress == null -> ""
                            progress.singleName != null ->
                                stringResource(
                                    R.string.provider_updating_single,
                                    progress.singleName,
                                )

                            else ->
                                stringResource(
                                    R.string.provider_updating_progress,
                                    progress.completed,
                                    progress.total,
                                )
                        }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LoadingIndicator()
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            },
        )
    }
}
