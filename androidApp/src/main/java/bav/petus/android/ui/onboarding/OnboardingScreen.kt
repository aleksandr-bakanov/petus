package bav.petus.android.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bav.petus.android.mysteryQuestReqular
import bav.petus.android.ui.common.toResId
import bav.petus.core.resources.ImageId
import bav.petus.viewModel.onboarding.OnboardingScreenViewModel
import bav.petus.viewModel.onboarding.OnboardingUiData
import bav.petus.viewModel.onboarding.OnboardingUiState


@Composable
fun OnboardingRoute(
    viewModel: OnboardingScreenViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    OnboardingScreen(
        uiState = uiState,
        onAction = viewModel::onNextClicked,
        onSkip = viewModel::skip,
    )
}

@Composable
private fun OnboardingScreen(
    uiState: OnboardingUiState,
    onAction: () -> Unit,
    onSkip: () -> Unit,
) {

    val currentPage = uiState.currentPage
    val pages = uiState.pages
    val pageData = pages[currentPage]

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 70.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    modifier = Modifier
                        .clickable {
                            onAction()
                        },
                    text = "Skip",
                    color = Color.Black.copy(alpha = 0.5f),
                    fontFamily = mysteryQuestReqular,
                    fontSize = 25.sp,
                )
                Spacer(modifier = Modifier.padding(10.dp))
                ThreeDotsPagerIndicator(totalDots = pages.size, selectedIndex = currentPage)
                Spacer(modifier = Modifier.padding(10.dp))
                Text(
                    modifier = Modifier
                        .clickable {
                            onAction()
                        },
                    text = "Next",
                    fontFamily = mysteryQuestReqular,
                    fontSize = 25.sp
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .padding(top = 150.dp),
            ) {
                Image(
                    painter = painterResource(id = pageData.image.toResId()),
                    contentDescription = null,
                    modifier = Modifier
                        .size(300.dp)
                        .clip(CircleShape)
                )
            }
            Row {
                Text(
                    text = pageData.title,
                    fontFamily = mysteryQuestReqular,
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .wrapContentHeight()
            ) {
                Text(
                    text = pageData.description,
                    textAlign = TextAlign.Center,
                    fontFamily = mysteryQuestReqular,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
fun ThreeDotsPagerIndicator(
    totalDots: Int = 10,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    selectedColor: Color = Color.Blue,
    unSelectedColor: Color = Color.LightGray,
    dotSize: Dp = 12.dp,
    dotSpacing: Dp = 8.dp
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        repeat(totalDots) { index ->
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(if (index == selectedIndex) selectedColor else unSelectedColor)
            )

            if (index != totalDots - 1) {
                Spacer(modifier = Modifier.width(dotSpacing))
            }
        }
    }
}

@Preview
@Composable
fun Preview() {
    OnboardingScreen(
        uiState = OnboardingUiState(
            pages = listOf(
                OnboardingUiData(
                    image = ImageId.FeedCat,
                    title = "Stats: Satiety",
                    description = "Each pet has 3 main stats: Satiety (green) - feed your pet when this stat is low."
                ),
                OnboardingUiData(
                    image = ImageId.FeedCat,
                    title = "Title Preview",
                    description = "Each pet has 3 main stats: Satiety (green) - feed your pet when this stat is low."
                ),
                OnboardingUiData(
                    image = ImageId.FeedCat,
                    title = "Stats: Satiety",
                    description = "Each pet has 3 main stats: Satiety (green) - feed your pet when this stat is low."
                ),
                OnboardingUiData(
                    image = ImageId.FeedCat,
                    title = "Stats: Satiety",
                    description = "Each pet has 3 main stats: Satiety (green) - feed your pet when this stat is low."
                )
            ),
            currentPage = 3
        ),
        onAction = {},
        onSkip = {}
    )
}