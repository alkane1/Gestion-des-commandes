package com.example.gestiondescommandes.ui
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.gestiondescommandes.MainViewModel
import com.example.gestiondescommandes.data.Order
import com.example.gestiondescommandes.data.ShipmentContainer
import com.example.gestiondescommandes.ui.components.AppFilledActionButton
import com.example.gestiondescommandes.ui.components.AppOutlinedActionButton
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
    val configuration = LocalConfiguration.current
    val compact = configuration.screenWidthDp < 420

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plan d'expedition") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    AppOutlinedActionButton(label = "Recalcul", onClick = { vm.buildShipmentPlan() })
                    AppFilledActionButton(label = "Solutions", onClick = { vm.proposeSolutions(3) })
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

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(inner)
                .padding(12.dp)
                .fillMaxWidth(),
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
                                                if (isSelected) {
                                                    AppOutlinedActionButton(label = "Active", onClick = { })
                                                } else {
                                                    AppFilledActionButton(
                                                        label = "Appliquer",
                                                        onClick = { vm.applySolution(name) }
                                                    )
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
                                                if (isSelected) {
                                                    AppOutlinedActionButton(label = "Active", onClick = { })
                                                } else {
                                                    AppFilledActionButton(
                                                        label = "Appliquer",
                                                        onClick = { vm.applySolution(name) }
                                                    )
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
                    DeferredOrdersSection(
                        deferredOrders = plan.deferredOrders,
                        deferredReasons = plan.deferredReasons,
                        onOpenAll = if (plan.deferredOrders.isNotEmpty()) {
                            { navController.navigate("deferred-orders") }
                        } else {
                            null
                        }
                    )
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
private fun DeferredOrdersSection(
    deferredOrders: List<Order>,
    deferredReasons: Map<String, String>,
    onOpenAll: (() -> Unit)? = null
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Commandes reportees", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Apercu des commandes non expediees dans ce plan",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (onOpenAll != null && deferredOrders.isNotEmpty()) {
                AppOutlinedActionButton(
                    label = "Voir toutes",
                    onClick = onOpenAll
                )
            }

            if (deferredOrders.isEmpty()) {
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Aucune commande reportee", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Toutes les commandes retenues par le plan ont pu etre expediees.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                deferredOrders.take(10).forEach { order ->
                    DeferredOrderCard(
                        order = order,
                        reason = deferredReasons[order.id] ?: "Reportee"
                    )
                }

                if (deferredOrders.size > 10) {
                    Text(
                        "... +${deferredOrders.size - 10} autre(s)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DeferredOrderCard(
    order: Order,
    reason: String
) {
    val reasonStyle = rememberDeferredReasonStyle(reason)
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(order.id, style = MaterialTheme.typography.titleSmall)
                    Text(
                        order.note.ifBlank { "Commande reportee" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(8.dp))
                AssistChip(
                    onClick = {},
                    label = { Text(order.priority.toFrenchLabel()) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DeferredMetric("Poids", "${order.weightKg} kg", Modifier.weight(1f))
                DeferredMetric("Volume", "${order.volumeM3} m3", Modifier.weight(1f))
                DeferredMetric("Valeur", "${order.priceEur} EUR", Modifier.weight(1f))
            }

            Card(
                shape = MaterialTheme.shapes.small,
                colors = CardDefaults.cardColors(
                    containerColor = reasonStyle.containerColor
                ),
                border = BorderStroke(1.dp, reasonStyle.borderColor)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = reasonStyle.icon,
                        contentDescription = null,
                        tint = reasonStyle.iconTint
                    )
                    Text(
                        reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun DeferredMetric(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeferredOrdersScreen(
    vm: MainViewModel,
    onBack: () -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val plan = state.currentPlan

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Toutes les reportees") },
                navigationIcon = {
                    AppOutlinedActionButton(label = "Retour", onClick = onBack)
                }
            )
        }
    ) { pad ->
        if (plan == null) {
            Column(
                modifier = Modifier
                    .padding(pad)
                    .padding(16.dp)
            ) {
                Text("Aucun plan disponible.")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                DeferredOrdersSection(
                    deferredOrders = plan.deferredOrders,
                    deferredReasons = plan.deferredReasons
                )
            }
        }
    }
}

private data class DeferredReasonStyle(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val containerColor: Color,
    val borderColor: Color,
    val iconTint: Color
)

@Composable
private fun rememberDeferredReasonStyle(reason: String): DeferredReasonStyle {
    val lower = reason.lowercase()
    return when {
        "poids" in lower -> DeferredReasonStyle(
            icon = Icons.Filled.Scale,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f),
            borderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.45f),
            iconTint = MaterialTheme.colorScheme.tertiary
        )
        "volume" in lower -> DeferredReasonStyle(
            icon = Icons.Filled.Inventory2,
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
            borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f),
            iconTint = MaterialTheme.colorScheme.secondary
        )
        "priorite" in lower || "place disponible" in lower -> DeferredReasonStyle(
            icon = Icons.Filled.LocalShipping,
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
            iconTint = MaterialTheme.colorScheme.primary
        )
        else -> DeferredReasonStyle(
            icon = Icons.Filled.WarningAmber,
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
            borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.35f),
            iconTint = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun ContainerCardV2(
    c: ShipmentContainer,
    onOpen: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Conteneur n°${c.index + 1}", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Recette: ${c.revenueEur} EUR | Commandes: ${c.orders.size}",
                    style = MaterialTheme.typography.bodySmall
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    AppOutlinedActionButton(
                        label = if (expanded) "Reduire" else "Details",
                        onClick = { expanded = !expanded }
                    )
                    AppFilledActionButton(
                        label = "Voir",
                        onClick = { onOpen(c.index) }
                    )
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


