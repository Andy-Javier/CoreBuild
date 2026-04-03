package edu.ucne.corebuild.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PerformanceBar(value: Float, modifier: Modifier = Modifier) {
    val color = when {
        value >= 0.75f -> Color(0xFF4CAF50) // Green
        value >= 0.45f -> Color(0xFFFFB300) // Amber
        else -> Color(0xFFF44336)           // Red
    }

    LinearProgressIndicator(
        progress = { value.coerceIn(0f, 1f) },
        modifier = modifier
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .fillMaxWidth(),
        color = color,
        trackColor = Color.Transparent
    )
}
