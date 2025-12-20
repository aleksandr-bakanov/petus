package bav.petus.android.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import bav.petus.android.HealthColor
import bav.petus.android.MyApplicationTheme

@Composable
fun StatBar(
    color: Color,
    fraction: Float,
    icon: ImageVector?,
    tweenDuration: Int = 500,
) {
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = tweenDuration) // smooth 500ms animation
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
    ) {
        if (icon != null) {
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                modifier = Modifier,
                imageVector = icon,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Box(
            modifier = Modifier
                .background(color)
                .fillMaxHeight()
                .fillMaxWidth(fraction = animatedFraction)
        )
    }
}

@Composable
@Preview
private fun StatBarPreview() {
    MyApplicationTheme {
        StatBar(
            color = HealthColor,
            fraction = 0.6f,
            icon = Icons.Filled.Favorite,
        )
    }
}