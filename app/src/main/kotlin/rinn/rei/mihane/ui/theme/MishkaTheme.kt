package rinn.rei.mihane.ui.theme

import android.graphics.Typeface
import android.graphics.fonts.Font
import android.graphics.fonts.FontFamily
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily as ComposeFontFamily
import androidx.compose.ui.text.font.FontWeight
import com.materialkolor.dynamiccolor.MaterialDynamicColors
import com.materialkolor.hct.Hct
import com.materialkolor.scheme.SchemeExpressive
import org.uwuaosp.compose.framework.theme.SettingsTheme
import org.uwuaosp.compose.framework.theme.settingsColorScheme
import rinn.rei.mihane.R

@Composable
fun MishkaTheme(config: ThemeConfig, dark: Boolean, content: @Composable () -> Unit) {
    val fallback = settingsColorScheme(dark, config.useMonet)
    val colors =
        remember(fallback, config, dark) {
            val seeded =
                if (config.useMonet || config.accentColor == ThemeAccentColor.Default) fallback
                else expressiveSeedScheme(config.accentColor.seedColor.toArgb(), dark)
            if (config.pureBlack && dark)
                seeded.copy(background = Color.Black, surface = Color.Black)
            else seeded
        }
    val typography = rememberAppTypography()
    SettingsTheme(darkTheme = dark, dynamicColor = config.useMonet, colorScheme = colors) {
        MaterialExpressiveTheme(colorScheme = colors, typography = typography, content = content)
    }
}

private fun expressiveSeedScheme(seed: Int, dark: Boolean): ColorScheme {
    val scheme =
        SchemeExpressive(
            Hct.fromInt(seed),
            dark,
            0.0,
            com.materialkolor.dynamiccolor.ColorSpec.SpecVersion.SPEC_2025,
        )
    val tokens = MaterialDynamicColors()
    fun c(value: com.materialkolor.dynamiccolor.DynamicColor) = Color(value.getArgb(scheme))
    return ColorScheme(
        primary = c(tokens.primary()),
        onPrimary = c(tokens.onPrimary()),
        primaryContainer = c(tokens.primaryContainer()),
        onPrimaryContainer = c(tokens.onPrimaryContainer()),
        inversePrimary = c(tokens.inversePrimary()),
        secondary = c(tokens.secondary()),
        onSecondary = c(tokens.onSecondary()),
        secondaryContainer = c(tokens.secondaryContainer()),
        onSecondaryContainer = c(tokens.onSecondaryContainer()),
        tertiary = c(tokens.tertiary()),
        onTertiary = c(tokens.onTertiary()),
        tertiaryContainer = c(tokens.tertiaryContainer()),
        onTertiaryContainer = c(tokens.onTertiaryContainer()),
        background = c(tokens.background()),
        onBackground = c(tokens.onBackground()),
        surface = c(tokens.surface()),
        onSurface = c(tokens.onSurface()),
        surfaceVariant = c(tokens.surfaceVariant()),
        onSurfaceVariant = c(tokens.onSurfaceVariant()),
        surfaceTint = c(tokens.primary()),
        inverseSurface = c(tokens.inverseSurface()),
        inverseOnSurface = c(tokens.inverseOnSurface()),
        error = c(tokens.error()),
        onError = c(tokens.onError()),
        errorContainer = c(tokens.errorContainer()),
        onErrorContainer = c(tokens.onErrorContainer()),
        outline = c(tokens.outline()),
        outlineVariant = c(tokens.outlineVariant()),
        scrim = c(tokens.scrim()),
        surfaceBright = c(tokens.surfaceBright()),
        surfaceDim = c(tokens.surfaceDim()),
        surfaceContainer = c(tokens.surfaceContainer()),
        surfaceContainerHigh = c(tokens.surfaceContainerHigh()),
        surfaceContainerHighest = c(tokens.surfaceContainerHighest()),
        surfaceContainerLow = c(tokens.surfaceContainerLow()),
        surfaceContainerLowest = c(tokens.surfaceContainerLowest()),
        primaryFixed = c(tokens.primaryFixed()),
        primaryFixedDim = c(tokens.primaryFixedDim()),
        onPrimaryFixed = c(tokens.onPrimaryFixed()),
        onPrimaryFixedVariant = c(tokens.onPrimaryFixedVariant()),
        secondaryFixed = c(tokens.secondaryFixed()),
        secondaryFixedDim = c(tokens.secondaryFixedDim()),
        onSecondaryFixed = c(tokens.onSecondaryFixed()),
        onSecondaryFixedVariant = c(tokens.onSecondaryFixedVariant()),
        tertiaryFixed = c(tokens.tertiaryFixed()),
        tertiaryFixedDim = c(tokens.tertiaryFixedDim()),
        onTertiaryFixed = c(tokens.onTertiaryFixed()),
        onTertiaryFixedVariant = c(tokens.onTertiaryFixedVariant()),
    )
}

