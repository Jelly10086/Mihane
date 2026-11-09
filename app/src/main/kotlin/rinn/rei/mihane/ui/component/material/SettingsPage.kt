package rinn.rei.mihane.ui.component.material

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.uwuaosp.compose.widget.scaffold.SettingsScreenScaffold

@Composable
fun LoadingPage(title: String, onBack: () -> Unit) {
    SettingsScreenScaffold(title, showBackButton = true, onNavigateUp = onBack) { insets ->
        androidx.compose.foundation.layout.Box(
            Modifier.fillMaxSize().padding(insets),
            contentAlignment = androidx.compose.ui.Alignment.Center,
        ) {
            androidx.compose.material3.LoadingIndicator()
        }
    }
}
