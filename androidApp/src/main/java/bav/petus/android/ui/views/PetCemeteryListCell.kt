package bav.petus.android.ui.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import bav.petus.android.HealthColor
import bav.petus.android.PsychColor
import bav.petus.android.SatietyColor
import bav.petus.android.ui.common.StatBar
import bav.petus.android.ui.common.toResId
import bav.petus.viewModel.zoo.PetThumbnailUiData

@Composable
fun PetCemeteryListCell(
    data: PetThumbnailUiData,
    onClick: () -> Unit,
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick() }
    ) {
        Text(
            text = data.pet.name,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Image(
            painter = painterResource(id = data.petImageResId.toResId()),
            contentDescription = "",
            modifier = Modifier.clip(RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp))
        )
    }
}
