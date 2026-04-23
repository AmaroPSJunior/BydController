package com.dolphin.lightcontrol

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Date

data class VehicleState(
    val internalLights: Boolean = false,
    val frontLights: Boolean = true,
    val rearLightsZone: Boolean = false,
    val trunkLights: Boolean = false,
    val readingLights: Boolean = false,
    val connected: Boolean = true,
    val batteryLevel: Int = 84,
    val estimatedRange: Int = 358,
    val isLocked: Boolean = true,
    val airConditioningOn: Boolean = false,
    val targetTemperature: Int = 18,
    val fanSpeed: Int = 3,
    val recirculation: Boolean = true,
    val defrost: Boolean = false,
    val autoHeadlightsOn: Boolean = true,
    val rearFogOn: Boolean = false,
    val lastSync: Date = Date(),
    val carName: String = "BYD Dolphin Plus",
    val bluetoothDevice: String? = "iPhone de Arcamos",
    val pairedDevices: List<String> = listOf("iPhone de Arcamos", "Samsung S23", "Central BYD"),
    val registeredVehicles: List<String> = listOf("BYD Dolphin Plus", "BYD Seal"),
    val cloudSyncStatus: String? = null,
    val isWifiConnected: Boolean = false,
    val bluetoothStatus: String = "Desconectado"
)

class BYDService {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api-sg.byd.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(BYDApi::class.java)
    
    private var authToken: String? = null
    private var currentVin: String? = null
    private var state = VehicleState()
    
    suspend fun login(user: String, pass: String): Boolean {
        return try {
            val response = api.login(LoginRequest(user, pass))
            if (response.isSuccessful) {
                authToken = "Bearer ${response.body()?.access_token}"
                fetchVehicles()
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun fetchVehicles() {
        authToken?.let { token ->
            val response = api.getVehicles(token)
            if (response.isSuccessful) {
                val vehicles = response.body()
                currentVin = vehicles?.firstOrNull()?.vin
                state = state.copy(
                    carName = vehicles?.firstOrNull()?.name ?: "Meu BYD",
                    registeredVehicles = vehicles?.map { it.name } ?: emptyList()
                )
            }
        }
    }

    suspend fun fetchData(): VehicleState {
        val token = authToken
        val vin = currentVin
        if (token != null && vin != null) {
            try {
                val response = api.getVehicleState(token, vin)
                if (response.isSuccessful) {
                    val cloudState = response.body()
                    cloudState?.let {
                        state = state.copy(
                            batteryLevel = it.soc,
                            estimatedRange = it.range,
                            isLocked = it.is_locked,
                            airConditioningOn = it.ac_on,
                            targetTemperature = it.target_temp,
                            lastSync = Date(it.last_update)
                        )
                    }
                }
            } catch (e: Exception) {
                // Manter estado local se falhar
            }
        }
        return state
    }

    private suspend fun sendCommand(cmd: String, value: Any? = null) {
        val token = authToken
        val vin = currentVin
        if (token != null && vin != null) {
            api.sendCommand(token, vin, CommandRequest(cmd, value))
        } else if (state.bluetoothStatus.startsWith("Conectado")) {
            // Mock local Bluetooth command
            println("Sending command via Bluetooth: $cmd")
        } else if (state.isWifiConnected) {
            // Mock local Wi-Fi command
            println("Sending command via Wi-Fi: $cmd")
        }
    }

    suspend fun toggleInternalLights(): Boolean {
        sendCommand("LIGHTS_INTERNAL_TOGGLE")
        state = state.copy(internalLights = !state.internalLights)
        return state.internalLights
    }

    suspend fun updateLightZone(zone: String, value: Boolean) {
        sendCommand("LIGHTS_ZONE_UPDATE", mapOf("zone" to zone, "state" to value))
        state = when(zone) {
            "front" -> state.copy(frontLights = value)
            "rear" -> state.copy(rearLightsZone = value)
            "trunk" -> state.copy(trunkLights = value)
            "reading" -> state.copy(readingLights = value)
            else -> state
        }
    }

    suspend fun toggleLock(): Boolean {
        sendCommand(if (state.isLocked) "UNLOCK" else "LOCK")
        state = state.copy(isLocked = !state.isLocked)
        return state.isLocked
    }

    suspend fun toggleAC(): Boolean {
        sendCommand(if (state.airConditioningOn) "AC_OFF" else "AC_ON")
        state = state.copy(airConditioningOn = !state.airConditioningOn)
        return state.airConditioningOn
    }

    suspend fun setTemperature(temp: Int) {
        sendCommand("SET_TEMP", temp)
        state = state.copy(targetTemperature = temp)
    }

    suspend fun setFanSpeed(speed: Int) {
        sendCommand("SET_FAN_SPEED", speed)
        state = state.copy(fanSpeed = speed)
    }

    suspend fun toggleRecirculation() {
        sendCommand("RECIRCULATION_TOGGLE")
        state = state.copy(recirculation = !state.recirculation)
    }

    suspend fun toggleDefrost() {
        sendCommand("DEFROST_TOGGLE")
        state = state.copy(defrost = !state.defrost)
    }

    suspend fun toggleAutoHeadlights() {
        sendCommand("AUTO_HEADLIGHTS_TOGGLE")
        state = state.copy(autoHeadlightsOn = !state.autoHeadlightsOn)
    }

    suspend fun toggleRearFog() {
        sendCommand("REAR_FOG_TOGGLE")
        state = state.copy(rearFogOn = !state.rearFogOn)
    }

    suspend fun connectBluetooth(status: String) {
        state = state.copy(bluetoothStatus = status)
    }

    suspend fun updateWifiStatus(status: String) {
        state = state.copy(isWifiConnected = status.startsWith("Conectado"))
    }

    suspend fun registerVehicle(name: String) {
        state = state.copy(registeredVehicles = state.registeredVehicles + name)
    }

    suspend fun updateCloudSyncStatus(status: String?) {
        state = state.copy(cloudSyncStatus = status)
    }

    suspend fun updatePairedDevices(devices: List<String>) {
        state = state.copy(pairedDevices = devices)
    }

    fun getState(): VehicleState {
        // Simular consumo/recuperação leve se não estiver sincronizado com nuvem oficial
        if (authToken == null) {
            val drift = (Math.random() * 2 - 1).toInt() // -1, 0 ou 1
            val newRange = (state.estimatedRange + drift).coerceIn(100, 500)
            state = state.copy(estimatedRange = newRange)
        }
        return state
    }
}
