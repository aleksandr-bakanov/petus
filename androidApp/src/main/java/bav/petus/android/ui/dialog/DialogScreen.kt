package bav.petus.android.ui.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bav.petus.android.ui.common.ActionButton
import bav.petus.viewModel.dialog.DialogScreenUiState
import bav.petus.viewModel.dialog.DialogScreenViewModel

@Composable
fun DialogRoute(
    viewModel: DialogScreenViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    uiState?.let {
        DialogScreen(
            uiState = it,
            onAction = viewModel::onAction,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogScreen(
    uiState: DialogScreenUiState,
    onAction: (DialogScreenViewModel.Action) -> Unit,
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
                            text = "Dialog with pet",
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
            Text(
                text = uiState.text,
                modifier = Modifier.fillMaxWidth()
            )
            uiState.answers.forEachIndexed { index, answerOption ->
                ActionButton(
                    text = answerOption,
                    color = Color.Red,
                    onClick = { onAction(DialogScreenViewModel.Action.ChooseDialogAnswer(index)) }
                )
            }
        }
    }
}