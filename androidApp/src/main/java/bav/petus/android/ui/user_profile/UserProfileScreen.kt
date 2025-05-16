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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bav.petus.android.R
import bav.petus.android.ui.common.ActionButton
import bav.petus.android.ui.common.UiState
import bav.petus.android.ui.pet_creation.PetCreationScreenViewModel
import bav.petus.core.inventory.InventoryItem
import bav.petus.model.PetType

@Composable
fun UserProfileRoute(
    viewModel: UserProfileScreenViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    UserProfileScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserProfileScreen(
    uiState: UiState<UserProfileUiState>,
    onAction: (UserProfileScreenViewModel.Action) -> Unit,
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
                        value = state.languageKnowledgeCatus,
                    )
                    LanguageKnowledgeStat(
                        type = PetType.Dogus,
                        value = state.languageKnowledgeDogus,
                    )
                    LanguageKnowledgeStat(
                        type = PetType.Frogus,
                        value = state.languageKnowledgeFrogus,
                    )
                    Text(text = "Inventory")
                    state.inventory.forEach { item ->
                        InventoryItemCell(item)
                    }
                    Text(text = "Abilities")
                    state.abilities.forEach { item ->
                        Text(text = "Ability -> ${item.name}")
                    }
//                    ActionButton(
//                        text = "Add necronomicon",
//                        color = Color(0xFF4CAF50),
//                        modifier = Modifier.padding(16.dp),
//                    ) {
//                        onAction(UserProfileScreenViewModel.Action.AddItem)
//                    }
//                    ActionButton(
//                        text = "Remove necronomicon",
//                        color = Color(0xFF4CAF50),
//                        modifier = Modifier.padding(16.dp),
//                    ) {
//                        onAction(UserProfileScreenViewModel.Action.RemoveItem)
//                    }
                }
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