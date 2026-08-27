package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AlertType(val label: String) {
    TRAIN_APPROACHING("TRAIN_APPROACHING"),
    FINAL_APPROACH("FINAL_APPROACH"),
    DESTINATION_ASSISTANCE("DESTINATION_ASSISTANCE")
}

@Entity(tableName = "alerts")
data class Alert(
    @PrimaryKey val alertId: String, // e.g. ALT-102458-3
    val requestId: String,
    val alertType: String,
    val triggerStation: String,
    val destinationStation: String,
    val trainId: String = "",
    val passengerCount: Int = 1,
    val assistanceType: String = "",
    val wheelchairRequired: Boolean = false,
    val etaRemainingSeconds: Long = 0L,
    val generatedAt: Long = System.currentTimeMillis(),
    val acknowledgedAt: Long? = null,
    val acknowledgedBy: String? = null,
    val status: String = "ACTIVE" // "ACTIVE", "ACKNOWLEDGED", "DISMISSED"
)
