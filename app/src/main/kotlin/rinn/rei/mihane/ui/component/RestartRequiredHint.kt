package rinn.rei.mihane.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.uwuaosp.compose.widget.preference.CustomPreferenceRow
import rinn.rei.mihane.R

@Composable
fun RestartRequiredHint(modifier: Modifier = Modifier) {
    HintCard(
        text = stringResource(R.string.settings_override_restart_hint),
        modifier = modifier.padding(top = 12.dp),
        bottomPadding = 6.dp,
    )
}

@Composable
fun HintCard(text: String, modifier: Modifier = Modifier, bottomPadding: Dp = 12.dp) {
    CustomPreferenceRow(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    horizontal = org.uwuaosp.compose.framework.theme.SettingsDimension.pagePadding
                )
                .padding(bottom = bottomPadding)
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
                text = text,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }
    }
}
