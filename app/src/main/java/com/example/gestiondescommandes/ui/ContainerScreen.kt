package com.example.gestiondescommandes.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestiondescommandes.data.ShipmentContainer
import com.example.gestiondescommandes.ui.components.AppOutlinedActionButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContainerScreen(
    container: ShipmentContainer,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details du conteneur") },
                navigationIcon = {
                    AppOutlinedActionButton(label = "Retour", onClick = onBack)
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp)
        ) {
            ContainerCardV2(
                c = container,
                onOpen = {}
            )
        }
    }
}
