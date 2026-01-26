package bav.petus.viewModel.main

import bav.petus.base.ViewModelWithNavigation
import bav.petus.core.engine.Engine
import bav.petus.core.engine.GameUpdateState
import bav.petus.core.engine.UserStats
import bav.petus.core.notification.UserNotification
import bav.petus.core.resources.ImageId
import bav.petus.core.resources.StringId
import bav.petus.repo.PetsRepository
import com.rickclephas.kmp.observableviewmodel.launch
import com.rickclephas.kmp.observableviewmodel.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class MainScreenUiState(
    val showCemetery: Boolean?,
    val notifications: List<UserNotification>,
    val gameUpdateState: GameUpdateState?,
    val bottomSheetType: BottomSheetType?,
)

sealed class BottomSheetType {
    data class Onboarding(
        val pages: List<OnboardingPage>,
        val currentPage: Int = 0,
        val leftButtonTitle: StringId,
        val leftButtonAction: () -> Unit,
        val rightButtonTitle: StringId,
        val rightButtonAction: () -> Unit,
    ) : BottomSheetType()
}

data class OnboardingPage(
    val image: ImageId,
    val title: StringId,
    val message: StringId,
)

class MainViewModel : ViewModelWithNavigation<MainViewModel.Navigation>(), KoinComponent {

    private val petsRepo: PetsRepository by inject()
    private val userStats: UserStats by inject()
    private val engine: Engine by inject()

    private val bottomSheetFlow: MutableStateFlow<BottomSheetType?> = MutableStateFlow(null)

    private val deadPetsFlow: StateFlow<Boolean?> = petsRepo.getAllPetsInCemeteryFlow()
        .map { deadPets -> deadPets.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val uiState: StateFlow<MainScreenUiState?> = combine(
        deadPetsFlow,
        userStats.getUserNotificationsFlow(),
        engine.gameStateUpdateFlow,
        bottomSheetFlow,
    ) { showCemetery, notifications, gameUpdateState, bottomSheet ->
        if (showCemetery == null) {
            null
        } else {
            MainScreenUiState(
                showCemetery = showCemetery,
                notifications = notifications,
                gameUpdateState = gameUpdateState,
                bottomSheetType = bottomSheet,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    init {
        viewModelScope.launch {
            val isOnboardingShownAtLeastOnce = userStats.getOnboardingIsShownAtLeastOnce()
            if (isOnboardingShownAtLeastOnce.not()) {
                onAction(Action.ShowOnboarding)
            }
        }
    }

    fun onAction(action: Action) {
        when (action) {
            is Action.TapOnNotification -> {
                viewModelScope.launch {
                    userStats.removeNotification(id = action.id)
                }
            }
            Action.ShowOnboarding -> {
                bottomSheetFlow.value = BottomSheetType.Onboarding(
                    pages = onboardingPages,
                    currentPage = 0,
                    leftButtonTitle = StringId.OnboardingSkipTitle,
                    leftButtonAction = {
                        onAction(Action.HideBottomSheet)
                    },
                    rightButtonTitle = StringId.OnboardingNextTitle,
                    rightButtonAction = {
                        onAction(Action.ShowNextOnboardingPage)
                    }
                )
            }
            Action.ShowNextOnboardingPage -> {
                (bottomSheetFlow.value as? BottomSheetType.Onboarding)?.let { state ->
                    val currentPage = state.currentPage
                    if (currentPage < state.pages.lastIndex) {
                        bottomSheetFlow.value = state.copy(
                            currentPage = currentPage + 1,
                            leftButtonTitle = StringId.OnboardingPreviousTitle,
                            leftButtonAction = {
                                onAction(Action.ShowPreviousOnboardingPage)
                            },
                            rightButtonTitle = if (currentPage + 1 == state.pages.lastIndex)
                                StringId.OnboardingLetsGoTitle
                            else
                                StringId.OnboardingNextTitle,
                            rightButtonAction = {
                                if (currentPage + 1 == state.pages.lastIndex)
                                    onAction(Action.HideBottomSheet)
                                else
                                    onAction(Action.ShowNextOnboardingPage)
                            },
                        )
                    }
                }
            }
            Action.ShowPreviousOnboardingPage -> {
                (bottomSheetFlow.value as? BottomSheetType.Onboarding)?.let { state ->
                    val currentPage = state.currentPage
                    if (currentPage > 0) {
                        bottomSheetFlow.value = state.copy(
                            currentPage = currentPage - 1,
                            leftButtonTitle = if (currentPage - 1 == 0)
                                StringId.OnboardingSkipTitle
                            else
                                StringId.OnboardingPreviousTitle,
                            leftButtonAction = {
                                if (currentPage - 1 == 0)
                                    onAction(Action.HideBottomSheet)
                                else
                                    onAction(Action.ShowPreviousOnboardingPage)
                            },
                            rightButtonTitle = StringId.OnboardingNextTitle,
                            rightButtonAction = {
                                onAction(Action.ShowNextOnboardingPage)
                            },
                        )
                    }
                }
            }
            Action.HideBottomSheet -> {
                bottomSheetFlow.value = null
                viewModelScope.launch {
                    userStats.setOnboardingIsShownAtLeastOnce(true)
                }
            }
        }
    }

    sealed interface Action {
        data class TapOnNotification(val id: String) : Action
        data object ShowOnboarding : Action
        data object ShowNextOnboardingPage : Action
        data object ShowPreviousOnboardingPage : Action
        data object HideBottomSheet : Action
    }

    sealed interface Navigation {

    }

    companion object {
        val onboardingPages = listOf(
            OnboardingPage(
                image = ImageId.OnboardingHome,
                title = StringId.OnboardingHomeTitle,
                message = StringId.OnboardingHomeMessage,
            ),
            OnboardingPage(
                image = ImageId.OnboardingSatiety,
                title = StringId.OnboardingSatietyTitle,
                message = StringId.OnboardingSatietyMessage,
            ),
            OnboardingPage(
                image = ImageId.OnboardingPsyche,
                title = StringId.OnboardingPsycheTitle,
                message = StringId.OnboardingPsycheMessage,
            ),
            OnboardingPage(
                image = ImageId.OnboardingHealth,
                title = StringId.OnboardingHealthTitle,
                message = StringId.OnboardingHealthMessage,
            ),
            OnboardingPage(
                image = ImageId.OnboardingIllness,
                title = StringId.OnboardingIllnessTitle,
                message = StringId.OnboardingIllnessMessage,
            ),
            OnboardingPage(
                image = ImageId.OnboardingCleanUp,
                title = StringId.OnboardingCleanUpTitle,
                message = StringId.OnboardingCleanUpMessage,
            ),
            OnboardingPage(
                image = ImageId.OnboardingTalk,
                title = StringId.OnboardingTalkTitle,
                message = StringId.OnboardingTalkMessage,
            ),
            OnboardingPage(
                image = ImageId.OnboardingAge,
                title = StringId.OnboardingAgeTitle,
                message = StringId.OnboardingAgeMessage,
            ),
            OnboardingPage(
                image = ImageId.OnboardingGoodLuck,
                title = StringId.OnboardingGoodLuckTitle,
                message = StringId.OnboardingGoodLuckMessage,
            ),
        )
    }
}