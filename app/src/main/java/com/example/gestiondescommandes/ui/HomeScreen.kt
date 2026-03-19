package com.example.gestiondescommandes.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gestiondescommandes.MainViewModel
import com.example.gestiondescommandes.data.Priority
import com.example.gestiondescommandes.ui.components.AppFilledIconActionButton
import com.example.gestiondescommandes.ui.components.AppOutlinedActionButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenV2(
    vm: MainViewModel,
    padding: PaddingValues,
    onOpenAbout: () -> Unit,
    onOpenManual: () -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val urgentCount = state.orders.count { it.priority == Priority.URGENT }
    val highCount = state.orders.count { it.priority == Priority.HIGH }
    val configuration = LocalConfiguration.current
    val compact = configuration.screenWidthDp < 420

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Accueil") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    AppFilledIconActionButton(
                        icon = if (state.darkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                        contentDescription = "Theme",
                        onClick = { vm.toggleDarkMode() }
                    )
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(inner)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(6.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Centre de pilotage", style = MaterialTheme.typography.labelLarge)
                    Text("Gestion des commandes", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Application de demonstration pour organiser les commandes, configurer les conteneurs et presenter les resultats du projet.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (compact) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                AppOutlinedActionButton(
                                    label = "A propos",
                                    onClick = onOpenAbout,
                                    modifier = Modifier.weight(1f)
                                )
                                AppOutlinedActionButton(
                                    label = "Manuel",
                                    onClick = onOpenManual,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AppOutlinedActionButton(label = "A propos", onClick = onOpenAbout)
                            AppOutlinedActionButton(label = "Manuel", onClick = onOpenManual)
                        }
                    }
                }
            }

            if (compact) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HomeKpiCard("Commandes", state.orders.size.toString(), Modifier.fillMaxWidth())
                    HomeKpiCard("Urgentes", urgentCount.toString(), Modifier.fillMaxWidth())
                    HomeKpiCard("Elevees", highCount.toString(), Modifier.fillMaxWidth())
                    HomeKpiCard("Conteneurs", "${state.config.containerCount}", Modifier.fillMaxWidth())
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeKpiCard("Commandes", state.orders.size.toString(), Modifier.weight(1f))
                    HomeKpiCard("Urgentes", urgentCount.toString(), Modifier.weight(1f))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeKpiCard("Elevees", highCount.toString(), Modifier.weight(1f))
                    HomeKpiCard("Conteneurs", "${state.config.containerCount}", Modifier.weight(1f))
                }
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Regles de chargement", style = MaterialTheme.typography.titleMedium)
                    if (compact) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            HomeRuleCard(
                                title = "Priorites",
                                description = "Les commandes URGENT et ELEVEE passent avant les autres.",
                                icon = Icons.Filled.PriorityHigh,
                                modifier = Modifier.fillMaxWidth()
                            )
                            HomeRuleCard(
                                title = "Capacites",
                                description = "Le chargement respecte toujours les limites de poids et de volume.",
                                icon = Icons.Filled.Scale,
                                modifier = Modifier.fillMaxWidth()
                            )
                            HomeRuleCard(
                                title = "Seuil minimal",
                                description = "Un conteneur sans priorite peut etre reporte s'il est trop peu rempli.",
                                icon = Icons.Filled.Warehouse,
                                modifier = Modifier.fillMaxWidth()
                            )
                            HomeRuleCard(
                                title = "Objectif",
                                description = "L'algorithme maximise la valeur expediee tout en limitant les reports.",
                                icon = Icons.Filled.AttachMoney,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HomeRuleCard(
                                title = "Priorites",
                                description = "Les commandes URGENT et ELEVEE passent avant les autres.",
                                icon = Icons.Filled.PriorityHigh,
                                modifier = Modifier.weight(1f)
                            )
                            HomeRuleCard(
                                title = "Capacites",
                                description = "Le chargement respecte toujours les limites de poids et de volume.",
                                icon = Icons.Filled.Scale,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HomeRuleCard(
                                title = "Seuil minimal",
                                description = "Un conteneur sans priorite peut etre reporte s'il est trop peu rempli.",
                                icon = Icons.Filled.Warehouse,
                                modifier = Modifier.weight(1f)
                            )
                            HomeRuleCard(
                                title = "Objectif",
                                description = "L'algorithme maximise la valeur expediee tout en limitant les reports.",
                                icon = Icons.Filled.AttachMoney,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeKpiCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun HomeRuleCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
