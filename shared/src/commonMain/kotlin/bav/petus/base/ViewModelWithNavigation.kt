package bav.petus.base

import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

open class ViewModelWithNavigation<NavigationType> : ViewModel() {

    private val _navigation = MutableSharedFlow<NavigationType>()
    val navigation: SharedFlow<NavigationType> = _navigation.asSharedFlow()

    protected fun navigate(value: NavigationType) {
        viewModelScope.launch { _navigation.emit(value) }
    }
}