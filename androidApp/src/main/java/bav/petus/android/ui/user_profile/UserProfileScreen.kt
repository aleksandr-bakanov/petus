package bav.petus.android.ui.user_profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import bav.petus.android.R
import bav.petus.core.inventory.InventoryItem
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
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            uiState.latestWeather?.let { weather ->
                Text(
                    text = weather,
                    style = MaterialTheme.typography.labelSmall
                )
            }
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
            Text(text = stringResource(R.string.ProfileScreenInventoryLabel))
            uiState.inventory.forEach { item ->
                InventoryItemCell(item)
            }
            Text(text = stringResource(R.string.ProfileScreenAbilitiesLabel))
            uiState.abilities.forEach { item ->
                Text(text = "Ability -> ${item.name}")
            }
        }
    }
}

@Composable
private fun LanguageKnowledgeStat(type: PetType, value: String) {
    val title = stringResource(id = when (type) {
            PetType.Catus -> R.string.LanguageKnowledgeTitleCatus
            PetType.Dogus -> R.string.LanguageKnowledgeTitleDogus
            PetType.Frogus -> R.string.LanguageKnowledgeTitleFrogus
        }
    )
    Row(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
        Text(
            text = title,
            modifier = Modifier.weight(2f)
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun InventoryItemCell(item: InventoryItem) {
    val title = item.id.name
    Row(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
        Text(
            text = title,
            modifier = Modifier.weight(2f)
        )
        Text(
            text = item.amount.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}