package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val user: String,
    val action: String,
    val timestamp: Long = System.currentTimeMillis(),
    val requestId: String? = null,
    val details: String = ""
)
