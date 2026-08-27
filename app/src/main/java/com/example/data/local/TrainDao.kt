package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Train
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainDao {
    @Query("SELECT * FROM trains ORDER BY trainId ASC")
    fun getAllTrains(): Flow<List<Train>>

    @Query("SELECT * FROM trains WHERE trainId = :trainId LIMIT 1")
    suspend fun getTrainById(trainId: String): Train?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrains(trains: List<Train>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrain(train: Train)

    @Update
    suspend fun updateTrain(train: Train)

    @Query("UPDATE trains SET currentStationId = :stationId, lastUpdated = :timestamp WHERE trainId = :trainId")
    suspend fun updateTrainLocation(trainId: String, stationId: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM trains")
    suspend fun getTrainCount(): Int
}
