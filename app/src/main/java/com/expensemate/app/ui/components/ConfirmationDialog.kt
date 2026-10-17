package com.expensemate.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.expensemate.app.ui.theme.ExpenseRed

@Composable
fun ConfirmationDialog(
    show: Boolean,
    title: String,
    message: String,
    confirmButtonText: String = "Delete",
    dismissButtonText: String = "Cancel",
    isDestructive: Boolean = true,
    icon: ImageVector = Icons.Default.Warning,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDestructive) ExpenseRed else MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
            },
            text = {
                Text(text = message, style = MaterialTheme.typography.bodyMedium)
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = if (isDestructive) {
                        ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                    } else {
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    }
                ) {
                    Text(text = confirmButtonText)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss) {
                    Text(text = dismissButtonText)
                }
            }
        )
    }
}
