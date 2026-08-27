package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Accessible
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.AssistanceType
import com.example.data.model.Station
import com.example.data.model.Train
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
import com.example.ui.theme.SpecialAssistanceOrange
import com.example.ui.theme.VisuallyImpairedPurple
import com.example.ui.theme.WheelchairBlue
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestCreationScreen(
    user: User,
    stations: List<Station>,
    trains: List<Train>,
    config: TravelTimeConfig,
    generatedRequestId: String,
    onBack: () -> Unit,
    onSubmitRequest: (
        sourceId: String,
        destId: String,
        trainId: String,
        passengerCount: Int,
        type: String,
        wheelchair: Boolean,
        platform: String,
        remarks: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    // Automatically selected source station from logged-in employee
    val sourceStation = remember(stations, user) {
        stations.firstOrNull { it.id == user.assignedStationId }
            ?: stations.firstOrNull { it.name.contains("Anand Vihar", ignoreCase = true) }
            ?: stations.firstOrNull()
            ?: Station("ST_28", "Anand Vihar ISBT", 28)
    }

    var selectedDestinationStation by remember {
        mutableStateOf(
            stations.firstOrNull { it.id == "ST_22" } // Mayur Vihar - I default
                ?: stations.getOrNull(5)
                ?: Station("ST_22", "Mayur Vihar - I", 22)
        )
    }

    var selectedTrainId by remember { mutableStateOf(trains.firstOrNull()?.trainId ?: "T-245") }
    var passengerCount by remember { mutableIntStateOf(1) }
    var selectedAssistanceType by remember { mutableStateOf(AssistanceType.WHEELCHAIR) }
    var wheelchairRequired by remember { mutableStateOf(true) }
    var selectedPlatform by remember { mutableStateOf("Platform 1 (+ Circular Line)") }
    var remarks by remember { mutableStateOf("") }
    var destinationDropdownExpanded by remember { mutableStateOf(false) }
    var trainDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Live clock for ETA engine
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            delay(1000)
        }
    }

    // Sequence & ETA calculation
    val stationCount = remember(sourceStation, selectedDestinationStation) {
        val diff = abs(selectedDestinationStation.sequenceNumber - sourceStation.sequenceNumber)
        if (diff == 0) 1 else diff
    }

    val estimatedTravelSeconds = (stationCount * config.secondsPerStation).toLong()
    val etaTimestamp = currentTimeMillis + (estimatedTravelSeconds * 1000)

    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val etaFormatter = remember { SimpleDateFormat("HH:mm:ss a", Locale.getDefault()) }

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
                        text = "NEW ASSISTANCE REQUEST",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Divyangjan & Passenger Intake",
                        fontSize = 11.sp,
                        color = CyanNeon
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // 1. Request ID Card (Automatic)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(PinkAccent, DarkSurfaceBorder))
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("SYSTEM REQUEST ID", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text(
                                    text = generatedRequestId,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = PinkAccent
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PinkPrimary.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("AUTO-GENERATED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PinkAccent)
                            }
                        }
                    }
                }

                // 2. Source Station (Auto-selected from logged-in employee)
                item {
                    Column {
                        Text("SOURCE STATION (AUTO-SELECTED)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(10.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(listOf(DarkSurfaceBorder, DarkSurfaceBorder))
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = sourceStation.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Sequence #${sourceStation.sequenceNumber} • ${sourceStation.platform}",
                                        fontSize = 11.sp,
                                        color = Color.LightGray
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked to employee station",
                                    tint = CyanNeon,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // 3. Destination Station Selector
                item {
                    Column {
                        Text("DESTINATION STATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))

                        ExposedDropdownMenuBox(
                            expanded = destinationDropdownExpanded,
                            onExpandedChange = { destinationDropdownExpanded = !destinationDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = "${selectedDestinationStation.name} (Seq #${selectedDestinationStation.sequenceNumber})",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = destinationDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .testTag("destination_station_dropdown"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PinkAccent,
                                    unfocusedBorderColor = DarkSurfaceBorder
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = destinationDropdownExpanded,
                                onDismissRequest = { destinationDropdownExpanded = false },
                                modifier = Modifier.background(DarkSurfaceElevated)
                            ) {
                                stations.filter { it.isActive }.forEach { st ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(st.name, color = Color.White, fontSize = 13.sp)
                                                Text("#${st.sequenceNumber}", color = PinkAccent, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                            }
                                        },
                                        onClick = {
                                            selectedDestinationStation = st
                                            destinationDropdownExpanded = false
                                            errorMessage = null
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Train ID & Platform Selector
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Train Dropdown
                        Column(modifier = Modifier.weight(1f)) {
                            Text("TRAIN ID", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            ExposedDropdownMenuBox(
                                expanded = trainDropdownExpanded,
                                onExpandedChange = { trainDropdownExpanded = !trainDropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedTrainId,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = trainDropdownExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                        .testTag("train_id_dropdown"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyanNeon,
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
                                        DropdownMenuItem(
                                            text = { Text(tr.trainId, color = Color.White) },
                                            onClick = {
                                                selectedTrainId = tr.trainId
                                                trainDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Passenger Count Stepper
                        Column(modifier = Modifier.weight(1f)) {
                            Text("PASSENGERS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .background(DarkSurface, RoundedCornerShape(10.dp))
                                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { if (passengerCount > 1) passengerCount-- },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = CyanNeon)
                                }
                                Text(
                                    text = passengerCount.toString(),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White
                                )
                                IconButton(
                                    onClick = { if (passengerCount < 10) passengerCount++ },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase", tint = CyanNeon)
                                }
                            }
                        }
                    }
                }

                // 5. Assistance Type Pills
                item {
                    Column {
                        Text("ASSISTANCE TYPE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AssistanceTypePill(
                                title = "Wheelchair",
                                icon = Icons.Default.Accessible,
                                selected = selectedAssistanceType == AssistanceType.WHEELCHAIR,
                                color = WheelchairBlue,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedAssistanceType = AssistanceType.WHEELCHAIR
                                    wheelchairRequired = true
                                }
                            )

                            AssistanceTypePill(
                                title = "Visual Impaired",
                                icon = Icons.Default.Visibility,
                                selected = selectedAssistanceType == AssistanceType.VISUALLY_IMPAIRED,
                                color = VisuallyImpairedPurple,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedAssistanceType = AssistanceType.VISUALLY_IMPAIRED
                                }
                            )

                            AssistanceTypePill(
                                title = "Other Special",
                                icon = Icons.Default.Info,
                                selected = selectedAssistanceType == AssistanceType.OTHER,
                                color = SpecialAssistanceOrange,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedAssistanceType = AssistanceType.OTHER
                                }
                            )
                        }
                    }
                }

                // 6. Wheelchair Required Switch (YES / NO) & Platform
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Accessible,
                                        contentDescription = "Wheelchair",
                                        tint = WheelchairBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("WHEELCHAIR REQUIRED", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(if (wheelchairRequired) "YES — Ramp deployment requested" else "NO — Physical guidance only", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                                Switch(
                                    checked = wheelchairRequired,
                                    onCheckedChange = { wheelchairRequired = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = WheelchairBlue
                                    ),
                                    modifier = Modifier.testTag("wheelchair_required_switch")
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("BOARDING PLATFORM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    "Platform 1 (+ Circular Line)",
                                    "Platform 2 (- Circular Line)"
                                ).forEach { plat ->
                                    val isSelected = selectedPlatform == plat
                                    val isPlus = plat.contains("+")
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) PinkPrimary.copy(alpha = 0.25f) else DarkSurfaceElevated)
                                            .border(1.dp, if (isSelected) PinkAccent else DarkSurfaceBorder, RoundedCornerShape(8.dp))
                                            .clickable { selectedPlatform = plat }
                                            .padding(vertical = 10.dp, horizontal = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = plat,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                                color = if (isSelected) Color.White else Color.LightGray
                                            )
                                            Text(
                                                text = if (isPlus) "Clockwise • Majlis → Shiv" else "Anti-Clockwise • Shiv → Majlis",
                                                fontSize = 9.5.sp,
                                                color = if (isSelected) CyanNeon else Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 7. Automatic ETA Engine Display Box
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141F2B)),
                        shape = RoundedCornerShape(14.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(CyanNeon.copy(alpha = 0.5f), AmberAlert.copy(alpha = 0.5f)))
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "ETA Engine",
                                        tint = CyanNeon,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "AUTOMATIC ETA ENGINE",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = CyanNeon,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Text(
                                    text = "@ ${config.secondsPerStation}s / station",
                                    fontSize = 10.sp,
                                    color = Color.LightGray
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("CURRENT TIME", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = timeFormatter.format(Date(currentTimeMillis)),
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.White
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("STATIONS REMAINING", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "$stationCount Stations",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PinkAccent
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("ESTIMATED TRAVEL", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${estimatedTravelSeconds / 60}m ${estimatedTravelSeconds % 60}s",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AmberAlert
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.4f))
                                    .padding(vertical = 8.dp, horizontal = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("CALCULATED ETA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GreenSuccess)
                                    Text(
                                        text = etaFormatter.format(Date(etaTimestamp)),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = GreenSuccess
                                    )
                                }
                            }
                        }
                    }
                }

                // Error Message if any
                if (errorMessage != null) {
                    item {
                        Text(
                            text = errorMessage!!,
                            color = RedCritical,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 8. CREATE REQUEST Action Button
                item {
                    Button(
                        onClick = {
                            if (sourceStation.id == selectedDestinationStation.id) {
                                errorMessage = "Source and Destination station cannot be identical."
                                return@Button
                            }
                            onSubmitRequest(
                                sourceStation.id,
                                selectedDestinationStation.id,
                                selectedTrainId,
                                passengerCount,
                                selectedAssistanceType.label,
                                wheelchairRequired,
                                selectedPlatform,
                                remarks
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("submit_create_request_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Create", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CREATE REQUEST & DISPATCH",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun AssistanceTypePill(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) color.copy(alpha = 0.25f) else DarkSurface)
            .border(1.5.dp, if (selected) color else DarkSurfaceBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (selected) color else Color.Gray,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) Color.White else Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
