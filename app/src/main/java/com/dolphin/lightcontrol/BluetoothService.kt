package com.dolphin.lightcontrol

import android.annotation.SuppressLint
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class BluetoothDeviceInfo(
    val name: String,
    val address: String
)

class BluetoothService(private val context: Context) {
    private val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    private val _discoveredDevices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())
    val discoveredDevices = _discoveredDevices.asStateFlow()

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    val name = it.name ?: "Dispositivo Desconhecido"
                    val address = it.address
                    val newDevice = BluetoothDeviceInfo(name, address)
                    
                    _discoveredDevices.update { currentList ->
                        if (currentList.none { d -> d.address == address }) {
                            currentList + newDevice
                        } else {
                            currentList
                        }
                    }
                }
            }
        }
    }

    private fun canScan(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermission(Manifest.permission.BLUETOOTH_SCAN)
        } else {
            hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    fun startScanning() {
        if (!canScan()) return
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return
        
        _discoveredDevices.update { emptyList() }
        
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(receiver, filter)
        bluetoothAdapter.startDiscovery()
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        if (!canScan()) return
        if (bluetoothAdapter?.isDiscovering == true) {
            bluetoothAdapter.cancelDiscovery()
        }
        try {
            context.unregisterReceiver(receiver)
        } catch (e: Exception) {
            // Ignorar se não registrado
        }
    }

    private val _connectionState = MutableStateFlow<String>("Desconectado")
    val connectionState = _connectionState.asStateFlow()

    @SuppressLint("MissingPermission")
    fun connectToDevice(address: String): Boolean {
        if (!canConnect()) return false
        val device = bluetoothAdapter?.getRemoteDevice(address)
        if (device != null) {
            _connectionState.value = "Conectando..."
            // Simular sucesso após pequeno delay para feedback visual
            _connectionState.value = "Conectado a ${device.name ?: address}"
            return true
        }
        _connectionState.value = "Falha na Conexão"
        return false
    }

    fun disconnect() {
        _connectionState.value = "Desconectado"
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun canConnect(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            true
        }
    }

    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDeviceInfo> {
        if (!canConnect()) return emptyList()
        return bluetoothAdapter?.bondedDevices?.map { 
            BluetoothDeviceInfo(it.name ?: "Unknown", it.address)
        } ?: emptyList()
    }
}
