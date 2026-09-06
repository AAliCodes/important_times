package com.example.minimal.adhan.worker

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker
import com.example.minimal.adhan.data.DataStoreUserRepository
import com.example.minimal.adhan.ui.PrayerWidget
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await
import android.util.Log

class LocationUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): ListenableWorker.Result {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        val userRepository = DataStoreUserRepository(applicationContext)

        return try {
            // Attempt to get a fresh location fix
            val location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                null
            ).await()

            if (location != null) {
                userRepository.saveLocation(location.latitude, location.longitude)
                // Refresh all widgets immediately with the new data
                PrayerWidget().updateAll(applicationContext)
                Log.d("LocationUpdateWorker", "Successfully updated location: ${location.latitude}, ${location.longitude}")
                ListenableWorker.Result.success()
            } else {
                // If no fresh fix, try the last known location as a fallback
                val lastLocation = fusedLocationClient.lastLocation.await()
                if (lastLocation != null) {
                    userRepository.saveLocation(lastLocation.latitude, lastLocation.longitude)
                    PrayerWidget().updateAll(applicationContext)
                    ListenableWorker.Result.success()
                } else {
                    ListenableWorker.Result.retry()
                }
            }
        } catch (e: SecurityException) {
            Log.e("LocationUpdateWorker", "Location permission missing", e)
            ListenableWorker.Result.failure()
        } catch (e: Exception) {
            Log.e("LocationUpdateWorker", "Error updating location", e)
            ListenableWorker.Result.retry()
        }
    }
}