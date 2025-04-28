package bav.petus.android.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

open class ViewModelWithNavigation<NavigationType> : ViewModel() {

    private val _navigation = MutableSharedFlow<NavigationType>()
    val navigation: SharedFlow<NavigationType> = _navigation.asSharedFlow()

    protected fun navigate(value: NavigationType) {
        viewModelScope.launch { _navigation.emit(value) }
    }
}