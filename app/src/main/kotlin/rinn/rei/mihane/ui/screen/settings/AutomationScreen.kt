package rinn.rei.mihane.ui.screen.settings

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
import org.uwuaosp.compose.widget.preference.SwitchPreferenceRow
import org.uwuaosp.compose.widget.scaffold.SettingsCollapsingAppBarScaffold
import rinn.rei.mihane.R
import rinn.rei.mihane.ui.component.CardItem
import rinn.rei.mihane.ui.component.LocalPreferencePosition
import rinn.rei.mihane.ui.component.groupedCardItems
import rinn.rei.mihane.ui.util.horizontalCutoutPadding
import rinn.rei.mihane.ui.util.readableContentPadding
import rinn.rei.mihane.viewmodel.AutomationViewModel

@Composable
fun AutomationScreen(viewModel: AutomationViewModel, onBack: () -> Unit = {}) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    SettingsCollapsingAppBarScaffold(
        title = stringResource(R.string.automation_title),
        showBackButton = true,
        onNavigateUp = onBack,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().horizontalCutoutPadding(),
            contentPadding = readableContentPadding(innerPadding),
        ) {
            item { Spacer(Modifier.height(12.dp)) }

            groupedCardItems(
                keyPrefix = "automation",
                outerBottomPadding = 12.dp,
                items =
                    listOf(
                        CardItem("allowExternal") {
                            SwitchPreferenceRow(
                                title = stringResource(R.string.automation_allow_external),
                                summary =
                                    stringResource(R.string.automation_allow_external_summary),
                                checked = uiState.allowed,
                                onCheckedChange = viewModel::setAllowed,
                                index = LocalPreferencePosition.current.first,
                                itemCount = LocalPreferencePosition.current.second,
                            )
                        }
                    ),
            )

            item {
                CustomPreferenceRow(
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(
                                horizontal =
                                    org.uwuaosp.compose.framework.theme.SettingsDimension
                                        .pagePadding
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
                            text = stringResource(R.string.automation_actions_hint),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                        Text(
                            text = stringResource(R.string.automation_result_hint),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                        Text(
                            text = stringResource(R.string.automation_scope_hint),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
        }
    }
}
