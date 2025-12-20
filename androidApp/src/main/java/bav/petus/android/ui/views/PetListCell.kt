package bav.petus.android.ui.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import bav.petus.android.HealthColor
import bav.petus.android.PsychColor
import bav.petus.android.SatietyColor
import bav.petus.android.ui.common.StatBar
import bav.petus.android.ui.common.toResId
import bav.petus.viewModel.zoo.PetThumbnailUiData

@Composable
fun PetListCell(
    data: PetThumbnailUiData,
    onClick: () -> Unit,
) {
    val maxHeight = 96.dp
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(maxHeight)
        .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(id = data.petImageResId.toResId()),
            contentDescription = "",
            modifier = Modifier
                .size(maxHeight)
                .clip(RoundedCornerShape(8.dp))
        )
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = data.pet.name,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            StatBar(
                color = SatietyColor,
                icon = null,
                fraction = data.satietyFraction,
            )
            StatBar(
                color = PsychColor,
                icon = null,
                fraction = data.psychFraction,
            )
            StatBar(
                color = HealthColor,
                icon = null,
                fraction = data.healthFraction,
            )
        }
    }
}

