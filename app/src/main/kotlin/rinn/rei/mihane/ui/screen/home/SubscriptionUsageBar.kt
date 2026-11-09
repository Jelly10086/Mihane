package rinn.rei.mihane.ui.screen.home

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
private fun animatedUsage(
    progress: Float
): Animatable<Float, androidx.compose.animation.core.AnimationVector1D> {
    val target = progress.coerceIn(0f, 1f)
    val animated = remember { Animatable(target) }
    val motion = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    LaunchedEffect(target) { animated.animateTo(target, motion) }
    return animated
}

@Composable
internal fun SubscriptionUsageBar(modifier: Modifier = Modifier, progress: Float, color: Color) {
    val animated = animatedUsage(progress)
    val width = with(LocalDensity.current) { 6.dp.toPx() }
    val stroke = remember(width) { Stroke(width, cap = StrokeCap.Round) }
    LinearWavyProgressIndicator(
        progress = { animated.value },
        modifier = modifier.height(14.dp),
        color = color,
        stroke = stroke,
        trackStroke = stroke,
    )
}

@Composable
internal fun SubscriptionQuotaBar(modifier: Modifier = Modifier, progress: Float, color: Color) {
    val animated = animatedUsage(progress)
    LinearProgressIndicator(progress = { animated.value }, modifier = modifier, color = color)
}
