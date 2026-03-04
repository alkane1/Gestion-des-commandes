package com.example.gestiondescommandes.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestiondescommandes.MainViewModel
import com.example.gestiondescommandes.ui.components.PriorityChip
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreenV2(vm: MainViewModel, orderId: String, onBack: () -> Unit) {
    val state by vm.state.collectAsState()
    val order = state.orders.firstOrNull { it.id == orderId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détail") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Retour") } }
            )
        }
    ) { pad ->
        if (order == null) {
            Box(Modifier.padding(pad).padding(16.dp)) { Text("Commande introuvable") }
            return@Scaffold
        }

        Column(
            Modifier.padding(pad).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(order.id, style = MaterialTheme.typography.titleLarge)
            PriorityChip(order.priority)

            Card(shape = MaterialTheme.shapes.large) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Poids: ${order.weightKg} kg")
                    Text("Volume: ${order.volumeM3} m³")
                    Text("Prix: ${order.priceEur} €")
                    Text("Fragile: ${if (order.fragile) "Oui" else "Non"}")
                    Text("Note: ${order.note}")
                }
            }
        }
    }
}