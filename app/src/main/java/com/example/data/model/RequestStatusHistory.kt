package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "request_status_history")
data class RequestStatusHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val requestId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val user: String,
    val locationStation: String,
    val previousStatus: String,
    val newStatus: String,
    val note: String = ""
)
