package com.example.gestiondescommandes

import androidx.lifecycle.ViewModel
import com.example.gestiondescommandes.algo.Packing
import com.example.gestiondescommandes.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class UiState(
    val orders: List<Order> = emptyList(),
    val config: ContainerConfig = ContainerConfig(),
    val selectedOrderId: String? = null,
    val currentPlan: ShipmentPlan? = null,
    val solutions: List<Pair<String, ShipmentPlan>> = emptyList(),
    val darkMode: Boolean = false // ✅ valeur par défaut
)

class MainViewModel : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    init {
        regenerateOrders()
    }

    fun regenerateOrders(count: Int = 25) {
        val newOrders = RandomOrders.generate(count)
        _state.update { it.copy(orders = newOrders, currentPlan = null, solutions = emptyList()) }
    }

    fun updateConfig(cfg: ContainerConfig) {
        _state.update { it.copy(config = cfg) }
    }

    fun selectOrder(id: String?) {
        _state.update { it.copy(selectedOrderId = id) }
    }

    fun buildShipmentPlan() {
        val s = _state.value
        val plan = Packing.buildPlan(s.orders, s.config)
        _state.update { it.copy(currentPlan = plan, solutions = emptyList()) }
    }

    fun proposeSolutions(count: Int = 3) {
        val s = _state.value
        val sols = Packing.proposeSolutions(s.orders, s.config, count)
        _state.update { it.copy(solutions = sols, currentPlan = sols.firstOrNull()?.second) }
    }

    fun toggleDarkMode() {
        _state.update { it.copy(darkMode = !it.darkMode) }
    }

    fun setDarkMode(enabled: Boolean) {
        _state.update { it.copy(darkMode = enabled) }
    }
}