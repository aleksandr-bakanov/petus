package bav.petus.android.ui.user_profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserProfileScreen(
    uiState: UserProfileUiState,
    onAction: (UserProfileScreenViewModel.Action) -> Unit,
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
                            text = stringResource(R.string.profile_screen_title),
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
            Text(text = "Languages")
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
            Text(text = "Inventory")
            uiState.inventory.forEach { item ->
                InventoryItemCell(item)
            }
            Text(text = "Abilities")
            uiState.abilities.forEach { item ->
                Text(text = "Ability -> ${item.name}")
            }
        }
    }
}

@Composable
private fun LanguageKnowledgeStat(type: PetType, value: String) {
    val title = stringResource(id = when (type) {
            PetType.Catus -> R.string.language_knowledge_title_catus
            PetType.Dogus -> R.string.language_knowledge_title_dogus
            PetType.Frogus -> R.string.language_knowledge_title_frogus
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