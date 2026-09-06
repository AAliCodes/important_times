package com.example.minimal.adhan.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class DataStoreUserRepository(private val context: Context) : UserRepository {
    companion object {
        val LATITUDE_KEY = doublePreferencesKey("latitude")
        val LONGITUDE_KEY = doublePreferencesKey("longitude")
        val MADHAB_KEY = stringPreferencesKey("madhab")
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

    override fun getMadhab(): Flow<Madhab> {
        return context.dataStore.data.map { preferences ->
            val madhabName = preferences[MADHAB_KEY] ?: Madhab.SHAFI.name
            try {
                Madhab.valueOf(madhabName)
            } catch (e: Exception) {
                Madhab.SHAFI
            }
        }
    }

    override suspend fun saveMadhab(madhab: Madhab) {
        context.dataStore.edit { preferences ->
            preferences[MADHAB_KEY] = madhab.name
        }
    }
}