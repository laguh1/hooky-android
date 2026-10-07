package com.hooky.app.ui.yarns

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.hooky.app.R

/** Value stored in [com.hooky.app.data.db.entity.YarnEntity.color] when the user left the colour empty. */
const val UNKNOWN_YARN_COLOR = "Unknown"

/** Colour name as shown to the user: the stored "no colour" placeholder becomes a localized label. */
@Composable
fun yarnColorLabel(color: String): String =
    if (color.isBlank() || color == UNKNOWN_YARN_COLOR) stringResource(R.string.yarn_color_unknown) else color
