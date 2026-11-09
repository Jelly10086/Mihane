/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rinn.rei.mihane.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.unit.IntOffset

/** Matches SettingsLib Spa's AnimatedNavGraphBuilder, including its entry delay. */
internal fun <S> AnimatedContentTransitionScope<S>.settingsTransition(
    back: Boolean
): ContentTransform {
    val direction =
        if (back) AnimatedContentTransitionScope.SlideDirection.End
        else AnimatedContentTransitionScope.SlideDirection.Start
    return (slideIntoContainer(
        direction,
        animationSpec = tween<IntOffset>(300, 75, LinearOutSlowInEasing),
        initialOffset = { it / 5 },
    ) + fadeIn(tween(300, 75, LinearOutSlowInEasing))) togetherWith
        (slideOutOfContainer(
            direction,
            animationSpec = tween<IntOffset>(300),
            targetOffset = { it / 5 },
        ) + fadeOut(tween(75, easing = FastOutLinearInEasing)))
}
