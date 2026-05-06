package com.hooky.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hooky.app.ui.theme.BorderLight
import com.hooky.app.ui.theme.Slate
import com.hooky.app.ui.theme.TextMuted
import com.hooky.app.ui.theme.TextSecondary
import com.hooky.app.ui.theme.White

enum class BadgeStyle { FILLED_SLATE, OUTLINE, MUTED }

@Composable
fun StatusBadge(
    text: String,
    style: BadgeStyle = BadgeStyle.OUTLINE
) {
    val shape = RoundedCornerShape(20.dp)

    val backgroundColor: Color
    val textColor: Color
    val borderColor: Color?

    when (style) {
        BadgeStyle.FILLED_SLATE -> {
            backgroundColor = Slate
            textColor = White
            borderColor = null
        }
        BadgeStyle.OUTLINE -> {
            backgroundColor = Color.Transparent
            textColor = TextSecondary
            borderColor = BorderLight
        }
        BadgeStyle.MUTED -> {
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant
            textColor = TextMuted
            borderColor = null
        }
    }

    val modifier = if (borderColor != null) {
        Modifier.border(width = 1.dp, color = borderColor, shape = shape)
    } else {
        Modifier
    }

    Surface(
        shape = shape,
        color = backgroundColor,
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
