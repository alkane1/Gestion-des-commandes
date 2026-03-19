package com.example.gestiondescommandes.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gestiondescommandes.MainViewModel
import com.example.gestiondescommandes.ui.components.AppFilledActionButton
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreenV2(vm: MainViewModel, padding: PaddingValues) {
    val state by vm.state.collectAsStateWithLifecycle()
    var cfg by remember(state.config) { mutableStateOf(state.config) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conteneurs") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    AppFilledActionButton(label = "Sauvegarder", onClick = { vm.updateConfig(cfg) })
                }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .padding(padding)
                .padding(inner)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SettingCard(
                title = "Capacite poids",
                subtitle = "${cfg.maxWeightKg.toInt()} kg"
            ) {
                Slider(
                    value = cfg.maxWeightKg.toFloat(),
                    onValueChange = { cfg = cfg.copy(maxWeightKg = it.toDouble()) },
                    valueRange = 500f..8000f
                )
            }

            SettingCard(
                title = "Capacite volume",
                subtitle = "${cfg.maxVolumeM3.toInt()} m3"
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

