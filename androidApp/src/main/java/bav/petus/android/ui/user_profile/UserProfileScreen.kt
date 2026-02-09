package bav.petus.android.ui.user_profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bav.petus.android.PsychColor
import bav.petus.android.R
import bav.petus.android.ui.common.toResId
import bav.petus.core.engine.toStringId
import bav.petus.core.inventory.InventoryItem
import bav.petus.core.inventory.toImageId
import bav.petus.core.resources.StringId
import bav.petus.model.PetType
import bav.petus.viewModel.main.MainViewModel
import bav.petus.viewModel.userProfile.UserProfileScreenViewModel
import bav.petus.viewModel.userProfile.UserProfileUiState

@Composable
fun UserProfileRoute(
    viewModel: UserProfileScreenViewModel,
    mainViewModel: MainViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    uiState?.let {
        UserProfileScreen(
            uiState = it,
            onAction = viewModel::onAction,
            showOnboarding = { mainViewModel.onAction(MainViewModel.Action.ShowOnboarding) }
        )
    }
}

@Composable
private fun UserProfileScreen(
    uiState: UserProfileUiState,
    onAction: (UserProfileScreenViewModel.Action) -> Unit,
    showOnboarding: () -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            uiState.latestWeather?.let { weather ->
                Text(
                    text = weather,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Image(
                painter = painterResource(id = R.drawable.user_profile_avatar),
                contentDescription = null,
                modifier = Modifier.size(112.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = showOnboarding,
                modifier = Modifier
                    .wrapContentSize(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PsychColor,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = stringResource(StringId.OnboardingHowToButtonTitle.toResId()),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(R.string.ProfileScreenLanguagesLabel))
            LanguageKnowledgeStat(
                type = PetType.Catus,
                value = uiState.languageKnowledgeCatus,
            )
            LanguageKnowledgeStat(
                type = PetType.Dogus,
                value = uiState.languageKnowledgeDogus,
            )
            LanguageKnowledgeStat(
                type = PetType.Frogus,
                value = uiState.languageKnowledgeFrogus,
            )
            LanguageKnowledgeStat(
                type = PetType.Bober,
                value = uiState.languageKnowledgeBober,
            )
            LanguageKnowledgeStat(
                type = PetType.Fractal,
                value = uiState.languageKnowledgeFractal,
            )
            LanguageKnowledgeStat(
                type = PetType.Dragon,
                value = uiState.languageKnowledgeDragon,
            )
            LanguageKnowledgeStat(
                type = PetType.Alien,
                value = uiState.languageKnowledgeAlien,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(R.string.ProfileScreenInventoryLabel))
            LazyVerticalGrid(
                modifier = Modifier.heightIn(max = 10000.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                columns = GridCells.Fixed(count = 3)
            ) {
                items(uiState.inventory) { item ->
                    InventoryItemCell(item) {
                        onAction(UserProfileScreenViewModel.Action.TapInventoryItem(it.id))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(R.string.ProfileScreenAbilitiesLabel))
            uiState.abilities.forEach { item ->
                Text(
                    text = stringResource(id = item.toStringId().toResId()),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(R.string.ProfileScreenMiscLabel))
            CanPetsDieOfOldAgeRow(value = uiState.canPetsDieOfOldAge) {
                onAction(UserProfileScreenViewModel.Action.TapCanPetDieOfOldAgeSwitch(it))
            }
        }
    }
}

@Composable
private fun CanPetsDieOfOldAgeRow(
    value: Boolean,
    onClick: (Boolean) -> Unit
) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.CanPetsDieOfOldAge),
            modifier = Modifier.weight(3f)
        )
        Switch(
            checked = value,
            onCheckedChange = {
                onClick(it)
            },
            thumbContent = if (value) {
                {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                    )
                }
            } else {
                null
            }
        )
    }
}

@Composable
private fun UserProfileRow(title: String, message: String) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(3f)
        )
        Text(
            text = message,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun LanguageKnowledgeStat(type: PetType, value: String) {
    val title = stringResource(id = when (type) {
            PetType.Catus -> R.string.LanguageKnowledgeTitleCatus
            PetType.Dogus -> R.string.LanguageKnowledgeTitleDogus
            PetType.Frogus -> R.string.LanguageKnowledgeTitleFrogus
            PetType.Bober -> R.string.LanguageKnowledgeTitleBober
            PetType.Fractal -> R.string.LanguageKnowledgeTitleFractal
            PetType.Dragon -> R.string.LanguageKnowledgeTitleDragon
            PetType.Alien -> R.string.LanguageKnowledgeTitleAlien
        }
    )
    UserProfileRow(
        title = title,
        message = value
    )
}

@Composable
private fun InventoryItemCell(
    item: InventoryItem,
    onClick: (InventoryItem) -> Unit,
) {
    Image(
        painter = painterResource(id = item.id.toImageId().toResId()),
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(percent = 20))
            .clickable {
                onClick(item)
            }
    )
}