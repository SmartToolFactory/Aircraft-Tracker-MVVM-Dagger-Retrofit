package com.smarttoolfactory.opensky.model

/**
 * Data class for keeping Aircraft properties as vector state
 */
data class AircraftVectorState(
        val icao24: String, // 0 in response
        val originCountry: String, // 2 in response
        val latitude: Double, // 6 in response
        val longitude: Double, // 5 in response
        val trueTrack: Float? // 10 in response
)

