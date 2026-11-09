package rinn.rei.mihane.ui.screen.subscription

import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import org.uwuaosp.compose.widget.expressive.SettingsActionButton
import org.uwuaosp.compose.widget.expressive.SettingsButtonStyle
import rinn.rei.mihane.R

/** Dismissal must not interrupt the import's atomic commit. Cancellation belongs to its owner. */
@Composable
fun ImportProgressDialog(
    show: Boolean,
    step: String,
    title: String = stringResource(R.string.subscription_import_config),
    onCancel: (() -> Unit)? = null,
) {
    if (!show) return
    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        title = { Text(title) },
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                LoadingIndicator()
                Text(step, modifier = Modifier.weight(1f))
            }
        },
        confirmButton = {
            if (onCancel != null) {
                SettingsActionButton(
                    label = stringResource(R.string.common_cancel),
                    onClick = onCancel,
                    style = SettingsButtonStyle.Text,
                )
            }
        },
    )
}
