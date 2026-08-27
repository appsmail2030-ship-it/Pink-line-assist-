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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.AssistanceRequest
import com.example.data.model.Station
import com.example.ui.components.DemoModeBanner
import com.example.ui.components.StatMetricCard
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
import com.example.ui.theme.SpecialAssistanceOrange
import com.example.ui.theme.VisuallyImpairedPurple
import com.example.ui.theme.WheelchairBlue

@Composable
fun ReportsScreen(
    allRequests: List<AssistanceRequest>,
    stations: List<Station>,
    modifier: Modifier = Modifier
) {
    val totalCount = allRequests.size
    val wheelchairCount = allRequests.count { it.wheelchairRequired || it.assistanceType.contains("Wheelchair", ignoreCase = true) }
    val visualCount = allRequests.count { it.assistanceType.contains("Visual", ignoreCase = true) }
    val otherCount = allRequests.count { it.assistanceType.contains("Other", ignoreCase = true) }
    val completedCount = allRequests.count { it.status == "ASSISTANCE COMPLETED" }
    val cancelledCount = allRequests.count { it.status == "CANCELLED" }

    val stationNameMap = remember(stations) {
        stations.associate { it.id to it.name }
    }

    val sourceStationBreakdown = remember(allRequests) {
        allRequests.groupBy { it.sourceStationId }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
    }

    val trainBreakdown = remember(allRequests) {
        allRequests.groupBy { it.trainId }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
    }

    var csvExportData by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DemoModeBanner()

            if (csvExportData != null) {
                AlertDialog(
                    onDismissRequest = { csvExportData = null },
                    containerColor = DarkSurfaceElevated,
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FileDownload, contentDescription = "CSV", tint = CyanNeon)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("EXPORTED CSV DATA", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    },
                    text = {
                        Column {
                            Text("Assistance Operational Log CSV:", fontSize = 12.sp, color = Color.LightGray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = csvExportData!!,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = GreenSuccess
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { csvExportData = null },
                            colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
                        ) {
                            Text("DONE")
                        }
                    }
                )
            }

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
                                            imageVector = Icons.Default.Assessment,
                                            contentDescription = "Reports",
                                            tint = PinkAccent,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "OPERATIONAL ANALYTICS",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = "DMRC Line 7 Divyangjan Performance",
                                            fontSize = 11.sp,
                                            color = CyanNeon
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        val header = "RequestID,Source,Destination,Train,Pax,Type,Wheelchair,Status\n"
                                        val rows = allRequests.joinToString("\n") {
                                            "${it.requestId},${it.sourceStationId},${it.destinationStationId},${it.trainId},${it.passengerCount},${it.assistanceType},${it.wheelchairRequired},${it.status}"
                                        }
                                        csvExportData = header + rows
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("export_csv_button")
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = "Export", tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("EXPORT CSV", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Top Level Metrics Matrix
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatMetricCard(
                            title = "TOTAL REQUESTS",
                            value = totalCount.toString(),
                            icon = Icons.Default.InsertChart,
                            accentColor = PinkAccent,
                            modifier = Modifier.weight(1f)
                        )
                        StatMetricCard(
                            title = "COMPLETED",
                            value = completedCount.toString(),
                            icon = Icons.Default.Assessment,
                            accentColor = GreenSuccess,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Category Breakdown
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatMetricCard(
                            title = "WHEELCHAIR",
                            value = wheelchairCount.toString(),
                            icon = Icons.Default.InsertChart,
                            accentColor = WheelchairBlue,
                            modifier = Modifier.weight(1f)
                        )
                        StatMetricCard(
                            title = "VISUAL",
                            value = visualCount.toString(),
                            icon = Icons.Default.InsertChart,
                            accentColor = VisuallyImpairedPurple,
                            modifier = Modifier.weight(1f)
                        )
                        StatMetricCard(
                            title = "OTHER",
                            value = otherCount.toString(),
                            icon = Icons.Default.InsertChart,
                            accentColor = SpecialAssistanceOrange,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Top Source Stations Breakdown
                item {
                    Text(
                        text = "STATION ASSISTANCE VOLUME BREAKDOWN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon,
                        letterSpacing = 1.sp
                    )
                }

                if (sourceStationBreakdown.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("No assistance logs recorded yet.", color = Color.Gray, modifier = Modifier.padding(14.dp), fontSize = 12.sp)
                        }
                    }
                } else {
                    items(sourceStationBreakdown.take(8)) { (stId, count) ->
                        val stName = stationNameMap[stId] ?: stId
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = "Station", tint = PinkAccent, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Text("$count Passenger(s)", color = CyanNeon, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Train Volume Breakdown
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "TRAIN-WISE UTILIZATION MATRIX",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PinkAccent,
                        letterSpacing = 1.sp
                    )
                }

                items(trainBreakdown) { (trainId, count) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DirectionsTransit, contentDescription = "Train", tint = CyanNeon, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Train $trainId", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Text("$count Trips Served", color = AmberAlert, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
