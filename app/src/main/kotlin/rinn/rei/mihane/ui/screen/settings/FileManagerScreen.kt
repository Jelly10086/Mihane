package rinn.rei.mihane.ui.screen.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.uwuaosp.compose.widget.preference.PreferenceRow
import org.uwuaosp.compose.widget.scaffold.SettingsCollapsingAppBarScaffold
import rinn.rei.mihane.R
import rinn.rei.mihane.ui.component.LocalPreferencePosition
import rinn.rei.mihane.ui.util.horizontalCutoutPadding
import rinn.rei.mihane.ui.util.readableContentPadding
import rinn.rei.mihane.viewmodel.SubscriptionViewModel

@Composable
fun FileManagerScreen(
    subscriptionViewModel: SubscriptionViewModel?,
    onBack: () -> Unit = {},
    onOpenFile: (uuid: String, relativePath: String) -> Unit = { _, _ -> },
) {
    val uiState = subscriptionViewModel?.uiState?.collectAsStateWithLifecycle()?.value
    val importedSubs = uiState?.subscriptions.orEmpty().filter { it.imported }

    SettingsCollapsingAppBarScaffold(
        title = stringResource(R.string.file_manager_title),
        showBackButton = true,
        onNavigateUp = onBack,
    ) { innerPadding ->
        Box(modifier = Modifier) {
            if (importedSubs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.file_manager_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().horizontalCutoutPadding(),
                    contentPadding = readableContentPadding(innerPadding),
                ) {
                    item(key = "top_spacer", contentType = "spacer") {
                        Spacer(Modifier.height(12.dp))
                    }
                    items(importedSubs, key = { it.id }) { sub ->
                        Column(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .padding(
                                        horizontal =
                                            org.uwuaosp.compose.framework.theme.SettingsDimension
                                                .pagePadding
                                    )
                                    .padding(bottom = 12.dp)
                        ) {
                            PreferenceRow(
                                title = sub.name.ifBlank { sub.id.take(8) },
                                summary = sub.id,
                                onClick = {
                                    val files =
                                        subscriptionViewModel
                                            ?.fileManager
                                            ?.listImportedFiles(sub.id)
                                            .orEmpty()
                                    val firstFile =
                                        files.firstOrNull { it == "config.yaml" }
                                            ?: files.firstOrNull()
                                    if (firstFile != null) {
                                        onOpenFile(sub.id, firstFile)
                                    }
                                },
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                        }
                    }
                    item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
                }
            }
        }
    }
}
