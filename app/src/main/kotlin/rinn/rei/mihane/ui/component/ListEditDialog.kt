package rinn.rei.mihane.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.uwuaosp.compose.widget.expressive.SettingsActionButton
import rinn.rei.mihane.R
import rinn.rei.mihane.ui.component.material.FormField

/** 列表编辑对话框，用于编辑 DNS 服务器、域名列表等。 每行一条，支持三态：不修改（null）/ 编辑内容 / 清除。 */
@Composable
fun ListEditDialog(
    show: Boolean,
    title: String,
    textState: TextFieldState,
    onDismiss: () -> Unit,
    onConfirm: (List<String>?) -> Unit,
    onReset: () -> Unit,
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
                        label = stringResource(R.string.common_not_modified),
                        onClick = {
                            onReset()
                            onDismiss()
                        },
                        style = org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Text,
                    )
                    SettingsActionButton(
                        label = stringResource(R.string.common_clear),
                        onClick = {
                            onConfirm(emptyList())
                            onDismiss()
                        },
                        style = org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Text,
                    )
                    SettingsActionButton(
                        label = stringResource(R.string.common_confirm),
                        style = org.uwuaosp.compose.widget.expressive.SettingsButtonStyle.Text,
                        onClick = {
                            val text = textState.text.toString()
                            val list = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
                            onConfirm(list)
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
                    FormField(
                        state = textState,
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(R.string.network_one_per_line),
                    )
                }
            },
        )
    }
}
