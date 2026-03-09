package com.example.gestiondescommandes.ui.components

import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.gestiondescommandes.data.Priority

@Composable
fun PriorityChip(priority: Priority) {
    val label: String
    val containerColor = when (priority) {
        Priority.URGENT -> MaterialTheme.colorScheme.error
        Priority.HIGH -> MaterialTheme.colorScheme.tertiaryContainer
        Priority.NORMAL -> MaterialTheme.colorScheme.surfaceVariant
    }
    val labelColor = when (priority) {
        Priority.URGENT -> MaterialTheme.colorScheme.onError
        Priority.HIGH -> MaterialTheme.colorScheme.onTertiaryContainer
        Priority.NORMAL -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    when (priority) {
        Priority.URGENT -> {
            label = "URGENT"
        }

        Priority.HIGH -> {
            label = "ELEVEE"
        }

        Priority.NORMAL -> {
            label = "NORMALE"
        }
    }

    AssistChip(
        onClick = {},
        label = { Text(label, color = labelColor) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = containerColor
        )
    )
}

