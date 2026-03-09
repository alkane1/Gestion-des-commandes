package com.example.gestiondescommandes

import com.example.gestiondescommandes.algo.Packing
import com.example.gestiondescommandes.data.ContainerConfig
import com.example.gestiondescommandes.data.Order
import com.example.gestiondescommandes.data.Priority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PackingBuildPlanTest {

    @Test
    fun priority_orders_ship_even_if_container_is_under_threshold() {
        val config = ContainerConfig(
            maxWeightKg = 100.0,
            maxVolumeM3 = 100.0,
            minFillThreshold = 0.95,
            containerCount = 1
        )
        val orders = listOf(
            order(id = "U1", w = 10.0, v = 10.0, p = 120.0, priority = Priority.URGENT)
        )

        val plan = Packing.buildPlan(orders, config)

        assertEquals(listOf("U1"), plan.shippedOrders.map { it.id })
        assertTrue(plan.deferredOrders.isEmpty())
        assertEquals(120.0, plan.totalRevenueEur, 0.0001)
    }

    @Test
    fun normal_orders_under_threshold_are_deferred() {
        val config = ContainerConfig(
            maxWeightKg = 100.0,
            maxVolumeM3 = 100.0,
            minFillThreshold = 0.90,
            containerCount = 1
        )
        val orders = listOf(
            order(id = "N1", w = 10.0, v = 10.0, p = 80.0, priority = Priority.NORMAL)
        )

        val plan = Packing.buildPlan(orders, config)

        assertTrue(plan.shippedOrders.isEmpty())
        assertEquals(listOf("N1"), plan.deferredOrders.map { it.id })
        assertEquals(0.0, plan.totalRevenueEur, 0.0001)
    }

    @Test
    fun build_plan_keeps_a_strict_partition_between_shipped_and_deferred() {
        val config = ContainerConfig(
            maxWeightKg = 20.0,
            maxVolumeM3 = 20.0,
            minFillThreshold = 0.60,
            containerCount = 2
        )
        val orders = listOf(
            order("A", 8.0, 8.0, 100.0, Priority.HIGH),
            order("B", 8.0, 8.0, 90.0, Priority.NORMAL),
            order("C", 6.0, 6.0, 70.0, Priority.NORMAL),
            order("D", 4.0, 4.0, 50.0, Priority.NORMAL),
            order("E", 12.0, 12.0, 120.0, Priority.NORMAL)
        )

        val plan = Packing.buildPlan(orders, config)

        val shippedIds = plan.shippedOrders.map { it.id }.toSet()
        val deferredIds = plan.deferredOrders.map { it.id }.toSet()
        val inputIds = orders.map { it.id }.toSet()

        assertTrue(shippedIds.intersect(deferredIds).isEmpty())
        assertEquals(inputIds, shippedIds + deferredIds)
    }

    @Test
    fun shipped_containers_never_exceed_weight_or_volume_capacity() {
        val config = ContainerConfig(
            maxWeightKg = 25.0,
            maxVolumeM3 = 25.0,
            minFillThreshold = 0.50,
            containerCount = 3
        )
        val orders = listOf(
            order("O1", 10.0, 5.0, 60.0, Priority.NORMAL),
            order("O2", 5.0, 10.0, 65.0, Priority.NORMAL),
            order("O3", 8.0, 8.0, 70.0, Priority.HIGH),
            order("O4", 6.0, 4.0, 30.0, Priority.NORMAL),
            order("O5", 3.0, 7.0, 35.0, Priority.NORMAL),
            order("O6", 9.0, 9.0, 80.0, Priority.NORMAL)
        )

        val plan = Packing.buildPlan(orders, config)

        plan.containers
            .filter { it.orders.isNotEmpty() }
            .forEach { container ->
                assertTrue(container.usedWeightKg <= config.maxWeightKg + 1e-6)
                assertTrue(container.usedVolumeM3 <= config.maxVolumeM3 + 1e-6)
            }
    }

    private fun order(
        id: String,
        w: Double,
        v: Double,
        p: Double,
        priority: Priority
    ): Order {
        return Order(
            id = id,
            weightKg = w,
            volumeM3 = v,
            priceEur = p,
            priority = priority,
            fragile = false
        )
    }
}
