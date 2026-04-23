package com.dolphin.lightcontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    val discoveredDevices by viewModel.discoveredBluetoothDevices.collectAsState()
    val vehicleState = uiState.vehicleState
    val isToggling = uiState.isToggling
    val isSyncing = uiState.isSyncing

    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION)
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            viewModel.updatePairedDevices()
            viewModel.startBluetoothScan()
        }
    }

    Scaffold(
        containerColor = BgDeep,
        topBar = { Header(vehicleState, isSyncing) { viewModel.syncWithCloud() } },
        bottomBar = { NavigationFooter(uiState.currentTab) { viewModel.selectTab(it) } }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (uiState.currentTab) {
                "HOME" -> HomeScreen(vehicleState, isToggling, viewModel)
                "LUZES" -> LightsScreen(vehicleState, isToggling, viewModel)
                "CLIMA" -> ClimateScreen(vehicleState, isToggling, viewModel)
                "ENERGIA" -> EnergyScreen(vehicleState)
                "AJUSTES" -> SettingsScreen(vehicleState, discoveredDevices, viewModel) {
                    launcher.launch(permissionsToRequest)
                }
            }
        }
    }
}

@Composable
fun HomeScreen(state: VehicleState, isToggling: Boolean, viewModel: BYDViewModel) {
    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.width(300.dp).fillMaxHeight().border(end = 1.dp, color = GridLine).padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            InfoCard("Bateria", "${state.batteryLevel}%", true, state.batteryLevel)
            InfoCard("Autonomia", "${state.estimatedRange} km", false)
        }
        
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.DirectionsCar, null, tint = AccentBlue, modifier = Modifier.size(200.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text(state.carName, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Light)
            Text("By BYD Automotive", color = TextDim, fontSize = 12.sp)
        }

        Column(
            modifier = Modifier.width(350.dp).fillMaxHeight().border(start = 1.dp, color = GridLine).padding(32.dp),
            verticalArrangement = Arrangement.Center
        ) {
            ControlSmall(
                label = if (state.isLocked) "VEÍCULO TRANCADO" else "VEÍCULO ABERTO",
                icon = if (state.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                isActive = state.isLocked,
                isLoading = isToggling,
                onClick = { viewModel.toggleLock() }
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text("Segurança e Trava Automática Ativos", color = TextDim, fontSize = 11.sp)
        }
    }
}

@Composable
fun LightsScreen(state: VehicleState, isToggling: Boolean, viewModel: BYDViewModel) {
    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.width(300.dp).fillMaxHeight().border(end = 1.dp, color = GridLine).padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("ZONAS DE ILUMINAÇÃO", color = TextDim, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            LightZoneItem("Interior Frontal", state.frontLights) { viewModel.updateLightZone("front", it) }
            LightZoneItem("Interior Traseiro", state.rearLightsZone) { viewModel.updateLightZone("rear", it) }
            LightZoneItem("Porta-malas", state.trunkLights) { viewModel.updateLightZone("trunk", it) }
            LightZoneItem("Luz de Leitura", state.readingLights) { viewModel.updateLightZone("reading", it) }
        }

        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ControlHex(state.internalLights, isToggling) { viewModel.toggleLights() }
            Spacer(modifier = Modifier.height(32.dp))
            Text("MASTER SWITCH", color = if(state.internalLights) AccentBlue else TextDim, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
        }

        Column(
            modifier = Modifier.width(300.dp).fillMaxHeight().border(start = 1.dp, color = GridLine).padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("ILUMINAÇÃO EXTERNA", color = TextDim, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            ControlSmall("Faróis Automáticos", Icons.Default.LightMode, state.autoHeadlightsOn, isToggling) { viewModel.toggleAutoHeadlights() }
            ControlSmall("Lanterna de Neblina", Icons.Default.Cloud, state.rearFogOn, isToggling) { viewModel.toggleRearFog() }
        }
    }
}

@Composable
fun LightZoneItem(label: String, isOn: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(8.dp))
            .background(if(isOn) AccentBlue.copy(alpha = 0.1f) else BgCard).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = if(isOn) Color.White else TextDim, fontSize = 13.sp)
        Switch(checked = isOn, onCheckedChange = { onToggle(it) }, colors = SwitchDefaults.colors(checkedThumbColor = AccentBlue))
    }
}

