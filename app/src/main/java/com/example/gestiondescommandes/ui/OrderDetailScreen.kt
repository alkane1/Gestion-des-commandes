package com.example.gestiondescommandes.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gestiondescommandes.MainViewModel
import com.example.gestiondescommandes.ui.components.PriorityChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreenV2(vm: MainViewModel, orderId: String, onBack: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val order = state.orders.firstOrNull { it.id == orderId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail") },
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
                    Text("Volume: ${order.volumeM3} m3")
                    Text("Prix: ${order.priceEur} EUR")
                    Text("Fragile: ${if (order.fragile) "Oui" else "Non"}")
                    Text("Note: ${order.note}")
                }
            }
        }
    }
}

