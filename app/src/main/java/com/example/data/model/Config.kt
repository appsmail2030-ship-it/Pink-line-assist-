package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "travel_time_config")
data class TravelTimeConfig(
    @PrimaryKey val id: Int = 1,
    val secondsPerStation: Int = 130, // 130 seconds per station default
    val warningThresholdStations: Int = 3, // 3 stations advance alarm
    val criticalThresholdStations: Int = 1, // 1 station advance alarm
    val destinationThresholdStations: Int = 0, // Destination alarm
    val isDemoMode: Boolean = true,
    val audioAlarmsEnabled: Boolean = true
)
