package com.dolphin.lightcontrol

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
    val cloudSyncStatus: String? = null
)

class BYDService {
    private var state = VehicleState()
    
    suspend fun fetchData(): VehicleState {
        kotlinx.coroutines.delay(800)
        return state
    }

    suspend fun toggleInternalLights(): Boolean {
        kotlinx.coroutines.delay(400)
        state = state.copy(internalLights = !state.internalLights)
        return state.internalLights
    }

    suspend fun updateLightZone(zone: String, value: Boolean) {
        kotlinx.coroutines.delay(200)
        state = when(zone) {
            "front" -> state.copy(frontLights = value)
            "rear" -> state.copy(rearLightsZone = value)
            "trunk" -> state.copy(trunkLights = value)
            "reading" -> state.copy(readingLights = value)
            else -> state
        }
    }

    suspend fun toggleLock(): Boolean {
        kotlinx.coroutines.delay(500)
        state = state.copy(isLocked = !state.isLocked)
        return state.isLocked
    }

    suspend fun toggleAC(): Boolean {
        kotlinx.coroutines.delay(600)
        state = state.copy(airConditioningOn = !state.airConditioningOn)
        return state.airConditioningOn
    }

    suspend fun setTemperature(temp: Int) {
        kotlinx.coroutines.delay(100)
        state = state.copy(targetTemperature = temp)
    }

    suspend fun setFanSpeed(speed: Int) {
        kotlinx.coroutines.delay(100)
        state = state.copy(fanSpeed = speed)
    }

    suspend fun toggleRecirculation() {
        kotlinx.coroutines.delay(200)
        state = state.copy(recirculation = !state.recirculation)
    }

    suspend fun toggleDefrost() {
        kotlinx.coroutines.delay(200)
        state = state.copy(defrost = !state.defrost)
    }

    suspend fun toggleAutoHeadlights() {
        state = state.copy(autoHeadlightsOn = !state.autoHeadlightsOn)
    }

    suspend fun toggleRearFog() {
        state = state.copy(rearFogOn = !state.rearFogOn)
    }

    suspend fun connectBluetooth(deviceName: String) {
        kotlinx.coroutines.delay(2000)
        state = state.copy(bluetoothDevice = deviceName)
    }

    suspend fun registerVehicle(name: String) {
        kotlinx.coroutines.delay(1000)
        state = state.copy(registeredVehicles = state.registeredVehicles + name)
    }

    suspend fun updateCloudSyncStatus(status: String?) {
        state = state.copy(cloudSyncStatus = status)
    }

    fun getState(): VehicleState = state
}
