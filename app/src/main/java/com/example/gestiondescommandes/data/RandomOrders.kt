package com.example.gestiondescommandes.data

import java.util.UUID
import kotlin.random.Random

object RandomOrders {

    fun generate(count: Int, seed: Int? = null): List<Order> {
        val rnd = seed?.let { Random(it) } ?: Random.Default

        fun priority(): Priority {
            val x = rnd.nextDouble()
            return when {
                x < 0.10 -> Priority.URGENT
                x < 0.35 -> Priority.HIGH
                else -> Priority.NORMAL
            }
        }

        return (1..count).map { idx ->
            val weight = rnd.nextDouble(20.0, 500.0)
            val volume = rnd.nextDouble(0.2, 8.0)
            val price = rnd.nextDouble(80.0, 7000.0)
            val prio = priority()
            val fragile = rnd.nextDouble() < 0.20

            Order(
                id = "CMD-${idx.toString().padStart(4, '0')}-${UUID.randomUUID().toString().take(4)}",
                weightKg = round2(weight),
                volumeM3 = round2(volume),
                priceEur = round2(price),
                priority = prio,
                fragile = fragile,
                note = if (fragile) "Manipulation délicate" else "Standard"
            )
        }
    }

    private fun round2(x: Double) = kotlin.math.round(x * 100.0) / 100.0
}