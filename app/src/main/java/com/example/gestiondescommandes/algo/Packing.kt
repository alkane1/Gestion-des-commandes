package com.example.gestiondescommandes.algo

import com.example.gestiondescommandes.data.ContainerConfig
import com.example.gestiondescommandes.data.Order
import com.example.gestiondescommandes.data.Priority
import com.example.gestiondescommandes.data.ShipmentContainer
import com.example.gestiondescommandes.data.ShipmentPlan
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

        val prioSorted = priorityOrders.sortedByDescending { it.priority }
        val remainingAfterPrio = placeGreedy(prioSorted, containers, config, heuristic)

        val remainingAll = placeGreedy(normalOrders + remainingAfterPrio, containers, config, heuristic)

        optimizeLocalSearch(containers, config)

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

        val shippedIds = shipped.map { it.id }.toSet()
        val deferred = (remainingAll + deferredFromLowFill)
            .distinctBy { it.id }
            .filter { it.id !in shippedIds }

        val deferredReasons = buildDeferredReasons(
            deferred = deferred,
            remaining = remainingAll,
            deferredFromLowFill = deferredFromLowFill,
            config = config
        )

        val revenue = finalContainers.sumOf { it.revenueEur }

        return ShipmentPlan(
            containers = finalContainers,
            shippedOrders = shipped,
            deferredOrders = deferred,
            deferredReasons = deferredReasons,
            totalRevenueEur = round2(revenue)
        )
    }

    fun proposeSolutions(
        orders: List<Order>,
        config: ContainerConfig,
        count: Int = 3
    ): List<Pair<String, ShipmentPlan>> {
        val heuristics = listOf(
            "Equilibre" to Heuristic(1.0, 1.0, 0.25, jitter = 0.02, seed = 1),
            "Poids prioritaire" to Heuristic(1.4, 0.8, 0.25, jitter = 0.03, seed = 2),
            "Volume prioritaire" to Heuristic(0.8, 1.4, 0.25, jitter = 0.03, seed = 3),
            "Prix agressif" to Heuristic(0.9, 0.9, 0.10, jitter = 0.05, seed = 4)
        ).take(max(1, count))

        return heuristics
            .map { (name, h) -> name to buildPlan(orders, config, h) }
            .sortedByDescending { it.second.totalRevenueEur }
    }

    private class MutableContainer(val index: Int, val maxW: Double, val maxV: Double) {
        val orders = mutableListOf<Order>()
        var usedW = 0.0
        var usedV = 0.0
        var revenue = 0.0

        fun canFit(o: Order): Boolean =
            usedW + o.weightKg <= maxW && usedV + o.volumeM3 <= maxV

        fun canFitAfterRemoving(o: Order, add: Order): Boolean {
            val nextW = usedW - o.weightKg + add.weightKg
            val nextV = usedV - o.volumeM3 + add.volumeM3
            return nextW <= maxW && nextV <= maxV
        }

        fun add(o: Order) {
            orders.add(o)
            usedW += o.weightKg
            usedV += o.volumeM3
            revenue += o.priceEur
        }

        fun remove(o: Order) {
            if (orders.remove(o)) {
                usedW -= o.weightKg
                usedV -= o.volumeM3
                revenue -= o.priceEur
            }
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

    private fun optimizeLocalSearch(
        containers: MutableList<MutableContainer>,
        config: ContainerConfig,
        maxIterations: Int = 30
    ) {
        var currentScore = evaluateScore(containers, config)

        repeat(maxIterations) {
            var bestAction: (() -> Unit)? = null
            var bestScore = currentScore

            for (i in containers.indices) {
                for (j in containers.indices) {
                    if (i == j) continue

                    val source = containers[i]
                    val target = containers[j]

                    for (order in source.orders.toList()) {
                        if (order.priority != Priority.NORMAL) continue
                        if (!target.canFit(order)) continue

                        val candidateScore = evaluateMove(containers, config, i, j, order)
                        if (candidateScore > bestScore + 1e-6) {
                            bestScore = candidateScore
                            bestAction = {
                                source.remove(order)
                                target.add(order)
                            }
                        }
                    }

                    for (left in source.orders.toList()) {
                        if (left.priority != Priority.NORMAL) continue
                        for (right in target.orders.toList()) {
                            if (right.priority != Priority.NORMAL) continue
                            if (!source.canFitAfterRemoving(left, right)) continue
                            if (!target.canFitAfterRemoving(right, left)) continue

                            val candidateScore = evaluateSwap(containers, config, i, j, left, right)
                            if (candidateScore > bestScore + 1e-6) {
                                bestScore = candidateScore
                                bestAction = {
                                    source.remove(left)
                                    target.remove(right)
                                    source.add(right)
                                    target.add(left)
                                }
                            }
                        }
                    }
                }
            }

            if (bestAction == null) return
            bestAction.invoke()
            currentScore = bestScore
        }
    }

    private fun evaluateMove(
        containers: MutableList<MutableContainer>,
        config: ContainerConfig,
        sourceIndex: Int,
        targetIndex: Int,
        order: Order
    ): Double {
        val cloned = cloneContainers(containers)
        val source = cloned[sourceIndex]
        val target = cloned[targetIndex]
        source.remove(order)
        target.add(order)
        return evaluateScore(cloned, config)
    }

    private fun evaluateSwap(
        containers: MutableList<MutableContainer>,
        config: ContainerConfig,
        leftIndex: Int,
        rightIndex: Int,
        leftOrder: Order,
        rightOrder: Order
    ): Double {
        val cloned = cloneContainers(containers)
        val left = cloned[leftIndex]
        val right = cloned[rightIndex]
        left.remove(leftOrder)
        right.remove(rightOrder)
        left.add(rightOrder)
        right.add(leftOrder)
        return evaluateScore(cloned, config)
    }

    private fun cloneContainers(containers: List<MutableContainer>): MutableList<MutableContainer> {
        return containers.map { original ->
            MutableContainer(original.index, original.maxW, original.maxV).also { copy ->
                original.orders.forEach { copy.add(it) }
            }
        }.toMutableList()
    }

    private fun evaluateScore(containers: List<MutableContainer>, config: ContainerConfig): Double {
        var shippedRevenue = 0.0
        var shippedCount = 0
        var lowFillContainers = 0
        var fillSum = 0.0
        var nonEmpty = 0

        containers.forEach { c ->
            if (c.orders.isNotEmpty()) {
                nonEmpty++
                fillSum += (c.fillWeight() + c.fillVolume()) / 2.0
            }

            val hasPriority = c.orders.any { it.priority != Priority.NORMAL }
            val combined = (c.fillWeight() + c.fillVolume()) / 2.0
            val isShipped = hasPriority || combined >= config.minFillThreshold

            if (isShipped) {
                shippedRevenue += c.revenue
                shippedCount += c.orders.size
            } else if (c.orders.isNotEmpty()) {
                lowFillContainers++
            }
        }

        val avgFill = if (nonEmpty == 0) 0.0 else fillSum / nonEmpty

        return (shippedRevenue * 1000.0) +
            (shippedCount * 10.0) -
            (lowFillContainers * 5.0) +
            avgFill
    }

    private fun buildDeferredReasons(
        deferred: List<Order>,
        remaining: List<Order>,
        deferredFromLowFill: List<Order>,
        config: ContainerConfig
    ): Map<String, String> {
        val reasons = mutableMapOf<String, String>()

        deferredFromLowFill.forEach { order ->
            reasons[order.id] = "Reportee: conteneur sous le seuil minimal de remplissage"
        }

        remaining.forEach { order ->
            reasons[order.id] = when {
                order.weightKg > config.maxWeightKg ->
                    "Reportee: poids superieur a la capacite maximale d'un conteneur"

                order.volumeM3 > config.maxVolumeM3 ->
                    "Reportee: volume superieur a la capacite maximale d'un conteneur"

                order.priority != Priority.NORMAL ->
                    "Reportee: capacite insuffisante malgre la priorite"

                else ->
                    "Reportee: aucune place disponible apres priorisation et optimisation"
            }
        }

        return deferred.associate { order ->
            order.id to (reasons[order.id] ?: "Reportee: raison non determinee")
        }
    }

    private fun round2(x: Double) = kotlin.math.round(x * 100.0) / 100.0
}
