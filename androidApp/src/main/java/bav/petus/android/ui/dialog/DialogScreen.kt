package bav.petus.android.ui.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bav.petus.android.MyApplicationTheme
import bav.petus.android.ui.common.DialogButton
import bav.petus.android.ui.common.toResId
import bav.petus.core.resources.ImageId
import bav.petus.viewModel.dialog.DialogMessage
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

@Composable
private fun DialogScreen(
    uiState: DialogScreenUiState,
    onAction: (DialogScreenViewModel.Action) -> Unit,
) {
    Scaffold(
        bottomBar = {
            Column(modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                uiState.answers.forEachIndexed { index, answerOption ->
                    DialogButton(
                        text = answerOption,
                        onClick = { onAction(DialogScreenViewModel.Action.ChooseDialogAnswer(index)) }
                    )
                }
            }
        }
    ) { padding ->
        val scrollState = rememberLazyListState()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = scrollState,
            contentPadding = padding,
            reverseLayout = true,
        ) {
            items(uiState.messages) { message ->
                DialogMessageCell(message)
            }
        }
        LaunchedEffect(uiState.messages.size) {
            scrollState.scrollToItem(0)
        }
    }
}

@Composable
private fun DialogMessageCell(message: DialogMessage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = if (message.isImageAtStart) Arrangement.Start else Arrangement.End,
    ) {
        if (message.isImageAtStart) {
            Avatar(imageId = message.imageId)
            Spacer(modifier = Modifier.size(8.dp))
            MessageText(text = message.text, isImageAtStart = true)
            Spacer(modifier = Modifier.size(16.dp))
        } else {
            Spacer(modifier = Modifier.size(16.dp))
            MessageText(text = message.text, isImageAtStart = false)
            Spacer(modifier = Modifier.size(8.dp))
            Avatar(imageId = message.imageId)
        }
    }
}

@Composable
private fun Avatar(imageId: ImageId) {
    Image(
        painter = painterResource(id = imageId.toResId()),
        contentDescription = null,
        modifier = Modifier
            .size(84.dp)
            .clip(CircleShape)
    )
}

@Composable
private fun RowScope.MessageText(text: String, isImageAtStart: Boolean) {
    Box(
        modifier = Modifier.weight(1f),
        contentAlignment = if (isImageAtStart) Alignment.CenterStart else Alignment.CenterEnd,
    ) {
        Text(
            text = text,
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Preview
@Composable
private fun DialogScreenPreview() {
    MyApplicationTheme {
        DialogScreen(
            uiState = DialogScreenUiState(
                messages = listOf(
                    DialogMessage(
                        isImageAtStart = true,
                        imageId = ImageId.CatNewbornActive,
                        text = "Hello world",
                    ),
                    DialogMessage(
                        isImageAtStart = false,
                        imageId = ImageId.UserProfileAvatar,
                        text = "Oh, hi Mark!",
                    ),
                    DialogMessage(
                        isImageAtStart = false,
                        imageId = ImageId.UserProfileAvatar,
                        text = "Oh, hi Mark! Nice to see you. How is it going",
                    ),
                    DialogMessage(
                        isImageAtStart = true,
                        imageId = ImageId.CatNewbornActive,
                        text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
                    ),
                ).reversed(),
                answers = listOf("Hello", "How are you?")
            ),
            onAction = {}
        )
    }
}