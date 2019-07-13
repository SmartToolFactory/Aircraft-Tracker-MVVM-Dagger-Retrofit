package com.smarttoolfactory.opensky.api

import com.smarttoolfactory.opensky.api.response.AllStateVectorsResponse

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenSkyApi {


    /**
     * The state of an aircraft is a summary of all tracking information (mainly position, velocity, and identity) at a certain point in time.
     * These states of aircraft can be retrieved as state vectors in the form of a JSON object.
     *
     * @param latitudeMin  lamin lower bound for the latitude in decimal degrees
     * @param longitudeMin lomin lower bound for the longitude in decimal degrees
     * @param latitudeMax  lamax upper bound for the latitude in decimal degrees
     * @param longitudeMax lomax upper bound for the longitude in decimal degrees
     * @return states of aircraft in specified boundaries
     */
    @GET("states/all")
    fun getAllStateVectors(
            @Query("lomin") longitudeMin: String?,
            @Query("lamin") latitudeMin: String?,
            @Query("lomax") longitudeMax: String?,
            @Query("lamax") latitudeMax: String?
    ): Call<AllStateVectorsResponse>

    companion object {

        const val BASE_URL = "https://Y2FzZTAx:s3iQ2UUeknd27b7@opensky-network.org/api/"
    }


}
