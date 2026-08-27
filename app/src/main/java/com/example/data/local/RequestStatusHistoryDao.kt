package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.RequestStatusHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface RequestStatusHistoryDao {
    @Query("SELECT * FROM request_status_history WHERE requestId = :requestId ORDER BY timestamp ASC")
    fun getHistoryForRequest(requestId: String): Flow<List<RequestStatusHistory>>

    @Query("SELECT * FROM request_status_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<RequestStatusHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: RequestStatusHistory): Long
}
