package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Alert
import com.example.data.model.AssistanceRequest
import com.example.data.model.AuditLog
import com.example.data.model.RequestStatus
import com.example.data.model.RequestStatusHistory
import com.example.data.model.Station
import com.example.data.model.Train
import com.example.data.model.TravelTimeConfig
import com.example.data.model.User
import com.example.data.repository.PinkLineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PinkLineViewModel(
    private val repository: PinkLineRepository
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    val isOnline: StateFlow<Boolean> = repository.isOnline

    val stations: StateFlow<List<Station>> = repository.getAllStations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Station.DEFAULT_PINK_LINE_STATIONS)

    val trains: StateFlow<List<Train>> = repository.getAllTrains()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Train.DEFAULT_TRAINS)

    val activeRequests: StateFlow<List<AssistanceRequest>> = repository.getActiveRequests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historyRequests: StateFlow<List<AssistanceRequest>> = repository.getHistoryRequests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRequests: StateFlow<List<AssistanceRequest>> = repository.getAllRequests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCount: StateFlow<Int> = repository.getActiveCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingCount: StateFlow<Int> = repository.getPendingCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val completedTodayCount: StateFlow<Int> = repository.getCompletedTodayCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val alerts: StateFlow<List<Alert>> = repository.getAllAlerts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val config: StateFlow<TravelTimeConfig> = repository.getConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TravelTimeConfig())

    val auditLogs: StateFlow<List<AuditLog>> = repository.getAuditLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun login(user: User) {
        _currentUser.value = user
        viewModelScope.launch {
            repository.recordAudit(
                user = "${user.name} (${user.employeeId})",
                action = "USER_LOGIN",
                details = "Logged in with role ${user.role.name} at station ${user.stationName}"
            )
        }
    }

    fun logout() {
        val user = _currentUser.value
        _currentUser.value = null
        if (user != null) {
            viewModelScope.launch {
                repository.recordAudit(
                    user = "${user.name} (${user.employeeId})",
                    action = "USER_LOGOUT",
                    details = "Logged out of system"
                )
            }
        }
    }

    fun toggleOnline() {
        val current = repository.isOnline.value
        repository.setOnlineStatus(!current)
    }

    fun syncNow() {
        viewModelScope.launch {
            repository.syncPendingRequests()
        }
    }

    fun generateRequestId(): String = repository.generateRequestId()

    fun createRequest(
        sourceStationId: String,
        destStationId: String,
        trainId: String,
        passengerCount: Int,
        type: String,
        wheelchair: Boolean,
        platform: String,
        remarks: String,
        onSuccess: () -> Unit
    ) {
        val user = _currentUser.value?.name ?: "OPERATOR"
        viewModelScope.launch {
            repository.createRequest(
                sourceStationId = sourceStationId,
                destinationStationId = destStationId,
                trainId = trainId,
                passengerCount = passengerCount,
                assistanceType = type,
                wheelchairRequired = wheelchair,
                circularDirection = platform,
                createdBy = user,
                remarks = remarks
            )
            onSuccess()
        }
    }

    fun updateStatus(requestId: String, newStatus: RequestStatus) {
        val user = _currentUser.value?.name ?: "OPERATOR"
        val station = _currentUser.value?.stationName ?: "CONTROL ROOM"
        viewModelScope.launch {
            repository.updateRequestStatus(requestId, newStatus, user, station)
        }
    }

    fun completeRequest(requestId: String) {
        val user = _currentUser.value?.name ?: "OPERATOR"
        val station = _currentUser.value?.stationName ?: "DESTINATION"
        viewModelScope.launch {
            repository.completeRequest(requestId, user, station)
        }
    }

    fun acknowledgeAlert(alertId: String) {
        val user = _currentUser.value?.name ?: "OPERATOR"
        viewModelScope.launch {
            repository.acknowledgeAlert(alertId, user)
        }
    }

    fun moveTrainToNextStation(trainId: String, nextStationId: String) {
        val user = _currentUser.value?.name ?: "SIMULATOR"
        viewModelScope.launch {
            repository.updateTrainLocation(trainId, nextStationId, user)
        }
    }

    fun setTrainStation(trainId: String, stationId: String) {
        val user = _currentUser.value?.name ?: "SIMULATOR"
        viewModelScope.launch {
            repository.updateTrainLocation(trainId, stationId, user)
        }
    }

    fun updateConfig(newConfig: TravelTimeConfig) {
        val user = _currentUser.value?.name ?: "ADMIN"
        viewModelScope.launch {
            repository.updateConfig(newConfig, user)
        }
    }

    fun updateStation(station: Station) {
        val user = _currentUser.value?.name ?: "ADMIN"
        viewModelScope.launch {
            repository.updateStation(station, user)
        }
    }

    fun testSiren() = repository.testSiren()
    fun testWarningApproachAlarm() = repository.testWarningApproachAlarm()
    fun testCriticalSiren() = repository.testCriticalSiren()
    fun testDestinationAlarm() = repository.testDestinationAlarm()
    fun testAcknowledgmentChime() = repository.testAcknowledgmentChime()
    fun stopAlarm() = repository.stopAlarm()

    fun getHistoryForRequest(requestId: String) = repository.getHistoryForRequest(requestId)

    class Factory(private val repository: PinkLineRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PinkLineViewModel(repository) as T
        }
    }
}
