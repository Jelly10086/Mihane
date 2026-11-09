package rinn.rei.mihane.ui.screen.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import rinn.rei.mihane.R
import rinn.rei.mihane.ui.component.AppIcons

@Composable
internal fun ProxyPowerMenu(
    onRestart: () -> Unit,
    onStop: () -> Unit,
    onReload: () -> Unit,
    onStart: () -> Unit,
    isRunning: Boolean,
    isStarting: Boolean,
    isStopping: Boolean,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val busy = isStarting || isStopping
    LaunchedEffect(isRunning, busy) { expanded = false }
    BackHandler(expanded) { expanded = false }
    if (busy) {
        FloatingActionButton({}, modifier.padding(end = 16.dp, bottom = 16.dp)) {
            LoadingIndicator()
        }
        return
    }
    FloatingActionButtonMenu(
        expanded = expanded,
        modifier = modifier,
        button = {
            ToggleFloatingActionButton(
                checked = expanded,
                onCheckedChange = { if (isRunning) expanded = it else onStart() },
            ) {
                Icon(
                    painter =
                        rememberVectorPainter(
                            if (checkedProgress > 0.5f) AppIcons.Close else AppIcons.Power
                        ),
                    contentDescription =
                        stringResource(
                            if (isRunning) R.string.common_more else R.string.home_start
                        ),
                    modifier = Modifier.animateIcon({ checkedProgress }),
                )
            }
        },
    ) {
        FloatingActionButtonMenuItem(
            onClick = {
                expanded = false
                onReload()
            },
            icon = { Icon(AppIcons.Refresh, null) },
            text = { Text(stringResource(R.string.home_reload)) },
        )
        FloatingActionButtonMenuItem(
            onClick = {
                expanded = false
                onRestart()
            },
            icon = { Icon(AppIcons.Restart, null) },
            text = { Text(stringResource(R.string.home_restart)) },
        )
        FloatingActionButtonMenuItem(
            onClick = {
                expanded = false
                onStop()
            },
            icon = { Icon(AppIcons.Stop, null) },
            text = { Text(stringResource(R.string.home_stop)) },
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}
