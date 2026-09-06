package com.example.minimal.adhan.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.data.DateComponents
import com.example.minimal.adhan.data.UserRepository
import com.example.minimal.adhan.utils.formatToTime
import com.example.minimal.adhan.data.*
import com.example.minimal.adhan.engine.*
import com.example.minimal.adhan.utils.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Date

data class DashboardUiState(
    val isLoading: Boolean = true,
    val hasLocation: Boolean = false,
    val madhab: Madhab = Madhab.SHAFI,
    val prayerTimes: List<Pair<String, String>> = emptyList()
)

class DashboardViewModel(
    private val userRepository: UserRepository,
    private val engine: PrayerTimesEngine
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // Combine location and madhab flows to update UI whenever either changes
            combine(
                userRepository.getLocation(),
                userRepository.getMadhab()
            ) { coordinates, madhab ->
                if (coordinates != null) {
                    val times = engine.calculatePrayerTimes(
                        coordinates.latitude, coordinates.longitude, DateComponents.from(Date()), madhab
                    )
                    val formattedTimes = listOf(
                        "Fajr" to times.fajr.formatToTime(),
                        "Sunrise" to times.sunrise.formatToTime(),
                        "Dhuhr" to times.dhuhr.formatToTime(),
                        "Asr" to times.asr.formatToTime(),
                        "Maghrib" to times.maghrib.formatToTime(),
                        "Isha" to times.isha.formatToTime()
                    )
                    DashboardUiState(false, true, madhab, formattedTimes)
                } else {
                    DashboardUiState(false, false, madhab)
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun toggleMadhab() {
        viewModelScope.launch {
            val current = _uiState.value.madhab
            val next = if (current == Madhab.SHAFI) Madhab.HANAFI else Madhab.SHAFI
            userRepository.saveMadhab(next)
        }
    }
}

class DashboardViewModelFactory(
    private val userRepository: UserRepository,
    private val engine: PrayerTimesEngine
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DashboardViewModel(userRepository, engine) as T
    }
}