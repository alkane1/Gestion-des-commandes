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
fun ManualScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manuel") },
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
            ManualSection(
                title = "Utilisation rapide",
                lines = listOf(
                    "1. Ouvrir l'ecran Commandes",
                    "2. Verifier ou regenerer les commandes",
                    "3. Configurer les conteneurs",
                    "4. Calculer le plan",
                    "5. Comparer les solutions",
                    "6. Consulter les conteneurs et les reports"
                )
            )

            ManualSection(
                title = "Ecrans disponibles",
                lines = listOf(
                    "Accueil : resume global de l'application",
                    "Commandes : liste, filtres et recherche",
                    "Conteneurs : configuration des capacites",
                    "Plan : resultat du calcul et comparaison des solutions",
                    "A propos : objectif et cadre du projet"
                )
            )

            ManualSection(
                title = "Algorithme de remplissage",
                lines = listOf(
                    "Les commandes prioritaires sont traitees avant les autres",
                    "Le placement initial utilise une heuristique gloutonne",
                    "Une optimisation locale ameliore ensuite la repartition",
                    "Les conteneurs trop peu remplis sans priorite peuvent etre reportes"
                )
            )
        }
    }
}

@Composable
private fun ManualSection(title: String, lines: List<String>) {
    Card(shape = MaterialTheme.shapes.large) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            lines.forEach { line ->
                Text(line, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
