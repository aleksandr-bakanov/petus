package bav.petus.android.ui.pet_details

import android.widget.ScrollView
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import bav.petus.android.HealthColor
import bav.petus.android.PsychColor
import bav.petus.android.R
import bav.petus.android.SatietyColor
import bav.petus.android.ui.common.ActionButton
import bav.petus.android.ui.common.AnimatedImageButton
import bav.petus.android.ui.common.StatBar
import bav.petus.android.ui.common.UiState
import bav.petus.model.PetType

@Composable
fun PetDetailsRoute(
    viewModel: PetDetailsScreenViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    PetDetailsScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PetDetailsScreen(
    uiState: UiState<PetDetailsUiState>,
    onAction: (PetDetailsScreenViewModel.Action) -> Unit,
) {
    when (uiState) {
        is UiState.Failure -> {}
        UiState.Initial -> {}
        UiState.Loading -> {}
        is UiState.Success -> {
            val state = uiState.data

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = state.title,
                                style = MaterialTheme.typography.headlineLarge
                            )
                        }
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState()), // Make scrollable
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Pet Image
                        Image(
                            painter = painterResource(id = state.petImageResId),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                        // Stats
                        StatBar(
                            title = "SAT",
                            color = SatietyColor,
                            fraction = state.satietyFraction,
                        )
                        StatBar(
                            title = "PSY",
                            color = PsychColor,
                            fraction = state.psychFraction,
                        )
                        StatBar(
                            title = "HLT",
                            color = HealthColor,
                            fraction = state.healthFraction,
                        )
                    }

                    // Action Buttons
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val scrollState = rememberScrollState()
                        val targetButtonRowHeight = if (state.isAnyButtonShown) 112.dp else 0.dp
                        val animatedButtonRowHeight by animateDpAsState(targetValue = targetButtonRowHeight, animationSpec = tween(300))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(animatedButtonRowHeight)
                                .horizontalScroll(scrollState),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            if (state.showFeedButton) {
                                AnimatedImageButton(
                                    painter = painterResource(id = state.petType.feedButtonImageId()),
                                ) {
                                    onAction(PetDetailsScreenViewModel.Action.TapFeedButton)
                                }
                            }
                            if (state.showHealButton) {
                                AnimatedImageButton(
                                    painter = painterResource(id = state.petType.healButtonImageId()),
                                ) {
                                    onAction(PetDetailsScreenViewModel.Action.TapHealButton)
                                }
                            }
                            if (state.showPlayButton) {
                                AnimatedImageButton(
                                    painter = painterResource(id = state.petType.playButtonImageId()),
                                ) {
                                    onAction(PetDetailsScreenViewModel.Action.TapPlayButton)
                                }
                            }
                            if (state.showPoopButton) {
                                AnimatedImageButton(
                                    painter = painterResource(id = state.petType.poopButtonImageId()),
                                ) {
                                    onAction(PetDetailsScreenViewModel.Action.TapPoopButton)
                                }
                            }
                            if (state.showWakeUpButton) {
                                AnimatedImageButton(
                                    painter = painterResource(id = state.petType.wakeUpButtonImageId()),
                                ) {
                                    onAction(PetDetailsScreenViewModel.Action.TapWakeUpButton)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Pet Info Texts
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoText(text = state.creationTime)
                        InfoText(text = state.ageState)
                        InfoText(text = state.sleepState)
                        InfoText(text = state.satiety)
                        InfoText(text = state.psych)
                        InfoText(text = state.health)
                        InfoText(text = state.illness)
                        InfoText(text = state.pooped)
                        InfoText(text = state.timeOfDeath)
                    }

                    Spacer(modifier = Modifier.height(32.dp)) // breathing space at bottom
                }
            }
        }
    }
}

@Composable
private fun InfoText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.fillMaxWidth()
    )
}

private fun PetType.feedButtonImageId(): Int {
    return when (this) {
        PetType.Catus -> R.drawable.feed_cat
        PetType.Dogus -> R.drawable.feed_dog
        PetType.Frogus -> R.drawable.feed_frog
    }
}

private fun PetType.playButtonImageId(): Int {
    return when (this) {
        PetType.Catus -> R.drawable.play_cat
        PetType.Dogus -> R.drawable.play_dog
        PetType.Frogus -> R.drawable.play_frog
    }
}

private fun PetType.healButtonImageId(): Int {
    return when (this) {
        PetType.Catus -> R.drawable.heal_cat
        PetType.Dogus -> R.drawable.heal_dog
        PetType.Frogus -> R.drawable.heal_frog
    }
}

private fun PetType.poopButtonImageId(): Int {
    return when (this) {
        PetType.Catus -> R.drawable.clean_up_cat
        PetType.Dogus -> R.drawable.clean_up_dog
        PetType.Frogus -> R.drawable.clean_up_frog
    }
}

private fun PetType.wakeUpButtonImageId(): Int {
    return when (this) {
        PetType.Catus -> R.drawable.wake_up_cat
        PetType.Dogus -> R.drawable.wake_up_dog
        PetType.Frogus -> R.drawable.wake_up_frog
    }
}