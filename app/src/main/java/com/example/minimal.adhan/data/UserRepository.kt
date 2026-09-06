package com.example.minimal.adhan.data

import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getLocation(): Flow<Coordinates?>
    suspend fun saveLocation(latitude: Double, longitude: Double)
    
    fun getMadhab(): Flow<Madhab>
    suspend fun saveMadhab(madhab: Madhab)
}