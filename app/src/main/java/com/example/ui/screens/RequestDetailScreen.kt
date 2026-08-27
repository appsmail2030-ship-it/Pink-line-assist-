package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.data.model.RequestStatusHistory
import com.example.data.model.Station
import com.example.ui.components.AssistanceTypeBadge
import com.example.ui.components.DemoModeBanner
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.PinkPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RequestDetailScreen(
    request: AssistanceRequest,
    history: List<RequestStatusHistory>,
    stations: List<Station>,
    onBack: () -> Unit,
    onStatusChange: (String, RequestStatus) -> Unit,
    onComplete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val fullDateFormatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault()) }

    val stationNameMap = remember(stations) {
        stations.associate { it.id to it.name }
    }

    val sourceName = stationNameMap[request.sourceStationId] ?: request.sourceStationId
    val destName = stationNameMap[request.destinationStationId] ?: request.destinationStationId

    Surface(
        modifier = modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DemoModeBanner()

            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "ASSISTANCE DETAILS: ${request.requestId}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Train ${request.trainId} • $sourceName to $destName",
                        fontSize = 11.sp,
                        color = CyanNeon
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Status & Primary Info Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(14.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(PinkAccent, DarkSurfaceBorder))
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = request.requestId,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = PinkAccent
                                )
                                StatusBadge(status = request.status)
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("ORIGIN", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text(sourceName, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                }
                                Icon(Icons.Default.DirectionsTransit, contentDescription = "Arrow", tint = CyanNeon, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                    Text("DESTINATION", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text(destName, fontSize = 14.sp, color = CyanNeon, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AssistanceTypeBadge(type = request.assistanceType, wheelchairRequired = request.wheelchairRequired)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (request.circularDirection.contains("+")) PinkPrimary.copy(alpha = 0.2f) else CyanNeon.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = request.circularDirection,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (request.circularDirection.contains("+")) PinkAccent else CyanNeon
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Passenger Count: ${request.passengerCount} Passenger(s)", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text("Created At: ${fullDateFormatter.format(Date(request.createdAt))}", fontSize = 11.sp, color = Color.Gray)
                            Text("Estimated Arrival: ${fullDateFormatter.format(Date(request.estimatedArrival))}", fontSize = 11.sp, color = CyanNeon)
                        }
                    }
                }

                // Action Bar if active
                if (request.status != RequestStatus.ASSISTANCE_COMPLETED.label) {
                    item {
                        Button(
                            onClick = { onComplete(request.requestId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("detail_complete_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Complete", tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("MARK ASSISTANCE COMPLETED", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Section: Status Transition History Timeline
                item {
                    Text(
                        text = "STATUS AUDIT & TRANSITION TIMELINE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon,
                        letterSpacing = 1.sp
                    )
                }

                if (history.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = DarkSurface)) {
                            Text("No transition logs available.", color = Color.Gray, modifier = Modifier.padding(14.dp), fontSize = 12.sp)
                        }
                    }
                } else {
                    items(history) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(PinkAccent)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(item.newStatus, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                        Text("By ${item.user} @ ${item.locationStation}", fontSize = 11.sp, color = Color.Gray)
                                        if (item.note.isNotEmpty()) {
                                            Text(item.note, fontSize = 10.sp, color = CyanNeon)
                                        }
                                    }
                                }

                                Text(
                                    text = timeFormatter.format(Date(item.timestamp)),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.LightGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
