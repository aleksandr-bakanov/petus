package bav.petus.android.ui.zoo

import androidx.lifecycle.viewModelScope
import bav.petus.android.base.ViewModelWithNavigation
import bav.petus.android.ui.common.PetImageUseCase
import bav.petus.android.ui.common.UiState
import bav.petus.core.engine.Engine
import bav.petus.model.Pet
import bav.petus.repo.PetsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ZooUiState(
    val pets: List<PetThumbnailUiData>,
    val showLocationRationaleButton: Boolean,
)

data class PetThumbnailUiData(
    val pet: Pet,
    val petImageResId: Int,
    val satietyFraction: Float,
    val psychFraction: Float,
    val healthFraction: Float,
)

data class ZooScreenViewModelArgs(
    val shouldShowBackgroundLocationPermissionRationale: () -> Boolean,
)

class ZooScreenViewModel(
    private val petsRepo: PetsRepository,
    private val engine: Engine,
    private val petImageUseCase: PetImageUseCase,
    private val args: ZooScreenViewModelArgs,
) : ViewModelWithNavigation<ZooScreenViewModel.Navigation>() {

    val uiState: StateFlow<UiState<ZooUiState>> = petsRepo.getAllPetsInZooFlow()
        .map { pets ->
            UiState.Success(
                ZooUiState(
                    pets = pets.map {
                        PetThumbnailUiData(
                            pet = it,
                            petImageResId = petImageUseCase.getPetImageResId(it),
                            satietyFraction = engine.getPetSatietyFraction(it),
                            psychFraction = engine.getPetPsychFraction(it),
                            healthFraction = engine.getPetHealthFraction(it),
                        )
                    },
                    showLocationRationaleButton = args.shouldShowBackgroundLocationPermissionRationale(),
                )
            )
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, UiState.Initial)

    fun onAction(action: Action) {
        when (action) {
            is Action.OpenPetDetails -> navigate(Navigation.ToDetails(action.petId))
            Action.TapCreateNewPetButton -> navigate(Navigation.ToNewPetCreation)
            Action.TapRationaleButton -> navigate(Navigation.OpenApplicationSettings)
        }
    }

    sealed interface Action {
        data class OpenPetDetails(val petId: Long) : Action
        data object TapRationaleButton : Action
        data object TapCreateNewPetButton : Action
    }

    sealed interface Navigation {
        data class ToDetails(val petId: Long) : Navigation
        data object OpenApplicationSettings : Navigation
        data object ToNewPetCreation : Navigation
    }
}