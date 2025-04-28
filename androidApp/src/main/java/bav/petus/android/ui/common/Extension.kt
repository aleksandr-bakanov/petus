package bav.petus.android.ui.common

import androidx.compose.ui.graphics.Color
import bav.petus.model.PetType

fun PetType.backgroundColor(): Color {
    return when (this) {
        PetType.Frogus -> Color.Green
        PetType.Catus -> Color.Blue
        PetType.Dogus -> Color.Red
    }
}