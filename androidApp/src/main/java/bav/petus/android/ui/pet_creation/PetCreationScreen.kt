package bav.petus.android.ui.pet_creation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bav.petus.android.ui.common.ActionButton
import bav.petus.android.ui.common.PetTypePicker
import bav.petus.android.ui.common.UiState
import bav.petus.android.ui.zoo.ZooScreenViewModel
import bav.petus.model.PetType

@Composable
fun PetCreationRoute(
    viewModel: PetCreationScreenViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    PetCreationScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PetCreationScreen(
    uiState: UiState<PetCreationUiState>,
    onAction: (PetCreationScreenViewModel.Action) -> Unit,
) {
    when (uiState) {
        is UiState.Failure -> {}
        UiState.Initial -> {}
        UiState.Loading -> {}
        is UiState.Success -> {
            val state = uiState.data
            Scaffold(
                topBar = {
                    TopAppBar(title = {
                        Text(
                            "Create pet",
                            style = MaterialTheme.typography.headlineLarge
                        )
                    })
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        text = "Select pet type",
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center,
                    )
                    PetTypePicker(selectedValue = state.type) {
                        onAction(PetCreationScreenViewModel.Action.UpdateType(it))
                    }
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        text = "Enter pet name:",
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center,
                    )
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        value = state.name,
                        onValueChange = {
                            onAction(PetCreationScreenViewModel.Action.UpdateName(it))
                        },
                        textStyle = MaterialTheme.typography.headlineLarge,
                        singleLine = true,
                    )

                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        text = state.typeDescription,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Justify,
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    ActionButton(
                        text = "Create ${state.type} ${state.name}",
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.padding(16.dp),
                    ) {
                        onAction(PetCreationScreenViewModel.Action.TapCreateButton)
                    }
                }
            }
        }
    }
}
