package bav.petus.android.ui.common

sealed class UiState<out T : Any> {

    data object Initial : UiState<Nothing>()

    data object Loading : UiState<Nothing>()

    data class Failure(
        val errorMessage: String = "",
    ) : UiState<Nothing>()

    data class Success<T : Any>(val data: T) : UiState<T>()
}