package rinn.rei.mihane.ui.component.material

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.uwuaosp.compose.widget.menu.SettingsDropdownMenu
import org.uwuaosp.compose.widget.menu.SettingsMenuGroup
import org.uwuaosp.compose.widget.menu.SettingsMenuItem
import org.uwuaosp.compose.widget.preference.PreferenceRow
import rinn.rei.mihane.ui.component.LocalPreferencePosition

@Composable
fun MenuPreference(
    title: String,
    items: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    summary: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    PreferenceRow(
        title,
        summary?.takeUnless { it == items.getOrNull(selectedIndex) },
        modifier = modifier,
        index = LocalPreferencePosition.current.first,
        itemCount = LocalPreferencePosition.current.second,
        enabled = enabled,
        onClick = { expanded = true },
        trailingContent = {
            Box {
                Text(
                    items.getOrNull(selectedIndex).orEmpty(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                SettingsDropdownMenu(expanded, { expanded = false }) {
                    SettingsMenuGroup {
                        items.forEachIndexed { index, label ->
                            SettingsMenuItem(
                                label,
                                selected = index == selectedIndex,
                                index = index,
                                itemCount = items.size,
                                onClick = {
                                    expanded = false
                                    onSelectedIndexChange(index)
                                },
                            )
                        }
                    }
                }
            }
        },
    )
}

@Composable
fun FormField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    label: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    inputTransformation: InputTransformation? = null,
    onKeyboardAction: androidx.compose.foundation.text.input.KeyboardActionHandler? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    useLabelAsPlaceholder: Boolean = false,
) {
    TextField(
        state,
        modifier,
        label =
            if (useLabelAsPlaceholder || label.isEmpty()) null
            else {
                { Text(label) }
            },
        placeholder =
            if (useLabelAsPlaceholder) {
                { Text(label) }
            } else null,
        keyboardOptions = keyboardOptions,
        inputTransformation = inputTransformation,
        onKeyboardAction = onKeyboardAction,
        trailingIcon = trailingIcon,
    )
}

@Composable
fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    useLabelAsPlaceholder: Boolean = false,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    TextField(
        value,
        onValueChange,
        modifier,
        label =
            if (useLabelAsPlaceholder || label.isEmpty()) null
            else {
                { Text(label) }
            },
        placeholder =
            if (useLabelAsPlaceholder) {
                { Text(label) }
            } else null,
        keyboardOptions = keyboardOptions,
        trailingIcon = trailingIcon,
    )
}

val LocalSheetDismiss = staticCompositionLocalOf<(() -> Unit)?> { null }

@Composable
fun ContentSheet(
    show: Boolean,
    onDismissRequest: () -> Unit,
    title: String? = null,
    startAction: (@Composable () -> Unit)? = null,
    endAction: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!show) return
    CompositionLocalProvider(LocalSheetDismiss provides onDismissRequest) {
        ModalBottomSheet(onDismissRequest) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                startAction?.invoke()
                title?.let { Text(it, style = MaterialTheme.typography.titleLarge) }
                endAction?.invoke()
            }
            Column(Modifier.fillMaxWidth().imePadding(), content = content)
        }
    }
}

@Composable
fun SelectionCheckbox(
    state: ToggleableState,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TriStateCheckbox(state, onClick, modifier, enabled)
}

@Composable
fun PageTabs(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    PrimaryScrollableTabRow(selectedTabIndex, modifier) {
        tabs.forEachIndexed { index, title ->
            Tab(index == selectedTabIndex, { onTabSelected(index) }, text = { Text(title) })
        }
    }
}

fun Modifier.shapeBackground(color: Color, cornerRadius: Dp) =
    background(color, RoundedCornerShape(cornerRadius))

fun Modifier.shapeClip(cornerRadius: Dp) = clip(RoundedCornerShape(cornerRadius))

@Composable
fun AnchoredMenu(
    show: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    SettingsDropdownMenu(show, onDismissRequest) { SettingsMenuGroup(content = content) }
}

@Composable
fun ChoiceMenuItem(
    text: String,
    optionSize: Int,
    index: Int,
    isSelected: Boolean,
    onSelectedIndexChange: (Int) -> Unit,
) {
    SettingsMenuItem(text, isSelected, { onSelectedIndexChange(index) }, index, optionSize)
}

@Composable
fun RefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing,
        onRefresh,
        modifier,
        content = { content() },
    )
}
