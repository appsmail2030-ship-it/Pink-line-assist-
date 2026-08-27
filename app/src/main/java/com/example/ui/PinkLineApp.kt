package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssistanceRequest
import com.example.data.model.UserRole
import com.example.ui.screens.AdminConfigScreen
import com.example.ui.screens.AuditLogScreen
import com.example.ui.screens.ControlRoomDashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.RequestCreationScreen
import com.example.ui.screens.RequestDetailScreen
import com.example.ui.screens.StationAssistantDashboardScreen
import com.example.ui.screens.TrainSimulatorScreen
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.PinkPrimary
import com.example.ui.viewmodel.PinkLineViewModel

enum class ScreenTab {
    DASHBOARD,
    SIMULATOR,
    REPORTS,
    ADMIN_CONFIG,
    AUDIT_LOGS
}

enum class NavigationSubScreen {
    NONE,
    CREATE_REQUEST,
    REQUEST_DETAIL,
    AUDIT_LOG_FULL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinkLineApp(
    viewModel: PinkLineViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val activeRequests by viewModel.activeRequests.collectAsState()
    val allRequests by viewModel.allRequests.collectAsState()
    val stations by viewModel.stations.collectAsState()
    val trains by viewModel.trains.collectAsState()
    val alerts by viewModel.alerts.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val config by viewModel.config.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val activeCount by viewModel.activeCount.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()
    val completedTodayCount by viewModel.completedTodayCount.collectAsState()

    var currentTab by remember { mutableStateOf(ScreenTab.DASHBOARD) }
    var currentSubScreen by remember { mutableStateOf(NavigationSubScreen.NONE) }
    var selectedDetailRequest by remember { mutableStateOf<AssistanceRequest?>(null) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    if (currentUser == null) {
        LoginScreen(
            onLoginSuccess = { user ->
                viewModel.login(user)
                currentTab = ScreenTab.DASHBOARD
                currentSubScreen = NavigationSubScreen.NONE
            }
        )
        return
    }

    val user = currentUser!!

    // Logout Confirmation Dialog
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            containerColor = DarkSurfaceElevated,
            title = {
                Text("CONFIRM LOGOUT", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you sure you want to log out of Pink Line Assist?", color = Color.LightGray)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirm = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
                ) {
                    Text("LOGOUT")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("CANCEL", color = Color.Gray)
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            if (currentSubScreen == NavigationSubScreen.NONE) {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(PinkAccent)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PINK LINE ASSIST",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp,
                                color = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showLogoutConfirm = true },
                            modifier = Modifier.testTag("logout_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Logout",
                                tint = Color.LightGray
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = DarkSurface
                    )
                )
            }
        },
        bottomBar = {
            if (currentSubScreen == NavigationSubScreen.NONE) {
                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = Color.White
                ) {
                    val isAssistant = user.role == UserRole.STATION_ASSISTANT
                    val isControlRoom = user.role == UserRole.CONTROL_ROOM
                    val isAdmin = user.role == UserRole.ADMIN

                    // Dashboard Tab
                    NavigationBarItem(
                        selected = currentTab == ScreenTab.DASHBOARD,
                        onClick = { currentTab = ScreenTab.DASHBOARD },
                        icon = {
                            Icon(
                                imageVector = if (isAssistant) Icons.Default.SupportAgent else Icons.Default.Security,
                                contentDescription = "Operations"
                            )
                        },
                        label = { Text(if (isAssistant) "Assistant" else "Operations", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PinkAccent,
                            selectedTextColor = PinkAccent,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = PinkPrimary.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier.testTag("nav_tab_dashboard")
                    )

                    // Simulator Tab
                    NavigationBarItem(
                        selected = currentTab == ScreenTab.SIMULATOR,
                        onClick = { currentTab = ScreenTab.SIMULATOR },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.FastForward,
                                contentDescription = "Simulator"
                            )
                        },
                        label = { Text("Simulator", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PinkAccent,
                            selectedTextColor = PinkAccent,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = PinkPrimary.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier.testTag("nav_tab_simulator")
                    )

                    // Reports Tab
                    NavigationBarItem(
                        selected = currentTab == ScreenTab.REPORTS,
                        onClick = { currentTab = ScreenTab.REPORTS },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = "Reports"
                            )
                        },
                        label = { Text("Reports", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PinkAccent,
                            selectedTextColor = PinkAccent,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = PinkPrimary.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier.testTag("nav_tab_reports")
                    )

                    // Admin Config Tab (For Admin or Control Room)
                    if (isAdmin || isControlRoom) {
                        NavigationBarItem(
                            selected = currentTab == ScreenTab.ADMIN_CONFIG,
                            onClick = { currentTab = ScreenTab.ADMIN_CONFIG },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Config"
                                )
                            },
                            label = { Text("Config", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PinkAccent,
                                selectedTextColor = PinkAccent,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = PinkPrimary.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier.testTag("nav_tab_admin_config")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentSubScreen) {
                NavigationSubScreen.CREATE_REQUEST -> {
                    RequestCreationScreen(
                        user = user,
                        stations = stations,
                        trains = trains,
                        config = config,
                        generatedRequestId = viewModel.generateRequestId(),
                        onBack = { currentSubScreen = NavigationSubScreen.NONE },
                        onSubmitRequest = { src, dst, trainId, pax, type, wheelchair, plat, remarks ->
                            viewModel.createRequest(src, dst, trainId, pax, type, wheelchair, plat, remarks) {
                                currentSubScreen = NavigationSubScreen.NONE
                            }
                        }
                    )
                }

                NavigationSubScreen.REQUEST_DETAIL -> {
                    if (selectedDetailRequest != null) {
                        val history by viewModel.getHistoryForRequest(selectedDetailRequest!!.requestId)
                            .collectAsState(initial = emptyList())
                        RequestDetailScreen(
                            request = selectedDetailRequest!!,
                            history = history,
                            stations = stations,
                            onBack = { currentSubScreen = NavigationSubScreen.NONE },
                            onStatusChange = { reqId, status -> viewModel.updateStatus(reqId, status) },
                            onComplete = { reqId ->
                                viewModel.completeRequest(reqId)
                                currentSubScreen = NavigationSubScreen.NONE
                            }
                        )
                    }
                }

                NavigationSubScreen.AUDIT_LOG_FULL -> {
                    AuditLogScreen(
                        auditLogs = auditLogs,
                        onBack = { currentSubScreen = NavigationSubScreen.NONE }
                    )
                }

                NavigationSubScreen.NONE -> {
                    when (currentTab) {
                        ScreenTab.DASHBOARD -> {
                            if (user.role == UserRole.STATION_ASSISTANT) {
                                StationAssistantDashboardScreen(
                                    user = user,
                                    activeRequests = activeRequests,
                                    stations = stations,
                                    activeCount = activeCount,
                                    pendingCount = pendingCount,
                                    completedTodayCount = completedTodayCount,
                                    isOnline = isOnline,
                                    onToggleNetwork = { viewModel.toggleOnline() },
                                    onSyncNow = { viewModel.syncNow() },
                                    onNewRequestClick = { currentSubScreen = NavigationSubScreen.CREATE_REQUEST },
                                    onStatusChange = { reqId, status -> viewModel.updateStatus(reqId, status) },
                                    onCompleteRequest = { reqId -> viewModel.completeRequest(reqId) },
                                    onViewDetails = { req ->
                                        selectedDetailRequest = req
                                        currentSubScreen = NavigationSubScreen.REQUEST_DETAIL
                                    }
                                )
                            } else {
                                ControlRoomDashboardScreen(
                                    user = user,
                                    requests = allRequests,
                                    stations = stations,
                                    trains = trains,
                                    alerts = alerts,
                                    completedTodayCount = completedTodayCount,
                                    isOnline = isOnline,
                                    onToggleNetwork = { viewModel.toggleOnline() },
                                    onSyncNow = { viewModel.syncNow() },
                                    onAcknowledgeAlert = { alertId -> viewModel.acknowledgeAlert(alertId) },
                                    onStatusChange = { reqId, status -> viewModel.updateStatus(reqId, status) },
                                    onCompleteRequest = { reqId -> viewModel.completeRequest(reqId) },
                                    onViewDetails = { req ->
                                        selectedDetailRequest = req
                                        currentSubScreen = NavigationSubScreen.REQUEST_DETAIL
                                    }
                                )
                            }
                        }

                        ScreenTab.SIMULATOR -> {
                            TrainSimulatorScreen(
                                trains = trains,
                                stations = stations,
                                activeRequests = activeRequests,
                                alerts = alerts,
                                onMoveTrainToNextStation = { trainId, nextStationId ->
                                    viewModel.moveTrainToNextStation(trainId, nextStationId)
                                },
                                onSetTrainStation = { trainId, stationId ->
                                    viewModel.setTrainStation(trainId, stationId)
                                }
                            )
                        }

                        ScreenTab.REPORTS -> {
                            ReportsScreen(
                                allRequests = allRequests,
                                stations = stations
                            )
                        }

                        ScreenTab.ADMIN_CONFIG -> {
                            AdminConfigScreen(
                                user = user,
                                config = config,
                                stations = stations,
                                onSaveConfig = { newConfig -> viewModel.updateConfig(newConfig) },
                                onUpdateStation = { st -> viewModel.updateStation(st) },
                                onViewAuditLogs = { currentSubScreen = NavigationSubScreen.AUDIT_LOG_FULL },
                                onTestSiren = { viewModel.testSiren() },
                                onTestWarningApproach = { viewModel.testWarningApproachAlarm() },
                                onTestCriticalSiren = { viewModel.testCriticalSiren() },
                                onTestDestinationAlarm = { viewModel.testDestinationAlarm() },
                                onTestAcknowledgment = { viewModel.testAcknowledgmentChime() },
                                onStopAudio = { viewModel.stopAlarm() }
                            )
                        }

                        ScreenTab.AUDIT_LOGS -> {
                            AuditLogScreen(
                                auditLogs = auditLogs,
                                onBack = { currentTab = ScreenTab.DASHBOARD }
                            )
                        }
                    }
                }
            }
        }
    }
}
