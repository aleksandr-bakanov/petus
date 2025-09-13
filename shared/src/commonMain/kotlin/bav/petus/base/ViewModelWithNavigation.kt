package bav.petus.base

import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

open class ViewModelWithNavigation<NavigationType> : ViewModel() {

    private val _navigation = Channel<NavigationType>()
    val navigation: Flow<NavigationType> = _navigation.receiveAsFlow()

    protected fun navigate(value: NavigationType) {
        viewModelScope.launch { _navigation.send(value) }
    }
}