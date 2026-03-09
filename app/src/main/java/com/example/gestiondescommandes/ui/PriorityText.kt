package com.example.gestiondescommandes.ui

import com.example.gestiondescommandes.data.Priority

fun Priority.toFrenchLabel(): String = when (this) {
    Priority.URGENT -> "URGENT"
    Priority.HIGH -> "ELEVEE"
    Priority.NORMAL -> "NORMALE"
}
