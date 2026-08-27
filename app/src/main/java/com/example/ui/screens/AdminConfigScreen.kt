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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.Station
import com.example.data.model.TravelTimeConfig
import com.example.data.model.User
import com.example.ui.components.DemoModeBanner
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.PinkPrimary
import com.example.ui.theme.RedCritical

@Composable
fun AdminConfigScreen(
    user: User,
    config: TravelTimeConfig,
    stations: List<Station>,
    onSaveConfig: (TravelTimeConfig) -> Unit,
    onUpdateStation: (Station) -> Unit,
    onViewAuditLogs: () -> Unit,
    onTestSiren: () -> Unit = {},
    onTestWarningApproach: () -> Unit = {},
    onTestCriticalSiren: () -> Unit = {},
    onTestDestinationAlarm: () -> Unit = {},
    onTestAcknowledgment: () -> Unit = {},
    onStopAudio: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var secondsPerStation by remember(config) { mutableIntStateOf(config.secondsPerStation) }
    var warningThreshold by remember(config) { mutableIntStateOf(config.warningThresholdStations) }
    var criticalThreshold by remember(config) { mutableIntStateOf(config.criticalThresholdStations) }
    var audioAlarmsEnabled by remember(config) { mutableStateOf(config.audioAlarmsEnabled) }
    var configSaveSuccess by remember { mutableStateOf(false) }

    // Station editor dialog
    var stationBeingEdited by remember { mutableStateOf<Station?>(null) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DemoModeBanner()

            if (stationBeingEdited != null) {
                var editName by remember { mutableStateOf(stationBeingEdited!!.name) }
                var editSeq by remember { mutableIntStateOf(stationBeingEdited!!.sequenceNumber) }
                var editPlatform by remember { mutableStateOf(stationBeingEdited!!.platform) }
                var editActive by remember { mutableStateOf(stationBeingEdited!!.isActive) }

                AlertDialog(
                    onDismissRequest = { stationBeingEdited = null },
                    containerColor = DarkSurfaceElevated,
                    title = {
                        Text("EDIT STATION: ${stationBeingEdited!!.id}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text("Station Name") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PinkAccent, unfocusedBorderColor = DarkSurfaceBorder)
                            )

                            OutlinedTextField(
                                value = editPlatform,
                                onValueChange = { editPlatform = it },
                                label = { Text("Platform Information") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanNeon, unfocusedBorderColor = DarkSurfaceBorder)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Station Operational", fontSize = 13.sp, color = Color.White)
                                Switch(
                                    checked = editActive,
                                    onCheckedChange = { editActive = it },
                                    colors = SwitchDefaults.colors(checkedTrackColor = GreenSuccess)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                onUpdateStation(
                                    stationBeingEdited!!.copy(
                                        name = editName,
                                        sequenceNumber = editSeq,
                                        platform = editPlatform,
                                        isActive = editActive
                                    )
                                )
                                stationBeingEdited = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
                        ) {
                            Text("SAVE CHANGES")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { stationBeingEdited = null }) {
                            Text("CANCEL", color = Color.Gray)
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
                                listOf(Color(0xFFFFB300).copy(alpha = 0.6f), PinkAccent.copy(alpha = 0.4f))
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
                                            .background(Color(0xFFFFB300).copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AdminPanelSettings,
                                            contentDescription = "Admin",
                                            tint = Color(0xFFFFB300),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "ADMINISTRATION & CONFIG",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = "DMRC System Parameters & Station Rules",
                                            fontSize = 11.sp,
                                            color = CyanNeon
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = onViewAuditLogs,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.History, contentDescription = "Audit", tint = CyanNeon, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("AUDIT LOG", fontSize = 11.sp, color = CyanNeon, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // 1. ETA Engine & Alert Thresholds
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(DarkSurfaceBorder, DarkSurfaceBorder))
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Speed, contentDescription = "ETA", tint = PinkAccent, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ETA & ALERT ENGINE TUNING",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Travel time per station
                            Column {
                                Text("Travel Time Per Station (Seconds)", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = secondsPerStation.toString(),
                                    onValueChange = { secondsPerStation = it.toIntOrNull() ?: 130 },
                                    modifier = Modifier.fillMaxWidth().testTag("config_seconds_per_station"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PinkAccent,
                                        unfocusedBorderColor = DarkSurfaceBorder
                                    ),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Text("Standard DMRC Pink Line headway default is 130 seconds.", fontSize = 10.sp, color = Color.Gray)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Warning threshold
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Warning Alert", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = warningThreshold.toString(),
                                        onValueChange = { warningThreshold = it.toIntOrNull() ?: 3 },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberAlert, unfocusedBorderColor = DarkSurfaceBorder),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    Text("3 Stations away", fontSize = 10.sp, color = AmberAlert)
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Critical Alert", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = criticalThreshold.toString(),
                                        onValueChange = { criticalThreshold = it.toIntOrNull() ?: 1 },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RedCritical, unfocusedBorderColor = DarkSurfaceBorder),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    Text("1 Station away", fontSize = 10.sp, color = RedCritical)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Audio Alarms Switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.NotificationsActive, contentDescription = "Audio Alarm", tint = PinkAccent, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Audible Station Alarms", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Play siren tone on approaching / arrived", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                                Switch(
                                    checked = audioAlarmsEnabled,
                                    onCheckedChange = { audioAlarmsEnabled = it },
                                    colors = SwitchDefaults.colors(checkedTrackColor = PinkPrimary)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Interactive Alarm & Siren Audio Test Panel
                            Text(
                                text = "SIREN & ALARM TONE TESTING",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanNeon,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceElevated)
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = onTestSiren,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                    ) {
                                        Text("🚨 Wailing Siren", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = RedCritical)
                                    }

                                    OutlinedButton(
                                        onClick = onTestWarningApproach,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                    ) {
                                        Text("⚠️ 2-3 Stn Warning", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = AmberAlert)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = onTestCriticalSiren,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                    ) {
                                        Text("⚡ 1 Stn Critical", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = RedCritical)
                                    }

                                    OutlinedButton(
                                        onClick = onTestDestinationAlarm,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                    ) {
                                        Text("🏁 Destination", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = PinkAccent)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = onTestAcknowledgment,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                    ) {
                                        Text("✓ Acknowledge Tone", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = GreenSuccess)
                                    }

                                    OutlinedButton(
                                        onClick = onStopAudio,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                    ) {
                                        Text("⏹ Stop / Mute", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    onSaveConfig(
                                        config.copy(
                                            secondsPerStation = secondsPerStation,
                                            warningThresholdStations = warningThreshold,
                                            criticalThresholdStations = criticalThreshold,
                                            audioAlarmsEnabled = audioAlarmsEnabled
                                        )
                                    )
                                    configSaveSuccess = true
                                },
                                modifier = Modifier.fillMaxWidth().height(44.dp).testTag("save_config_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SAVE SYSTEM CONFIGURATION", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // 2. Pink Line Station Sequence Matrix (38 Stations)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PINK LINE CORRIDOR STATIONS (${stations.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Tap to edit",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                items(stations.sortedBy { it.sequenceNumber }, key = { it.id }) { st ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { stationBeingEdited = st },
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "#${st.sequenceNumber}",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = PinkAccent,
                                    modifier = Modifier.width(36.dp)
                                )
                                Column {
                                    Text(
                                        text = st.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (st.isActive) Color.White else Color.Gray
                                    )
                                    Text(
                                        text = "${st.id} • ${st.platform}",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (st.isActive) GreenSuccess.copy(alpha = 0.2f) else RedCritical.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (st.isActive) "ACTIVE" else "INACTIVE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (st.isActive) GreenSuccess else RedCritical
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
