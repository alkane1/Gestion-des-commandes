package com.example.gestiondescommandes.ui.components

import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.gestiondescommandes.data.Priority

@Composable
fun PriorityChip(priority: Priority) {

    val label: String
    val containerColor: Color
    val labelColor: Color

    when (priority) {
        Priority.URGENT -> {
            label = "URGENT"
            containerColor = Color(0xFFB00020)
            labelColor = Color.White
        }

        Priority.HIGH -> {
            label = "ÉLEVÉE"
            containerColor = Color(0xFFFFCDD2)
            labelColor = Color(0xFFB00020)
        }

        Priority.NORMAL -> {
            label = "NORMALE"
            containerColor = Color(0xFFE0E0E0)
            labelColor = Color(0xFF424242)
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