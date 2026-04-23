package com.dolphin.lightcontrol

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BYDViewModel(application: Application) : AndroidViewModel(application) {
    private val service = BYDService()
    private val bluetoothService = BluetoothService(application)
    
    private val _uiState = MutableStateFlow(VehicleUIState())
    val uiState: StateFlow<VehicleUIState> = _uiState.asStateFlow()

    val discoveredBluetoothDevices = bluetoothService.discoveredDevices

    init {
        refreshState()
        updatePairedDevices()
    }

    private fun refreshState() {
        val currentState = service.getState()
        _uiState.update { it.copy(vehicleState = currentState) }
    }

    fun updatePairedDevices() {
        viewModelScope.launch {
            val paired = bluetoothService.getPairedDevices().map { it.name }
            service.updatePairedDevices(paired)
            refreshState()
        }
    }

    fun login(user: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(cloudSyncStatus = "Sincronizando...") }
            val success = service.login(user, pass)
            if (success) {
                _uiState.update { it.copy(cloudSyncStatus = "Conectado ao Cloud") }
                refreshState()
            } else {
                _uiState.update { it.copy(cloudSyncStatus = "Erro na autenticação") }
            }
        }
    }

    fun startBluetoothScan() {
        bluetoothService.startScanning()
    }

    fun stopBluetoothScan() {
        bluetoothService.stopScanning()
    }

    fun pairBluetooth(device: String, address: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isToggling = true) }
            if (address != null) {
                bluetoothService.connectToDevice(address)
            }
            service.connectBluetooth(device)
            refreshState()
            _uiState.update { it.copy(isToggling = false) }
        }
    }

    fun addNewVehicle(name: String) {
        performAction { service.registerVehicle(name) }
    }

    fun toggleLights() {
        performAction { service.toggleInternalLights() }
    }

    fun toggleLock() {
        performAction { service.toggleLock() }
    }

    fun toggleAC() {
        performAction { service.toggleAC() }
    }

    fun updateTemp(temp: Int) {
        performAction { service.setTemperature(temp) }
    }

    fun updateLightZone(zone: String, value: Boolean) {
        performAction { service.updateLightZone(zone, value) }
    }

    fun setFanSpeed(speed: Int) {
        performAction { service.setFanSpeed(speed) }
    }

    fun toggleRecirculation() {
        performAction { service.toggleRecirculation() }
    }

    fun toggleDefrost() {
        performAction { service.toggleDefrost() }
    }

    fun toggleAutoHeadlights() {
        performAction { service.toggleAutoHeadlights() }
    }

    fun toggleRearFog() {
        performAction { service.toggleRearFog() }
    }

    fun selectTab(tab: String) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun syncWithCloud() {
        val apiKey = System.getenv("GEMINI_API_KEY") ?: ""
        if (apiKey.isBlank()) {
            _uiState.update { it.copy(vehicleState = it.vehicleState.copy(cloudSyncStatus = "Error: API Key Missing")) }
            return
        }

        val cloudSyncService = CloudSyncService(apiKey)
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            try {
                val result = cloudSyncService.syncVehicleData(_uiState.value.vehicleState)
                service.updateCloudSyncStatus(result)
                refreshState()
            } catch (e: Exception) {
                service.updateCloudSyncStatus("Error: ${e.message}")
                refreshState()
            } finally {
                _uiState.update { it.copy(isSyncing = false) }
            }
        }
    }

    private fun performAction(action: suspend () -> Unit) {
        if (_uiState.value.isToggling) return
        viewModelScope.launch {
            _uiState.update { it.copy(isToggling = true) }
            try {
                action()
                refreshState()
            } catch (e: Exception) {
                // Notificar erro UI
            } finally {
                _uiState.update { it.copy(isToggling = false) }
            }
        }
    }
}

data class VehicleUIState(
    val vehicleState: VehicleState = VehicleState(),
    val isToggling: Boolean = false,
    val isSyncing: Boolean = false,
    val currentTab: String = "LUZES"
)
