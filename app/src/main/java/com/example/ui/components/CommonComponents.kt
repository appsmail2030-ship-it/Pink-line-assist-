package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessible
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Alert
import com.example.data.model.AlertType
import com.example.data.model.RequestStatus
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CyanNeon
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
fun DemoModeBanner(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF2C1500),
                        Color(0xFF422006),
                        Color(0xFF2C1500)
                    )
                )
            )
            .border(1.dp, AmberAlert.copy(alpha = 0.6f))
            .padding(vertical = 4.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Demo Mode",
                tint = AmberAlert,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "DEMO MODE — Simulated Pink Line Assistance (Prototype)",
                color = AmberAlert,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun NetworkStatusBar(
    isOnline: Boolean,
    onToggleNetwork: () -> Unit,
    onSyncNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(if (isOnline) Color(0xFF0F2618) else Color(0xFF331515))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isOnline) GreenSuccess else RedCritical)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                contentDescription = if (isOnline) "Online" else "Offline",
                tint = if (isOnline) GreenSuccess else RedCritical,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isOnline) "SYSTEM ONLINE • Real-time Sync Active" else "OFFLINE MODE • Local DB Active",
                color = if (isOnline) GreenSuccess else RedCritical,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (isOnline) "Simulate Offline" else "Go Online",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable { onToggleNetwork() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
            if (!isOnline) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onSyncNow,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync now",
                        tint = CyanNeon,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, icon) = when (status.uppercase()) {
        "REQUESTED" -> Triple(AmberAlert.copy(alpha = 0.2f), AmberAlert, Icons.Default.HourglassEmpty)
        "ACCEPTED" -> Triple(CyanNeon.copy(alpha = 0.2f), CyanNeon, Icons.Default.CheckCircle)
        "TRAIN APPROACHING" -> Triple(PinkNeon.copy(alpha = 0.25f), PinkAccent, Icons.Default.DirectionsTransit)
        "BOARDING" -> Triple(SpecialAssistanceOrange.copy(alpha = 0.25f), SpecialAssistanceOrange, Icons.Default.Accessible)
        "EN ROUTE" -> Triple(WheelchairBlue.copy(alpha = 0.25f), WheelchairBlue, Icons.Default.DirectionsTransit)
        "2 STATIONS AWAY" -> Triple(AmberAlert.copy(alpha = 0.3f), AmberAlert, Icons.Default.NotificationsActive)
        "1 STATION AWAY" -> Triple(RedCritical.copy(alpha = 0.3f), RedCritical, Icons.Default.Warning)
        "ARRIVED" -> Triple(GreenSuccess.copy(alpha = 0.3f), GreenSuccess, Icons.Default.Alarm)
        "ASSISTANCE COMPLETED" -> Triple(GreenSuccess.copy(alpha = 0.2f), GreenSuccess, Icons.Default.CheckCircle)
        "CANCELLED" -> Triple(Color.Gray.copy(alpha = 0.2f), Color.Gray, Icons.Default.Error)
        else -> Triple(Color.DarkGray, Color.White, Icons.Default.Info)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = status,
            tint = textColor,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = status,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun AssistanceTypeBadge(
    type: String,
    wheelchairRequired: Boolean,
    modifier: Modifier = Modifier
) {
    val (color, icon, label) = when {
        type.contains("Wheelchair", ignoreCase = true) || wheelchairRequired ->
            Triple(WheelchairBlue, Icons.Default.Accessible, "Wheelchair")
        type.contains("Visual", ignoreCase = true) ->
            Triple(VisuallyImpairedPurple, Icons.Default.Visibility, "Visually Impaired")
        else ->
            Triple(SpecialAssistanceOrange, Icons.Default.Info, "Special Assistance")
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    subtext: String? = null
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(accentColor.copy(alpha = 0.5f), DarkSurfaceBorder)
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 0.5.sp
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                fontFamily = FontFamily.Monospace
            )

            if (subtext != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtext,
                    fontSize = 11.sp,
                    color = accentColor.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun PriorityAlertOverlayDialog(
    alert: Alert,
    onAcknowledge: (String) -> Unit,
    onViewRequest: (String) -> Unit,
    onCallStation: (String) -> Unit
) {
    val isDestination = alert.alertType == AlertType.DESTINATION_ASSISTANCE.label
    val isCritical = alert.alertType == AlertType.FINAL_APPROACH.label

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val headerColor = when {
        isDestination -> RedCritical
        isCritical -> RedCritical
        else -> AmberAlert
    }

    val titleText = when {
        isDestination -> "🚨 DESTINATION ASSISTANCE ALARM"
        isCritical -> "🚨 CRITICAL APPROACH — 1 STATION AWAY"
        else -> "⚠️ PRIORITY ASSISTANCE — TRAIN APPROACHING"
    }

    Dialog(
        onDismissRequest = { /* Require explicit action */ },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .scale(pulseScale)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF14070B))
                .border(2.5.dp, headerColor, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(headerColor.copy(alpha = 0.25f))
                        .border(1.dp, headerColor, RoundedCornerShape(10.dp))
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = titleText,
                        color = headerColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Detail specs
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1319)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("REQUEST ID", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text(alert.requestId, fontSize = 15.sp, color = PinkAccent, fontWeight = FontWeight.ExtraBold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("TRAIN ID", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text(alert.trainId.ifEmpty { "T-245" }, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("DESTINATION", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text(alert.destinationStation, fontSize = 14.sp, color = CyanNeon, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("PASSENGERS", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("${alert.passengerCount} Passenger(s)", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("WHEELCHAIR", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text(
                                if (alert.wheelchairRequired) "YES — RAMP REQUIRED" else "NO",
                                fontSize = 14.sp,
                                color = if (alert.wheelchairRequired) WheelchairBlue else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (alert.etaRemainingSeconds > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("APPROX. ARRIVAL", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text(
                                    "in ${(alert.etaRemainingSeconds / 60) + 1} min (${alert.etaRemainingSeconds}s)",
                                    fontSize = 14.sp,
                                    color = AmberAlert,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Button(
                    onClick = { onAcknowledge(alert.alertId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("acknowledge_alert_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = headerColor),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Acknowledge", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ACKNOWLEDGE ALARM", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onViewRequest(alert.requestId) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("view_request_alert_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("VIEW REQUEST", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { onCallStation(alert.destinationStation) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("call_station_alert_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanNeon)
                    ) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("CALL STATION", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
