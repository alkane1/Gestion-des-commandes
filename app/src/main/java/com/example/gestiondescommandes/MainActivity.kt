package com.example.gestiondescommandes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestiondescommandes.ui.AppNav
import com.example.gestiondescommandes.ui.theme.AppTheme
import androidx.compose.runtime.getValue
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: MainViewModel = viewModel()
            val state by vm.state.collectAsState()

            AppTheme(darkTheme = state.darkMode) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNav(vm)
                }
            }
        }
    }
}