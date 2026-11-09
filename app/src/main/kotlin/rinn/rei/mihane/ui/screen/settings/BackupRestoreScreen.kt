package rinn.rei.mihane.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.uwuaosp.compose.widget.expressive.SettingsActionButton
import org.uwuaosp.compose.widget.preference.PreferenceRow
import org.uwuaosp.compose.widget.scaffold.SettingsCollapsingAppBarScaffold
import rinn.rei.mihane.R
import rinn.rei.mihane.platform.FilePicker
import rinn.rei.mihane.platform.PlatformStorage
import rinn.rei.mihane.platform.StorageKeys
import rinn.rei.mihane.ui.component.AppIcons
import rinn.rei.mihane.ui.component.CardItem
import rinn.rei.mihane.ui.component.HintCard
import rinn.rei.mihane.ui.component.LocalPreferencePosition
import rinn.rei.mihane.ui.component.groupedCardItems
import rinn.rei.mihane.ui.component.material.FormField
import rinn.rei.mihane.ui.util.horizontalCutoutPadding
import rinn.rei.mihane.ui.util.readableContentPadding
import rinn.rei.mihane.viewmodel.BackupViewModel

@Composable
fun BackupRestoreScreen(
    viewModel: BackupViewModel,
    storage: PlatformStorage,
    filePicker: FilePicker? = null,
    onRestartApp: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLocalDialog by remember { mutableStateOf(false) }
    var showWebDavDialog by remember { mutableStateOf(false) }
    // 恢复来源（本地文件 / WebDAV）共用同一个覆盖确认对话框，确认后执行挂起的动作
    var pendingRestoreAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    SettingsCollapsingAppBarScaffold(
        title = stringResource(R.string.backup_title),
        showBackButton = true,
        onNavigateUp = onBack,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().horizontalCutoutPadding(),
            contentPadding = readableContentPadding(innerPadding),
        ) {
            item(key = "top_spacer") { Spacer(Modifier.height(12.dp)) }
            item(key = "hint") { HintCard(text = stringResource(R.string.backup_hint)) }
            groupedCardItems(
                keyPrefix = "backup_entries",
                outerBottomPadding = 12.dp,
                items =
                    listOf(
                        CardItem("local") {
                            PreferenceRow(
                                title = stringResource(R.string.backup_section_local),
                                summary = stringResource(R.string.backup_local_row_summary),
                                enabled = filePicker != null && !uiState.isBusy,
                                onClick = { showLocalDialog = true },
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                        },
                        CardItem("webdav") {
                            PreferenceRow(
                                title = stringResource(R.string.backup_section_webdav),
                                summary = stringResource(R.string.backup_webdav_row_summary),
                                enabled = !uiState.isBusy,
                                onClick = { showWebDavDialog = true },
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                        },
                    ),
            )
            item(key = "bottom_spacer") { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
        }
    }

    if (filePicker != null) {
        LocalBackupDialog(
            show = showLocalDialog,
            isBusy = uiState.isBusy,
            onDismiss = { showLocalDialog = false },
            onExport = {
                showLocalDialog = false
                viewModel.exportBackup { onResult ->
                    filePicker.createZipDocument(defaultBackupFileName(), onResult)
                }
            },
            onRestore = {
                showLocalDialog = false
                pendingRestoreAction = {
                    filePicker.pickZipDocument { uri ->
                        if (uri != null) viewModel.restoreFromDocument(uri)
                    }
                }
            },
        )
    }

    WebDavDialog(
        show = showWebDavDialog,
        isBusy = uiState.isBusy,
        storage = storage,
        onDismiss = { showWebDavDialog = false },
        onTest = { viewModel.testConnection() },
        onBackup = { viewModel.backup() },
        onRestore = {
            showWebDavDialog = false
            pendingRestoreAction = { viewModel.restore() }
        },
    )

    if (pendingRestoreAction != null) {
        AlertDialog(
            onDismissRequest = { pendingRestoreAction = null },
            confirmButton = {
                FlowRow(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SettingsActionButton(
                        label = stringResource(R.string.common_cancel),
                        onClick = { pendingRestoreAction = null },
                        style = org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Text,
                    )
                    SettingsActionButton(
                        label = stringResource(R.string.common_confirm),
                        style = org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Text,
                        onClick = {
                            val action = pendingRestoreAction
                            pendingRestoreAction = null
                            action?.invoke()
                        },
                    )
                }
            },
            title = { Text(stringResource(R.string.backup_restore_confirm_title)) },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(stringResource(R.string.backup_restore_confirm_summary))
                }
            },
        )
    }

    // 恢复成功后强制重启：OverrideJsonStore / Repository Flow 等内存态不随磁盘恢复刷新，不给取消项
    if (uiState.restoreCompleted) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                FlowRow(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SettingsActionButton(
                        label = stringResource(R.string.backup_restart_now),
                        style = org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Text,
                        onClick = onRestartApp,
                    )
                }
            },
            title = { Text(stringResource(R.string.backup_restore_done_title)) },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(stringResource(R.string.backup_restore_done_summary))
                }
            },
        )
    }
}

