package com.hooky.app.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hooky.app.R
import com.hooky.app.ui.theme.BorderStrong
import com.hooky.app.ui.theme.BrandPurple
import com.hooky.app.ui.theme.TextSecondary

/**
 * Three first steps for new users. Each step ticks itself off from real data
 * (a piece exists, a yarn exists, a row was counted) and is a shortcut to the
 * screen where it is done. The card hides once all are done or when dismissed.
 */
@Composable
fun GettingStartedCard(
    hasPiece: Boolean,
    hasYarn: Boolean,
    hasCountedRows: Boolean,
    doneCount: Int,
    onAddPiece: () -> Unit,
    onAddYarn: () -> Unit,
    onUseCounter: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, BorderStrong),
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.getting_started_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(R.string.getting_started_progress, doneCount, 3),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.getting_started_dismiss),
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Step(stringResource(R.string.getting_started_step_piece), hasPiece, onAddPiece)
            Step(stringResource(R.string.getting_started_step_yarn), hasYarn, onAddYarn)
            Step(stringResource(R.string.getting_started_step_counter), hasCountedRows, onUseCounter)
        }
    }
}

@Composable
private fun Step(label: String, done: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .clickable(enabled = !done, onClick = onClick)
            .padding(end = 12.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = if (done) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
            contentDescription = null,
            tint = if (done) BrandPurple else BorderStrong,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(12.dp))
        // Full width with normal word wrapping, so long ES/PT labels wrap between words.
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (done) TextSecondary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (!done) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary
            )
        }
    }
}
