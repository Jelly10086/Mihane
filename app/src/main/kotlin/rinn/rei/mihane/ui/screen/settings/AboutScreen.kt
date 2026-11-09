package rinn.rei.mihane.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.uwuaosp.compose.widget.preference.PreferenceRow
import org.uwuaosp.compose.widget.scaffold.SettingsCollapsingAppBarScaffold
import rinn.rei.mihane.BuildConfig
import rinn.rei.mihane.R

private data class LicenseEntry(
    val title: String,
    val license: String,
    val asset: String,
    val url: String,
)

private val licenses =
    listOf(
        LicenseEntry("mihane", "GPL-3.0", "gpl-3.0.txt", "https://github.com/Jelly10086/Mihane"),
        LicenseEntry("mihomo", "GPL-3.0", "gpl-3.0.txt", "https://github.com/MetaCubeX/mihomo"),
        LicenseEntry(
            "uwuSDK",
            "Apache-2.0",
            "apache-2.0.txt",
            "https://github.com/uwuAOSP/platform_uwu-sdk",
        ),
        LicenseEntry(
            "AndroidX / Material",
            "Apache-2.0",
            "apache-2.0.txt",
            "https://github.com/androidx/androidx",
        ),
        LicenseEntry(
            "Material Color Utilities (Kotlin)",
            "Apache-2.0",
            "apache-2.0.txt",
            "https://github.com/jordond/MaterialKolor",
        ),
        LicenseEntry(
            "scripta",
            "Apache-2.0",
            "apache-2.0.txt",
            "https://github.com/YuKongA/scripta",
        ),
        LicenseEntry(
            "Kotlin / kotlinx / Ktor",
            "Apache-2.0",
            "apache-2.0.txt",
            "https://github.com/JetBrains/kotlin",
        ),
        LicenseEntry(
            "Koin",
            "Apache-2.0",
            "apache-2.0.txt",
            "https://github.com/InsertKoinIO/koin",
        ),
        LicenseEntry(
            "AndroidHiddenApiBypass",
            "Apache-2.0",
            "apache-2.0.txt",
            "https://github.com/LSPosed/AndroidHiddenApiBypass",
        ),
        LicenseEntry(
            "quickie",
            "Apache-2.0",
            "apache-2.0.txt",
            "https://github.com/G00fY2/quickie",
        ),
        LicenseEntry(
            "Material Symbols Rounded",
            "Apache-2.0",
            "apache-2.0.txt",
            "https://github.com/google/material-design-icons",
        ),
        LicenseEntry(
            "Outfit",
            "SIL Open Font License 1.1",
            "outfit.txt",
            "https://github.com/Outfitio/Outfit-Fonts",
        ),
        LicenseEntry(
            "MiSans",
            "MiSans 字体知识产权许可协议",
            "misans.txt",
            "https://hyperos.mi.com/font/zh/download/",
        ),
    )

@Composable
fun AboutScreen(
    onBack: () -> Unit = {},
    mihomoVersion: String = "",
    onOpenUrl: (String) -> Unit = {},
) {
    var selectedTitle by rememberSaveable { mutableStateOf<String?>(null) }
    val selected = licenses.firstOrNull { it.title == selectedTitle }
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    LaunchedEffect(selected) {
        text = ""
        val entry = selected ?: return@LaunchedEffect
        text =
            withContext(Dispatchers.IO) {
                context.assets.open("licenses/${entry.asset}").bufferedReader().use {
                    it.readText()
                }
            }
    }
    if (selected != null) {
        val entry = selected
        androidx.activity.compose.BackHandler { selectedTitle = null }
        SettingsCollapsingAppBarScaffold(
            entry.title,
            showBackButton = true,
            onNavigateUp = { selectedTitle = null },
        ) { padding ->
            org.uwuaosp.compose.widget.ui.SettingsContent {
                LazyColumn(Modifier.fillMaxSize(), contentPadding = padding) {
                    item {
                        Text(
                            entry.license,
                            Modifier.padding(24.dp),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    item {
                        TextButton(
                            onClick = { onOpenUrl(entry.url) },
                            modifier = Modifier.padding(horizontal = 16.dp),
                            shapes = ButtonDefaults.shapes(),
                        ) {
                            Text(entry.url)
                        }
                    }
                    item { SelectionContainerText(text) }
                }
            }
        }
    } else {
        SettingsCollapsingAppBarScaffold(
            stringResource(R.string.settings_about),
            showBackButton = true,
            onNavigateUp = onBack,
        ) { padding ->
            org.uwuaosp.compose.widget.ui.SettingsContent {
                LazyColumn(Modifier.fillMaxSize(), contentPadding = padding) {
                    item {
                        Column(Modifier.padding(24.dp)) {
                            Text(
                                stringResource(R.string.app_name),
                                style = MaterialTheme.typography.displaySmall,
                            )
                            Text(
                                BuildConfig.VERSION_NAME,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                "mihomo $mihomoVersion",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    item {
                        Text(
                            stringResource(R.string.about_open_source),
                            Modifier.padding(24.dp),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    items(licenses.size, key = { licenses[it].title }) { index ->
                        val entry = licenses[index]
                        if (index == licenses.size - 2) {
                            Text(
                                stringResource(R.string.about_font_resources),
                                Modifier.padding(24.dp),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        PreferenceRow(
                            entry.title,
                            entry.license,
                            Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                            onClick = { selectedTitle = entry.title },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectionContainerText(text: String) {
    androidx.compose.foundation.text.selection.SelectionContainer {
        Text(text, Modifier.padding(24.dp), style = MaterialTheme.typography.bodyMedium)
    }
}
