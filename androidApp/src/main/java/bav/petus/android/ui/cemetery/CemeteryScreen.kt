package bav.petus.android.ui.cemetery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bav.petus.android.ui.views.PetCemeteryListCell
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            items(uiState.pets) { data ->
                PetCemeteryListCell(
                    data = data,
                    onClick = {
                        onAction(CemeteryScreenViewModel.Action.TapOnPet(data.pet.id))
                    }
                )
            }
        }
    }
}