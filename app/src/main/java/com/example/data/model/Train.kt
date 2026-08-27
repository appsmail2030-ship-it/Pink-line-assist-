package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trains")
data class Train(
    @PrimaryKey val trainId: String,
    val currentStationId: String,
    val direction: String, // "+ CIRCULAR LINE (Clockwise)" or "- CIRCULAR LINE (Anti-Clockwise)"
    val status: String = "IN_TRANSIT", // "IN_TRANSIT", "APPROACHING", "STOPPED"
    val lastUpdated: Long = System.currentTimeMillis(),
    val simulationMode: Boolean = true
) {
    companion object {
        val DEFAULT_TRAINS = listOf(
            Train("T-245", "ST_28", "+ CIRCULAR LINE (Clockwise)", "IN_TRANSIT", System.currentTimeMillis(), true),
            Train("T-108", "ST_01", "+ CIRCULAR LINE (Clockwise)", "IN_TRANSIT", System.currentTimeMillis(), true),
            Train("T-302", "ST_22", "- CIRCULAR LINE (Anti-Clockwise)", "IN_TRANSIT", System.currentTimeMillis(), true),
            Train("T-419", "ST_18", "- CIRCULAR LINE (Anti-Clockwise)", "IN_TRANSIT", System.currentTimeMillis(), true)
        )
    }
}
