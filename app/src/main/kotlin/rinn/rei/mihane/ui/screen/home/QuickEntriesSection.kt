package rinn.rei.mihane.ui.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.uwuaosp.compose.framework.theme.SettingsDimension
import org.uwuaosp.compose.widget.preference.PreferenceRow
import org.uwuaosp.compose.widget.preference.SettingsSectionHeader
import rinn.rei.mihane.R
import rinn.rei.mihane.ui.component.AppIcons

fun LazyListScope.quickEntriesSection(
    isRunning: Boolean,
    onNavigateLog: () -> Unit = {},
    onNavigateProvider: () -> Unit = {},
    onNavigateConnection: () -> Unit = {},
    onNavigateDnsQuery: () -> Unit = {},
) {
    item(key = "quick_entries_title") {
        SettingsSectionHeader(
            title = stringResource(R.string.home_tools),
            modifier = Modifier.padding(start = HomeContentPadding - 8.dp),
        )
    }
    item(key = "quick_entries") {
        androidx.compose.foundation.layout.Column(
            Modifier.fillMaxWidth()
                .padding(horizontal = HomeContentPadding)
                .padding(bottom = SettingsDimension.pageBottomPadding),
            verticalArrangement = Arrangement.spacedBy(SettingsDimension.sectionSpacing),
        ) {
            HomeCardPair(
                first = { cardModifier ->
                    PreferenceRow(
                        title = stringResource(R.string.home_providers),
                        summary = stringResource(R.string.home_providers_subtitle),
                        modifier = cardModifier,
                        icon = AppIcons.Refresh,
                        onClick = onNavigateProvider,
                    )
                },
                second = { cardModifier ->
                    PreferenceRow(
                        title = stringResource(R.string.home_connections),
                        summary = stringResource(R.string.home_connections_subtitle),
                        modifier = cardModifier,
                        icon = AppIcons.Wifi,
                        onClick = onNavigateConnection,
                    )
                },
            )
            HomeCardPair(
                first = { cardModifier ->
                    PreferenceRow(
                        title = stringResource(R.string.home_logs),
                        summary = stringResource(R.string.home_logs_subtitle),
                        modifier = cardModifier,
                        icon = AppIcons.Info,
                        onClick = onNavigateLog,
                    )
                },
                second = { cardModifier ->
                    PreferenceRow(
                        title = stringResource(R.string.home_dns),
                        summary = stringResource(R.string.home_dns_subtitle),
                        modifier = cardModifier,
                        icon = AppIcons.Search,
                        onClick = onNavigateDnsQuery,
                    )
                },
            )
        }
    }
}
