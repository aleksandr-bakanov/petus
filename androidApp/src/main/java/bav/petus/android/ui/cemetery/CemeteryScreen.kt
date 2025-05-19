package bav.petus.android.ui.cemetery

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import bav.petus.android.ui.views.PetListCell
import bav.petus.viewModel.cemetery.CemeteryScreenViewModel
import bav.petus.viewModel.cemetery.CemeteryUiState

@Composable
fun CemeteryRoute(
    viewModel: CemeteryScreenViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    uiState?.let {
        CemeteryScreen(
            uiState = it,
            onAction = viewModel::onAction,
        )
    }
}

@Composable
private fun CemeteryScreen(
    uiState: CemeteryUiState,
    onAction: (CemeteryScreenViewModel.Action) -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.pets.isEmpty()) {
                Text(
                    "Cemetery is empty",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.pets) { data ->
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