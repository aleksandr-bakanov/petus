package bav.petus.android.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import bav.petus.android.R
import bav.petus.model.PetType

@Composable
fun PetTypePicker(
    selectedValue: PetType,
    availablePetTypes: List<PetType>,
    onSelect: (PetType) -> Unit,
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .horizontalScroll(scrollState),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        availablePetTypes.forEach { item ->
            val isSelected = item == selectedValue

            ImageItem(
                imageResId = item.eggImageResId(),
                isSelected = isSelected,
                onClick = { onSelect(item) }
            )
        }
    }
}

@Composable
private fun ImageItem(
    imageResId: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val targetSize = if (isSelected) 96.dp else 72.dp
    val targetBorder = if (isSelected) 3.dp else 0.dp
    val targetElevation = if (isSelected) 8.dp else 2.dp
    val targetBorderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    val animatedSize by animateDpAsState(targetValue = targetSize, animationSpec = tween(300))
    val animatedBorder by animateDpAsState(targetValue = targetBorder, animationSpec = tween(300))
    val animatedElevation by animateDpAsState(targetValue = targetElevation, animationSpec = tween(300))
    val animatedBorderColor by animateColorAsState(targetValue = targetBorderColor, animationSpec = tween(300))

    Box(
        modifier = Modifier
            .padding(8.dp)
            .size(animatedSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                BorderStroke(animatedBorder, animatedBorderColor),
                CircleShape
            )
            .shadow(
                elevation = animatedElevation,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun PetType.eggImageResId(): Int {
    return when (this) {
        PetType.Catus -> R.drawable.catus_egg
        PetType.Dogus -> R.drawable.dogus_egg
        PetType.Frogus -> R.drawable.frogus_egg
        PetType.Bober -> R.drawable.bober_egg
        PetType.Fractal -> R.drawable.fractal_egg
    }
}
