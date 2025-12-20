package bav.petus.android.ui.pet_details

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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.TagFaces
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
                        icon = Icons.Filled.LocalDining,
                        fraction = uiState.satietyFraction,
                    )
                    StatBar(
                        color = PsychColor,
                        icon = Icons.Filled.TagFaces,
                        fraction = uiState.psychFraction,
                    )
                    StatBar(
                        color = HealthColor,
                        icon = Icons.Filled.Favorite,
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .horizontalScroll(scrollState),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    if (uiState.showSpeakButton) {
                        AnimatedImageButton(
                            painter = painterResource(id = uiState.petType.speakButtonImageId()),
                            title = stringResource(id = R.string.ButtonTitleSpeak),
                        ) {
                            onAction(PetDetailsScreenViewModel.Action.TapSpeakButton)
                        }
                    }
                    if (uiState.showPoopButton) {
                        AnimatedImageButton(
                            painter = painterResource(id = uiState.petType.poopButtonImageId()),
                            title = stringResource(id = R.string.ButtonTitlePoop),
                        ) {
                            onAction(PetDetailsScreenViewModel.Action.TapPoopButton)
                        }
                    }
                    if (uiState.showFeedButton) {
                        AnimatedImageButton(
                            painter = painterResource(id = uiState.petType.feedButtonImageId()),
                            title = stringResource(id = R.string.ButtonTitleFeed),
                        ) {
                            onAction(PetDetailsScreenViewModel.Action.TapFeedButton)
                        }
                    }
                    if (uiState.showHealButton) {
                        AnimatedImageButton(
                            painter = painterResource(id = uiState.petType.healButtonImageId()),
                            title = stringResource(id = R.string.ButtonTitleHeal),
                        ) {
                            onAction(PetDetailsScreenViewModel.Action.TapHealButton)
                        }
                    }
                    if (uiState.showPlayButton) {
                        AnimatedImageButton(
                            painter = painterResource(id = uiState.petType.playButtonImageId()),
                            title = stringResource(id = R.string.ButtonTitlePlay),
                        ) {
                            onAction(PetDetailsScreenViewModel.Action.TapPlayButton)
                        }
                    }
                    if (uiState.showWakeUpButton) {
                        AnimatedImageButton(
                            painter = painterResource(id = uiState.petType.wakeUpButtonImageId()),
                            title = stringResource(id = R.string.ButtonTitleWakeUp),
                        ) {
                            onAction(PetDetailsScreenViewModel.Action.TapWakeUpButton)
                        }
                    }
                    if (uiState.showBuryButton) {
                        AnimatedImageButton(
                            painter = painterResource(id = uiState.petType.buryButtonImageId()),
                            title = stringResource(id = R.string.ButtonTitleBury),
                        ) {
                            onAction(PetDetailsScreenViewModel.Action.TapBuryButton)
                        }
                    }
                    if (uiState.showForgetButton) {
                        AnimatedImageButton(
                            painter = painterResource(id = R.drawable.forget_fractal),
                            title = stringResource(id = R.string.ButtonTitleForget),
                        ) {
                            onAction(PetDetailsScreenViewModel.Action.TapForgetButton)
                        }
                    }
                    if (uiState.showResurrectButton) {
                        AnimatedImageButton(
                            painter = painterResource(id = uiState.petType.resurrectButtonImageId()),
                            title = stringResource(id = R.string.ButtonTitleResurrect),
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
        PetType.Dragon -> R.drawable.feed_dragon
    }
}

private fun PetType.playButtonImageId(): Int {
    return when (this) {
        PetType.Catus -> R.drawable.play_cat
        PetType.Dogus -> R.drawable.play_dog
        PetType.Frogus -> R.drawable.play_frog
        PetType.Bober -> R.drawable.play_bober
        PetType.Fractal -> R.drawable.play_fractal
        PetType.Dragon -> R.drawable.play_dragon
    }
}

private fun PetType.healButtonImageId(): Int {
    return when (this) {
        PetType.Catus -> R.drawable.heal_cat
        PetType.Dogus -> R.drawable.heal_dog
        PetType.Frogus -> R.drawable.heal_frog
        PetType.Bober -> R.drawable.heal_bober
        PetType.Fractal -> R.drawable.heal_fractal
        PetType.Dragon -> R.drawable.heal_dragon
    }
}

private fun PetType.poopButtonImageId(): Int {
    return when (this) {
        PetType.Catus -> R.drawable.clean_up_cat
        PetType.Dogus -> R.drawable.clean_up_dog
        PetType.Frogus -> R.drawable.clean_up_frog
        PetType.Bober -> R.drawable.clean_up_bober
        PetType.Fractal -> R.drawable.clean_up_fractal
        PetType.Dragon -> R.drawable.clean_up_dragon
    }
}

private fun PetType.wakeUpButtonImageId(): Int {
    return when (this) {
        PetType.Catus -> R.drawable.wake_up_cat
        PetType.Dogus -> R.drawable.wake_up_dog
        PetType.Frogus -> R.drawable.wake_up_frog
        PetType.Bober -> R.drawable.wake_up_bober
        PetType.Fractal -> R.drawable.wake_up_fractal
        PetType.Dragon -> R.drawable.wake_up_dragon
    }
}

private fun PetType.buryButtonImageId(): Int {
    return when (this) {
        PetType.Catus -> R.drawable.bury_cat
        PetType.Dogus -> R.drawable.bury_dog
        PetType.Frogus -> R.drawable.bury_frog
        PetType.Bober -> R.drawable.bury_bober
        PetType.Fractal -> R.drawable.bury_bober // Can't bury fractal
        PetType.Dragon -> R.drawable.bury_dragon
    }
}

private fun PetType.speakButtonImageId(): Int {
    return when (this) {
        PetType.Catus -> R.drawable.speak_cat
        PetType.Dogus -> R.drawable.speak_dog
        PetType.Frogus -> R.drawable.speak_frog
        PetType.Bober -> R.drawable.speak_bober
        PetType.Fractal -> R.drawable.speak_fractal
        PetType.Dragon -> R.drawable.speak_dragon
    }
}

private fun PetType.resurrectButtonImageId(): Int {
    return when (this) {
        PetType.Catus -> R.drawable.resurrect_cat
        PetType.Dogus -> R.drawable.resurrect_dog
        PetType.Frogus -> R.drawable.resurrect_frog
        PetType.Bober -> R.drawable.resurrect_bober
        PetType.Fractal -> R.drawable.resurrect_bober // Can't resurrect fractal
        PetType.Dragon -> R.drawable.resurrect_dragon
    }
}