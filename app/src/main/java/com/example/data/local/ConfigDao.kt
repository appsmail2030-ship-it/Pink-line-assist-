package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TravelTimeConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfigDao {
    @Query("SELECT * FROM travel_time_config WHERE id = 1 LIMIT 1")
    fun getConfig(): Flow<TravelTimeConfig?>

    @Query("SELECT * FROM travel_time_config WHERE id = 1 LIMIT 1")
    suspend fun getConfigSync(): TravelTimeConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setConfig(config: TravelTimeConfig)

    @Update
    suspend fun updateConfig(config: TravelTimeConfig)
}
