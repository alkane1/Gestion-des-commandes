package com.example.gestiondescommandes.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestiondescommandes.ui.components.AppOutlinedActionButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("A propos") },
                navigationIcon = {
                    AppOutlinedActionButton(label = "Retour", onClick = onBack)
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(shape = MaterialTheme.shapes.large) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Objectif du projet", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Cette application montre comment planifier le chargement de commandes dans des conteneurs en tenant compte des capacites, des priorites et de la rentabilite."
                    )
                }
            }

            Card(shape = MaterialTheme.shapes.large) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Valeur de demonstration", style = MaterialTheme.typography.titleMedium)
                    Text("Visualisation des commandes et de leurs priorites")
                    Text("Configuration dynamique des conteneurs")
                    Text("Comparaison de plusieurs solutions de chargement")
                    Text("Explication des commandes reportees")
                }
            }

            Card(shape = MaterialTheme.shapes.large) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Algorithme", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Le moteur applique un placement glouton puis une optimisation locale par deplacements et echanges entre conteneurs afin d'ameliorer la recette expediee et le remplissage."
                    )
                }
            }
        }
    }
}
