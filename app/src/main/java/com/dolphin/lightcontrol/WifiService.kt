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
        try {
            if (context.checkSelfPermission(android.Manifest.permission.ACCESS_NETWORK_STATE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                _wifiState.value = "Sem Permissão"
                return
            }
            
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            
            if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                val info = wifiManager.connectionInfo
                val ssid = if (info.ssid != "<unknown ssid>") info.ssid.removeSurrounding("\"") else "WiFi Conectado"
                _wifiState.value = "Conectado: $ssid"
            } else {
                _wifiState.value = "Desconectado"
            }
        } catch (e: Exception) {
            _wifiState.value = "Erro ao ler WiFi"
        }
    }
}
