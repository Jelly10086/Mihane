package rinn.rei.mihane.ui.navigation

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavKey

class Navigator(val backStack: SnapshotStateList<Route>) {
    fun push(key: Route) {
        if (key !in backStack) {
            backStack.add(key)
        }
    }

    fun replace(key: Route) {
        if (backStack.isNotEmpty()) {
            backStack[backStack.lastIndex] = key
        } else {
            backStack.add(key)
        }
    }

    fun pop() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }

    fun popUntil(predicate: (NavKey) -> Boolean) {
        while (backStack.size > 1 && !predicate(backStack.last())) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    fun current() = backStack.lastOrNull()

    fun backStackSize() = backStack.size
}

val LocalNavigator = staticCompositionLocalOf<Navigator> { error("LocalNavigator not provided") }
