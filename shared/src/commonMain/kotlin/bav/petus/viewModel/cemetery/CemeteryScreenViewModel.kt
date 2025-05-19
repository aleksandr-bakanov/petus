package bav.petus.viewModel.cemetery

import bav.petus.base.ViewModelWithNavigation
import bav.petus.core.engine.Engine
import bav.petus.repo.PetsRepository
import bav.petus.useCase.PetImageUseCase
import bav.petus.viewModel.zoo.PetThumbnailUiData
import com.rickclephas.kmp.observableviewmodel.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class CemeteryUiState(
    val pets: List<PetThumbnailUiData>,
)

class CemeteryScreenViewModel : ViewModelWithNavigation<CemeteryScreenViewModel.Navigation>(), KoinComponent {

    private val petsRepo: PetsRepository by inject()
    private val engine: Engine by inject()
    private val petImageUseCase: PetImageUseCase by inject()

    val uiState: StateFlow<CemeteryUiState?> = petsRepo.getAllPetsInCemeteryFlow()
        .map { pets ->
            CemeteryUiState(
                pets = pets.map {
                    PetThumbnailUiData(
                        pet = it,
                        petImageResId = petImageUseCase.getPetImageId(it),
                        satietyFraction = engine.getPetSatietyFraction(it),
                        psychFraction = engine.getPetPsychFraction(it),
                        healthFraction = engine.getPetHealthFraction(it),
                    )
                },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

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