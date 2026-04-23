package com.dolphin.lightcontrol

import retrofit2.Response
import retrofit2.http.*

interface BYDApi {
    @POST("user/v2/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("car/v1/vehicles")
    suspend fun getVehicles(@Header("Authorization") token: String): Response<List<VehicleInfo>>

    @GET("car/v1/vehicles/{vin}/state")
    suspend fun getVehicleState(
        @Header("Authorization") token: String,
        @Path("vin") vin: String
    ): Response<VehicleStateResponse>

    @POST("car/v1/vehicles/{vin}/remote-control")
    suspend fun sendCommand(
        @Header("Authorization") token: String,
        @Path("vin") vin: String,
        @Body request: CommandRequest
    ): Response<CommandResponse>
}

data class LoginRequest(val username: String, val password: String, val grant_type: String = "password")
data class LoginResponse(val access_token: String, val refresh_token: String, val expires_in: Int)
data class VehicleInfo(val vin: String, val model: String, val name: String)

data class VehicleStateResponse(
    val soc: Int, // State of Charge
    val range: Int,
    val is_locked: Boolean,
    val ac_on: Boolean,
    val target_temp: Int,
    val last_update: Long
)

data class CommandRequest(val command: String, val value: Any? = null)
data class CommandResponse(val success: Boolean, val message: String?)
