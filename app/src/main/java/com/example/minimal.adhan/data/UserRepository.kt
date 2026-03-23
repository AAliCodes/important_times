package com.example.minimal.adhan.data

import com.batoulapps.adhan.Coordinates
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getLocation(): Flow<Coordinates?>
    suspend fun saveLocation(latitude: Double, longitude: Double)
}