package com.example.gestiondescommandes.algo

import com.example.gestiondescommandes.data.*
import kotlin.math.max
import kotlin.random.Random

object Packing {

    data class Heuristic(
        val alphaWeight: Double,
        val betaVolume: Double,
        val priorityBonus: Double,
        val jitter: Double = 0.0,
        val seed: Int? = null
    )

    fun buildPlan(
        orders: List<Order>,
        config: ContainerConfig,
        heuristic: Heuristic = Heuristic(alphaWeight = 1.0, betaVolume = 1.0, priorityBonus = 0.25)
    ): ShipmentPlan {
        val containers = (0 until config.containerCount).map {
            MutableContainer(it, config.maxWeightKg, config.maxVolumeM3)
        }.toMutableList()

        val (priorityOrders, normalOrders) = orders.partition { it.priority != Priority.NORMAL }

        // 1) Prioritaires d'abord (URGENT puis HIGH)
        val prioSorted = priorityOrders.sortedByDescending { it.priority }
        val remainingAfterPrio = placeGreedy(prioSorted, containers, config, heuristic)

        // 2) Normales ensuite (optimisation recette)
        val remainingAll = placeGreedy(normalOrders + remainingAfterPrio, containers, config, heuristic)

        // 3) Appliquer règle seuil : conteneur < seuil reporté, sauf s'il contient des prioritaires
        val finalContainers = containers.map { mc ->
            val combined = (mc.fillWeight() + mc.fillVolume()) / 2.0
            val hasPriority = mc.orders.any { it.priority != Priority.NORMAL }
            if (!hasPriority && combined < config.minFillThreshold) {
                mc.toDeferredContainer()
            } else {
                mc.toShipmentContainer()
            }
        }

        val shipped = finalContainers.flatMap { it.orders }

        val deferredFromLowFill = containers
            .filter { mc ->
                val combined = (mc.fillWeight() + mc.fillVolume()) / 2.0
                val hasPriority = mc.orders.any { it.priority != Priority.NORMAL }
                !hasPriority && combined < config.minFillThreshold
            }
            .flatMap { it.orders }

        val deferred = (remainingAll + deferredFromLowFill)
            .distinctBy { it.id }
            .filter { it.id !in shipped.map { s -> s.id }.toSet() }

        val revenue = finalContainers.sumOf { it.revenueEur }

        return ShipmentPlan(
            containers = finalContainers,
            shippedOrders = shipped,
            deferredOrders = deferred,
            totalRevenueEur = round2(revenue)
        )
    }

    fun proposeSolutions(
        orders: List<Order>,
        config: ContainerConfig,
        count: Int = 3
    ): List<Pair<String, ShipmentPlan>> {
        val heuristics = listOf(
            "Équilibré" to Heuristic(1.0, 1.0, 0.25, jitter = 0.02, seed = 1),
            "Poids prioritaire" to Heuristic(1.4, 0.8, 0.25, jitter = 0.03, seed = 2),
            "Volume prioritaire" to Heuristic(0.8, 1.4, 0.25, jitter = 0.03, seed = 3),
            "Prix agressif" to Heuristic(0.9, 0.9, 0.10, jitter = 0.05, seed = 4),
        ).take(max(1, count))

        return heuristics
            .map { (name, h) -> name to buildPlan(orders, config, h) }
            .sortedByDescending { it.second.totalRevenueEur }
    }

    // ---------- internals ----------

    private class MutableContainer(val index: Int, val maxW: Double, val maxV: Double) {
        val orders = mutableListOf<Order>()
        var usedW = 0.0
        var usedV = 0.0
        var revenue = 0.0

        fun canFit(o: Order): Boolean =
            usedW + o.weightKg <= maxW && usedV + o.volumeM3 <= maxV

        fun add(o: Order) {
            orders.add(o)
            usedW += o.weightKg
            usedV += o.volumeM3
            revenue += o.priceEur
        }

        fun fillWeight() = if (maxW <= 0) 0.0 else usedW / maxW
        fun fillVolume() = if (maxV <= 0) 0.0 else usedV / maxV

        fun toShipmentContainer(): ShipmentContainer {
            val fw = fillWeight()
            val fv = fillVolume()
            val combined = (fw + fv) / 2.0
            return ShipmentContainer(
                index = index,
                orders = orders.toList(),
                usedWeightKg = round2(usedW),
                usedVolumeM3 = round2(usedV),
                revenueEur = round2(revenue),
                fillWeight = round2(fw),
                fillVolume = round2(fv),
                combinedFill = round2(combined)
            )
        }

        fun toDeferredContainer(): ShipmentContainer {
            return ShipmentContainer(
                index = index,
                orders = emptyList(),
                usedWeightKg = 0.0,
                usedVolumeM3 = 0.0,
                revenueEur = 0.0,
                fillWeight = 0.0,
                fillVolume = 0.0,
                combinedFill = 0.0
            )
        }
    }

    private fun placeGreedy(
        orders: List<Order>,
        containers: MutableList<MutableContainer>,
        config: ContainerConfig,
        h: Heuristic
    ): List<Order> {
        val rnd = h.seed?.let { Random(it) } ?: Random.Default

        val scored = orders.map { o ->
            val wRatio = o.weightKg / config.maxWeightKg
            val vRatio = o.volumeM3 / config.maxVolumeM3
            val denom = (h.alphaWeight * wRatio) + (h.betaVolume * vRatio) + 1e-9

            val prioBonus = when (o.priority) {
                Priority.URGENT -> h.priorityBonus * 2.0
                Priority.HIGH -> h.priorityBonus
                Priority.NORMAL -> 0.0
            }
            val jitter = rnd.nextDouble(-1.0, 1.0) * h.jitter
            val score = (o.priceEur / denom) * (1.0 + prioBonus + jitter)
            o to score
        }.sortedByDescending { it.second }

        val remaining = mutableListOf<Order>()

        for ((o, _) in scored) {
            val best = containers
                .filter { it.canFit(o) }
                .minByOrNull { c ->
                    val newW = (c.usedW + o.weightKg) / config.maxWeightKg
                    val newV = (c.usedV + o.volumeM3) / config.maxVolumeM3
                    1.0 - ((newW + newV) / 2.0)
                }

            if (best != null) best.add(o) else remaining.add(o)
        }
        return remaining
    }

    private fun round2(x: Double) = kotlin.math.round(x * 100.0) / 100.0
}