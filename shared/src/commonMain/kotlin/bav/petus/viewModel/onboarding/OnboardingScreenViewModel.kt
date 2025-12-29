package bav.petus.viewModel.onboarding

import bav.petus.core.resources.ImageId
import com.rickclephas.kmp.observableviewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class OnboardingUiState(
    val pages: List<OnboardingUiData>,
    val currentPage: Int = 0
)

data class OnboardingUiData(
    val image: ImageId,
    val title: String,
    val description: String,
)

class OnboardingScreenViewModel : ViewModel() {

    private val onboardingPages = listOf(
        OnboardingUiData(ImageId.FeedCat, "Title 1", "Subtitle 1"),
        OnboardingUiData(ImageId.FeedCat, "Title 2", "Subtitle 2"),
        OnboardingUiData(ImageId.FeedCat, "Title 3", "Subtitle 3"),
        OnboardingUiData(ImageId.PlayCat, "Title 4", "Subtitle 4"),
        OnboardingUiData(ImageId.BuryCat, "Title 5", "Subtitle 5"),
        OnboardingUiData(ImageId.HealCat, "Title 6", "Subtitle 6"),
        OnboardingUiData(ImageId.CleanUpCat, "Title 7", "Subtitle 7"),
        OnboardingUiData(ImageId.SpeakCat, "Title 8", "Subtitle 8"),
        OnboardingUiData(ImageId.CatEgg, "Title 9", "Subtitle 9"),
        OnboardingUiData(ImageId.UserProfileAvatar, "Title 10", "Subtitle 10"),
    )

    private val _uiState = MutableStateFlow(
        OnboardingUiState(
            pages = onboardingPages,
            currentPage = 0
        )
    )
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onNextClicked() {
        val current = _uiState.value.currentPage
        if (current < onboardingPages.lastIndex) {
            _uiState.value = _uiState.value.copy(currentPage = current + 1)
        } else
        {
            //TBD
        }
    }

    fun skip() {
        //TBD
    }
}