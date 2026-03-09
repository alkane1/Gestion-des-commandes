package com.example.gestiondescommandes.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.gestiondescommandes.MainViewModel
import com.example.gestiondescommandes.data.ShipmentContainer
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreenV2(
    vm: MainViewModel,
    padding: PaddingValues,
    navController: NavController
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val plan = state.currentPlan

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plan") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    TextButton(onClick = { vm.buildShipmentPlan() }) { Text("Recalcul") }
                    TextButton(onClick = { vm.proposeSolutions(3) }) { Text("Solutions") }
                }
            )
        }
    ) { inner ->
        if (plan == null) {
            Column(
                Modifier
                    .padding(padding)
                    .padding(inner)
                    .padding(12.dp)
            ) {
                Text("Aucun plan. Va dans Commandes -> Calculer.")
            }
            return@Scaffold
        }

        val nonEmpty = plan.containers.filter { it.orders.isNotEmpty() }
        val avgFill = if (nonEmpty.isNotEmpty()) nonEmpty.map { it.combinedFill }.average() else 0.0
        val fillColor = when {
            avgFill < 0.40 -> Color(0xFFB00020)
            avgFill < 0.70 -> Color(0xFFFFA000)
            else -> Color(0xFF2E7D32)
        }

        BoxWithConstraints(
            modifier = Modifier
                .padding(padding)
                .padding(inner)
                .padding(12.dp)
        ) {
            val compact = maxWidth < 420.dp

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Spacer(Modifier.height(2.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(6.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Recette totale", style = MaterialTheme.typography.labelMedium)
                            Text("${plan.totalRevenueEur} EUR", style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                }

                item {
                    if (compact) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            KpiCard(title = "Conteneurs", value = "${plan.containers.size}", modifier = Modifier.fillMaxWidth())
                            KpiCard(title = "Expediees", value = "${plan.shippedOrders.size}", modifier = Modifier.fillMaxWidth())
                            KpiCard(title = "Reportees", value = "${plan.deferredOrders.size}", modifier = Modifier.fillMaxWidth())
                        }
                    } else {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            KpiCard(title = "Conteneurs", value = "${plan.containers.size}", modifier = Modifier.weight(1f))
                            KpiCard(title = "Expediees", value = "${plan.shippedOrders.size}", modifier = Modifier.weight(1f))
                            KpiCard(title = "Reportees", value = "${plan.deferredOrders.size}", modifier = Modifier.weight(1f))
                        }
                    }
                }

                item {
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
                }

                item {
                    SummaryCard(
                        revenue = plan.totalRevenueEur,
                        shipped = plan.shippedOrders.size,
                        deferred = plan.deferredOrders.size
                    )
                }

                if (state.solutions.isNotEmpty()) {
                    val activeSolution = state.solutions.firstOrNull { it.first == state.selectedSolutionName }?.second
                        ?: plan
                    val activeFill = averageFill(activeSolution)

                    item {
                        Card(shape = MaterialTheme.shapes.large) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Solutions proposees", style = MaterialTheme.typography.titleSmall)
                                state.solutions.forEach { (name, sol) ->
                                    val isSelected = state.selectedSolutionName == name
                                    val solutionFill = averageFill(sol)
                                    val revenueDelta = sol.totalRevenueEur - activeSolution.totalRevenueEur
                                    val fillDeltaPoints = (solutionFill - activeFill) * 100.0
                                    val reportsGain = activeSolution.deferredOrders.size - sol.deferredOrders.size
                                    Card(
                                        shape = MaterialTheme.shapes.medium,
                                        border = if (isSelected) {
                                            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                        } else {
                                            null
                                        },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) {
                                                MaterialTheme.colorScheme.primaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.surface
                                            }
                                        )
                                    ) {
                                        if (compact) {
                                            Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(name, style = MaterialTheme.typography.titleSmall)
                                                Text("Recette: ${sol.totalRevenueEur} EUR", style = MaterialTheme.typography.bodySmall)
                                                Text(
                                                    "Remplissage: ${(solutionFill * 100).roundToInt()}% | Reports: ${sol.deferredOrders.size}",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                                Text(
                                                    "Delta: ${formatSigned(revenueDelta)} EUR | ${formatSigned(fillDeltaPoints)} pts | reports ${formatSigned(reportsGain.toDouble())}",
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                                TextButton(onClick = { vm.applySolution(name) }) {
                                                    Text(if (isSelected) "Active" else "Appliquer cette solution")
                                                }
                                            }
                                        } else {
                                            Row(
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text(name, style = MaterialTheme.typography.titleSmall)
                                                    Text(
                                                        "Recette: ${sol.totalRevenueEur} EUR",
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                    Text(
                                                        "Remplissage: ${(solutionFill * 100).roundToInt()}% | Reports: ${sol.deferredOrders.size}",
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                    Text(
                                                        "Delta: ${formatSigned(revenueDelta)} EUR | ${formatSigned(fillDeltaPoints)} pts | reports ${formatSigned(reportsGain.toDouble())}",
                                                        style = MaterialTheme.typography.labelSmall
                                                    )
                                                }
                                                TextButton(onClick = { vm.applySolution(name) }) {
                                                    Text(if (isSelected) "Active" else "Appliquer cette solution")
                                                }
                                            }
                                        }
                                        LinearProgressIndicator(
                                            progress = { solutionFill.toFloat() },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp)
                                        )
                                        Spacer(Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                items(plan.containers, key = { it.index }) { c ->
                    ContainerCardV2(
                        c = c,
                        onOpen = { idx -> navController.navigate("container/$idx") }
                    )
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    Text("Reportees (apercu):", style = MaterialTheme.typography.titleSmall)
                    plan.deferredOrders.take(10).forEach { o ->
                        Text("- ${o.id} (${o.priority.toFrenchLabel()}) - ${o.priceEur} EUR")
                    }
                    if (plan.deferredOrders.size > 10) Text("... +${plan.deferredOrders.size - 10} autres")
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

private fun averageFill(plan: com.example.gestiondescommandes.data.ShipmentPlan): Double {
    val nonEmpty = plan.containers.filter { it.orders.isNotEmpty() }
    if (nonEmpty.isEmpty()) return 0.0
    return nonEmpty.map { it.combinedFill }.average()
}

private fun formatSigned(value: Double): String {
    val rounded = kotlin.math.round(value * 10.0) / 10.0
    return if (rounded >= 0) "+$rounded" else rounded.toString()
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
            Text("Resume expedition", style = MaterialTheme.typography.titleSmall)
            Text("Recette: $revenue EUR", style = MaterialTheme.typography.titleMedium)
            Text("Expediees: $shipped | Reportees: $deferred")
        }
    }
}

@Composable
fun ContainerCardV2(
    c: ShipmentContainer,
    onOpen: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val compact = c.orders.size > 9

    Card(
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            BoxWithConstraints {
                val smallWidth = maxWidth < 360.dp
                if (smallWidth || compact) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column {
                            Text("Conteneur #${c.index + 1}", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Recette: ${c.revenueEur} EUR | Commandes: ${c.orders.size}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { expanded = !expanded }) {
                                Text(
                                    if (expanded) "Reduire" else "Details",
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            TextButton(onClick = { onOpen(c.index) }) {
                                Text(
                                    "Voir",
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                } else {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Conteneur #${c.index + 1}", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Recette: ${c.revenueEur} EUR | Commandes: ${c.orders.size}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Row {
                            TextButton(onClick = { expanded = !expanded }) {
                                Text(
                                    if (expanded) "Reduire" else "Details",
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            TextButton(onClick = { onOpen(c.index) }) {
                                Text(
                                    "Voir",
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Text("Poids ${(c.fillWeight * 100).roundToInt()}%", style = MaterialTheme.typography.labelMedium)
            LinearProgressIndicator(
                progress = { c.fillWeight.toFloat() },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Volume ${(c.fillVolume * 100).roundToInt()}%", style = MaterialTheme.typography.labelMedium)
            LinearProgressIndicator(
                progress = { c.fillVolume.toFloat() },
                modifier = Modifier.fillMaxWidth()
            )

            if (expanded) {
                HorizontalDivider()
                if (c.orders.isEmpty()) {
                    AssistChip(onClick = {}, label = { Text("Vide / reporte") })
                } else {
                    c.orders.forEach { o ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${o.id} | ${o.priority.toFrenchLabel()}", style = MaterialTheme.typography.bodyMedium)
                            Text("${o.priceEur} EUR", style = MaterialTheme.typography.bodyMedium)
                        }
                        if (o.fragile) Text("Fragile", style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}



