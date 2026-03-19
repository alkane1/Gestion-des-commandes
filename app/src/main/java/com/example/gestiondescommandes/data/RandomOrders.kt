package com.example.gestiondescommandes.data

import kotlin.random.Random

object RandomOrders {

    fun generate(count: Int, seed: Int? = null): List<Order> {
        val rnd = seed?.let { Random(it) } ?: Random.Default
        val clients = listOf(
            "Hopital Central",
            "Carrefour Logistique",
            "Atelier Nord",
            "Pharmacie Regionale",
            "TechDistrib",
            "Maison Habitat",
            "AutoPieces Express",
            "AgroSud"
        )
        val templates = listOf(
            DemoTemplate("Medicaments refrigeres", 40.0, 180.0, 1200.0, 4200.0, Priority.URGENT, true, "Livraison medicale prioritaire"),
            DemoTemplate("Equipements electroniques", 60.0, 220.0, 2500.0, 6800.0, Priority.HIGH, true, "Materiel sensible"),
            DemoTemplate("Pieces detachees", 80.0, 420.0, 900.0, 3600.0, Priority.NORMAL, false, "Flux atelier standard"),
            DemoTemplate("Mobilier compact", 140.0, 480.0, 700.0, 2400.0, Priority.NORMAL, false, "Livraison amenagement"),
            DemoTemplate("Produits alimentaires secs", 120.0, 500.0, 600.0, 2100.0, Priority.NORMAL, false, "Distribution regionale"),
            DemoTemplate("Consommables pharma", 30.0, 140.0, 1400.0, 3800.0, Priority.HIGH, false, "Stock officine"),
            DemoTemplate("Materiel fragile", 25.0, 110.0, 1600.0, 5200.0, Priority.HIGH, true, "Manipulation delicate"),
            DemoTemplate("Documentation imprimee", 20.0, 90.0, 250.0, 900.0, Priority.NORMAL, false, "Lot administratif")
        )

        fun randomPriority(): Priority {
            val x = rnd.nextDouble()
            return when {
                x < 0.10 -> Priority.URGENT
                x < 0.35 -> Priority.HIGH
                else -> Priority.NORMAL
            }
        }

        return (1..count).map { idx ->
            val template = templates.random(rnd)
            val client = clients.random(rnd)
            val weight = rnd.nextDouble(template.minWeightKg, template.maxWeightKg)
            val volume = rnd.nextDouble(0.4, 7.5)
            val price = rnd.nextDouble(template.minPriceEur, template.maxPriceEur)
            val priority = if (rnd.nextDouble() < 0.70) template.priority else randomPriority()
            val fragile = template.fragile || rnd.nextDouble() < 0.12

            Order(
                id = "CMD-${idx.toString().padStart(4, '0')}",
                weightKg = round2(weight),
                volumeM3 = round2(volume),
                priceEur = round2(price),
                priority = priority,
                fragile = fragile,
                note = "${template.label} pour $client - ${template.note}"
            )
        }
    }

    private data class DemoTemplate(
        val label: String,
        val minWeightKg: Double,
        val maxWeightKg: Double,
        val minPriceEur: Double,
        val maxPriceEur: Double,
        val priority: Priority,
        val fragile: Boolean,
        val note: String
    )

    private fun round2(x: Double) = kotlin.math.round(x * 100.0) / 100.0
}