@Composable
private fun rememberAppTypography(): Typography {
    val resources = androidx.compose.ui.platform.LocalResources.current
    return remember(resources) {
        // A family is a weight set; fallback is a separate family, never a second face of the same
        // weight.
        val latin =
            (100..900 step 100).map { weight ->
                Font.Builder(resources, R.font.outfit_variable)
                    .setFontVariationSettings("'wght' $weight")
                    .setWeight(weight)
                    .build()
            }
        val chinese =
            (100..900 step 100).map { weight ->
                Font.Builder(resources, R.font.misans_variable)
                    .setFontVariationSettings("'wght' $weight")
                    .setWeight(weight)
                    .build()
            }
        fun family(fonts: List<Font>): FontFamily =
            FontFamily.Builder(fonts.first())
                .apply { fonts.drop(1).forEach { addFont(it) } }
                .build()
        val first = family(latin)
        val fallback = family(chinese)
        val faces =
            (100..900 step 100).associateWith { weight ->
                ComposeFontFamily(
                    Typeface.CustomFallbackBuilder(first)
                        .addCustomFallback(fallback)
                        .setSystemFallback("sans-serif")
                        .setStyle(
                            android.graphics.fonts.FontStyle(
                                weight,
                                android.graphics.fonts.FontStyle.FONT_SLANT_UPRIGHT,
                            )
                        )
                        .build()
                )
            }
        val d = Typography()
        fun androidx.compose.ui.text.TextStyle.face() =
            copy(
                fontFamily = faces[(fontWeight ?: FontWeight.Normal).weight] ?: faces.getValue(400)
            )
        d.copy(
            displayLarge = d.displayLarge.face(),
            displayMedium = d.displayMedium.face(),
            displaySmall = d.displaySmall.face(),
            headlineLarge = d.headlineLarge.face(),
            headlineMedium = d.headlineMedium.face(),
            headlineSmall = d.headlineSmall.face(),
            titleLarge = d.titleLarge.face(),
            titleMedium = d.titleMedium.face(),
            titleSmall = d.titleSmall.face(),
            bodyLarge = d.bodyLarge.face(),
            bodyMedium = d.bodyMedium.face(),
            bodySmall = d.bodySmall.face(),
            labelLarge = d.labelLarge.face(),
            labelMedium = d.labelMedium.face(),
            labelSmall = d.labelSmall.face(),
            displayLargeEmphasized = d.displayLargeEmphasized.face(),
            displayMediumEmphasized = d.displayMediumEmphasized.face(),
            displaySmallEmphasized = d.displaySmallEmphasized.face(),
            headlineLargeEmphasized = d.headlineLargeEmphasized.face(),
            headlineMediumEmphasized = d.headlineMediumEmphasized.face(),
            headlineSmallEmphasized = d.headlineSmallEmphasized.face(),
            titleLargeEmphasized = d.titleLargeEmphasized.face(),
            titleMediumEmphasized = d.titleMediumEmphasized.face(),
            titleSmallEmphasized = d.titleSmallEmphasized.face(),
            bodyLargeEmphasized = d.bodyLargeEmphasized.face(),
            bodyMediumEmphasized = d.bodyMediumEmphasized.face(),
            bodySmallEmphasized = d.bodySmallEmphasized.face(),
            labelLargeEmphasized = d.labelLargeEmphasized.face(),
            labelMediumEmphasized = d.labelMediumEmphasized.face(),
            labelSmallEmphasized = d.labelSmallEmphasized.face(),
        )
    }
}
