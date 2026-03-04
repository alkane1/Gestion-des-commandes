package com.example.gestiondescommandes.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestiondescommandes.MainViewModel
import com.example.gestiondescommandes.data.Order
import com.example.gestiondescommandes.data.Priority
import com.example.gestiondescommandes.ui.components.PriorityChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersListScreenV2(
    vm: MainViewModel,
    padding: PaddingValues,
    onOpenDetail: (String) -> Unit
) {
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Commandes") },
                actions = {
                    TextButton(onClick = { vm.buildShipmentPlan() }) { Text("Calculer") }
                    TextButton(onClick = { vm.proposeSolutions(3) }) { Text("Solutions") }
                    IconButton(onClick = { vm.toggleDarkMode() }) {Text("🌙")}
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { vm.regenerateOrders() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Régénérer")
            }
        }
    ) { inner ->
        Column(
            Modifier
                .padding(padding)
                .padding(inner)
                .padding(12.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total: ${state.orders.size}", style = MaterialTheme.typography.titleMedium)
                val urgent = state.orders.count { it.priority == Priority.URGENT }
                val high = state.orders.count { it.priority == Priority.HIGH }
                Text("Urgent: $urgent • High: $high", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(10.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.orders) { order ->
                    OrderCard(order = order, onClick = { onOpenDetail(order.id) })
                }
            }
        }
    }
}

@Composable
private fun OrderCard(order: Order, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(order.id, style = MaterialTheme.typography.titleSmall)
                PriorityChip(order.priority)
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Poids: ${order.weightKg} kg")
                Text("Volume: ${order.volumeM3} m³")
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "Prix",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    "${order.priceEur} €",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (order.fragile) {
                AssistChip(
                    onClick = {},
                    label = { Text("Fragile") }
                )
            }
        }
    }
}

@Composable
private fun PriorityChip(p: Priority) {
    val label = when (p) {
        Priority.URGENT -> "URGENT"
        Priority.HIGH -> "HIGH"
        Priority.NORMAL -> "NORMAL"
    }
    val colors = when (p) {
        Priority.URGENT -> AssistChipDefaults.assistChipColors()
        Priority.HIGH -> AssistChipDefaults.assistChipColors()
        Priority.NORMAL -> AssistChipDefaults.assistChipColors()
    }
    AssistChip(
        onClick = {},
        label = { Text(label) },
        colors = colors
    )
}