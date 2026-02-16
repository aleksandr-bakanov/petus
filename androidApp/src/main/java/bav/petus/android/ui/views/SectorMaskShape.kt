package bav.petus.android.ui.views

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class SectorMaskShape(
    private val percentage: Float,
) : Shape {
    private var path = Path()
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val center = size.center
        val radius = size.minDimension / 2
        val startAngle = 360f * percentage
        val sweepAngle = 360f - 360f * percentage

        path.apply {
            rewind()
            moveTo(center.x, center.y)
            arcTo(
                rect = Rect(center, radius),
                startAngleDegrees = -90f + startAngle,
                sweepAngleDegrees = sweepAngle,
                forceMoveTo = false
            )
            close()
        }

        return Outline.Generic(path)
    }
}
