package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AssistanceRequest
import kotlinx.coroutines.flow.Flow

@Dao
interface AssistanceRequestDao {
    @Query("SELECT * FROM assistance_requests ORDER BY createdAt DESC")
    fun getAllRequests(): Flow<List<AssistanceRequest>>

    @Query("SELECT * FROM assistance_requests WHERE status NOT IN ('ASSISTANCE COMPLETED', 'CANCELLED') ORDER BY createdAt DESC")
    fun getActiveRequests(): Flow<List<AssistanceRequest>>

    @Query("SELECT * FROM assistance_requests WHERE status IN ('ASSISTANCE COMPLETED', 'CANCELLED') ORDER BY updatedAt DESC")
    fun getHistoryRequests(): Flow<List<AssistanceRequest>>

    @Query("SELECT * FROM assistance_requests WHERE sourceStationId = :stationId OR destinationStationId = :stationId ORDER BY createdAt DESC")
    fun getRequestsForStation(stationId: String): Flow<List<AssistanceRequest>>

    @Query("SELECT * FROM assistance_requests WHERE requestId = :requestId LIMIT 1")
    suspend fun getRequestById(requestId: String): AssistanceRequest?

    @Query("SELECT * FROM assistance_requests WHERE isSynced = 0")
    suspend fun getUnsyncedRequests(): List<AssistanceRequest>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: AssistanceRequest): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequests(requests: List<AssistanceRequest>)

    @Update
    suspend fun updateRequest(request: AssistanceRequest)

    @Query("UPDATE assistance_requests SET status = :status, updatedAt = :updatedAt WHERE requestId = :requestId")
    suspend fun updateStatus(requestId: String, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE assistance_requests SET status = 'ASSISTANCE COMPLETED', completedAt = :completedAt, actualArrival = :completedAt, updatedAt = :completedAt WHERE requestId = :requestId")
    suspend fun markCompleted(requestId: String, completedAt: Long = System.currentTimeMillis())

    @Query("UPDATE assistance_requests SET isSynced = 1 WHERE requestId = :requestId")
    suspend fun markSynced(requestId: String)

    @Query("SELECT COUNT(*) FROM assistance_requests WHERE status NOT IN ('ASSISTANCE COMPLETED', 'CANCELLED')")
    fun getActiveCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM assistance_requests WHERE status = 'REQUESTED'")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM assistance_requests WHERE status = 'ASSISTANCE COMPLETED' AND completedAt >= :startOfDayTimestamp")
    fun getCompletedTodayCount(startOfDayTimestamp: Long): Flow<Int>
}
