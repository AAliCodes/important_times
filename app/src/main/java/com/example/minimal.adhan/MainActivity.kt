package com.example.minimal.adhan

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.example.minimal.adhan.data.DataStoreUserRepository
import com.example.minimal.adhan.data.UserRepository
import com.example.minimal.adhan.engine.PrayerTimesEngine
import com.example.minimal.adhan.ui.DashboardScreen
import com.example.minimal.adhan.ui.DashboardViewModel
import com.example.minimal.adhan.ui.DashboardViewModelFactory

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var userRepository: UserRepository
    private lateinit var engine: PrayerTimesEngine

    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModelFactory(userRepository, engine)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) fetchLocationAndSave()
        else Toast.makeText(this, "Location is required for accurate prayer times.", Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        userRepository = DataStoreUserRepository(this)
        engine = PrayerTimesEngine()

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            DashboardScreen(
                uiState = uiState,
                onRequestLocation = { requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) }
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocationAndSave() {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    lifecycleScope.launch {
                        userRepository.saveLocation(location.latitude, location.longitude)
                        Toast.makeText(this@MainActivity, "Location saved!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Could not determine location.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}