package com.hooky.app.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp

/**
 * Text for narrow slots (side-by-side buttons, chips, half-width field labels) that may
 * wrap between words but never inside one. If a word doesn't fit the available width
 * (long ES/PT translations, large system font), the text shrinks until it does.
 */
@Composable
fun WordSafeText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current
) {
    val baseSize = if (style.fontSize.isSpecified) style.fontSize else 14.sp
    var scale by remember(text, baseSize) { mutableStateOf(1f) }
    Text(
        text = text,
        modifier = modifier,
        style = style.copy(fontSize = baseSize * scale),
        onTextLayout = { layout ->
            val breaksMidWord = (0 until layout.lineCount - 1).any { line ->
                val end = layout.getLineEnd(line)
                end in 1 until text.length && !text[end - 1].isWhitespace() && !text[end].isWhitespace()
            }
            if (breaksMidWord && scale > MIN_SCALE) scale -= 0.05f
        }
    )
}

private const val MIN_SCALE = 0.6f
