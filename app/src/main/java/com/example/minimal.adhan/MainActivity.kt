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
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.example.minimal.adhan.data.DataStoreUserRepository
import com.example.minimal.adhan.data.UserRepository
import com.example.minimal.adhan.engine.PrayerTimesEngine
import com.example.minimal.adhan.ui.DashboardScreen
import com.example.minimal.adhan.ui.DashboardViewModel
import com.example.minimal.adhan.ui.DashboardViewModelFactory
import com.example.minimal.adhan.ui.PrayerWidget
import com.example.minimal.adhan.worker.LocationUpdateWorker

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

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
        if (isGranted) {
            fetchLocationAndSave()
            scheduleLocationUpdates()
        } else {
            Toast.makeText(this, "Location is required for accurate prayer times.", Toast.LENGTH_LONG).show()
        }
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
                onRequestLocation = { requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
                onToggleMadhab = {
                    viewModel.toggleMadhab()
                    lifecycleScope.launch {
                        PrayerWidget().updateAll(this@MainActivity)
                    }
                },
                onRefreshLocation = {
                    fetchLocationAndSave()
                }
            )
        }
        
        // Schedule updates if permission is already granted
        scheduleLocationUpdates()
    }

    private fun scheduleLocationUpdates() {
        val workRequest = PeriodicWorkRequestBuilder<LocationUpdateWorker>(6, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "location_update",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocationAndSave() {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    lifecycleScope.launch {
                        userRepository.saveLocation(location.latitude, location.longitude)
                        PrayerWidget().updateAll(this@MainActivity)
                        Toast.makeText(this@MainActivity, "Location updated!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Could not get current location. Check if GPS is on.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to get location: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}