package bav.petus.android.ui.pet_creation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import bav.petus.android.ui.common.ActionButton
import bav.petus.android.ui.common.PetTypePicker
import bav.petus.android.ui.common.toResId
import bav.petus.viewModel.petCreation.PetCreationScreenViewModel
import bav.petus.viewModel.petCreation.PetCreationUiState

@Composable
fun PetCreationRoute(
    viewModel: PetCreationScreenViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    uiState?.let {
        PetCreationScreen(
            uiState = it,
            onAction = viewModel::onAction,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PetCreationScreen(
    uiState: PetCreationUiState,
    onAction: (PetCreationScreenViewModel.Action) -> Unit,
) {
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
            PetTypePicker(
                selectedValue = uiState.type,
                availablePetTypes = uiState.availablePetTypes,
            ) {
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
                value = uiState.name,
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
                text = stringResource(uiState.typeDescription.toResId()),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Justify,
            )

            Spacer(modifier = Modifier.weight(1f))

            ActionButton(
                text = "Create ${uiState.type} ${uiState.name}",
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(16.dp),
            ) {
                onAction(PetCreationScreenViewModel.Action.TapCreateButton)
            }
        }
    }
}
