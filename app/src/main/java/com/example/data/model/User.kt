package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    STATION_ASSISTANT,
    CONTROL_ROOM,
    ADMIN
}

data class User(
    val employeeId: String,
    val name: String,
    val role: UserRole,
    val assignedStationId: String,
    val stationName: String,
    val token: String = "jwt-token-${System.currentTimeMillis()}"
)