@Composable
private fun LocalBackupDialog(
    show: Boolean,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onExport: () -> Unit,
    onRestore: () -> Unit,
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
                }
            },
            title = { Text(stringResource(R.string.backup_section_local)) },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    PreferenceRow(
                        title = stringResource(R.string.backup_export_local),
                        iconContent = {
                            Icon(
                                imageVector = AppIcons.Backup,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 16.dp),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        },
                        enabled = !isBusy,
                        onClick = onExport,
                        index = LocalPreferencePosition.current.first,
                        itemCount = LocalPreferencePosition.current.second,
                    )
                    PreferenceRow(
                        title = stringResource(R.string.backup_restore_local),
                        iconContent = {
                            Icon(
                                imageVector = AppIcons.Import,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 16.dp),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        },
                        enabled = !isBusy,
                        onClick = onRestore,
                        index = LocalPreferencePosition.current.first,
                        itemCount = LocalPreferencePosition.current.second,
                    )
                }
            },
        )
    }
}

@Composable
private fun WebDavDialog(
    show: Boolean,
    isBusy: Boolean,
    storage: PlatformStorage,
    onDismiss: () -> Unit,
    onTest: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
) {
    var url by remember { mutableStateOf(storage.getString(StorageKeys.WEBDAV_URL, "")) }
    var username by remember { mutableStateOf(storage.getString(StorageKeys.WEBDAV_USERNAME, "")) }
    var password by remember { mutableStateOf(storage.getString(StorageKeys.WEBDAV_PASSWORD, "")) }

    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                FlowRow(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {}
            },
            title = { Text(stringResource(R.string.backup_section_webdav)) },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // 长内容：滚动区收缩 + 底部按钮行固定，防小屏上按钮被顶出可视区
                    Column(Modifier.heightIn(max = 500.dp)) {
                        Column(
                            modifier =
                                Modifier.weight(1f, fill = false)
                                    .verticalScroll(rememberScrollState())
                        ) {
                            FormField(
                                value = url,
                                onValueChange = {
                                    url = it
                                    storage.putString(StorageKeys.WEBDAV_URL, it)
                                },
                                modifier =
                                    Modifier.fillMaxWidth()
                                        .padding(horizontal = 24.dp)
                                        .padding(bottom = 8.dp),
                                label = stringResource(R.string.backup_server_url),
                            )
                            FormField(
                                value = username,
                                onValueChange = {
                                    username = it
                                    storage.putString(StorageKeys.WEBDAV_USERNAME, it)
                                },
                                modifier =
                                    Modifier.fillMaxWidth()
                                        .padding(horizontal = 24.dp)
                                        .padding(bottom = 8.dp),
                                label = stringResource(R.string.backup_username),
                            )
                            FormField(
                                value = password,
                                onValueChange = {
                                    password = it
                                    storage.putString(StorageKeys.WEBDAV_PASSWORD, it)
                                },
                                modifier =
                                    Modifier.fillMaxWidth()
                                        .padding(horizontal = 24.dp)
                                        .padding(bottom = 12.dp),
                                label = stringResource(R.string.backup_password),
                            )
                            PreferenceRow(
                                title = stringResource(R.string.backup_now),
                                iconContent = {
                                    Icon(
                                        imageVector = AppIcons.Backup,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 16.dp),
                                        tint = MaterialTheme.colorScheme.onSurface,
                                    )
                                },
                                enabled = !isBusy,
                                onClick = onBackup,
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                            PreferenceRow(
                                title = stringResource(R.string.backup_restore_now),
                                iconContent = {
                                    Icon(
                                        imageVector = AppIcons.Import,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 16.dp),
                                        tint = MaterialTheme.colorScheme.onSurface,
                                    )
                                },
                                enabled = !isBusy,
                                onClick = onRestore,
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                        }
                        Row(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .padding(top = 12.dp)
                                    .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            SettingsActionButton(
                                label = stringResource(R.string.backup_test_connection),
                                modifier = Modifier.weight(1f),
                                enabled = !isBusy,
                                onClick = onTest,
                                style =
                                    org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Tonal,
                            )
                            SettingsActionButton(
                                label = stringResource(R.string.common_close),
                                modifier = Modifier.weight(1f),
                                onClick = onDismiss,
                                style =
                                    org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Tonal,
                            )
                        }
                    }
                }
            },
        )
    }
}

private fun defaultBackupFileName(): String {
    val ts = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
    return "mishka-backup-$ts.zip"
}
