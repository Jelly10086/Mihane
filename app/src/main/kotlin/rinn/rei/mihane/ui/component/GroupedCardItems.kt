package rinn.rei.mihane.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.uwuaosp.compose.framework.theme.SettingsDimension
import org.uwuaosp.compose.widget.preference.CustomPreferenceRow

// Lazy rows retain independent keys; SDK rows receive their visible position.
val LocalPreferencePosition = staticCompositionLocalOf { 0 to 1 }

@Composable
fun CardSegment(
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier,
    outerHorizontalPadding: Dp = SettingsDimension.pagePadding,
    outerTopPadding: Dp = 0.dp,
    outerBottomPadding: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    CustomPreferenceRow(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    start = outerHorizontalPadding,
                    end = outerHorizontalPadding,
                    top = outerTopPadding,
                    bottom = outerBottomPadding,
                ),
        index = if (isFirst) 0 else if (isLast) 2 else 1,
        itemCount = if (isFirst && isLast) 1 else 3,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = SettingsDimension.preferenceVerticalPadding),
            content = content,
        )
    }
}

class CardItem(val key: String, val content: @Composable ColumnScope.() -> Unit)

fun LazyListScope.groupedCardItems(
    keyPrefix: String,
    items: List<CardItem>,
    outerTopPadding: Dp = 0.dp,
    outerBottomPadding: Dp = SettingsDimension.sectionSpacing,
    outerHorizontalPadding: Dp = SettingsDimension.pagePadding,
    insidePadding: PaddingValues = PaddingValues(0.dp),
) {
    items.forEachIndexed { index, cardItem ->
        item(key = "$keyPrefix:${cardItem.key}") {
            CompositionLocalProvider(LocalPreferencePosition provides (index to items.size)) {
                Column(
                    Modifier.fillMaxWidth()
                        .padding(
                            start = outerHorizontalPadding,
                            end = outerHorizontalPadding,
                            top =
                                if (index == 0) outerTopPadding else SettingsDimension.groupSpacing,
                            bottom = if (index == items.lastIndex) outerBottomPadding else 0.dp,
                        )
                        .padding(insidePadding),
                    content = cardItem.content,
                )
            }
        }
    }
}
