package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssistanceRequest
import com.example.data.model.RequestStatus
import com.example.data.model.Station
import com.example.data.model.User
import com.example.ui.components.AssistanceTypeBadge
import com.example.ui.components.DemoModeBanner
import com.example.ui.components.NetworkStatusBar
import com.example.ui.components.StatMetricCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.PinkNeon
import com.example.ui.theme.PinkPrimary
import com.example.ui.theme.SpecialAssistanceOrange
import com.example.ui.theme.WheelchairBlue
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StationAssistantDashboardScreen(
    user: User,
    activeRequests: List<AssistanceRequest>,
    stations: List<Station>,
    activeCount: Int,
    pendingCount: Int,
    completedTodayCount: Int,
    isOnline: Boolean,
    onToggleNetwork: () -> Unit,
    onSyncNow: () -> Unit,
    onNewRequestClick: () -> Unit,
    onStatusChange: (String, RequestStatus) -> Unit,
    onCompleteRequest: (String) -> Unit,
    onViewDetails: (AssistanceRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    // Live ticking time state
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            delay(1000)
        }
    }

    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val dateFormatter = remember { SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()) }

    val stationNameMap = remember(stations) {
        stations.associate { it.id to it.name }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Demo Banner
            DemoModeBanner()

            // Network Sync Status
            NetworkStatusBar(
                isOnline = isOnline,
                onToggleNetwork = onToggleNetwork,
                onSyncNow = onSyncNow
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                // Header Station & Clock
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(16.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(
                                listOf(PinkAccent.copy(alpha = 0.5f), CyanNeon.copy(alpha = 0.3f))
                            )
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(PinkPrimary.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = "Station",
                                            tint = PinkAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "ASSIGNED STATION",
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = user.stationName,
                                            fontSize = 16.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = timeFormatter.format(Date(currentTimeMillis)),
                                        fontSize = 18.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        color = CyanNeon
                                    )
                                    Text(
                                        text = dateFormatter.format(Date(currentTimeMillis)),
                                        fontSize = 11.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Operator",
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Assistant: ${user.name} (${user.employeeId})",
                                    fontSize = 12.sp,
                                    color = Color.LightGray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // 3 Metric Cards: Active Assistance, Pending Requests, Completed Today
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatMetricCard(
                            title = "Active",
                            value = activeCount.toString(),
                            icon = Icons.Default.DirectionsTransit,
                            accentColor = PinkAccent,
                            modifier = Modifier.weight(1f)
                        )
                        StatMetricCard(
                            title = "Pending",
                            value = pendingCount.toString(),
                            icon = Icons.Default.HourglassTop,
                            accentColor = AmberAlert,
                            modifier = Modifier.weight(1f)
                        )
                        StatMetricCard(
                            title = "Completed",
                            value = completedTodayCount.toString(),
                            icon = Icons.Default.TaskAlt,
                            accentColor = GreenSuccess,
                            modifier = Modifier.weight(1f),
                            subtext = "Today"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Large Primary Button: NEW ASSISTANCE REQUEST
                item {
                    Button(
                        onClick = onNewRequestClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("new_assistance_request_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PinkPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NEW ASSISTANCE REQUEST",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Section Title: Active Operations
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTIVE STATION ASSISTANCE (${activeRequests.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (activeRequests.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "No active requests",
                                    tint = GreenSuccess,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "All Assistance Clear",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "No pending or active requests at this station. Click above to register a passenger.",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(activeRequests, key = { it.requestId }) { req ->
                        StationRequestCard(
                            request = req,
                            sourceStationName = stationNameMap[req.sourceStationId] ?: req.sourceStationId,
                            destStationName = stationNameMap[req.destinationStationId] ?: req.destinationStationId,
                            onStatusChange = onStatusChange,
                            onComplete = onCompleteRequest,
                            onViewDetails = onViewDetails,
                            currentTimeMillis = currentTimeMillis
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StationRequestCard(
    request: AssistanceRequest,
    sourceStationName: String,
    destStationName: String,
    onStatusChange: (String, RequestStatus) -> Unit,
    onComplete: (String) -> Unit,
    onViewDetails: (AssistanceRequest) -> Unit,
    currentTimeMillis: Long,
    modifier: Modifier = Modifier
) {
    val etaRemainingSeconds = ((request.estimatedArrival - currentTimeMillis) / 1000).coerceAtLeast(0)
    val etaMinutes = (etaRemainingSeconds / 60)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onViewDetails(request) }
            .testTag("request_card_${request.requestId}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(DarkSurfaceBorder, PinkAccent.copy(alpha = 0.3f))
            )
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top row: ID + Status + Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = request.requestId,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = PinkAccent
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• Train ${request.trainId}",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                StatusBadge(status = request.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Route visualizer: Source -> Dest
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("FROM", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(sourceStationName, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
                Icon(
                    imageVector = Icons.Default.DirectionsTransit,
                    contentDescription = "Arrow",
                    tint = CyanNeon,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("TO (DESTINATION)", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(destStationName, fontSize = 13.sp, color = CyanNeon, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Passenger tags & ETA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AssistanceTypeBadge(
                        type = request.assistanceType,
                        wheelchairRequired = request.wheelchairRequired
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (request.circularDirection.contains("+")) PinkPrimary.copy(alpha = 0.2f) else CyanNeon.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = request.circularDirection,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (request.circularDirection.contains("+")) PinkAccent else CyanNeon
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "ETA",
                        tint = AmberAlert,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (etaRemainingSeconds > 0) "ETA ~${etaMinutes}m (${etaRemainingSeconds}s)" else "ARRIVED / DUE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (etaRemainingSeconds > 0) AmberAlert else GreenSuccess
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Status Transition Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (request.status) {
                    RequestStatus.REQUESTED.label -> {
                        Button(
                            onClick = { onStatusChange(request.requestId, RequestStatus.ACCEPTED) },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("accept_request_${request.requestId}"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("ACCEPT REQUEST", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    RequestStatus.ACCEPTED.label, RequestStatus.TRAIN_APPROACHING.label -> {
                        Button(
                            onClick = { onStatusChange(request.requestId, RequestStatus.BOARDING) },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("board_request_${request.requestId}"),
                            colors = ButtonDefaults.buttonColors(containerColor = SpecialAssistanceOrange),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("BOARD PASSENGER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    RequestStatus.BOARDING.label -> {
                        Button(
                            onClick = { onStatusChange(request.requestId, RequestStatus.EN_ROUTE) },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WheelchairBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("DEPART / EN ROUTE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    RequestStatus.EN_ROUTE.label, RequestStatus.TWO_STATIONS_AWAY.label, RequestStatus.ONE_STATION_AWAY.label -> {
                        OutlinedButton(
                            onClick = { onStatusChange(request.requestId, RequestStatus.ARRIVED) },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("MARK ARRIVED", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    RequestStatus.ARRIVED.label -> {
                        Button(
                            onClick = { onComplete(request.requestId) },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("complete_request_${request.requestId}"),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("COMPLETE ASSISTANCE", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    else -> {
                        OutlinedButton(
                            onClick = { onComplete(request.requestId) },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("CLOSE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
