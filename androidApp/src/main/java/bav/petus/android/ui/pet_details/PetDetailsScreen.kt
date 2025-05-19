package bav.petus.android.ui.pet_details

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bav.petus.android.HealthColor
import bav.petus.android.PsychColor
import bav.petus.android.R
import bav.petus.android.SatietyColor
import bav.petus.android.ui.common.ActionButton
import bav.petus.android.ui.common.AnimatedImageButton
import bav.petus.android.ui.common.StatBar
import bav.petus.android.ui.common.toResId
import bav.petus.model.AgeState
import bav.petus.model.PetType
import bav.petus.model.Place
import bav.petus.viewModel.petDetails.PetDetailsScreenViewModel
import bav.petus.viewModel.petDetails.PetDetailsUiState

@Composable
fun PetDetailsRoute(
    viewModel: PetDetailsScreenViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    uiState?.let {
        PetDetailsScreen(
            uiState = it,
            onAction = viewModel::onAction,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PetDetailsScreen(
    uiState: PetDetailsUiState,
    onAction: (PetDetailsScreenViewModel.Action) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = uiState.title,
                            style = MaterialTheme.typography.headlineLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Pet Image
                Image(
                    painter = painterResource(id = uiState.petImageResId.toResId()),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                // Stats
                StatBar(
                    title = "SAT",
                    color = SatietyColor,
                    fraction = uiState.satietyFraction,
                )
                StatBar(
                    title = "PSY",
                    color = PsychColor,
                    fraction = uiState.psychFraction,
                )
                StatBar(
                    title = "HLT",
                    color = HealthColor,
                    fraction = uiState.healthFraction,
                )
//                ActionButton(
//                    text = "Kill pet",
//                    color = Color.Red,
//                    onClick = { onAction(PetDetailsScreenViewModel.Action.Kill) }
//                )
//                ActionButton(
//                    text = "Resurrect pet",
//                    color = Color.Red,
//                    onClick = { onAction(PetDetailsScreenViewModel.Action.Resurrect) }
//                )
//                ActionButton(
//                    text = "Egg",
//                    color = Color.Red,
//                    onClick = { onAction(PetDetailsScreenViewModel.Action.ChangeAgeState(AgeState.Egg)) }
//                )
//                ActionButton(
//                    text = "Newborn",
//                    color = Color.Red,
//                    onClick = { onAction(PetDetailsScreenViewModel.Action.ChangeAgeState(AgeState.NewBorn)) }
//                )
//                ActionButton(
//                    text = "Adult",
//                    color = Color.Red,
//                    onClick = { onAction(PetDetailsScreenViewModel.Action.ChangeAgeState(AgeState.Adult)) }
//                )
//                ActionButton(
//                    text = "Old",
//                    color = Color.Red,
//                    onClick = { onAction(PetDetailsScreenViewModel.Action.ChangeAgeState(AgeState.Old)) }
//                )
//                ActionButton(
//                    text = "To cemetery",
//                    color = Color.Red,
//                    onClick = { onAction(PetDetailsScreenViewModel.Action.ChangePlace(Place.Cemetery)) }
//                )
//                ActionButton(
//                    text = "To zoo",
//                    color = Color.Red,
//                    onClick = { onAction(PetDetailsScreenViewModel.Action.ChangePlace(Place.Zoo)) }
//                )
            }

            // Action Buttons
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val scrollState = rememberScrollState()
                val targetButtonRowHeight = if (uiState.isAnyButtonShown) 112.dp else 0.dp
                val animatedButtonRowHeight by animateDpAsState(targetValue = targetButtonRowHeight, animationSpec = tween(300))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(animatedButtonRowHeight)
                        .horizontalScroll(scrollState),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    if (uiState.showFeedButton) {
                        AnimatedImageButton(
                            painter = painterResource(id = uiState.petType.feedButtonImageId()),
                        ) {
                            onAction(PetDetailsScreenViewModel.Action.TapFeedButton)
                        }
                    }
                    if (uiState.showHealButton) {
                        AnimatedImageButton(
                            painter = painterResource(id = uiState.petType.healButtonImageId()),
                        ) {
                            onAction(PetDetailsScreenViewModel.Action.TapHealButton)
                        }
                    }
                    if (uiState.showPlayButton) {
                        AnimatedImageButton(
                            painter = painterResource(id = uiState.petType.playButtonImageId()),
                        ) {
                            onAction(PetDetailsScreenViewModel.Action.TapPlayButton)
                        }
                    }
                    if (uiState.showPoopButton) {
                        AnimatedImageButton(
                            painter = painterResource(id = uiState.petType.poopButtonImageId()),
                        ) {
                            onAction(PetDetailsScreenViewModel.Action.TapPoopButton)
                        }
                    }
                    if (uiState.showWakeUpButton) {
                        AnimatedImageButton(
                            painter = painterResource(id = uiState.petType.wakeUpButtonImageId()),
                        ) {
                            onAction(PetDetailsScreenViewModel.Action.TapWakeUpButton)
                        }
                    }
                    if (uiState.showBuryButton) {
                        AnimatedImageButton(
                            painter = painterResource(id = uiState.petType.buryButtonImageId()),
                        ) {
                            onAction(PetDetailsScreenViewModel.Action.TapBuryButton)
                        }
                    }
                    if (uiState.showSpeakButton) {
                        AnimatedImageButton(
                            painter = painterResource(id = uiState.petType.speakButtonImageId()),
                        ) {
                            onAction(PetDetailsScreenViewModel.Action.TapSpeakButton)
                        }
                    }
                    if (uiState.showResurrectButton) {
                        AnimatedImageButton(
                            painter = painterResource(id = uiState.petType.resurrectButtonImageId()),
                        ) {
                            onAction(PetDetailsScreenViewModel.Action.TapResurrectButton)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
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

private fun PetType.buryButtonImageId(): Int {
    return when (this) {
        PetType.Catus -> R.drawable.bury_cat
        PetType.Dogus -> R.drawable.bury_dog
        PetType.Frogus -> R.drawable.bury_frog
    }
}

private fun PetType.speakButtonImageId(): Int {
    return when (this) {
        PetType.Catus -> R.drawable.speak_cat
        PetType.Dogus -> R.drawable.speak_dog
        PetType.Frogus -> R.drawable.speak_frog
    }
}

private fun PetType.resurrectButtonImageId(): Int {
    return when (this) {
        PetType.Catus -> R.drawable.resurrect_cat
        PetType.Dogus -> R.drawable.resurrect_dog
        PetType.Frogus -> R.drawable.resurrect_frog
    }
}