package com.hooky.app.ui.yarns.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hooky.app.domain.model.YarnLabelScanResult
import com.hooky.app.ui.theme.Slate
import com.hooky.app.ui.theme.TextMuted
import com.hooky.app.ui.theme.TextSecondary
import com.hooky.app.ui.theme.White
import com.hooky.app.ui.theme.BorderLight

@Composable
fun YarnLabelScanConfirmDialog(
    result: YarnLabelScanResult,
    onApply: () -> Unit,
    onScanAnother: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Label Detected",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = "Review the detected fields. Only non-empty values will be applied.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = BorderLight)
                Spacer(modifier = Modifier.height(8.dp))

                result.name?.let { ScanResultRow("Name", it) }
                result.brand?.let { ScanResultRow("Brand", it) }
                result.colorName?.let { ScanResultRow("Color", it) }
                result.colorCode?.let { ScanResultRow("Color code", it) }
                result.material?.let { ScanResultRow("Material", it.displayName) }
                result.materialComposition?.let { ScanResultRow("Composition", it) }
                result.weightCategory?.let { ScanResultRow("Weight", it.displayName) }
                result.ballWeightG?.let { ScanResultRow("Ball weight", "${it}g") }
                result.ballLengthM?.let { ScanResultRow("Ball length", "${it}m") }
                result.hookSizeMm?.let { ScanResultRow("Hook size", "${it}mm") }
                result.needleSizeMm?.let { ScanResultRow("Needle size", "${it}mm") }
                result.gauge?.let { ScanResultRow("Gauge", it) }
                result.washTemp?.let { ScanResultRow("Wash temp", "${it}°C") }
                result.machineWash?.let { ScanResultRow("Machine wash", if (it) "Yes" else "No") }
                result.handWash?.let { ScanResultRow("Hand wash", if (it) "Yes" else "No") }
                result.tumbleDry?.let { ScanResultRow("Tumble dry", if (it) "Yes" else "No") }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onScanAnother,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "+ Scan another side",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onApply,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Slate,
                    contentColor = White
                )
            ) {
                Text("Apply", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun ScanResultRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            modifier = Modifier.weight(0.4f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.6f)
        )
    }
}
