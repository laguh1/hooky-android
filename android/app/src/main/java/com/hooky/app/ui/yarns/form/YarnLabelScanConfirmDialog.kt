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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hooky.app.R
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
                text = stringResource(R.string.yarn_scan_dialog_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            val yes = stringResource(R.string.common_yes)
            val no = stringResource(R.string.common_no)
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = stringResource(R.string.yarn_scan_dialog_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = BorderLight)
                Spacer(modifier = Modifier.height(8.dp))

                result.name?.let { ScanResultRow(stringResource(R.string.yarn_field_name), it) }
                result.brand?.let { ScanResultRow(stringResource(R.string.yarn_field_brand), it) }
                result.colorName?.let { ScanResultRow(stringResource(R.string.yarn_field_color), it) }
                result.colorCode?.let { ScanResultRow(stringResource(R.string.yarn_field_color_code), it) }
                result.material?.let { ScanResultRow(stringResource(R.string.yarn_field_material), it.displayName) }
                result.materialComposition?.let { ScanResultRow(stringResource(R.string.yarn_field_composition), it) }
                result.weightCategory?.let { ScanResultRow(stringResource(R.string.yarn_field_weight_category), it.displayName) }
                result.ballWeightG?.let { ScanResultRow(stringResource(R.string.yarn_label_ball_weight), "${it}g") }
                result.ballLengthM?.let { ScanResultRow(stringResource(R.string.yarn_label_ball_length), "${it}m") }
                result.hookSizeMm?.let { ScanResultRow(stringResource(R.string.yarn_label_hook_size), "${it}mm") }
                result.needleSizeMm?.let { ScanResultRow(stringResource(R.string.yarn_label_needle_size), "${it}mm") }
                result.gauge?.let { ScanResultRow(stringResource(R.string.yarn_label_gauge), it) }
                result.washTemp?.let { ScanResultRow(stringResource(R.string.yarn_label_iron_temp), "${it}°C") }
                result.machineWash?.let { ScanResultRow(stringResource(R.string.yarn_care_machine_wash), if (it) yes else no) }
                result.handWash?.let { ScanResultRow(stringResource(R.string.yarn_care_hand_wash), if (it) yes else no) }
                result.tumbleDry?.let { ScanResultRow(stringResource(R.string.yarn_care_tumble_dry), if (it) yes else no) }
                result.bleach?.let { ScanResultRow(stringResource(R.string.yarn_care_bleach), if (it) yes else no) }
                result.dryClean?.let { ScanResultRow(stringResource(R.string.yarn_care_dry_clean), if (it) yes else no) }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onScanAnother,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.yarn_scan_another_side),
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
                Text(stringResource(R.string.action_apply), fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(stringResource(R.string.action_cancel))
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
