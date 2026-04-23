package com.dolphin.lightcontrol

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class WifiService(private val context: Context) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val _wifiState = MutableStateFlow<String>("Desconectado")
    val wifiState = _wifiState.asStateFlow()

    fun updateWifiStatus() {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        
        if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            val info = wifiManager.connectionInfo
            val ssid = info.ssid.removeSurrounding("\"")
            _wifiState.value = "Conectado: $ssid"
        } else {
            _wifiState.value = "Desconectado"
        }
    }
}
