package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Alert
import com.example.data.model.AssistanceRequest
import com.example.data.model.AuditLog
import com.example.data.model.RequestStatusHistory
import com.example.data.model.Station
import com.example.data.model.Train
import com.example.data.model.TravelTimeConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Station::class,
        Train::class,
        AssistanceRequest::class,
        RequestStatusHistory::class,
        Alert::class,
        TravelTimeConfig::class,
        AuditLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun stationDao(): StationDao
    abstract fun trainDao(): TrainDao
    abstract fun assistanceRequestDao(): AssistanceRequestDao
    abstract fun alertDao(): AlertDao
    abstract fun requestStatusHistoryDao(): RequestStatusHistoryDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun configDao(): ConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pink_line_assist.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        populateInitialData(database)
                    }
                }
            }

            suspend fun populateInitialData(database: AppDatabase) {
                // Populate stations
                database.stationDao().insertStations(Station.DEFAULT_PINK_LINE_STATIONS)

                // Populate trains
                database.trainDao().insertTrains(Train.DEFAULT_TRAINS)

                // Populate default config
                database.configDao().setConfig(TravelTimeConfig())

                // Initial audit log
                database.auditLogDao().insertLog(
                    AuditLog(
                        user = "SYSTEM",
                        action = "SYSTEM_INITIALIZED",
                        details = "Pink Line Assist database initialized with 38 stations and default trains."
                    )
                )
            }
        }
    }
}
