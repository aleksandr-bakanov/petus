package bav.petus.android.ui.quest_status

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.Path
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import bav.petus.viewModel.questStatus.QuestDescription
import bav.petus.viewModel.questStatus.QuestStatusUiState
import bav.petus.viewModel.questStatus.QuestStatusViewModel
import kotlin.collections.List

@Composable
fun QuestStatusRoute(
    viewModel: QuestStatusViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    QuestStatusScreen(
        uiState = uiState,
    )
}

@Composable
private fun QuestStatusScreen(
    uiState: QuestStatusUiState,
) {

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Spacer(modifier = Modifier.height(8.dp))
            for (quest in uiState.quests) {
                var isExpanded by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .animateContentSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentSize()
                    ) {
                        QuestTitleRow(
                            title = quest.questName,
                            message = quest.stagesDescription,
                        )
                        if(isExpanded) {
                            Text(
                                text = quest.questDescription,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .padding(8.dp),
                            )
                        }
                    }
                    Icon(
                        imageVector = if (isExpanded) {
                            Icons.Filled.KeyboardArrowUp
                        } else {
                            Icons.Filled.KeyboardArrowDown
                        },
                        contentDescription = "Expand icon",
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clickable { isExpanded = !isExpanded }
                            .align( Alignment.CenterVertically)

                    )

                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun QuestTitleRow(title: String, message: String) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .padding(start = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(3f)
        )
        Text(
            text = message,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

@Preview
@Composable
fun SimpleComposablePreview() {
    QuestStatusScreen(QuestStatusUiState(
        listOf(
            QuestDescription(
                questName = "Quest 1",
                stagesDescription = "Stage 2/5",
                questDescription = "This is a description of quest 1. It has several stages to complete."
            ),
            QuestDescription(
                questName = "Quest 2",
                stagesDescription = "Stage 1/3",
                questDescription = "This is a description of quest 2. It has several stages to complete."
            ),
            QuestDescription(
                questName = "Quest 3",
                stagesDescription = "Stage 4/4",
                questDescription = "This is a description of quest 3. It has several stages to complete."
            )
        )
    ))
}