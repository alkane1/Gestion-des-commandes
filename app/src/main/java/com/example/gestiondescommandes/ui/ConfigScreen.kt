package com.example.gestiondescommandes.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestiondescommandes.MainViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreenV2(vm: MainViewModel, padding: PaddingValues) {
    val state by vm.state.collectAsState()
    var cfg by remember { mutableStateOf(state.config) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conteneurs") },
                actions = {
                    TextButton(onClick = { vm.updateConfig(cfg) }) { Text("Sauvegarger") }
                }
            )
        }
    ) { inner ->
        Column(
            Modifier.padding(padding).padding(inner).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SettingCard(
                title = "Capacité poids",
                subtitle = "${cfg.maxWeightKg.toInt()} kg"
            ) {
                Slider(
                    value = cfg.maxWeightKg.toFloat(),
                    onValueChange = { cfg = cfg.copy(maxWeightKg = it.toDouble()) },
                    valueRange = 500f..8000f
                )
            }

            SettingCard(
                title = "Capacité volume",
                subtitle = "${cfg.maxVolumeM3.toInt()} m³"
            ) {
                Slider(
                    value = cfg.maxVolumeM3.toFloat(),
                    onValueChange = { cfg = cfg.copy(maxVolumeM3 = it.toDouble()) },
                    valueRange = 10f..200f
                )
            }

            SettingCard(
                title = "Seuil min de remplissage",
                subtitle = "${(cfg.minFillThreshold * 100).roundToInt()} %"
            ) {
                Slider(
                    value = cfg.minFillThreshold.toFloat(),
                    onValueChange = { cfg = cfg.copy(minFillThreshold = it.toDouble()) },
                    valueRange = 0.10f..0.95f
                )
            }

            SettingCard(
                title = "Nombre de conteneurs",
                subtitle = "${cfg.containerCount}"
            ) {
                Slider(
                    value = cfg.containerCount.toFloat(),
                    onValueChange = { cfg = cfg.copy(containerCount = it.roundToInt().coerceIn(1, 12)) },
                    valueRange = 1f..12f
                )
            }

            HorizontalDivider()

            Text(
                "Règles:\n• HIGH/URGENT partent immédiatement.\n• Conteneur sans priorité et sous le seuil → report.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SettingCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(shape = MaterialTheme.shapes.large, elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.labelLarge)
            }
            content()
        }
    }
}