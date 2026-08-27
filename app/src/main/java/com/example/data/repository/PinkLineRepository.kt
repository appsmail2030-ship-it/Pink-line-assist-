package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.model.Alert
import com.example.data.model.AlertType
import com.example.data.model.AssistanceRequest
import com.example.data.model.AuditLog
import com.example.data.model.RequestStatus
import com.example.data.model.RequestStatusHistory
import com.example.data.model.Station
import com.example.data.model.Train
import com.example.data.model.TravelTimeConfig
import com.example.ui.components.AudioAlarmManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import kotlin.math.abs

class PinkLineRepository(
    private val database: AppDatabase,
    private val audioAlarmManager: AudioAlarmManager? = null,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private val stationDao = database.stationDao()
    private val trainDao = database.trainDao()
    private val requestDao = database.assistanceRequestDao()
    private val alertDao = database.alertDao()
    private val historyDao = database.requestStatusHistoryDao()
    private val auditLogDao = database.auditLogDao()
    private val configDao = database.configDao()

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _alertEvents = MutableSharedFlow<Alert>(extraBufferCapacity = 10)
    val alertEvents: SharedFlow<Alert> = _alertEvents.asSharedFlow()

    init {
        scope.launch {
            // Ensure default stations and trains are populated if empty
            if (stationDao.getStationCount() == 0) {
                stationDao.insertStations(Station.DEFAULT_PINK_LINE_STATIONS)
            }
            if (trainDao.getTrainCount() == 0) {
                trainDao.insertTrains(Train.DEFAULT_TRAINS)
            }
            if (configDao.getConfigSync() == null) {
                configDao.setConfig(TravelTimeConfig())
            }
        }
    }

    fun setOnlineStatus(online: Boolean) {
        _isOnline.value = online
        if (online) {
            scope.launch { syncPendingRequests() }
        }
    }

    // Stations
    fun getAllStations(): Flow<List<Station>> = stationDao.getAllStations()
    fun getActiveStations(): Flow<List<Station>> = stationDao.getActiveStations()
    suspend fun getStationById(id: String): Station? = stationDao.getStationById(id)
    suspend fun updateStation(station: Station, user: String) {
        stationDao.updateStation(station)
        recordAudit(user, "UPDATE_STATION", details = "Updated station ${station.name} (${station.id})")
    }
    suspend fun insertStation(station: Station, user: String) {
        stationDao.insertStation(station)
        recordAudit(user, "CREATE_STATION", details = "Created station ${station.name} (${station.id})")
    }

    // Trains
    fun getAllTrains(): Flow<List<Train>> = trainDao.getAllTrains()
    suspend fun getTrainById(trainId: String): Train? = trainDao.getTrainById(trainId)

    // Config
    fun getConfig(): Flow<TravelTimeConfig> = configDao.getConfig().map { it ?: TravelTimeConfig() }
    suspend fun updateConfig(config: TravelTimeConfig, user: String) {
        configDao.updateConfig(config)
        recordAudit(user, "UPDATE_CONFIG", details = "Updated travel time: ${config.secondsPerStation}s/station, Warning: ${config.warningThresholdStations} stations, Critical: ${config.criticalThresholdStations} stations")
    }

    // Requests
    fun getAllRequests(): Flow<List<AssistanceRequest>> = requestDao.getAllRequests()
    fun getActiveRequests(): Flow<List<AssistanceRequest>> = requestDao.getActiveRequests()
    fun getHistoryRequests(): Flow<List<AssistanceRequest>> = requestDao.getHistoryRequests()
    fun getRequestsForStation(stationId: String): Flow<List<AssistanceRequest>> = requestDao.getRequestsForStation(stationId)
    fun getActiveCount(): Flow<Int> = requestDao.getActiveCount()
    fun getPendingCount(): Flow<Int> = requestDao.getPendingCount()

    fun getCompletedTodayCount(): Flow<Int> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return requestDao.getCompletedTodayCount(calendar.timeInMillis)
    }

    suspend fun getRequestById(requestId: String): AssistanceRequest? = requestDao.getRequestById(requestId)
    fun getHistoryForRequest(requestId: String): Flow<List<RequestStatusHistory>> = historyDao.getHistoryForRequest(requestId)

    // Alerts
    fun getActiveAlerts(): Flow<List<Alert>> = alertDao.getActiveAlerts()
    fun getAllAlerts(): Flow<List<Alert>> = alertDao.getAllAlerts()

    // Audit logs
    fun getAuditLogs(): Flow<List<AuditLog>> = auditLogDao.getAllLogs()

    suspend fun recordAudit(user: String, action: String, requestId: String? = null, details: String = "") {
        auditLogDao.insertLog(
            AuditLog(
                user = user,
                action = action,
                requestId = requestId,
                details = details,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    // Unique Request ID Generator (e.g. PM-102458)
    fun generateRequestId(): String {
        val randomDigits = (100000..999999).random()
        return "PM-$randomDigits"
    }

    // Distance and Station Count Calculation
    suspend fun calculateStationCount(sourceStationId: String, destStationId: String): Int {
        val src = stationDao.getStationById(sourceStationId)
        val dst = stationDao.getStationById(destStationId)
        if (src == null || dst == null) return 1
        val diff = abs(dst.sequenceNumber - src.sequenceNumber)
        return if (diff == 0) 1 else diff
    }

    // ETA Calculation Formula: current timestamp + station count * secondsPerStation
    suspend fun calculateETA(stationCount: Int): Pair<Long, Long> {
        val config = configDao.getConfigSync() ?: TravelTimeConfig()
        val secondsPerStation = config.secondsPerStation
        val estimatedTravelSeconds = (stationCount * secondsPerStation).toLong()
        val etaTimestamp = System.currentTimeMillis() + (estimatedTravelSeconds * 1000)
        return Pair(estimatedTravelSeconds, etaTimestamp)
    }

    // Request Creation
    suspend fun createRequest(
        sourceStationId: String,
        destinationStationId: String,
        trainId: String,
        passengerCount: Int,
        assistanceType: String,
        wheelchairRequired: Boolean,
        circularDirection: String,
        createdBy: String,
        remarks: String = ""
    ): AssistanceRequest {
        val stationCount = calculateStationCount(sourceStationId, destinationStationId)
        val (_, etaTimestamp) = calculateETA(stationCount)
        val requestId = generateRequestId()

        val newRequest = AssistanceRequest(
            requestId = requestId,
            sourceStationId = sourceStationId,
            destinationStationId = destinationStationId,
            trainId = trainId,
            passengerCount = passengerCount,
            assistanceType = assistanceType,
            wheelchairRequired = wheelchairRequired,
            circularDirection = circularDirection,
            stationCount = stationCount,
            estimatedArrival = etaTimestamp,
            status = RequestStatus.REQUESTED.label,
            createdBy = createdBy,
            isSynced = _isOnline.value,
            remarks = remarks
        )

        requestDao.insertRequest(newRequest)

        // Record history
        historyDao.insertHistory(
            RequestStatusHistory(
                requestId = requestId,
                user = createdBy,
                locationStation = sourceStationId,
                previousStatus = "NONE",
                newStatus = RequestStatus.REQUESTED.label,
                note = "Request created with $passengerCount passenger(s), Type: $assistanceType"
            )
        )

        // Record audit
        recordAudit(
            user = createdBy,
            action = "REQUEST_CREATED",
            requestId = requestId,
            details = "Created assistance request for train $trainId from $sourceStationId to $destinationStationId. Wheelchair: $wheelchairRequired"
        )

        // Play request created chime
        audioAlarmManager?.playRequestCreatedChime()

        return newRequest
    }

    // Request Status Update
    suspend fun updateRequestStatus(
        requestId: String,
        newStatus: RequestStatus,
        user: String,
        currentStation: String,
        note: String = ""
    ) {
        val request = requestDao.getRequestById(requestId) ?: return
        val prevStatus = request.status
        requestDao.updateStatus(requestId, newStatus.label)

        historyDao.insertHistory(
            RequestStatusHistory(
                requestId = requestId,
                user = user,
                locationStation = currentStation,
                previousStatus = prevStatus,
                newStatus = newStatus.label,
                note = note
            )
        )

        recordAudit(
            user = user,
            action = "STATUS_CHANGED",
            requestId = requestId,
            details = "Status changed from $prevStatus to ${newStatus.label} at station $currentStation"
        )

        // Play acknowledgment feedback for operator actions
        if (newStatus == RequestStatus.ACCEPTED || newStatus == RequestStatus.BOARDING ||
            newStatus == RequestStatus.ARRIVED || newStatus == RequestStatus.ASSISTANCE_COMPLETED) {
            audioAlarmManager?.playAcknowledgmentChime()
        }
    }

    // Request Completion
    suspend fun completeRequest(
        requestId: String,
        user: String,
        stationId: String,
        remarks: String = ""
    ) {
        val request = requestDao.getRequestById(requestId) ?: return
        val prevStatus = request.status
        requestDao.markCompleted(requestId)

        historyDao.insertHistory(
            RequestStatusHistory(
                requestId = requestId,
                user = user,
                locationStation = stationId,
                previousStatus = prevStatus,
                newStatus = RequestStatus.ASSISTANCE_COMPLETED.label,
                note = "Assistance successfully completed. $remarks"
            )
        )

        recordAudit(
            user = user,
            action = "REQUEST_COMPLETED",
            requestId = requestId,
            details = "Completed request $requestId at station $stationId. Remarks: $remarks"
        )

        // Dismiss or acknowledge active alerts for this request
        alertDao.acknowledgeAllAlertsForRequest(requestId, user)
        audioAlarmManager?.stopAlarm()
        audioAlarmManager?.playAcknowledgmentChime()
    }

    // Alert Acknowledgment
    suspend fun acknowledgeAlert(alertId: String, user: String) {
        alertDao.acknowledgeAlert(alertId, user)
        audioAlarmManager?.stopAlarm()
        audioAlarmManager?.playAcknowledgmentChime()
        recordAudit(
            user = user,
            action = "ALERT_ACKNOWLEDGED",
            details = "Acknowledged alert $alertId"
        )
    }

    // Train Simulator & Alert Trigger Engine
    suspend fun updateTrainLocation(trainId: String, newStationId: String, user: String) {
        val train = trainDao.getTrainById(trainId) ?: return
        trainDao.updateTrainLocation(trainId, newStationId)
        val station = stationDao.getStationById(newStationId)

        recordAudit(
            user = user,
            action = "TRAIN_MOVED",
            details = "Train $trainId moved to ${station?.name ?: newStationId}"
        )

        // Trigger Alert Engine & Auto-Status Progression for all active requests associated with this train
        evaluateAlertRulesForTrain(trainId, newStationId, user)
    }

    // Core Alert Engine & Status Engine
    private suspend fun evaluateAlertRulesForTrain(trainId: String, trainStationId: String, user: String) {
        val config = configDao.getConfigSync() ?: TravelTimeConfig()
        val trainStation = stationDao.getStationById(trainStationId) ?: return
        val activeRequests = requestDao.getActiveRequests().firstOrNull() ?: emptyList()
        val trainRequests = activeRequests.filter { it.trainId.equals(trainId, ignoreCase = true) }

        for (req in trainRequests) {
            val destStation = stationDao.getStationById(req.destinationStationId) ?: continue
            val srcStation = stationDao.getStationById(req.sourceStationId)

            val remainingStations = abs(destStation.sequenceNumber - trainStation.sequenceNumber)
            val isAtSource = trainStation.sequenceNumber == srcStation?.sequenceNumber

            // Update request status based on distance
            when {
                remainingStations == 0 -> {
                    if (req.status != RequestStatus.ARRIVED.label && req.status != RequestStatus.ASSISTANCE_COMPLETED.label) {
                        updateRequestStatus(req.requestId, RequestStatus.ARRIVED, "SYSTEM", trainStation.name, "Train arrived at destination")
                    }
                }
                remainingStations == 1 -> {
                    if (req.status != RequestStatus.ONE_STATION_AWAY.label && req.status != RequestStatus.ARRIVED.label) {
                        updateRequestStatus(req.requestId, RequestStatus.ONE_STATION_AWAY, "SYSTEM", trainStation.name, "1 Station away from destination")
                    }
                }
                remainingStations == 2 -> {
                    if (req.status != RequestStatus.TWO_STATIONS_AWAY.label && req.status != RequestStatus.ONE_STATION_AWAY.label && req.status != RequestStatus.ARRIVED.label) {
                        updateRequestStatus(req.requestId, RequestStatus.TWO_STATIONS_AWAY, "SYSTEM", trainStation.name, "2 Stations away from destination")
                    }
                }
                isAtSource -> {
                    if (req.status == RequestStatus.REQUESTED.label || req.status == RequestStatus.ACCEPTED.label || req.status == RequestStatus.TRAIN_APPROACHING.label) {
                        updateRequestStatus(req.requestId, RequestStatus.BOARDING, "SYSTEM", trainStation.name, "Train at source platform, passenger boarding")
                    }
                }
                else -> {
                    if (req.status == RequestStatus.BOARDING.label) {
                        updateRequestStatus(req.requestId, RequestStatus.EN_ROUTE, "SYSTEM", trainStation.name, "Train en route to destination")
                    }
                }
            }

            // Alert Rules:
            // 1. Destination threshold (remaining == 0)
            if (remainingStations <= config.destinationThresholdStations) {
                generateAlertIfNotExists(
                    requestId = req.requestId,
                    alertType = AlertType.DESTINATION_ASSISTANCE,
                    triggerStation = trainStation.name,
                    destinationStation = destStation.name,
                    trainId = trainId,
                    passengerCount = req.passengerCount,
                    assistanceType = req.assistanceType,
                    wheelchairRequired = req.wheelchairRequired,
                    remainingStations = 0,
                    secondsPerStation = config.secondsPerStation,
                    playAlarm = config.audioAlarmsEnabled
                )
            }
            // 2. Critical threshold (remaining <= 1)
            else if (remainingStations <= config.criticalThresholdStations) {
                generateAlertIfNotExists(
                    requestId = req.requestId,
                    alertType = AlertType.FINAL_APPROACH,
                    triggerStation = trainStation.name,
                    destinationStation = destStation.name,
                    trainId = trainId,
                    passengerCount = req.passengerCount,
                    assistanceType = req.assistanceType,
                    wheelchairRequired = req.wheelchairRequired,
                    remainingStations = remainingStations,
                    secondsPerStation = config.secondsPerStation,
                    playAlarm = config.audioAlarmsEnabled
                )
            }
            // 3. Warning threshold (remaining <= 3)
            else if (remainingStations <= config.warningThresholdStations) {
                generateAlertIfNotExists(
                    requestId = req.requestId,
                    alertType = AlertType.TRAIN_APPROACHING,
                    triggerStation = trainStation.name,
                    destinationStation = destStation.name,
                    trainId = trainId,
                    passengerCount = req.passengerCount,
                    assistanceType = req.assistanceType,
                    wheelchairRequired = req.wheelchairRequired,
                    remainingStations = remainingStations,
                    secondsPerStation = config.secondsPerStation,
                    playAlarm = config.audioAlarmsEnabled
                )
            }
        }
    }

    private suspend fun generateAlertIfNotExists(
        requestId: String,
        alertType: AlertType,
        triggerStation: String,
        destinationStation: String,
        trainId: String,
        passengerCount: Int,
        assistanceType: String,
        wheelchairRequired: Boolean,
        remainingStations: Int,
        secondsPerStation: Int,
        playAlarm: Boolean
    ) {
        val existing = alertDao.getAlertByRequestAndType(requestId, alertType.label)
        if (existing == null) {
            val alertId = "ALT-$requestId-${alertType.name.take(3)}"
            val etaRemainingSeconds = (remainingStations * secondsPerStation).toLong()
            val alert = Alert(
                alertId = alertId,
                requestId = requestId,
                alertType = alertType.label,
                triggerStation = triggerStation,
                destinationStation = destinationStation,
                trainId = trainId,
                passengerCount = passengerCount,
                assistanceType = assistanceType,
                wheelchairRequired = wheelchairRequired,
                etaRemainingSeconds = etaRemainingSeconds,
                generatedAt = System.currentTimeMillis(),
                status = "ACTIVE"
            )
            alertDao.insertAlert(alert)
            _alertEvents.emit(alert)

            recordAudit(
                user = "SYSTEM_ALERT_ENGINE",
                action = "ALERT_GENERATED",
                requestId = requestId,
                details = "Generated alert ${alertType.label} at $triggerStation for destination $destinationStation. Train: $trainId"
            )

            // Trigger audible alarm if enabled
            if (playAlarm && audioAlarmManager != null) {
                val destStation = stationDao.getStationById(destinationStation)
                val platform = destStation?.circularDirection ?: "Platform 1 (+ Circular Line)"
                when (alertType) {
                    AlertType.DESTINATION_ASSISTANCE -> audioAlarmManager.playDestinationAlarm(
                        stationName = destStation?.name ?: destinationStation,
                        trainId = trainId,
                        platform = platform
                    )
                    AlertType.FINAL_APPROACH -> audioAlarmManager.playCriticalSiren(
                        stationName = destStation?.name ?: destinationStation,
                        trainId = trainId,
                        platform = platform
                    )
                    AlertType.TRAIN_APPROACHING -> audioAlarmManager.playWarningApproachAlarm(
                        stationName = destStation?.name ?: destinationStation,
                        trainId = trainId,
                        platform = platform
                    )
                }
            }
        }
    }

    // Audio & Siren Sound Testing
    fun testSiren() {
        audioAlarmManager?.playSirenAlarm()
    }

    fun testWarningApproachAlarm() {
        audioAlarmManager?.playWarningApproachAlarm()
    }

    fun testCriticalSiren() {
        audioAlarmManager?.playCriticalSiren()
    }

    fun testDestinationAlarm() {
        audioAlarmManager?.playDestinationAlarm()
    }

    fun testAcknowledgmentChime() {
        audioAlarmManager?.playAcknowledgmentChime()
    }

    fun stopAlarm() {
        audioAlarmManager?.stopAlarm()
    }

    // Offline Synchronization
    suspend fun syncPendingRequests(): Int = withContext(Dispatchers.IO) {
        val unsynced = requestDao.getUnsyncedRequests()
        for (req in unsynced) {
            // Simulate sending to remote backend
            requestDao.markSynced(req.requestId)
        }
        if (unsynced.isNotEmpty()) {
            recordAudit(
                user = "SYSTEM_SYNC",
                action = "SYNC_COMPLETED",
                details = "Synchronized ${unsynced.size} offline assistance requests to central backend."
            )
        }
        unsynced.size
    }
}
