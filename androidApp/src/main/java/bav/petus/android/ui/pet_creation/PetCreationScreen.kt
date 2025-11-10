package bav.petus.android.ui.pet_creation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bav.petus.android.R
import bav.petus.android.ui.common.ActionButton
import bav.petus.android.ui.common.PetTypePicker
import bav.petus.android.ui.common.toResId
import bav.petus.model.PetType
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

@Composable
private fun PetCreationScreen(
    uiState: PetCreationUiState,
    onAction: (PetCreationScreenViewModel.Action) -> Unit,
) {
    Scaffold { padding ->
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
                text = stringResource(id = R.string.PetCreationScreenSelectPetType),
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
                text = stringResource(id = R.string.PetCreationScreenEnterPetName),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
            )
            val focusManager = LocalFocusManager.current
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                value = uiState.name,
                placeholder = { Text(stringResource(id = R.string.PetCreationScreenEnterPetNameHint)) },
                onValueChange = {
                    onAction(PetCreationScreenViewModel.Action.UpdateName(it))
                },
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_dice_svgrepo),
                        contentDescription = "dice icon",
                        modifier = Modifier
                            .size(54.dp)
                            .padding(end = 18.dp)
                            .clickable {
                                onAction(PetCreationScreenViewModel.Action.GetRandomName)
                                focusManager.clearFocus()
                            },
                        tint = Color.Gray
                    )
                },
                textStyle = MaterialTheme.typography.headlineLarge,
                singleLine = true
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
                text = createButtonText(
                    type = uiState.type,
                    name = uiState.name,
                ),
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(16.dp),
            ) {
                onAction(PetCreationScreenViewModel.Action.TapCreateButton)
            }
        }
    }
}

@Composable
private fun createButtonText(type: PetType, name: String): String {
    return when (type) {
        PetType.Fractal -> stringResource(
            id = R.string.PetCreationScreenButtonTemplateFractal,
            name,
        )
        else -> stringResource(
            id = R.string.PetCreationScreenButtonTemplate,
            petTypeString(type),
            name,
        )
    }
}

@Composable
private fun petTypeString(type: PetType): String {
    return stringResource(
        id = when (type) {
            PetType.Catus -> R.string.PetCreationScreenPetTypeCatus
            PetType.Dogus -> R.string.PetCreationScreenPetTypeDogus
            PetType.Frogus -> R.string.PetCreationScreenPetTypeFrogus
            PetType.Bober -> R.string.PetCreationScreenPetTypeBober
            PetType.Fractal -> R.string.PetCreationScreenPetTypeFractal
            PetType.Dragon -> R.string.PetCreationScreenPetTypeDragon
        }
    )
}
