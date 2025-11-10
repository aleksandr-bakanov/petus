package bav.petus.android.ui.user_profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import bav.petus.android.R
import bav.petus.android.ui.common.toResId
import bav.petus.core.engine.toStringId
import bav.petus.core.inventory.InventoryItem
import bav.petus.core.inventory.toStringId
import bav.petus.model.PetType
import bav.petus.viewModel.userProfile.UserProfileScreenViewModel
import bav.petus.viewModel.userProfile.UserProfileUiState

@Composable
fun UserProfileRoute(
    viewModel: UserProfileScreenViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    uiState?.let {
        UserProfileScreen(
            uiState = it,
            onAction = viewModel::onAction,
        )
    }
}

@Composable
private fun UserProfileScreen(
    uiState: UserProfileUiState,
    onAction: (UserProfileScreenViewModel.Action) -> Unit,
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
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(R.string.ProfileScreenInventoryLabel))
            uiState.inventory.forEach { item ->
                InventoryItemCell(item)
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
            UserProfileRow(
                title = stringResource(R.string.ZooSizeTitle),
                message = uiState.zooSize,
            )
        }
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
        }
    )
    UserProfileRow(
        title = title,
        message = value
    )
}

@Composable
private fun InventoryItemCell(item: InventoryItem) {
    val title = stringResource(item.id.toStringId().toResId())
    UserProfileRow(
        title = title,
        message = item.amount.toString()
    )
}