@Composable
fun ClimateScreen(state: VehicleState, isToggling: Boolean, viewModel: BYDViewModel) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Left: Fan Speed
        Column(
            modifier = Modifier.width(300.dp).fillMaxHeight().border(end = 1.dp, color = GridLine).padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("VELOCIDADE DO VENTILADOR", color = TextDim, fontSize = 11.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1, 2, 3, 4, 5).forEach { speed ->
                    Box(
                        modifier = Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(4.dp))
                            .background(if(speed <= state.fanSpeed) AccentBlue else Color(0xFF1A1D23))
                            .clickable { viewModel.setFanSpeed(speed) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$speed", color = if(speed <= state.fanSpeed) BgDeep else TextDim, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            ControlSmall("Recirculação", Icons.Default.Autorenew, state.recirculation, isToggling) { viewModel.toggleRecirculation() }
            ControlSmall("Desembaçador", Icons.Default.Waves, state.defrost, isToggling) { viewModel.toggleDefrost() }
        }

        // Center: Large Temp
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("TEMPERATURA INTERNA", color = TextDim, fontSize = 12.sp, letterSpacing = 2.sp)
            Text("${state.targetTemperature}°C", color = Color.White, fontSize = 120.sp, fontWeight = FontWeight.ExtraLight)
            Row(horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                IconButton(onClick = { viewModel.updateTemp(state.targetTemperature - 1) }, modifier = Modifier.size(64.dp).background(BgCard, CircleShape).border(1.dp, GridLine, CircleShape)) {
                    Icon(Icons.Default.Remove, null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
                IconButton(onClick = { viewModel.updateTemp(state.targetTemperature + 1) }, modifier = Modifier.size(64.dp).background(BgCard, CircleShape).border(1.dp, GridLine, CircleShape)) {
                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
        }

        // Right: Temp Slider & AC
        Column(
            modifier = Modifier.width(300.dp).fillMaxHeight().border(start = 1.dp, color = GridLine).padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.width(40.dp).weight(1f).clip(RoundedCornerShape(100.dp)).background(Color(0xFF1A1D23))
                    .pointerInput(Unit) { detectTapGestures { offset ->
                        val percent = 1f - (offset.y / size.height)
                        val newTemp = (16 + (percent * 12)).toInt().coerceIn(16, 28)
                        viewModel.updateTemp(newTemp)
                    } }.padding(vertical = 2.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                val fillPercent = (state.targetTemperature - 16) / 12f
                Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(fillPercent.coerceIn(0f, 1f)).clip(RoundedCornerShape(100.dp)).background(Brush.verticalGradient(listOf(AccentBlue, Color(0xFF00B8D4)))))
            }
            Spacer(modifier = Modifier.height(32.dp))
            Box(
                modifier = Modifier.size(80.dp).clip(CircleShape).background(if(state.airConditioningOn) AccentBlue.copy(alpha = 0.1f) else BgCard).border(2.dp, if(state.airConditioningOn) AccentBlue else GridLine, CircleShape).clickable { viewModel.toggleAC() },
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.AcUnit, null, tint = if(state.airConditioningOn) AccentBlue else TextDim, modifier = Modifier.size(32.dp)) }
        }
    }
}

@Composable
fun EnergyScreen(state: VehicleState) {
    Column(modifier = Modifier.fillMaxSize().padding(40.dp)) {
        Text("FLUXO DE ENERGIA", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(40.dp))
        Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(40.dp)) {
            // Simulated Graph
            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(BgCard).border(1.dp, GridLine)) {
                // Background Grid
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    repeat(5) { Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(GridLine)) }
                }
                Text("Gráfico de Consumo (Últimos 50km)", modifier = Modifier.align(Alignment.Center), color = TextDim)
            }
            Column(modifier = Modifier.width(300.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                InfoCard("Consumo Médio", "15.4 kWh/100km")
                InfoCard("Energia Recuperada", "2.1 kWh")
                InfoCard("Saúde da Bateria", "98%")
            }
        }
    }
}

