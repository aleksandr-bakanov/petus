package bav.petus.android.ui.cemetery

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import bav.petus.android.ui.common.UiState
import bav.petus.android.ui.views.PetListCell
import bav.petus.android.ui.zoo.PetThumbnailUiData

@Composable
fun CemeteryRoute(
    viewModel: CemeteryScreenViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    CemeteryScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
    )
}

@Composable
private fun CemeteryScreen(
    uiState: UiState<CemeteryUiState>,
    onAction: (CemeteryScreenViewModel.Action) -> Unit,
) {
    when (uiState) {
        is UiState.Failure -> {}
        UiState.Initial -> {}
        UiState.Loading -> {}
        is UiState.Success -> {
            val state = uiState.data
            Scaffold { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    if (state.pets.isEmpty()) {
                        Text(
                            "Cemetery is empty",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    else {
                        LazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {
                            items(state.pets) { data: PetThumbnailUiData ->
                                PetListCell(
                                    data = data,
                                    onClick = {
                                        onAction(CemeteryScreenViewModel.Action.TapOnPet(data.pet.id))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}