package bav.petus.viewModel.zoo

import bav.petus.base.ViewModelWithNavigation
import bav.petus.core.engine.Engine
import bav.petus.core.engine.UserStats
import bav.petus.core.resources.ImageId
import bav.petus.model.Pet
import bav.petus.repo.PetsRepository
import bav.petus.useCase.PetImageUseCase
import com.rickclephas.kmp.observableviewmodel.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class ZooUiState(
    val pets: List<PetThumbnailUiData>,
    val showCreateNewPetButton: Boolean,
)

data class PetThumbnailUiData(
    val pet: Pet,
    val petImageResId: ImageId,
    val satietyFraction: Float,
    val psychFraction: Float,
    val healthFraction: Float,
)

class ZooScreenViewModel : ViewModelWithNavigation<ZooScreenViewModel.Navigation>(), KoinComponent {

    private val petsRepo: PetsRepository by inject()
    private val engine: Engine by inject()
    private val petImageUseCase: PetImageUseCase by inject()
    private val userStats: UserStats by inject()

    val uiState: StateFlow<ZooUiState?> = combine(
        petsRepo.getAllPetsInZooFlow(),
        userStats.getUserProfileFlow(),
    ) { pets, profile ->
        ZooUiState(
            pets = pets.map {
                PetThumbnailUiData(
                    pet = it,
                    petImageResId = petImageUseCase.getPetImageId(it),
                    satietyFraction = engine.getPetSatietyFraction(it),
                    psychFraction = engine.getPetPsychFraction(it),
                    healthFraction = engine.getPetHealthFraction(it),
                )
            },
            showCreateNewPetButton = pets.size < profile.zooSize
        )
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun onAction(action: Action) {
        when (action) {
            is Action.OpenPetDetails -> navigate(Navigation.ToDetails(action.petId))
            Action.TapCreateNewPetButton -> navigate(Navigation.ToNewPetCreation)
        }
    }

    sealed interface Action {
        data class OpenPetDetails(val petId: Long) : Action
        data object TapCreateNewPetButton : Action
    }

    sealed interface Navigation {
        data class ToDetails(val petId: Long) : Navigation
        data object ToNewPetCreation : Navigation
    }
}