package com.hooky.app.ui.pieces

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.hooky.app.R
import com.hooky.app.ui.components.BadgeStyle
import com.hooky.app.ui.components.StatusBadge
import com.hooky.app.util.DueDate
import java.time.format.DateTimeFormatter

private val SHORT = DateTimeFormatter.ofPattern("dd/MM")

/**
 * "Entrega 15/12", "Entrega hoje" or "Atrasada · 15/12" while a delivery is open.
 * Purple when it's due within [DueDate.SOON_DAYS] days or late; outlined otherwise.
 * Shows nothing once the piece is Sold/Gifted or has no delivery date.
 */
@Composable
fun DueBadge(dueDate: String?, destination: String) {
    if (!DueDate.isOpen(dueDate, destination)) return
    val date = DueDate.parse(dueDate) ?: return
    val days = DueDate.daysLeft(dueDate) ?: return
    val short = date.format(SHORT)
    val text = when {
        days < 0 -> stringResource(R.string.piece_due_late, short)
        days == 0L -> stringResource(R.string.piece_due_today)
        else -> stringResource(R.string.piece_due_badge, short)
    }
    StatusBadge(
        text = text,
        style = if (DueDate.isSoonOrLate(dueDate, destination)) BadgeStyle.ACCENT else BadgeStyle.OUTLINE
    )
}
