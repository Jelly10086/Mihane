package rinn.rei.mihane.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import rinn.rei.mihane.R

// 主题枚举 → 用户可见名称的唯一映射，避免多处 when 漂移

/** colorMode 的取值，顺序即选项列表顺序：0 跟随系统 / 1 浅色 / 2 深色 */
val ThemeColorModes = listOf(0, 1, 2)

@Composable
fun themeColorModeLabel(colorMode: Int): String =
    stringResource(
        when (colorMode) {
            1 -> R.string.settings_theme_light
            2 -> R.string.settings_theme_dark
            else -> R.string.settings_theme_system
        }
    )

@Composable
fun ThemeAccentColor.label(): String =
    stringResource(
        when (this) {
            ThemeAccentColor.Default -> R.string.settings_theme_accent_default
            ThemeAccentColor.Blue -> R.string.settings_theme_accent_blue
            ThemeAccentColor.Purple -> R.string.settings_theme_accent_purple
            ThemeAccentColor.Pink -> R.string.settings_theme_accent_pink
            ThemeAccentColor.Red -> R.string.settings_theme_accent_red
            ThemeAccentColor.Orange -> R.string.settings_theme_accent_orange
            ThemeAccentColor.Yellow -> R.string.settings_theme_accent_yellow
            ThemeAccentColor.Green -> R.string.settings_theme_accent_green
            ThemeAccentColor.Teal -> R.string.settings_theme_accent_teal
        }
    )
