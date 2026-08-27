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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.ui.components.DemoModeBanner
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.PinkPrimary
import com.example.ui.theme.RedCritical

@Composable
fun LoginScreen(
    onLoginSuccess: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    var employeeId by remember { mutableStateOf("ST001") }
    var password by remember { mutableStateOf("demo123") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val handleLogin = {
        when {
            employeeId.equals("ST001", ignoreCase = true) && password == "demo123" -> {
                onLoginSuccess(
                    User(
                        employeeId = "ST001",
                        name = "Rajesh Sharma (Assistant)",
                        role = UserRole.STATION_ASSISTANT,
                        assignedStationId = "ST_28",
                        stationName = "Anand Vihar ISBT"
                    )
                )
            }
            employeeId.equals("CR001", ignoreCase = true) && password == "demo123" -> {
                onLoginSuccess(
                    User(
                        employeeId = "CR001",
                        name = "Priya Verma (Controller)",
                        role = UserRole.CONTROL_ROOM,
                        assignedStationId = "OCC_01",
                        stationName = "Central Control Room (OCC)"
                    )
                )
            }
            employeeId.equals("AD001", ignoreCase = true) && password == "demo123" -> {
                onLoginSuccess(
                    User(
                        employeeId = "AD001",
                        name = "Vikram Malhotra (Admin)",
                        role = UserRole.ADMIN,
                        assignedStationId = "HQ_01",
                        stationName = "Pink Line Metro HQ"
                    )
                )
            }
            else -> {
                errorMessage = "Invalid credentials. Use demo accounts below."
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Demo Banner
            DemoModeBanner()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Futuristic Pink Line Logo & Header
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(PinkPrimary, Color(0xFF880E4F))
                            )
                        )
                        .border(2.dp, PinkAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsTransit,
                        contentDescription = "Pink Line",
                        tint = Color.White,
                        modifier = Modifier.size(46.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "PINK LINE ASSIST",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = Color.White
                )

                Text(
                    text = "Divyangjan & Special Assistance Operations",
                    fontSize = 12.sp,
                    color = CyanNeon,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Login Form Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.verticalGradient(
                            listOf(PinkAccent.copy(alpha = 0.4f), DarkSurfaceBorder)
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "SECURE EMPLOYEE LOGIN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = employeeId,
                            onValueChange = { employeeId = it; errorMessage = null },
                            label = { Text("Employee ID") },
                            leadingIcon = {
                                Icon(Icons.Default.Badge, contentDescription = "Employee ID", tint = PinkAccent)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("employee_id_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PinkAccent,
                                unfocusedBorderColor = DarkSurfaceBorder,
                                focusedLabelColor = PinkAccent,
                                unfocusedLabelColor = Color.Gray
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; errorMessage = null },
                            label = { Text("Password") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = "Password", tint = CyanNeon)
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("password_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanNeon,
                                unfocusedBorderColor = DarkSurfaceBorder,
                                focusedLabelColor = CyanNeon,
                                unfocusedLabelColor = Color.Gray
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage!!,
                                color = RedCritical,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = handleLogin,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("login_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "LOGIN TO SYSTEM",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Quick Demo Accounts Fill Buttons
                Text(
                    text = "QUICK DEMO ACCOUNTS (ONE-TAP)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DemoRolePill(
                        role = "Assistant",
                        id = "ST001",
                        icon = Icons.Default.SupportAgent,
                        color = PinkAccent,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            employeeId = "ST001"
                            password = "demo123"
                            errorMessage = null
                        }
                    )

                    DemoRolePill(
                        role = "Control Room",
                        id = "CR001",
                        icon = Icons.Default.Security,
                        color = CyanNeon,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            employeeId = "CR001"
                            password = "demo123"
                            errorMessage = null
                        }
                    )

                    DemoRolePill(
                        role = "Admin",
                        id = "AD001",
                        icon = Icons.Default.AdminPanelSettings,
                        color = Color(0xFFFFB300),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            employeeId = "AD001"
                            password = "demo123"
                            errorMessage = null
                        }
                    )
                }
            }

            // Footer
            Text(
                text = "Operational Assistance Prototype • DMRC Line 7 Specification",
                fontSize = 10.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
private fun DemoRolePill(
    role: String,
    id: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(color.copy(alpha = 0.5f), DarkSurfaceBorder)))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = role,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = role,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = id,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = color
            )
        }
    }
}
