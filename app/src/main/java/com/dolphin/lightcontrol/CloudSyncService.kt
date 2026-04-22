package com.dolphin.lightcontrol

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content

class CloudSyncService(private val apiKey: String) {
    private val model = GenerativeModel(
        modelName = "gemini-3-flash-preview",
        apiKey = apiKey
    )

    suspend fun syncVehicleData(state: VehicleState): String {
        val prompt = """
            Analyze the following BYD vehicle state and provide a one-sentence "Cloud Sync" summary 
            confirming the status and suggesting one optimization if any.
            Vehicle State:
            - Battery: ${state.batteryLevel}%
            - Range: ${state.estimatedRange}km
            - AC: ${if (state.airConditioningOn) "On" else "Off"}
            - Temperature: ${state.targetTemperature}°C
            - Locked: ${state.isLocked}
            Format the response as: "Sync OK: [Summary]"
        """.trimIndent()

        return try {
            val response = model.generateContent(prompt)
            response.text ?: "Sync Failed: Empty Response"
        } catch (e: Exception) {
            "Sync Error: ${e.message}"
        }
    }
}
