package com.dolphin.lightcontrol

import java.util.Date

data class VehicleState(
    val internalLights: Boolean = false,
    val connected: Boolean = true,
    val batteryLevel: Int = 84,
    val estimatedRange: Int = 358,
    val isLocked: Boolean = true,
    val airConditioningOn: Boolean = false,
    val targetTemperature: Int = 22,
    val lastSync: Date = Date(),
    val carName: String = "BYD Dolphin Plus"
)

class BYDService {
    private var state = VehicleState()
    
    // NOTA: Para controle REAL, você deve integrar com a API da BYD (Cloud) 
    // ou uma interface OBD-II. Este serviço atua como uma ponte (Bridge).
    // Exemplo: usar Retrofit para chamar https://api.byd.com/v1/vehicle/control

    suspend fun fetchData(): VehicleState {
        kotlinx.coroutines.delay(1200)
        return state
    }

    suspend fun toggleInternalLights(): Boolean {
        kotlinx.coroutines.delay(800)
        state = state.copy(internalLights = !state.internalLights)
        return state.internalLights
    }

    suspend fun toggleLock(): Boolean {
        kotlinx.coroutines.delay(1500)
        state = state.copy(isLocked = !state.isLocked)
        return state.isLocked
    }

    suspend fun toggleAirConditioning(): Boolean {
        kotlinx.coroutines.delay(2000)
        state = state.copy(airConditioningOn = !state.airConditioningOn)
        return state.airConditioningOn
    }

    suspend fun setTemperature(temp: Int): Int {
        kotlinx.coroutines.delay(500)
        state = state.copy(targetTemperature = temp)
        return state.targetTemperature
    }

    fun getState(): VehicleState = state
}
