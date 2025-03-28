package bav.petus.android

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bav.petus.model.Pet
import bav.petus.PetsSDK
import kotlinx.coroutines.launch

class PetsViewModel(private val sdk: PetsSDK) : ViewModel() {
    private val _state = mutableStateOf(PetsUiState())
    val state: State<PetsUiState> = _state

    init {
        loadPets()
        getWeather()
    }

    fun updateRationale(value: Boolean) {
        _state.value = _state.value.copy(showRationale = value)
    }

    fun getWeather() {
        viewModelScope.launch {
            try {
                val dto = sdk.getWeather(52.3676, 4.9041)
                Log.i("cqhg43", dto.toString())
            } catch (e: Exception) {
                Log.e("cqhg43", "PetsViewModel::getWeather ${e.localizedMessage}")
            }
        }
    }

    fun loadPets() {
        viewModelScope.launch {
            try {
                val pets = sdk.getPets()
                _state.value = _state.value.copy(pets = pets)
            } catch (e: Exception) {
                _state.value = _state.value.copy(pets = emptyList())
            }
        }
    }

    fun addPet() {
        val name = _state.value.newPetName
        if (name.isNotEmpty()) {
            viewModelScope.launch {
                sdk.addPet(Pet(name = name))
            }
        }
    }

    fun setNewPetName(value: String) {
        _state.value = _state.value.copy(newPetName = value)
    }
}

data class PetsUiState(
    val pets: List<Pet> = emptyList(),
    val newPetName: String = "",
    val showRationale: Boolean = false,
)