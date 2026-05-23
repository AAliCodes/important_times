package za.co.amp.prayertimes

import android.app.Application

/**
 * Application subclass that holds singleton instances of all repositories, use cases,
 * and schedulers (manual dependency injection).
 *
 * WorkManager and ConnectivityChangeReceiver registration will be wired in task 13.
 */
class PrayerTimesApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Dependency wiring will be added in task 13
    }
}
