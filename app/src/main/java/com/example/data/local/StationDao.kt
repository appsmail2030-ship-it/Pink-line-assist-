package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Station
import kotlinx.coroutines.flow.Flow

@Dao
interface StationDao {
    @Query("SELECT * FROM stations ORDER BY sequenceNumber ASC")
    fun getAllStations(): Flow<List<Station>>

    @Query("SELECT * FROM stations WHERE isActive = 1 ORDER BY sequenceNumber ASC")
    fun getActiveStations(): Flow<List<Station>>

    @Query("SELECT * FROM stations WHERE id = :stationId LIMIT 1")
    suspend fun getStationById(stationId: String): Station?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStations(stations: List<Station>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStation(station: Station)

    @Update
    suspend fun updateStation(station: Station)

    @Query("SELECT COUNT(*) FROM stations")
    suspend fun getStationCount(): Int
}