@Composable
fun SettingsScreen(state: VehicleState, discoveredDevices: List<BluetoothDeviceInfo>, viewModel: BYDViewModel, onScanRequested: () -> Unit) {
    var showBluetoothDialog by remember { mutableStateOf(false) }
    var showVehicleDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(40.dp)) {
        Text("CONFIGURAÇÕES DO SISTEMA", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            // Devices & Bluetooth
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("DISPOSITIVOS & CONEXÃO", color = TextDim, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                SettingsItem("Bluetooth", state.bluetoothDevice ?: "Desconectado") { 
                    onScanRequested()
                    showBluetoothDialog = true 
                }
                SettingsItem("Visualizar Dispositivos Pareados", "Total: ${state.pairedDevices.size}") {}
            }

            // Vehicle Fleet
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("GERENCIAR FROTA", color = TextDim, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                SettingsItem("Veículo Atual", state.carName) {}
                SettingsItem("Adicionar Novo Veículo", "Native Registration") { showVehicleDialog = true }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
        Text("SISTEMA", color = TextDim, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        SettingsItem("Software", "v2.4.1 (Stable Build)") {}
    }

    if (showBluetoothDialog) {
        AlertDialog(
            onDismissRequest = { 
                viewModel.stopBluetoothScan()
                showBluetoothDialog = false 
            },
            containerColor = BgCard,
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Conectar Bluetooth", color = Color.White)
                    Spacer(modifier = Modifier.weight(1f))
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = AccentBlue, strokeWidth = 2.dp)
                }
            },
            text = {
                Column(modifier = Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                    Text("Dispositivos Encontrados:", color = TextDim, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                    
                    if (discoveredDevices.isEmpty()) {
                        Text("Buscando dispositivos próximos...", color = Color.Gray, modifier = Modifier.padding(vertical = 20.dp))
                    }

                    discoveredDevices.forEach { device ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { 
                                viewModel.pairBluetooth(device.name, device.address)
                                viewModel.stopBluetoothScan()
                                showBluetoothDialog = false
                            }.padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(device.name, color = if(state.bluetoothDevice == device.name) AccentBlue else Color.White)
                                Text(device.address, color = Color.Gray, fontSize = 10.sp)
                            }
                            if(state.bluetoothDevice == device.name) Icon(Icons.Default.Check, null, tint = AccentBlue)
                        }
                        Divider(color = GridLine)
                    }
                }
            },
            confirmButton = { 
                TextButton(onClick = { 
                    viewModel.stopBluetoothScan()
                    showBluetoothDialog = false 
                }) { Text("FECHAR", color = AccentBlue) } 
            }
        )
    }

    if (showVehicleDialog) {
        var newVehicleName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showVehicleDialog = false },
            containerColor = BgCard,
            title = { Text("Cadastrar Novo Veículo", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = newVehicleName,
                    onValueChange = { newVehicleName = it },
                    label = { Text("Nome do Veículo") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = GridLine
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if(newVehicleName.isNotBlank()) {
                        viewModel.addNewVehicle(newVehicleName)
                        showVehicleDialog = false
                    }
                }) { Text("CADASTRAR", color = AccentBlue) }
            }
        )
    }
}

@Composable
fun SettingsItem(label: String, value: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().height(72.dp).background(BgCard).clickable { onClick() }.padding(24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.White, fontSize = 16.sp)
        Text(value, color = AccentBlue, fontSize = 16.sp)
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
fun Header(state: VehicleState, isSyncing: Boolean, onSyncClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .border(bottom = 1.dp, color = GridLine)
            .padding(horizontal = 40.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "BYD DOLPHIN PLUS",
                color = AccentBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                letterSpacing = 4.sp
            )
            if (state.cloudSyncStatus != null) {
                Text(state.cloudSyncStatus, color = Color.Gray, fontSize = 10.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
            StatusText("${state.targetTemperature}°C")
            
            IconButton(onClick = onSyncClick, enabled = !isSyncing) {
                if (isSyncing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = AccentBlue, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Sync, "Sync Cloud", tint = if (state.cloudSyncStatus != null) AccentBlue else TextDim)
                }
            }

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
fun NavigationFooter(currentTab: String, onTabSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(BgCard)
            .border(top = 1.dp, color = GridLine)
    ) {
        NavItem(Icons.Default.Home, "Home", currentTab == "HOME") { onTabSelect("HOME") }
        NavItem(Icons.Default.WbSunny, "Luzes", currentTab == "LUZES") { onTabSelect("LUZES") }
        NavItem(Icons.Default.Thermostat, "Clima", currentTab == "CLIMA") { onTabSelect("CLIMA") }
        NavItem(Icons.Default.EvStation, "Energia", currentTab == "ENERGIA") { onTabSelect("ENERGIA") }
        NavItem(Icons.Default.Settings, "Ajustes", currentTab == "AJUSTES") { onTabSelect("AJUSTES") }
    }
}

@Composable
fun RowScope.NavItem(icon: ImageVector, label: String, isActive: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(if (isActive) AccentBlue.copy(alpha = 0.05f) else Color.Transparent)
            .clickable { onClick() }
            .border(end = 1.dp, color = GridLine),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon, 
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = if (isActive) AccentBlue else TextDim
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(label.uppercase(), fontSize = 11.sp, color = if (isActive) AccentBlue else TextDim, fontWeight = FontWeight.Bold)
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
