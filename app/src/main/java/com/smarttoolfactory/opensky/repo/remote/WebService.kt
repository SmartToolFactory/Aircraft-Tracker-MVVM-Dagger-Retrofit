package com.smarttoolfactory.opensky.repo.remote


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.smarttoolfactory.opensky.api.OpenSkyApi
import com.smarttoolfactory.opensky.api.request.AllStateVectorRequest
import com.smarttoolfactory.opensky.api.response.AllStateVectorsResponse
import com.smarttoolfactory.opensky.model.AircraftVectorState
import com.smarttoolfactory.opensky.repo.Resource
import com.smarttoolfactory.opensky.utils.AppExecutors
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WebService uses OpenSky interface to request make api calls and retrieve data from api
 */

@Singleton
class WebService @Inject
constructor() {

    @Inject
    lateinit var openSkyApi: OpenSkyApi

    @Inject
    lateinit var appExecutors: AppExecutors


    /**
     * This method returns a Resource file that contains network states such as LOADING, SUCCESS and FAIL, also
     * stores data of aircraft vectors as a list of [AircraftVectorState]
     */
    fun getAircraftVectorStateList(request: AllStateVectorRequest): LiveData<Resource<List<AircraftVectorState>>> {

        val listAirCraftVectorState = MutableLiveData<Resource<List<AircraftVectorState>>>()

        listAirCraftVectorState.postValue(Resource.loading(null))


        val call = openSkyApi.getAllStateVectors(
            request.longitudeMin,
            request.latitudeMin,
            request.longitudeMax,
            request.latitudeMax
        )

        call.enqueue(object : Callback<AllStateVectorsResponse> {

            override fun onResponse(call: Call<AllStateVectorsResponse>, response: Response<AllStateVectorsResponse>) {

                if (response.body() == null) {
                    listAirCraftVectorState.postValue(Resource.error("Error", null))
                } else {

                    // Threading wiwh AppExecutors
                    appExecutors.networkIO().execute {
                        println("WebService appExecutors thread ${Thread.currentThread().name}")
                        parseNetworkResponse(response, listAirCraftVectorState)
                    }
                }

            }

            override fun onFailure(call: Call<AllStateVectorsResponse>, t: Throwable) {
                listAirCraftVectorState.setValue(Resource.error(t.message!!, null))
            }
        })

        return listAirCraftVectorState

    }

    private fun parseNetworkResponse(
        response: Response<AllStateVectorsResponse>,
        listAirCraftVectorState: MutableLiveData<Resource<List<AircraftVectorState>>>
    ) {

        val aircraftVectorStateList = ArrayList<AircraftVectorState>()
        val vectorLists = response.body()!!.states

        vectorLists?.let {


            for (vectors in vectorLists) {

                val icao24: String = vectors[0]
                val originCountry: String = vectors[2]
                val latitude: Double = vectors[6].toDouble()
                val longitude: Double = vectors[5].toDouble()


                var trueTrack: Float? = null

                vectors[10]?.apply {
                    trueTrack = try {
                        vectors[10].toFloatOrNull()
                    } catch (e: NumberFormatException) {
                        e.printStackTrace()
                        0f
                    }
                }


                val aircraftVectorState = AircraftVectorState(icao24, originCountry, latitude, longitude, trueTrack)
                aircraftVectorStateList.add(aircraftVectorState)

            }

            listAirCraftVectorState.postValue(Resource.success(aircraftVectorStateList))
        }
    }


}
