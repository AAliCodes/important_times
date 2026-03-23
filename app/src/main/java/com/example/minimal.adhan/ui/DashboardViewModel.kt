package com.example.minimal.adhan.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.batoulapps.adhan.data.DateComponents
import com.example.minimal.adhan.data.UserRepository
import com.example.minimal.adhan.utils.formatToTime
import com.example.minimal.adhan.data.*
import com.example.minimal.adhan.engine.*
import com.example.minimal.adhan.utils.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

data class DashboardUiState(
    val isLoading: Boolean = true,
    val hasLocation: Boolean = false,
    val prayerTimes: List<Pair<String, String>> = emptyList()
)

class DashboardViewModel(
    private val userRepository: UserRepository,
    private val engine: PrayerTimesEngine
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init { loadDailyPrayers() }

    private fun loadDailyPrayers() {
        viewModelScope.launch {
            userRepository.getLocation().collect { coordinates ->
                if (coordinates != null) {
                    val times = engine.calculatePrayerTimes(
                        coordinates.latitude, coordinates.longitude, DateComponents.from(Date())
                    )
                    val formattedTimes = listOf(
                        "Fajr" to times.fajr.formatToTime(),
                        "Sunrise" to times.sunrise.formatToTime(),
                        "Dhuhr" to times.dhuhr.formatToTime(),
                        "Asr" to times.asr.formatToTime(),
                        "Maghrib" to times.maghrib.formatToTime(),
                        "Isha" to times.isha.formatToTime()
                    )
                    _uiState.value = DashboardUiState(false, true, formattedTimes)
                } else {
                    _uiState.value = DashboardUiState(false, false)
                }
            }
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