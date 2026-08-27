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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.Station
import com.example.data.model.Train
import com.example.ui.components.DemoModeBanner
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
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainSimulatorScreen(
    trains: List<Train>,
    stations: List<Station>,
    activeRequests: List<AssistanceRequest>,
    alerts: List<Alert>,
    onMoveTrainToNextStation: (trainId: String, nextStationId: String) -> Unit,
    onSetTrainStation: (trainId: String, stationId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTrainId by remember { mutableStateOf(trains.firstOrNull()?.trainId ?: "T-245") }
    val currentTrain = trains.firstOrNull { it.trainId == selectedTrainId } ?: trains.firstOrNull()

    val currentStation = stations.firstOrNull { it.id == currentTrain?.currentStationId }
        ?: stations.firstOrNull { it.id == "ST_28" } // Anand Vihar default
        ?: stations.firstOrNull()

    var trainDropdownExpanded by remember { mutableStateOf(false) }
    var stationDropdownExpanded by remember { mutableStateOf(false) }
    var directionTowardsMajlisPark by remember { mutableStateOf(true) }

    // Find next station according to sequence and direction
    val currentSeq = currentStation?.sequenceNumber ?: 28
    val nextStation = remember(stations, currentSeq, directionTowardsMajlisPark) {
        val activeStations = stations.filter { it.isActive }.sortedBy { it.sequenceNumber }
        if (directionTowardsMajlisPark) {
            // Decrement sequence towards Majlis Park (Seq 1)
            activeStations.lastOrNull { it.sequenceNumber < currentSeq } ?: activeStations.lastOrNull()
        } else {
            // Increment sequence towards Shiv Vihar (Seq 38)
            activeStations.firstOrNull { it.sequenceNumber > currentSeq } ?: activeStations.firstOrNull()
        }
    }

    // Active requests attached to this train
    val trainRequests = activeRequests.filter { it.trainId.equals(selectedTrainId, ignoreCase = true) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DemoModeBanner()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(14.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(
                                listOf(PinkAccent.copy(alpha = 0.5f), CyanNeon.copy(alpha = 0.4f))
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
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(PinkPrimary.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FastForward,
                                            contentDescription = "Simulator",
                                            tint = PinkAccent,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "TRAIN SIMULATION ENGINE",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = "Live Movement & Real-time Alert Trigger",
                                            fontSize = 11.sp,
                                            color = CyanNeon
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Train Selector & Direction Controls
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(DarkSurfaceBorder, DarkSurfaceBorder))
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("SELECT SIMULATION TRAIN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Spacer(modifier = Modifier.height(6.dp))

                            ExposedDropdownMenuBox(
                                expanded = trainDropdownExpanded,
                                onExpandedChange = { trainDropdownExpanded = !trainDropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = "Train $selectedTrainId (${currentStation?.name ?: "Unknown"})",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = trainDropdownExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                        .testTag("simulator_train_selector"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PinkAccent,
                                        unfocusedBorderColor = DarkSurfaceBorder
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                ExposedDropdownMenu(
                                    expanded = trainDropdownExpanded,
                                    onDismissRequest = { trainDropdownExpanded = false },
                                    modifier = Modifier.background(DarkSurfaceElevated)
                                ) {
                                    trains.forEach { tr ->
                                        val st = stations.firstOrNull { it.id == tr.currentStationId }
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("Train ${tr.trainId}", color = Color.White, fontWeight = FontWeight.Bold)
                                                    Text(st?.name ?: tr.currentStationId, color = CyanNeon, fontSize = 12.sp)
                                                }
                                            },
                                            onClick = {
                                                selectedTrainId = tr.trainId
                                                trainDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Direction Switcher
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("CIRCULAR PLATFORM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                OutlinedButton(
                                    onClick = { directionTowardsMajlisPark = !directionTowardsMajlisPark },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.SwapVert, contentDescription = "Switch", tint = CyanNeon, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (directionTowardsMajlisPark) "Platform 2 (- Circular Line)" else "Platform 1 (+ Circular Line)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyanNeon
                                    )
                                }
                            }
                        }
                    }
                }

                // Current Location & NEXT STATION Movement Action Box
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161E2E)),
                        shape = RoundedCornerShape(14.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(PinkAccent, CyanNeon))
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("CURRENT TRAIN POSITION", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = currentStation?.name ?: "Unknown Station",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Sequence #${currentStation?.sequenceNumber ?: 0}",
                                        fontSize = 11.sp,
                                        color = PinkAccent
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Next",
                                    tint = CyanNeon,
                                    modifier = Modifier.size(24.dp)
                                )

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("NEXT UPCOMING STATION", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = nextStation?.name ?: "Terminus Reached",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyanNeon
                                    )
                                    Text(
                                        text = "Sequence #${nextStation?.sequenceNumber ?: 0}",
                                        fontSize = 11.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Large Action Button: NEXT STATION
                            Button(
                                onClick = {
                                    if (nextStation != null) {
                                        onMoveTrainToNextStation(selectedTrainId, nextStation.id)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("simulator_next_station_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Next Station", modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "MOVE TRAIN TO NEXT STATION: ${nextStation?.name?.uppercase() ?: "NEXT"}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }

                // Stepping Corridor Track Visualizer
                item {
                    Text(
                        text = "CORRIDOR SIMULATION SEQUENCE (DMRC LINE 7)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    val exampleCorridor = listOf(
                        "ST_28" to "Anand Vihar ISBT",
                        "ST_27" to "IP Extension",
                        "ST_26" to "Mandawali - West Vinod Nagar",
                        "ST_25" to "East Vinod Nagar - Mayur Vihar II",
                        "ST_22" to "Mayur Vihar - I"
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            exampleCorridor.forEachIndexed { index, (stId, stName) ->
                                val isTrainHere = currentStation?.id == stId
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isTrainHere) PinkPrimary.copy(alpha = 0.25f) else Color.Transparent)
                                        .clickable { onSetTrainStation(selectedTrainId, stId) }
                                        .padding(vertical = 8.dp, horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(if (isTrainHere) PinkNeon else Color.Gray)
                                                .border(2.dp, if (isTrainHere) Color.White else Color.Transparent, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = stName,
                                            fontSize = 13.sp,
                                            fontWeight = if (isTrainHere) FontWeight.Black else FontWeight.Normal,
                                            color = if (isTrainHere) PinkAccent else Color.White
                                        )
                                    }

                                    if (isTrainHere) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(PinkPrimary)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("TRAIN $selectedTrainId HERE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    } else {
                                        Text("Jump Here", fontSize = 10.sp, color = CyanNeon)
                                    }
                                }

                                if (index < exampleCorridor.size - 1) {
                                    Row(modifier = Modifier.padding(start = 14.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height(14.dp)
                                                .background(DarkSurfaceBorder)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Active Requests & Alert Engine Monitoring for this Train
                item {
                    Text(
                        text = "ACTIVE PASSENGER ASSISTANCE ON TRAIN $selectedTrainId (${trainRequests.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    if (trainRequests.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "No active assistance requests booked for Train $selectedTrainId. Create a request on the Station Assistant screen to test auto-alerts.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            trainRequests.forEach { req ->
                                val destSt = stations.firstOrNull { it.id == req.destinationStationId }
                                val remaining = if (currentStation != null && destSt != null) {
                                    abs(destSt.sequenceNumber - currentStation.sequenceNumber)
                                } else 0

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(req.requestId, fontWeight = FontWeight.Bold, color = PinkAccent, fontSize = 13.sp)
                                            Text("Dest: ${destSt?.name ?: req.destinationStationId}", color = Color.White, fontSize = 12.sp)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Distance: $remaining stations remaining", color = if (remaining <= 3) AmberAlert else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text("Status: ${req.status}", color = CyanNeon, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
