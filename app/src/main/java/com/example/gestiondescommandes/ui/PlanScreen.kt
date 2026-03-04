package com.example.gestiondescommandes.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.gestiondescommandes.MainViewModel
import com.example.gestiondescommandes.data.ShipmentContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreenV2(vm: MainViewModel, padding: PaddingValues) {
    val state by vm.state.collectAsState()
    val plan = state.currentPlan

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plan") },
                actions = {
                    TextButton(onClick = { vm.buildShipmentPlan() }) { Text("Recalcul") }
                    TextButton(onClick = { vm.proposeSolutions(3) }) { Text("Solutions") }
                }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .padding(padding)
                .padding(inner)
                .padding(12.dp)
        ) {

            if (plan == null) {
                Text("Aucun plan. Va dans Commandes → Calculer.")
                return@Column
            }

            // =========================
            // ✅ DASHBOARD KPI
            // =========================
            Spacer(Modifier.height(12.dp))

            // Carte principale Recette
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(6.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Recette totale", style = MaterialTheme.typography.labelMedium)
                    Text("${plan.totalRevenueEur} €", style = MaterialTheme.typography.headlineMedium)
                }
            }

            Spacer(Modifier.height(12.dp))

            // 3 KPI en ligne
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    title = "Conteneurs",
                    value = "${plan.containers.size}",
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Expédiées",
                    value = "${plan.shippedOrders.size}",
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Reportées",
                    value = "${plan.deferredOrders.size}",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            // KPI 4 : Remplissage moyen (conteneurs non vides)
            val nonEmpty = plan.containers.filter { it.orders.isNotEmpty() }
            val avgFill = if (nonEmpty.isNotEmpty()) nonEmpty.map { it.combinedFill }.average() else 0.0

            val fillColor = when {
                avgFill < 0.40 -> Color(0xFFB00020)  // rouge
                avgFill < 0.70 -> Color(0xFFFFA000)  // orange
                else -> Color(0xFF2E7D32)             // vert
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(6.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Remplissage moyen", style = MaterialTheme.typography.labelMedium)
                        Text("${(avgFill * 100).toInt()}%", style = MaterialTheme.typography.titleLarge)
                    }

                    LinearProgressIndicator(
                        progress = { avgFill.toFloat() },
                        modifier = Modifier.fillMaxWidth(),
                        color = fillColor
                    )

                    Text(
                        "Moyenne sur ${nonEmpty.size} conteneur(s) non vide(s)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            // =========================
            // ✅ FIN DASHBOARD KPI
            // =========================

            // (Optionnel) Résumé texte : tu peux le garder ou le supprimer (doublon avec KPI)
            SummaryCard(
                revenue = plan.totalRevenueEur,
                shipped = plan.shippedOrders.size,
                deferred = plan.deferredOrders.size
            )

            Spacer(Modifier.height(10.dp))

            if (state.solutions.isNotEmpty()) {
                Card(shape = MaterialTheme.shapes.large) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Solutions proposées", style = MaterialTheme.typography.titleSmall)
                        state.solutions.forEach { (name, sol) ->
                            Text("• $name → ${sol.totalRevenueEur} €  (exp: ${sol.shippedOrders.size}, rep: ${sol.deferredOrders.size})")
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(plan.containers) { c -> ContainerCardV2(c) }

                item {
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    Text("Reportées (aperçu):", style = MaterialTheme.typography.titleSmall)
                    plan.deferredOrders.take(10).forEach { o ->
                        Text("• ${o.id} (${o.priority}) - ${o.priceEur}€")
                    }
                    if (plan.deferredOrders.size > 10) Text("… +${plan.deferredOrders.size - 10} autres")
                }
            }
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun SummaryCard(revenue: Double, shipped: Int, deferred: Int) {
    Card(shape = MaterialTheme.shapes.large, elevation = CardDefaults.cardElevation(4.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Résumé expédition", style = MaterialTheme.typography.titleSmall)
            Text("Recette: $revenue €", style = MaterialTheme.typography.titleMedium)
            Text("Expédiées: $shipped   •   Reportées: $deferred")
        }
    }
}

@Composable
private fun ContainerCardV2(c: ShipmentContainer) {
    Card(shape = MaterialTheme.shapes.large, elevation = CardDefaults.cardElevation(3.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Conteneur #${c.index + 1}", style = MaterialTheme.typography.titleSmall)
                Text("${(c.combinedFill * 100).toInt()}%")
            }

            LinearProgressIndicator(progress = { c.combinedFill.toFloat() })

            Text("Poids ${(c.fillWeight * 100).toInt()}% • Volume ${(c.fillVolume * 100).toInt()}%")
            Text("Recette: ${c.revenueEur} € • Commandes: ${c.orders.size}")

            if (c.orders.isNotEmpty()) {
                HorizontalDivider()
                Text("Top commandes:", style = MaterialTheme.typography.labelLarge)
                c.orders.take(5).forEach { o ->
                    Text("• ${o.id} (${o.priority}) - ${o.priceEur}€")
                }
                if (c.orders.size > 5) Text("… +${c.orders.size - 5} autres")
            } else {
                AssistChip(onClick = {}, label = { Text("Reporté / Vide") })
            }
        }
    }
}