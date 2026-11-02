package com.hooky.app.ui.yarns

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Yarn colour circle. A thin outline keeps white and very light yarns visible. */
@Composable
fun YarnColorDot(rgb: Int, modifier: Modifier = Modifier, size: Dp = 14.dp) {
    Box(
        modifier = modifier
            .size(size)
            .background(Color(0xFF000000 or rgb.toLong()), CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f), CircleShape)
    )
}
