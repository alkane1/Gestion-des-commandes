package com.example.gestiondescommandes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.gestiondescommandes.algo.Packing
import com.example.gestiondescommandes.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiState(
    val orders: List<Order> = emptyList(),
    val config: ContainerConfig = ContainerConfig(),
    val selectedOrderId: String? = null,
    val currentPlan: ShipmentPlan? = null,
    val solutions: List<Pair<String, ShipmentPlan>> = emptyList(),
    val selectedSolutionName: String? = null,
    val darkMode: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state
    private val userPreferencesStore = UserPreferencesStore(application)

    init {
        regenerateOrders()
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            userPreferencesStore.preferencesFlow.collect { prefs ->
                _state.update { state ->
                    state.copy(
                        darkMode = prefs.darkMode,
                        config = prefs.config
                    )
                }
            }
        }
    }

    fun regenerateOrders(count: Int = 25) {
        val newOrders = RandomOrders.generate(count)
        _state.update {
            it.copy(
                orders = newOrders,
                currentPlan = null,
                solutions = emptyList(),
                selectedSolutionName = null
            )
        }
    }

    fun updateConfig(cfg: ContainerConfig) {
        _state.update { it.copy(config = cfg) }
        viewModelScope.launch {
            userPreferencesStore.setConfig(cfg)
        }
    }

    fun selectOrder(id: String?) {
        _state.update { it.copy(selectedOrderId = id) }
    }

    fun buildShipmentPlan() {
        val s = _state.value
        val plan = Packing.buildPlan(s.orders, s.config)
        _state.update { it.copy(currentPlan = plan, solutions = emptyList(), selectedSolutionName = null) }
    }

    fun proposeSolutions(count: Int = 3) {
        val s = _state.value
        val sols = Packing.proposeSolutions(s.orders, s.config, count)
        _state.update {
            it.copy(
                solutions = sols,
                currentPlan = sols.firstOrNull()?.second,
                selectedSolutionName = sols.firstOrNull()?.first
            )
        }
    }

    fun applySolution(name: String) {
        val s = _state.value
        val selected = s.solutions.firstOrNull { it.first == name } ?: return
        _state.update {
            it.copy(
                currentPlan = selected.second,
                selectedSolutionName = selected.first
            )
        }
    }

    fun toggleDarkMode() {
        val enabled = !_state.value.darkMode
        _state.update { it.copy(darkMode = enabled) }
        viewModelScope.launch {
            userPreferencesStore.setDarkMode(enabled)
        }
    }

    fun setDarkMode(enabled: Boolean) {
        _state.update { it.copy(darkMode = enabled) }
        viewModelScope.launch {
            userPreferencesStore.setDarkMode(enabled)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                MainViewModel(application)
            }
        }
    }
}
