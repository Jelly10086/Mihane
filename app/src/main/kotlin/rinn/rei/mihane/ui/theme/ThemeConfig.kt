package rinn.rei.mihane.ui.theme

import androidx.compose.ui.graphics.Color
import rinn.rei.mihane.platform.PlatformStorage
import rinn.rei.mihane.platform.StorageKeys

data class ThemeConfig(
    val colorMode: Int = 0,
    val pureBlack: Boolean = false,
    val useMonet: Boolean = true,
    val accentColor: ThemeAccentColor = ThemeAccentColor.Default,
    val densityScale: Float = DefaultDensityScale,
)

const val MinDensityScale = 0.8f
const val MaxDensityScale = 1.1f
const val DefaultDensityScale = 1f

fun normalizeDensityScale(value: Float) =
    if (value.isFinite()) value.coerceIn(MinDensityScale, MaxDensityScale) else DefaultDensityScale

fun ThemeConfig.resolveIsDark(systemDark: Boolean) =
    when (colorMode) {
        1 -> false
        2 -> true
        else -> systemDark
    }

enum class ThemeAccentColor(val storageValue: String, val seedColor: Color) {
    Default("default", Color(0xFF3482FF)),
    Blue("blue", Color(0xFF3482FF)),
    Purple("purple", Color(0xFF6750A4)),
    Pink("pink", Color(0xFFB0006D)),
    Red("red", Color(0xFFBA1A1A)),
    Orange("orange", Color(0xFFB65D00)),
    Yellow("yellow", Color(0xFF7D5700)),
    Green("green", Color(0xFF006D3B)),
    Teal("teal", Color(0xFF006A6A));

    companion object {
        fun fromStorage(value: String): ThemeAccentColor =
            entries.firstOrNull { it.storageValue == value } ?: Default
    }
}

fun readThemeConfig(storage: PlatformStorage) =
    ThemeConfig(
        colorMode =
            when (storage.getString(StorageKeys.DARK_MODE, "system")) {
                "light" -> 1
                "dark" -> 2
                else -> 0
            },
        pureBlack = storage.getString(StorageKeys.THEME_PURE_BLACK, "false") == "true",
        useMonet = storage.getString(StorageKeys.THEME_MONET, "true") == "true",
        accentColor =
            ThemeAccentColor.fromStorage(
                storage.getString(StorageKeys.THEME_ACCENT_COLOR, "default")
            ),
        densityScale =
            normalizeDensityScale(
                storage.getString(StorageKeys.THEME_DENSITY_SCALE, "1.0").toFloatOrNull() ?: 1f
            ),
    )

fun writeThemeConfig(storage: PlatformStorage, config: ThemeConfig) {
    storage.putString(
        StorageKeys.DARK_MODE,
        when (config.colorMode) {
            1 -> "light"
            2 -> "dark"
            else -> "system"
        },
    )
    storage.putString(StorageKeys.THEME_PURE_BLACK, config.pureBlack.toString())
    storage.putString(StorageKeys.THEME_MONET, config.useMonet.toString())
    storage.putString(StorageKeys.THEME_ACCENT_COLOR, config.accentColor.storageValue)
    storage.putString(StorageKeys.THEME_DENSITY_SCALE, config.densityScale.toString())
}
