package bav.petus.android.ui.zoo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import bav.petus.android.R
import bav.petus.android.ui.common.ActionButton
import bav.petus.android.ui.views.PetListCell
import bav.petus.viewModel.zoo.PetThumbnailUiData
import bav.petus.viewModel.zoo.ZooScreenViewModel
import bav.petus.viewModel.zoo.ZooUiState

@Composable
fun ZooRoute(
    viewModel: ZooScreenViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    uiState?.let {
        ZooScreen(
            uiState = it,
            onAction = viewModel::onAction,
        )
    }
}

@Composable
private fun ZooScreen(
    uiState: ZooUiState,
    onAction: (ZooScreenViewModel.Action) -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(uiState.pets) { data: PetThumbnailUiData ->
                    PetListCell(
                        data = data, onClick = {
                            onAction(ZooScreenViewModel.Action.OpenPetDetails(data.pet.id))
                        })
                }
            }
            if (uiState.showCreateNewPetButton) {
                ActionButton(
                    text = stringResource(id = R.string.CreatePetButtonTitle),
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.padding(16.dp),
                ) {
                    onAction(ZooScreenViewModel.Action.TapCreateNewPetButton)
                }
            }
        }
    }
}


