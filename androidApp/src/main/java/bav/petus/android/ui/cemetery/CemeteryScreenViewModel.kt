package bav.petus.android.ui.cemetery

import androidx.lifecycle.viewModelScope
import bav.petus.android.base.ViewModelWithNavigation
import bav.petus.android.ui.common.PetImageUseCase
import bav.petus.android.ui.common.UiState
import bav.petus.android.ui.zoo.PetThumbnailUiData
import bav.petus.core.engine.Engine
import bav.petus.repo.PetsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class CemeteryUiState(
    val pets: List<PetThumbnailUiData>,
)

class CemeteryScreenViewModel(
    private val petsRepo: PetsRepository,
    private val engine: Engine,
    private val petImageUseCase: PetImageUseCase,
) : ViewModelWithNavigation<CemeteryScreenViewModel.Navigation>() {

    val uiState: StateFlow<UiState<CemeteryUiState>> = petsRepo.getAllDeadPetsFlow()
        .map { pets ->
            UiState.Success(
                CemeteryUiState(
                    pets = pets.map {
                        PetThumbnailUiData(
                            pet = it,
                            petImageResId = petImageUseCase.getPetImageResId(it),
                            satietyFraction = engine.getPetSatietyFraction(it),
                            psychFraction = engine.getPetPsychFraction(it),
                            healthFraction = engine.getPetHealthFraction(it),
                        )
                    },
                )
            )
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, UiState.Initial)

    fun onAction(action: Action) {
        when (action) {
            is Action.TapOnPet -> navigate(Navigation.ToDetails(action.petId))
        }
    }

    sealed interface Action {
        data class TapOnPet(val petId: Long) : Action
    }

    sealed interface Navigation {
        data class ToDetails(val petId: Long) : Navigation
    }
}