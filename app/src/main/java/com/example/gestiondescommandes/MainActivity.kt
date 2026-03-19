package com.example.gestiondescommandes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gestiondescommandes.ui.AppNav
import com.example.gestiondescommandes.ui.theme.AppTheme
import androidx.compose.runtime.getValue
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            val vm: MainViewModel = viewModel(factory = MainViewModel.Factory)
            val state by vm.state.collectAsStateWithLifecycle()

            AppTheme(darkTheme = state.darkMode) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNav(vm)
                }
            }
        }
    }
}

