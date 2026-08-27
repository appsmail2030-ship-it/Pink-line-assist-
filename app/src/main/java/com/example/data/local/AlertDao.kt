package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Alert
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts ORDER BY generatedAt DESC")
    fun getAllAlerts(): Flow<List<Alert>>

    @Query("SELECT * FROM alerts WHERE status = 'ACTIVE' ORDER BY generatedAt DESC")
    fun getActiveAlerts(): Flow<List<Alert>>

    @Query("SELECT * FROM alerts WHERE requestId = :requestId AND alertType = :alertType LIMIT 1")
    suspend fun getAlertByRequestAndType(requestId: String, alertType: String): Alert?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAlert(alert: Alert): Long

    @Update
    suspend fun updateAlert(alert: Alert)

    @Query("UPDATE alerts SET status = 'ACKNOWLEDGED', acknowledgedAt = :timestamp, acknowledgedBy = :user WHERE alertId = :alertId")
    suspend fun acknowledgeAlert(alertId: String, user: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE alerts SET status = 'ACKNOWLEDGED', acknowledgedAt = :timestamp, acknowledgedBy = :user WHERE requestId = :requestId")
    suspend fun acknowledgeAllAlertsForRequest(requestId: String, user: String, timestamp: Long = System.currentTimeMillis())
}
