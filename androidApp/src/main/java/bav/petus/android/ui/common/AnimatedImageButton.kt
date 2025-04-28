package bav.petus.android.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun AnimatedImageButton(
    painter: Painter,
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

    Box(
        modifier = modifier
            .scale(scale)
            .size(size.dp)
            .clickable {
                isPressed = true
                clickRequested = true
            }
    ) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier
                .size(size.dp)
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
