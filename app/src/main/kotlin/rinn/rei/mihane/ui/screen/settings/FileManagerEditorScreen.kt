package rinn.rei.mihane.ui.screen.settings

import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.uwuaosp.compose.widget.preference.CustomPreferenceRow
import org.uwuaosp.compose.widget.scaffold.SettingsCollapsingAppBarScaffold
import rinn.rei.mihane.R
import rinn.rei.mihane.data.bridge.MishkaCoreBridge
import rinn.rei.mihane.platform.ProfileFileManager
import rinn.rei.mihane.platform.showToast
import rinn.rei.mihane.ui.component.AppIcons
import rinn.rei.mihane.ui.theme.LocalAppDarkMode
import rinn.rei.mihane.ui.util.horizontalCutoutPadding
import rinn.rei.mihane.viewmodel.SubscriptionViewModel
import top.yukonga.scripta.editor.CodeEditor
import top.yukonga.scripta.editor.EditorColors
import top.yukonga.scripta.editor.EditorLanguage
import top.yukonga.scripta.editor.rememberSaveableCodeEditorController

@Composable
fun FileManagerEditorScreen(
    uuid: String,
    relativePath: String,
    subscriptionViewModel: SubscriptionViewModel? = null,
    onBack: () -> Unit = {},
) {
    val fileManager = subscriptionViewModel?.fileManager
    val controller = rememberSaveableCodeEditorController()
    var documentLoaded by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val savedMessage = stringResource(R.string.file_manager_saved)
    val saveFailedFormat = stringResource(R.string.file_manager_save_failed)
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(uuid, relativePath, fileManager) {
        if (documentLoaded) return@LaunchedEffect
        val content =
            withContext(Dispatchers.IO) { fileManager?.readImportedFile(uuid, relativePath) } ?: ""
        controller.setDocument(content)
        documentLoaded = true
    }

    SettingsCollapsingAppBarScaffold(
        title = relativePath,
        showBackButton = true,
        onNavigateUp = onBack,
        actions = {
            val canSave = controller.isModified && !isSaving && fileManager != null
            FilledTonalIconButton(
                enabled = canSave,
                onClick = onSave@{
                        if (fileManager == null) return@onSave
                        // 版本号与文本必须同刻捕获：校验期间的继续编辑不能被 markSaved 误吞
                        val version = controller.documentVersion
                        val newContent = controller.getText(controller.lineEnding)
                        // 取被编辑订阅的自定义 UA：校验阶段也走 in-process bridge，需要复用同一 UA
                        // 否则 GeoIP/provider 缺失时下载会用默认 UA 触发服务端拦截
                        val userAgent =
                            subscriptionViewModel.uiState.value.subscriptions
                                .find { it.id == uuid }
                                ?.userAgent
                                .orEmpty()
                        isSaving = true
                        scope.launch {
                            val err = runCatching {
                                saveWithValidation(
                                    fileManager = fileManager,
                                    uuid = uuid,
                                    relativePath = relativePath,
                                    newContent = newContent,
                                    userAgent = userAgent,
                                )
                            }
                            isSaving = false
                            err.onSuccess { errMsg ->
                                    if (errMsg == null) {
                                        controller.markSaved(version)
                                        showToast(savedMessage)
                                    } else {
                                        showToast(saveFailedFormat.format(errMsg), long = true)
                                    }
                                }
                                .onFailure { t ->
                                    showToast(
                                        saveFailedFormat.format(t.message ?: "unknown"),
                                        long = true,
                                    )
                                }
                        }
                    },
                shapes = androidx.compose.material3.IconButtonDefaults.shapes(),
            ) {
                if (isSaving) {
                    LoadingIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Icon(
                        imageVector = AppIcons.Ok,
                        contentDescription = stringResource(R.string.file_manager_save),
                        tint =
                            if (canSave) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .horizontalCutoutPadding()
                    .padding(top = innerPadding.calculateTopPadding())
        ) {
            CustomPreferenceRow(
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(
                            horizontal =
                                org.uwuaosp.compose.framework.theme.SettingsDimension.pagePadding
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
                        text = stringResource(R.string.file_manager_edit_warning),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }
            }

            // 编辑器自行消费底部系统栏 / IME insets，外层不加 imePadding / navigationBarsPadding。
            // 断开 MaterialTheme 全局注入的弹簧越界工厂：编辑器滚动保持硬钳制。
            CompositionLocalProvider(LocalOverscrollFactory provides null) {
                CodeEditor(
                    controller = controller,
                    language =
                        when {
                            isYamlPath(relativePath) -> EditorLanguage.Yaml
                            isJavaScriptPath(relativePath) -> EditorLanguage.JavaScript
                            else -> EditorLanguage.PlainText
                        },
                    colors =
                        (if (LocalAppDarkMode.current) EditorColors.Default else EditorColors.Light)
                            .copy(
                                background = MaterialTheme.colorScheme.surface,
                                foreground = MaterialTheme.colorScheme.onSurface,
                                gutterBackground = MaterialTheme.colorScheme.surfaceContainer,
                                gutterForeground = MaterialTheme.colorScheme.onSurfaceVariant,
                                cursor = MaterialTheme.colorScheme.primary,
                                selection = MaterialTheme.colorScheme.primaryContainer,
                                handle = MaterialTheme.colorScheme.primary,
                                symbolBarBackground =
                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                symbolBarForeground = MaterialTheme.colorScheme.onSurface,
                                symbolBarPressed = MaterialTheme.colorScheme.secondaryContainer,
                                currentLine = MaterialTheme.colorScheme.surfaceContainerLow,
                            ),
                    modifier = Modifier.fillMaxWidth().weight(1f),
                )
            }
        }
    }
}

private fun isYamlPath(path: String): Boolean =
    path.endsWith(".yaml", ignoreCase = true) || path.endsWith(".yml", ignoreCase = true)

private fun isJavaScriptPath(path: String): Boolean =
    path.endsWith(".js", ignoreCase = true) ||
        path.endsWith(".mjs", ignoreCase = true) ||
        path.endsWith(".cjs", ignoreCase = true)

// 仅校验 YAML（config.yaml / .yml / .yaml），其他文件直接写盘。返回 null 表示通过。
private suspend fun saveWithValidation(
    fileManager: ProfileFileManager,
    uuid: String,
    relativePath: String,
    newContent: String,
    userAgent: String,
): String? =
    withContext(Dispatchers.IO) {
        if (!isYamlPath(relativePath)) {
            fileManager.writeImportedFile(uuid, relativePath, newContent)
            return@withContext null
        }
        val original = fileManager.readImportedFile(uuid, relativePath)
        fileManager.writeImportedFile(uuid, relativePath, newContent)
        val workDir = fileManager.getImportedDir(uuid)
        val err =
            runCatching {
                    MishkaCoreBridge.fetchAndValid(
                        workDir = workDir,
                        url = "",
                        force = false,
                        httpProxy = null,
                        userAgent = userAgent,
                        // imported/ 下的 config 已在导入时解密为明文，重校验无需 age 密钥
                        ageSecretKey = "",
                        onProgress = {},
                    )
                }
                .exceptionOrNull()
                ?.message
        if (err != null && original != null) {
            fileManager.writeImportedFile(uuid, relativePath, original)
        }
        err
    }
