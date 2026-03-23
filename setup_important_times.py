import os

project_files = {
    # 1. Configuration Files
    "minimal.adhan/app/build.gradle.kts": """
dependencies {
    // Add these to your existing dependencies block:
    implementation("com.batoulapps.adhan:adhan-java:1.2.2")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    
    // Test dependencies
    testImplementation("junit:junit:4.13.2")
}
""",

    "minimal.adhan/app/src/main/AndroidManifest.xml": """
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.minimal.adhan">
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <application>
        </application>
</manifest>
""",

    # 2. Utilities and Engine
    "minimal.adhan/app/src/main/java/com/example/minimal.adhan/utils/TimeUtils.kt": """
package com.example.minimal.adhan.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Date.formatToTime(): String {
    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return formatter.format(this)
}
""",

    "minimal.adhan/app/src/main/java/com/example/minimal.adhan/engine/PrayerTimesEngine.kt": """
package com.example.minimal.adhan.engine

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.DateComponents
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes

class PrayerTimesEngine {
    fun calculatePrayerTimes(latitude: Double, longitude: Double, date: DateComponents): PrayerTimes {
        val coordinates = Coordinates(latitude, longitude)
        val parameters = CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
        parameters.madhab = Madhab.SHAFI
        return PrayerTimes(coordinates, date, parameters)
    }
}
""",

    # 3. Data Storage
    "minimal.adhan/app/src/main/java/com/example/minimal.adhan/data/UserRepository.kt": """
package com.example.minimal.adhan.data

import com.batoulapps.adhan.Coordinates
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getLocation(): Flow<Coordinates?>
    suspend fun saveLocation(latitude: Double, longitude: Double)
}
""",

    "minimal.adhan/app/src/main/java/com/example/minimal.adhan/data/DataStoreUserRepository.kt": """
package com.example.minimal.adhan.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.batoulapps.adhan.Coordinates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class DataStoreUserRepository(private val context: Context) : UserRepository {
    companion object {
        val LATITUDE_KEY = doublePreferencesKey("latitude")
        val LONGITUDE_KEY = doublePreferencesKey("longitude")
    }

    override fun getLocation(): Flow<Coordinates?> {
        return context.dataStore.data.map { preferences ->
            val lat = preferences[LATITUDE_KEY]
            val lon = preferences[LONGITUDE_KEY]
            if (lat != null && lon != null) Coordinates(lat, lon) else null
        }
    }

    override suspend fun saveLocation(latitude: Double, longitude: Double) {
        context.dataStore.edit { preferences ->
            preferences[LATITUDE_KEY] = latitude
            preferences[LONGITUDE_KEY] = longitude
        }
    }
}
""",

    # 4. User Interface
    "minimal.adhan/app/src/main/java/com/example/minimal.adhan/ui/DashboardViewModel.kt": """
package com.example.minimal.adhan.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.batoulapps.adhan.DateComponents
import com.example.minimal.adhan.data.UserRepository
import com.example.minimal.adhan.engine.PrayerTimesEngine
import com.example.minimal.adhan.utils.formatToTime
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
""",

    "minimal.adhan/app/src/main/java/com/example/minimal.adhan/ui/DashboardScreen.kt": """
package com.example.minimal.adhan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardScreen(uiState: DashboardUiState, onRequestLocation: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator()
                !uiState.hasLocation -> {
                    Text("Location required to calculate precise prayer times.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRequestLocation) { Text("Enable Location") }
                }
                else -> {
                    Text("Today", fontSize = 28.sp, fontWeight = FontWeight.Light, modifier = Modifier.padding(bottom = 32.dp))
                    LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(uiState.prayerTimes) { prayer ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(prayer.first, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                Text(prayer.second, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}
""",

    # 5. The Entry Point
    "minimal.adhan/app/src/main/java/com/example/minimal.adhan/MainActivity.kt": """
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
""",

    # 6. Test Cases
    "minimal.adhan/app/src/test/java/com/example/minimal.adhan/engine/PrayerTimesEngineTest.kt": """
package com.example.minimal.adhan.engine

import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.DateComponents
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PrayerTimesEngineTest {

    @Test
    fun `calculatePrayerTimes returns valid times for known date and location`() {
        val engine = PrayerTimesEngine()
        val coordinates = Coordinates(21.4225, 39.8262) // Mecca
        val fixedDate = DateComponents(2026, 3, 22) 

        val prayerTimes = engine.calculatePrayerTimes(
            latitude = coordinates.latitude,
            longitude = coordinates.longitude,
            date = fixedDate
        )

        assertNotNull("Fajr time should not be null", prayerTimes.fajr)
        assertNotNull("Dhuhr time should not be null", prayerTimes.dhuhr)
        assertTrue(
            "Fajr should be before Dhuhr",
            prayerTimes.fajr.before(prayerTimes.dhuhr)
        )
    }
}
""",

    "minimal.adhan/app/src/test/java/com/example/minimal.adhan/utils/TimeUtilsTest.kt": """
package com.example.minimal.adhan.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class TimeUtilsTest {

    @Test
    fun `formatToTime returns correctly formatted 12h string`() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 30)
        }
        
        val formattedTime = calendar.time.formatToTime()
        
        assertEquals("02:30 PM", formattedTime)
    }
}
"""
}

# Generate the directory tree and write the files
for file_path, content in project_files.items():
    os.makedirs(os.path.dirname(file_path), exist_ok=True)
    with open(file_path, "w") as f:
        f.write(content.strip())

print("✅ Success! All files (including tests) have been generated in the 'minimal.adhan' directory.")