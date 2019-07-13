package com.smarttoolfactory.opensky.repo


import androidx.lifecycle.LiveData

import com.smarttoolfactory.opensky.api.request.AllStateVectorRequest
import com.smarttoolfactory.opensky.model.AircraftVectorState
import com.smarttoolfactory.opensky.repo.remote.WebService
import com.smarttoolfactory.opensky.utils.AppExecutors

import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class is repository for retrieving data from web service or room data base if required
 */

@Singleton
class AircraftRepository
/**
 * Venue Repository retrieves aircraft data from web or offline source if specified
 *
 * @param appExecutors contains web, database and main threads for different type of operations
 * @param webService   request data via Retrofit2 from web
 */
@Inject
constructor(
        /**
         * App executor provides three threads to execute operation on main or worker threads
         */
        private val appExecutors: AppExecutors, private val webService: WebService) {

    fun getAircraftVectorStateResponse(request: AllStateVectorRequest): LiveData<Resource<List<AircraftVectorState>>> {
        return webService.getAircraftVectorStateList(request)
    }


}
