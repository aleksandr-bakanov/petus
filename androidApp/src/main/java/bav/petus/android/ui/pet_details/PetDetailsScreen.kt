package bav.petus.android.ui.pet_details

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import bav.petus.android.HealthColor
import bav.petus.android.PsychColor
import bav.petus.android.R
import bav.petus.android.SatietyColor
import bav.petus.android.ui.common.AnimatedImageButton
import bav.petus.android.ui.common.StatBar
import bav.petus.android.ui.common.toResId
import bav.petus.model.PetType
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
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = uiState.title,
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(PetDetailsScreenViewModel.Action.CloseScreen) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
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
                if (uiState.showStatBars) {
                    // Stats
                    StatBar(
                        color = SatietyColor,
                        fraction = uiState.satietyFraction,
                    )
                    StatBar(
                        color = PsychColor,
                        fraction = uiState.psychFraction,
                    )
                    StatBar(
                        color = HealthColor,
                        fraction = uiState.healthFraction,
                    )
                }
                if (uiState.showWillHatchSoon) {
                    Text(
                        text = stringResource(R.string.WillHatchSoon),
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    )
                }
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
                    if (uiState.showSpeakButton) {
                        AnimatedImageButton(
                            painter = painterResource(id = uiState.petType.speakButtonImageId()),
                        ) {
                            onAction(PetDetailsScreenViewModel.Action.TapSpeakButton)
                        }
                    }
                    if (uiState.showPoopButton) {
                        AnimatedImageButton(
                            painter = painterResource(id = uiState.petType.poopButtonImageId()),
                        ) {
                            onAction(PetDetailsScreenViewModel.Action.TapPoopButton)
                        }
                    }
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
                    if (uiState.showForgetButton) {
                        AnimatedImageButton(
                            painter = painterResource(id = R.drawable.forget_fractal),
                        ) {
                            onAction(PetDetailsScreenViewModel.Action.TapForgetButton)
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

            // Cemetery info
            uiState.lifespan?.let { span ->
                Column {
                    Text(
                        text = span,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    )
                    uiState.historyEvents.forEach { e ->
                        Text(
                            text = e,
                            textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun PetType.feedButtonImageId(): Int {
    return when (this) {
        PetType.Catus -> R.drawable.feed_cat
        PetType.Dogus -> R.drawable.feed_dog
        PetType.Frogus -> R.drawable.feed_frog
        PetType.Bober -> R.drawable.feed_bober
        PetType.Fractal -> R.drawable.feed_fractal
    }
}

private fun PetType.playButtonImageId(): Int {
    return when (this) {
        PetType.Catus -> R.drawable.play_cat
        PetType.Dogus -> R.drawable.play_dog
        PetType.Frogus -> R.drawable.play_frog
        PetType.Bober -> R.drawable.play_bober
        PetType.Fractal -> R.drawable.play_fractal
    }
}

private fun PetType.healButtonImageId(): Int {
    return when (this) {
        PetType.Catus -> R.drawable.heal_cat
        PetType.Dogus -> R.drawable.heal_dog
        PetType.Frogus -> R.drawable.heal_frog
        PetType.Bober -> R.drawable.heal_bober
        PetType.Fractal -> R.drawable.heal_fractal
    }
}

private fun PetType.poopButtonImageId(): Int {
    return when (this) {
        PetType.Catus -> R.drawable.clean_up_cat
        PetType.Dogus -> R.drawable.clean_up_dog
        PetType.Frogus -> R.drawable.clean_up_frog
        PetType.Bober -> R.drawable.clean_up_bober
        PetType.Fractal -> R.drawable.clean_up_fractal
    }
}

private fun PetType.wakeUpButtonImageId(): Int {
    return when (this) {
        PetType.Catus -> R.drawable.wake_up_cat
        PetType.Dogus -> R.drawable.wake_up_dog
        PetType.Frogus -> R.drawable.wake_up_frog
        PetType.Bober -> R.drawable.wake_up_bober
        PetType.Fractal -> R.drawable.wake_up_fractal
    }
}

private fun PetType.buryButtonImageId(): Int {
    return when (this) {
        PetType.Catus -> R.drawable.bury_cat
        PetType.Dogus -> R.drawable.bury_dog
        PetType.Frogus -> R.drawable.bury_frog
        PetType.Bober -> R.drawable.bury_bober
        PetType.Fractal -> R.drawable.bury_bober // Can't bury fractal
    }
}

private fun PetType.speakButtonImageId(): Int {
    return when (this) {
        PetType.Catus -> R.drawable.speak_cat
        PetType.Dogus -> R.drawable.speak_dog
        PetType.Frogus -> R.drawable.speak_frog
        PetType.Bober -> R.drawable.speak_bober
        PetType.Fractal -> R.drawable.speak_fractal
    }
}

private fun PetType.resurrectButtonImageId(): Int {
    return when (this) {
        PetType.Catus -> R.drawable.resurrect_cat
        PetType.Dogus -> R.drawable.resurrect_dog
        PetType.Frogus -> R.drawable.resurrect_frog
        PetType.Bober -> R.drawable.resurrect_bober
        PetType.Fractal -> R.drawable.resurrect_bober // Can't resurrect fractal
    }
}