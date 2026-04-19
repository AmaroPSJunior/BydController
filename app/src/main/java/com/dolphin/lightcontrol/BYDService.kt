package com.dolphin.lightcontrol

import java.util.Date

data class VehicleState(
    val internalLights: Boolean = false,
    val connected: Boolean = true,
    val batteryLevel: Int = 84,
    val lastSync: Date = Date()
)

class BYDService {
    private var state = VehicleState()

    suspend fun toggleInternalLights(): Boolean {
        // Simular latência de rede
        kotlinx.coroutines.delay(800)
        
        state = state.copy(
            internalLights = !state.internalLights,
            lastSync = Date()
        )
        return state.internalLights
    }

    fun getState(): VehicleState = state
}
