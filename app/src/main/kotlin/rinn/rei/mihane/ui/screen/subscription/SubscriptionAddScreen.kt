package rinn.rei.mihane.ui.screen.subscription

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.uwuaosp.compose.widget.preference.CustomPreferenceRow
import org.uwuaosp.compose.widget.preference.PreferenceRow
import org.uwuaosp.compose.widget.scaffold.SettingsCollapsingAppBarScaffold
import rinn.rei.mihane.R
import rinn.rei.mihane.ui.util.horizontalCutoutPadding
import rinn.rei.mihane.ui.util.readableContentPadding
import rinn.rei.mihane.viewmodel.SubscriptionViewModel

/** 创建配置 —— 选择添加方式（文件 / URL） */
@Composable
fun SubscriptionAddScreen(
    viewModel: SubscriptionViewModel? = null,
    onBack: () -> Unit = {},
    onPickFile: () -> Unit = {},
    onNavigateUrl: () -> Unit = {},
    onScanQR: (() -> Unit)? = null,
) {
    val uiState by viewModel?.uiState?.collectAsStateWithLifecycle() ?: return

    SettingsCollapsingAppBarScaffold(
        title = stringResource(R.string.subscription_create),
        showBackButton = true,
        onNavigateUp = onBack,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().horizontalCutoutPadding(),
            contentPadding = readableContentPadding(innerPadding),
        ) {
            item {
                Column(
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(
                                horizontal =
                                    org.uwuaosp.compose.framework.theme.SettingsDimension
                                        .pagePadding
                            )
                            .padding(top = 12.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(
                            org.uwuaosp.compose.framework.theme.SettingsDimension.groupSpacing
                        ),
                ) {
                    PreferenceRow(
                        title = stringResource(R.string.subscription_file),
                        summary = stringResource(R.string.subscription_file_summary),
                        enabled = !uiState.isLoading,
                        onClick = onPickFile,
                        index = 0,
                        itemCount = if (onScanQR == null) 2 else 3,
                    )
                    PreferenceRow(
                        title = stringResource(R.string.subscription_url),
                        summary = stringResource(R.string.subscription_url_summary),
                        enabled = !uiState.isLoading,
                        onClick = onNavigateUrl,
                        index = 1,
                        itemCount = if (onScanQR == null) 2 else 3,
                    )
                    if (onScanQR != null) {
                        PreferenceRow(
                            title = stringResource(R.string.subscription_qr),
                            summary = stringResource(R.string.subscription_qr_summary),
                            enabled = !uiState.isLoading,
                            onClick = onScanQR,
                            index = 2,
                            itemCount = if (onScanQR == null) 2 else 3,
                        )
                    }
                }
            }
            if (uiState.error.isNotEmpty()) {
                item(key = "error") {
                    CustomPreferenceRow(
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(
                                    horizontal =
                                        org.uwuaosp.compose.framework.theme.SettingsDimension
                                            .pagePadding
                                )
                                .padding(top = 12.dp)
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
                                text = uiState.error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
        }
    }

    ImportProgressDialog(
        show = uiState.isLoading,
        step =
            uiState.importProgress?.let { importStepLabel(it) }
                ?: stringResource(R.string.common_processing),
        onCancel = { viewModel.cancelCurrentUpdate() },
    )
}
