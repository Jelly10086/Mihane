package rinn.rei.mihane.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.uwuaosp.compose.widget.editor.SettingsSearchLayout
import org.uwuaosp.compose.widget.scaffold.SettingsScreenScaffold
import rinn.rei.mihane.R

@Composable
fun SearchPageScaffold(
    title: String,
    query: TextFieldState,
    searchBarState: SearchBarState,
    placeholder: String,
    showBackButton: Boolean,
    onNavigateUp: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    results: @Composable ColumnScope.() -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    SettingsScreenScaffold(
        title = title,
        showBackButton = showBackButton,
        onNavigateUp = onNavigateUp,
        actions = actions,
        header = {
            MaterialExpressiveTheme(
                colorScheme =
                    MaterialTheme.colorScheme.copy(
                        surface = MaterialTheme.colorScheme.surfaceContainer
                    )
            ) {
                SettingsSearchLayout(
                    textFieldState = query,
                    searchBarState = searchBarState,
                    placeholder = placeholder,
                    clearContentDescription = stringResource(R.string.common_close),
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    results = results,
                )
            }
        },
        content = content,
    )
}
