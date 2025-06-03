package bav.petus.android.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController

fun <T> NavBackStackEntry.getOnce(
    key: String,
    block: (T) -> Unit,
) {
    savedStateHandle.apply {
        get<T>(key)?.let { value ->
            remove<T>(key)
            block(value)
        }
    }
}

fun <T> NavHostController.putForPreviousAndClose(
    key: String,
    value: T?,
) {
    previousBackStackEntry?.savedStateHandle?.set(key, value)
    popBackStack()
}

fun NavBackStackEntry?.isTabSelected(tabName: String): Boolean {
    return this?.let { entry -> entry.destination.route == tabName } ?: false
}
