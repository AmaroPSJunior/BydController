package com.dolphin.lightcontrol

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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

    @SuppressLint("MissingPermission")
    fun startScanning() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return
        
        _discoveredDevices.update { emptyList() }
        
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(receiver, filter)
        bluetoothAdapter.startDiscovery()
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        if (bluetoothAdapter?.isDiscovering == true) {
            bluetoothAdapter.cancelDiscovery()
        }
        try {
            context.unregisterReceiver(receiver)
        } catch (e: Exception) {
            // Ignorar se não registrado
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(address: String): Boolean {
        val device = bluetoothAdapter?.getRemoteDevice(address)
        // Aqui seria a lógica real de conexão GATT ou Socket
        // Para o MVP, simulamos o pareamento de sucesso
        return device != null
    }

    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDeviceInfo> {
        return bluetoothAdapter?.bondedDevices?.map { 
            BluetoothDeviceInfo(it.name ?: "Unknown", it.address)
        } ?: emptyList()
    }
}
