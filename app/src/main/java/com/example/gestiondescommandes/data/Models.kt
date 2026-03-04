package com.example.gestiondescommandes.data

enum class Priority { NORMAL, HIGH, URGENT }

data class Order(
    val id: String,
    val weightKg: Double,
    val volumeM3: Double,
    val priceEur: Double,
    val priority: Priority,
    val fragile: Boolean,
    val note: String = ""
)

data class ContainerConfig(
    val maxWeightKg: Double = 2000.0,
    val maxVolumeM3: Double = 60.0,
    val minFillThreshold: Double = 0.60, // 60%
    val containerCount: Int = 3
)

data class ShipmentContainer(
    val index: Int,
    val orders: List<Order>,
    val usedWeightKg: Double,
    val usedVolumeM3: Double,
    val revenueEur: Double,
    val fillWeight: Double,   // 0..1
    val fillVolume: Double,   // 0..1
    val combinedFill: Double  // moyenne
)

data class ShipmentPlan(
    val containers: List<ShipmentContainer>,
    val shippedOrders: List<Order>,
    val deferredOrders: List<Order>,
    val totalRevenueEur: Double
)