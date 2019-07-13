package com.smarttoolfactory.opensky.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

import com.smarttoolfactory.opensky.api.request.AllStateVectorRequest
import com.smarttoolfactory.opensky.model.AircraftVectorState
import com.smarttoolfactory.opensky.repo.AircraftRepository
import com.smarttoolfactory.opensky.repo.Resource

import javax.inject.Inject

class AircraftViewModel @Inject
constructor(aircraftRepository: AircraftRepository) : ViewModel() {


    /**
     * Mutable data that stores latest [AllStateVectorRequest] in given latitude and longitude boundaries.
     */
    private val requestMutableLiveData = MutableLiveData<AllStateVectorRequest>()


    /**
     * Wrapper class for data fetch status, data, and status messages for all air vectors
     */

    val allStateVectors: LiveData<Resource<List<AircraftVectorState>>>


    /**
     * Mutable LiveData for storing loading state
     */
    val loading = MutableLiveData<Boolean>()


    /**
     *  Filter for searching flights
     */

    var flightFilter = MutableLiveData<String>()


    /**
     * LiveData to stop refresh when user is searching
     */
    var stopFlightRefreshTimer = MutableLiveData<Boolean>()


    init {
        allStateVectors = Transformations.switchMap(requestMutableLiveData) {
            aircraftRepository.getAircraftVectorStateResponse(it)
        }
    }


    /**
     * Changes query data to get state vectors for aircrafts from REST api via [AircraftRepository]
     */
    fun queryAllStateVectors(request: AllStateVectorRequest) {
        requestMutableLiveData.value = request
    }

    fun filterFlightsByOriginCountry(filter:String?) {
        flightFilter.value = filter
    }


    // Use this method if CompositeDisposable is used inside ViewModel
    override fun onCleared() {
        super.onCleared()
    }
}

