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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bav.petus.android.MyApplicationTheme
import bav.petus.android.ui.common.toResId
import bav.petus.core.resources.StringId
import bav.petus.viewModel.main.BottomSheetType
import bav.petus.viewModel.main.MainViewModel.Companion.onboardingPages

@Composable
fun OnboardingBottomSheet(
    uiState: BottomSheetType.Onboarding,
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
                            uiState.leftButtonAction.invoke()
                        },
                    text = stringResource(uiState.leftButtonTitle.toResId()),
                    fontSize = 16.sp,
                )
                Spacer(modifier = Modifier.padding(10.dp))
                ThreeDotsPagerIndicator(totalDots = pages.size, selectedIndex = currentPage)
                Spacer(modifier = Modifier.padding(10.dp))
                Text(
                    modifier = Modifier
                        .clickable {
                            uiState.rightButtonAction.invoke()
                        },
                    text = stringResource(uiState.rightButtonTitle.toResId()),
                    fontSize = 16.sp
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
            Image(
                painter = painterResource(id = pageData.image.toResId()),
                contentDescription = null,
                modifier = Modifier
                    .padding(top = 32.dp)
                    .fillMaxWidth(0.7f)
                    .clip(CircleShape)
            )

            Text(
                text = stringResource(pageData.title.toResId()),
                fontSize = 36.sp,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            )

            Text(
                text = stringResource(pageData.message.toResId()),
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun ThreeDotsPagerIndicator(
    totalDots: Int = 9,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    selectedColor: Color = Color.Blue,
    unSelectedColor: Color = Color.LightGray,
    dotHeight: Dp = 12.dp,
    dotWidth: Dp = 8.dp,
    dotSpacing: Dp = 6.dp
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        repeat(totalDots) { index ->
            Box(
                modifier = Modifier
                    .height(dotHeight)
                    .width(dotWidth)
                    .clip(RoundedCornerShape(50))
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
    MyApplicationTheme {
        OnboardingBottomSheet(
            uiState = BottomSheetType.Onboarding(
                pages = onboardingPages,
                currentPage = 0,
                leftButtonTitle = StringId.OnboardingSkipTitle,
                leftButtonAction = {},
                rightButtonTitle = StringId.OnboardingNextTitle,
                rightButtonAction = {}
            )
        )
    }
}