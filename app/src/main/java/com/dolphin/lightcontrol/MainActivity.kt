package com.dolphin.lightcontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// Theme Colors (Geometric Balance)
val BgDeep = Color(0xFF0A0C10)
val BgCard = Color(0xFF14171D)
val AccentBlue = Color(0xFF00E5FF)
val TextDim = Color(0xFF8E9299)
val GridLine = Color(0x0DFFFFFF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: BYDViewModel = viewModel()
            DolphinControlApp(viewModel)
        }
    }
}

@Composable
fun DolphinControlApp(viewModel: BYDViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val vehicleState = uiState.vehicleState
    val isToggling = uiState.isToggling

    Scaffold(
        containerColor = BgDeep,
        topBar = { Header(vehicleState) },
        bottomBar = { NavigationFooter() }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Left Panel (Monitoring)
            Column(
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
                    .border(end = 1.dp, color = GridLine)
                    .padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                InfoCard("Bateria", "${vehicleState.batteryLevel}%", true, vehicleState.batteryLevel)
                InfoCard("Autonomia", "${vehicleState.estimatedRange} km", false)
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Door Lock Control
                ControlSmall(
                    label = if (vehicleState.isLocked) "TRANCADO" else "DESTRANCADO",
                    icon = if (vehicleState.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    isActive = vehicleState.isLocked,
                    isLoading = isToggling,
                    onClick = { viewModel.toggleLock() }
                )
            }

            // Center Stage (Lights)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                ControlHex(vehicleState.internalLights, isToggling) {
                    viewModel.toggleLights()
                }
                
                Spacer(modifier = Modifier.height(40.dp))
                
                Text(
                    "BYD SMART CONNECT",
                    color = TextDim,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )
            }

            // Right Panel (Climate)
            Column(
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
                    .border(start = 1.dp, color = GridLine)
                    .padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                InfoCard("Climatização", if(vehicleState.airConditioningOn) "LIGADO" else "DESLIGADO", false)
                
                // AC Toggle
                ControlSmall(
                    label = "AR CONDICIONADO",
                    icon = Icons.Default.AcUnit,
                    isActive = vehicleState.airConditioningOn,
                    isLoading = isToggling,
                    onClick = { viewModel.toggleAC() }
                )

                // Temp Control
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("TEMPERATURA ALVO", color = TextDim, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { if(vehicleState.targetTemperature > 16) viewModel.updateTemp(vehicleState.targetTemperature - 1) }) {
                            Icon(Icons.Default.Remove, "Less", tint = Color.White)
                        }
                        Text("${vehicleState.targetTemperature}°C", color = Color.White, fontSize = 32.sp)
                        IconButton(onClick = { if(vehicleState.targetTemperature < 28) viewModel.updateTemp(vehicleState.targetTemperature + 1) }) {
                            Icon(Icons.Default.Add, "More", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ControlSmall(label: String, icon: ImageVector, isActive: Boolean, isLoading: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) AccentBlue.copy(alpha = 0.15f) else BgCard)
            .border(1.dp, if (isActive) AccentBlue else GridLine, RoundedCornerShape(12.dp))
            .clickable(enabled = !isLoading) { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (isActive) AccentBlue else TextDim, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(label.uppercase(), color = if (isActive) Color.White else TextDim, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterEnd).size(20.dp), color = AccentBlue, strokeWidth = 2.dp)
        }
    }
}

@Composable
fun Header(state: VehicleState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .border(bottom = 1.dp, color = GridLine)
            .padding(horizontal = 40.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "BYD DOLPHIN PLUS",
            color = AccentBlue,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            letterSpacing = 4.sp
        )
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            StatusText("22:15")
            StatusText("24°C")
            StatusText("● API CONNECTED", AccentBlue)
        }
    }
}

@Composable
fun StatusText(text: String, color: Color = TextDim) {
    Text(text, color = color, fontSize = 14.sp, letterSpacing = 1.sp)
}

@Composable
fun InfoCard(label: String, value: String, showProgress: Boolean = false, progress: Int = 0) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgCard)
            .border(start = 3.dp, color = if (progress == 0) TextDim else AccentBlue)
            .padding(24.dp)
    ) {
        Text(label.uppercase(), color = TextDim, fontSize = 11.sp)
        Text(value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Light)
        if (showProgress) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .padding(top = 12.dp)
                    .background(Color(0xFF333333))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress / 100f)
                        .fillMaxHeight()
                        .background(AccentBlue)
                )
            }
        }
    }
}

@Composable
fun ControlHex(isOn: Boolean, isLoading: Boolean, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse)
    )

    Box(contentAlignment = Alignment.Center) {
        // Halo
        Box(
            modifier = Modifier
                .size(320.dp)
                .blur(40.dp)
                .clip(CircleShape)
                .background(
                    if (isOn) AccentBlue.copy(alpha = alpha) 
                    else Color.White.copy(alpha = 0.05f)
                )
        )
        
        // Button
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(BgCard)
                .border(4.dp, AccentBlue, CircleShape)
                .clickable(enabled = !isLoading) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = AccentBlue, modifier = Modifier.size(48.dp))
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.PowerSettingsNew, 
                        contentDescription = "Toggle",
                        tint = AccentBlue,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        if (isOn) "LUZES ON" else "LUZES OFF",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
        
        // Badge
        Box(
            modifier = Modifier
                .offset(y = 100.dp)
                .background(AccentBlue, RoundedCornerShape(100.dp))
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Text("Lâmpadas Internas", color = BgDeep, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun NavigationFooter() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(BgCard)
            .border(top = 1.dp, color = GridLine)
    ) {
        NavItem("⌂", "Home", false)
        NavItem("☼", "Luzes", true)
        NavItem("♨", "Clima", false)
        NavItem("◓", "Energia", false)
        NavItem("⚙", "Ajustes", false)
    }
}

@Composable
fun RowScope.NavItem(icon: String, label: String, isActive: Boolean) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(if (isActive) AccentBlue.copy(alpha = 0.05f) else Color.Transparent)
            .border(end = 1.dp, color = GridLine),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(icon, fontSize = 24.sp, color = if (isActive) AccentBlue else TextDim)
        Spacer(modifier = Modifier.height(8.dp))
        Text(label.uppercase(), fontSize = 12.sp, color = if (isActive) AccentBlue else TextDim)
    }
}

// Extension for modifier border customization
fun Modifier.border(
    bottom: androidx.compose.ui.unit.Dp = 0.dp,
    top: androidx.compose.ui.unit.Dp = 0.dp,
    start: androidx.compose.ui.unit.Dp = 0.dp,
    end: androidx.compose.ui.unit.Dp = 0.dp,
    color: Color
): Modifier = this.then(
    Modifier.drawBehind {
        if (bottom > 0.dp) drawLine(color, androidx.compose.ui.geometry.Offset(0f, size.height), androidx.compose.ui.geometry.Offset(size.width, size.height), bottom.toPx())
        if (top > 0.dp) drawLine(color, androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(size.width, 0f), top.toPx())
        if (start > 0.dp) drawLine(color, androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(0f, size.height), start.toPx())
        if (end > 0.dp) drawLine(color, androidx.compose.ui.geometry.Offset(size.width, 0f), androidx.compose.ui.geometry.Offset(size.width, size.height), end.toPx())
    }
)
