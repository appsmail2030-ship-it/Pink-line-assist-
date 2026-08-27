package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AssistanceType(val label: String) {
    WHEELCHAIR("Wheelchair Assistant"),
    VISUALLY_IMPAIRED("Visually Impaired"),
    OTHER("Other Special Assistance");

    companion object {
        fun fromString(value: String): AssistanceType {
            return entries.firstOrNull { it.label.equals(value, ignoreCase = true) || it.name.equals(value, ignoreCase = true) }
                ?: OTHER
        }
    }
}

enum class RequestStatus(val label: String) {
    REQUESTED("REQUESTED"),
    ACCEPTED("ACCEPTED"),
    TRAIN_APPROACHING("TRAIN APPROACHING"),
    BOARDING("BOARDING"),
    EN_ROUTE("EN ROUTE"),
    TWO_STATIONS_AWAY("2 STATIONS AWAY"),
    ONE_STATION_AWAY("1 STATION AWAY"),
    ARRIVED("ARRIVED"),
    ASSISTANCE_COMPLETED("ASSISTANCE COMPLETED"),
    CANCELLED("CANCELLED");

    companion object {
        fun fromString(value: String): RequestStatus {
            return entries.firstOrNull { it.label.equals(value, ignoreCase = true) || it.name.equals(value, ignoreCase = true) }
                ?: REQUESTED
        }
    }
}

@Entity(tableName = "assistance_requests")
data class AssistanceRequest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val requestId: String, // e.g. PM-102458
    val sourceStationId: String,
    val destinationStationId: String,
    val trainId: String,
    val passengerCount: Int = 1,
    val assistanceType: String,
    val wheelchairRequired: Boolean,
    val circularDirection: String = "Circular Platform 1",
    val stationCount: Int,
    val estimatedArrival: Long,
    val actualArrival: Long? = null,
    val status: String = RequestStatus.REQUESTED.label,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val isSynced: Boolean = true,
    val remarks: String = ""
)
