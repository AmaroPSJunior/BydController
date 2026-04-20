package com.dolphin.lightcontrol

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BYDViewModel : ViewModel() {
    private val service = BYDService()
    
    private val _uiState = MutableStateFlow(VehicleUIState())
    val uiState: StateFlow<VehicleUIState> = _uiState.asStateFlow()

    init {
        refreshState()
    }

    private fun refreshState() {
        val currentState = service.getState()
        _uiState.update { it.copy(vehicleState = currentState) }
    }

    fun toggleLights() {
        if (_uiState.value.isToggling) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isToggling = true) }
            try {
                service.toggleInternalLights()
                refreshState()
            } catch (e: Exception) {
                // Tratar erro
            } finally {
                _uiState.update { it.copy(isToggling = false) }
            }
        }
    }
}

data class VehicleUIState(
    val vehicleState: VehicleState = VehicleState(),
    val isToggling: Boolean = false
)
