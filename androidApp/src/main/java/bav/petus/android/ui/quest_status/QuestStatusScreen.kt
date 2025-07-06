package bav.petus.android.ui.quest_status

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bav.petus.viewModel.questStatus.QuestStatusUiState
import bav.petus.viewModel.questStatus.QuestStatusViewModel

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
                QuestTitleRow(
                    title = quest.questName,
                    message = quest.stagesDescription,
                )
                Text(
                    text = quest.questDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
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
