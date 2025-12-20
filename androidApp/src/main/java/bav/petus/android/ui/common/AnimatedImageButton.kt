package bav.petus.android.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import bav.petus.android.MyApplicationTheme
import bav.petus.android.R
import kotlinx.coroutines.delay

@Composable
fun AnimatedImageButton(
    painter: Painter,
    title: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    size: Int = 112,
    onClick: () -> Unit = {},
) {
    var isPressed by remember { mutableStateOf(false) }
    var clickRequested by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = 0.4f,
            stiffness = 300f
        ),
        label = "ButtonScale"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .clickable {
                isPressed = true
                clickRequested = true
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier
                .size(size.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .width(size.dp)
        )
    }

    // Correct way to handle delayed click outside click handler
    if (clickRequested) {
        LaunchedEffect(Unit) {
            delay(100) // small delay for tap feel
            isPressed = false
            clickRequested = false
            onClick()
        }
    }
}

@Composable
@Preview
private fun AnimatedImageButtonPreview() {
    MyApplicationTheme {
        AnimatedImageButton(
            painter = painterResource(id = R.drawable.speak_dragon),
            title = "Speak Your Name Child"
        )
    }
}
