package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessible
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.Alert
import com.example.data.model.AssistanceRequest
import com.example.data.model.RequestStatus
import com.example.data.model.Station
import com.example.data.model.Train
import com.example.data.model.User
import com.example.ui.components.AssistanceTypeBadge
import com.example.ui.components.DemoModeBanner
import com.example.ui.components.NetworkStatusBar
import com.example.ui.components.PriorityAlertOverlayDialog
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
import com.example.ui.theme.RedCritical
import com.example.ui.theme.VisuallyImpairedPurple
import com.example.ui.theme.WheelchairBlue
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ControlRoomDashboardScreen(
    user: User,
    requests: List<AssistanceRequest>,
    stations: List<Station>,
    trains: List<Train>,
    alerts: List<Alert>,
    completedTodayCount: Int,
    isOnline: Boolean,
    onToggleNetwork: () -> Unit,
    onSyncNow: () -> Unit,
    onAcknowledgeAlert: (String) -> Unit,
    onStatusChange: (String, RequestStatus) -> Unit,
    onCompleteRequest: (String) -> Unit,
    onViewDetails: (AssistanceRequest) -> Unit,
    modifier: Modifier = Modifier
) {
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

    // Modal state for Call Station Simulation
    var callingStationName by remember { mutableStateOf<String?>(null) }
    var selectedRequestForStatusUpdate by remember { mutableStateOf<AssistanceRequest?>(null) }

    // Active unacknowledged alert for priority alert modal
    val activeAlert = alerts.firstOrNull { it.status == "ACTIVE" }

    val activeCount = requests.count { it.status != RequestStatus.ASSISTANCE_COMPLETED.label && it.status != RequestStatus.CANCELLED.label }
    val approachingCount = requests.count {
        it.status == RequestStatus.TRAIN_APPROACHING.label ||
                it.status == RequestStatus.TWO_STATIONS_AWAY.label ||
                it.status == RequestStatus.ONE_STATION_AWAY.label
    }
    val wheelchairCount = requests.count { it.wheelchairRequired || it.assistanceType.contains("Wheelchair", ignoreCase = true) }
    val visuallyImpairedCount = requests.count { it.assistanceType.contains("Visual", ignoreCase = true) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Priority Alert Overlay when triggered by alert engine
            if (activeAlert != null) {
                PriorityAlertOverlayDialog(
                    alert = activeAlert,
                    onAcknowledge = onAcknowledgeAlert,
                    onViewRequest = { reqId ->
                        val r = requests.firstOrNull { it.requestId == reqId }
                        if (r != null) onViewDetails(r)
                    },
                    onCallStation = { stationName ->
                        callingStationName = stationName
                    }
                )
            }

            // Call Station Simulation Dialog
            if (callingStationName != null) {
                AlertDialog(
                    onDismissRequest = { callingStationName = null },
                    containerColor = DarkSurfaceElevated,
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = CyanNeon)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("DMRC HOTLINE • DIRECT INTERCOM", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    text = {
                        Column {
                            Text("Calling Station Controller & Assistant at:", fontSize = 12.sp, color = Color.LightGray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(callingStationName!!, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = CyanNeon)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Line 7 Dedicated Radio Channel: ACTIVE", fontSize = 11.sp, color = GreenSuccess)
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { callingStationName = null },
                            colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
                        ) {
                            Text("END CALL")
                        }
                    }
                )
            }

            // Update Status Dialog
            if (selectedRequestForStatusUpdate != null) {
                val req = selectedRequestForStatusUpdate!!
                AlertDialog(
                    onDismissRequest = { selectedRequestForStatusUpdate = null },
                    containerColor = DarkSurfaceElevated,
                    title = {
                        Text("UPDATE STATUS: ${req.requestId}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            RequestStatus.entries.forEach { st ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (req.status == st.label) PinkPrimary.copy(alpha = 0.3f) else DarkSurface)
                                        .border(1.dp, if (req.status == st.label) PinkAccent else DarkSurfaceBorder, RoundedCornerShape(8.dp))
                                        .clickable {
                                            onStatusChange(req.requestId, st)
                                            selectedRequestForStatusUpdate = null
                                        }
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = st.label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (req.status == st.label) PinkAccent else Color.White
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { selectedRequestForStatusUpdate = null }) {
                            Text("CANCEL", color = Color.Gray)
                        }
                    }
                )
            }

            DemoModeBanner()

            NetworkStatusBar(
                isOnline = isOnline,
                onToggleNetwork = onToggleNetwork,
                onSyncNow = onSyncNow
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                // Header: PINK LINE ASSIST • CONTROL ROOM
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(14.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(
                                listOf(CyanNeon.copy(alpha = 0.6f), PinkAccent.copy(alpha = 0.4f))
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
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(CyanNeon.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = "Control Room",
                                            tint = CyanNeon,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "PINK LINE ASSIST",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PinkAccent,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = "CENTRAL CONTROL ROOM (OCC)",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
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

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Operator: ${user.name} (${user.employeeId})",
                                    fontSize = 12.sp,
                                    color = Color.LightGray
                                )
                                Text(
                                    text = "Monitored Trains: ${trains.size} • Stations: ${stations.size}",
                                    fontSize = 11.sp,
                                    color = PinkAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // 5 Statistics Badges
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatMetricCard(
                                title = "ACTIVE REQUESTS",
                                value = activeCount.toString(),
                                icon = Icons.Default.DirectionsTransit,
                                accentColor = PinkAccent,
                                modifier = Modifier.weight(1f)
                            )
                            StatMetricCard(
                                title = "APPROACHING",
                                value = approachingCount.toString(),
                                icon = Icons.Default.Alarm,
                                accentColor = AmberAlert,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatMetricCard(
                                title = "WHEELCHAIR",
                                value = wheelchairCount.toString(),
                                icon = Icons.Default.Accessible,
                                accentColor = WheelchairBlue,
                                modifier = Modifier.weight(1f)
                            )
                            StatMetricCard(
                                title = "VISUALLY IMPAIRED",
                                value = visuallyImpairedCount.toString(),
                                icon = Icons.Default.Visibility,
                                accentColor = VisuallyImpairedPurple,
                                modifier = Modifier.weight(1f)
                            )
                            StatMetricCard(
                                title = "COMPLETED",
                                value = completedTodayCount.toString(),
                                icon = Icons.Default.CheckCircle,
                                accentColor = GreenSuccess,
                                modifier = Modifier.weight(1f),
                                subtext = "Today"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Section Header: LIVE REQUEST TABLE
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE ASSISTANCE OPERATIONS MATRIX",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = CyanNeon,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${requests.size} Total Records",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (requests.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Clear", tint = GreenSuccess, modifier = Modifier.size(44.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No Active Assistance Requests", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Line 7 operations running normally.", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    items(requests, key = { it.requestId }) { req ->
                        ControlRoomRequestItem(
                            request = req,
                            sourceName = stationNameMap[req.sourceStationId] ?: req.sourceStationId,
                            destName = stationNameMap[req.destinationStationId] ?: req.destinationStationId,
                            onView = { onViewDetails(req) },
                            onAcknowledge = {
                                val match = alerts.firstOrNull { it.requestId == req.requestId && it.status == "ACTIVE" }
                                if (match != null) onAcknowledgeAlert(match.alertId)
                            },
                            onCallStation = { callingStationName = stationNameMap[req.destinationStationId] ?: req.destinationStationId },
                            onUpdateStatus = { selectedRequestForStatusUpdate = req },
                            onComplete = { onCompleteRequest(req.requestId) },
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
private fun ControlRoomRequestItem(
    request: AssistanceRequest,
    sourceName: String,
    destName: String,
    onView: () -> Unit,
    onAcknowledge: () -> Unit,
    onCallStation: () -> Unit,
    onUpdateStatus: () -> Unit,
    onComplete: () -> Unit,
    currentTimeMillis: Long,
    modifier: Modifier = Modifier
) {
    val etaRemainingSeconds = ((request.estimatedArrival - currentTimeMillis) / 1000).coerceAtLeast(0)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cr_request_${request.requestId}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(DarkSurfaceBorder, PinkAccent.copy(alpha = 0.3f))
            )
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Row 1: ID, Train ID, Status, ETA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = request.requestId,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = PinkAccent
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkSurfaceElevated)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Train ${request.trainId}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon
                        )
                    }
                }

                StatusBadge(status = request.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Route & Passengers
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("FROM: $sourceName", fontSize = 11.sp, color = Color.LightGray)
                    Text("TO: $destName", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (request.circularDirection.contains("+")) PinkPrimary.copy(alpha = 0.2f) else CyanNeon.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = request.circularDirection,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (request.circularDirection.contains("+")) PinkAccent else CyanNeon
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (etaRemainingSeconds > 0) "ETA ~${etaRemainingSeconds / 60}m" else "DUE / ARRIVED",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (etaRemainingSeconds > 0) AmberAlert else GreenSuccess
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 3: Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistanceTypeBadge(
                    type = request.assistanceType,
                    wheelchairRequired = request.wheelchairRequired
                )

                Text(
                    text = "By ${request.createdBy}",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 4: Action Buttons (VIEW, CALL STATION, UPDATE STATUS, COMPLETE)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onView,
                    modifier = Modifier.weight(1f).height(36.dp),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text("VIEW", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onCallStation,
                    modifier = Modifier.weight(1.2f).height(36.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanNeon),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("CALL", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onUpdateStatus,
                    modifier = Modifier.weight(1.2f).height(36.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberAlert),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Status", modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("STATUS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                if (request.status != RequestStatus.ASSISTANCE_COMPLETED.label) {
                    Button(
                        onClick = onComplete,
                        modifier = Modifier.weight(1.4f).height(36.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Complete", modifier = Modifier.size(12.dp), tint = Color.Black)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("COMPLETE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }
    }
}
