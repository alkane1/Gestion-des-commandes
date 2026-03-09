package com.example.gestiondescommandes.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gestiondescommandes.MainViewModel
import com.example.gestiondescommandes.data.Order
import com.example.gestiondescommandes.data.Priority
import com.example.gestiondescommandes.ui.components.PriorityChip

private enum class OrderFilter { ALL, URGENT, HIGH, NORMAL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersListScreenV2(
    vm: MainViewModel,
    padding: PaddingValues,
    onOpenDetail: (String) -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(OrderFilter.ALL) }

    val filteredOrders = remember(state.orders, searchQuery, filter) {
        state.orders.filter { order ->
            val matchesPriority = when (filter) {
                OrderFilter.ALL -> true
                OrderFilter.URGENT -> order.priority == Priority.URGENT
                OrderFilter.HIGH -> order.priority == Priority.HIGH
                OrderFilter.NORMAL -> order.priority == Priority.NORMAL
            }

            val q = searchQuery.trim()
            val matchesSearch =
                q.isEmpty() || order.id.contains(q, ignoreCase = true) || order.note.contains(q, ignoreCase = true)

            matchesPriority && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Centre des commandes") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    TextButton(onClick = { vm.buildShipmentPlan() }) { Text("Calculer") }
                    IconButton(onClick = { vm.toggleDarkMode() }) {
                        Icon(
                            imageVector = if (state.darkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = "Theme"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { vm.regenerateOrders() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Regenerer")
            }
        }
    ) { inner ->
        Column(
            Modifier
                .padding(padding)
                .padding(inner)
                .padding(12.dp)
        ) {
            val urgent = state.orders.count { it.priority == Priority.URGENT }
            val high = state.orders.count { it.priority == Priority.HIGH }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Affichees: ${filteredOrders.size}/${state.orders.size}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Urgent: $urgent • Elevee: $high",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 1.dp) {
                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Rechercher (id/note)") },
                        singleLine = true
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(selected = filter == OrderFilter.ALL, onClick = { filter = OrderFilter.ALL }, label = { Text("Toutes") })
                        }
                        item {
                            FilterChip(selected = filter == OrderFilter.URGENT, onClick = { filter = OrderFilter.URGENT }, label = { Text("Urgent") })
                        }
                        item {
                            FilterChip(selected = filter == OrderFilter.HIGH, onClick = { filter = OrderFilter.HIGH }, label = { Text("Elevee") })
                        }
                        item {
                            FilterChip(selected = filter == OrderFilter.NORMAL, onClick = { filter = OrderFilter.NORMAL }, label = { Text("Normale") })
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items = filteredOrders, key = { it.id }) { order ->
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
                Text("Volume: ${order.volumeM3} m3")
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "Prix",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    "${order.priceEur} EUR",
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
