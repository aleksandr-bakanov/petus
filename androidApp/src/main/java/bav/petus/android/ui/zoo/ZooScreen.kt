package bav.petus.android.ui.zoo

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import bav.petus.android.ui.common.ActionButton
import bav.petus.android.ui.common.UiState
import bav.petus.android.ui.views.PetListCell

@Composable
fun ZooRoute(
    viewModel: ZooScreenViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    ZooScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ZooScreen(
    uiState: UiState<ZooUiState>,
    onAction: (ZooScreenViewModel.Action) -> Unit,
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
                    if (state.showLocationRationaleButton) {
                        ActionButton(
                            text = "GIVE ACCESS TO BACKGROUND LOCATION",
                            color = Color(0xFFFF9800) // Orange
                        ) {
                            onAction(ZooScreenViewModel.Action.TapRationaleButton)
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(state.pets) { data: PetThumbnailUiData ->
                            PetListCell(
                                data = data, onClick = {
                                    onAction(ZooScreenViewModel.Action.OpenPetDetails(data.pet.id))
                                })
                        }
                    }
                    ActionButton(
                        text = "Create new pet",
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.padding(16.dp),
                    ) {
                        onAction(ZooScreenViewModel.Action.TapCreateNewPetButton)
                    }
                }
            }
        }
    }
}


