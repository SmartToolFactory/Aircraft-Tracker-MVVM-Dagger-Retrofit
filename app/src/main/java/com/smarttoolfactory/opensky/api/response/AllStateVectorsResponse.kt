package com.smarttoolfactory.opensky.api.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class AllStateVectorsResponse {

    @SerializedName("time")
    @Expose
    var time: Int? = null
    @SerializedName("states")
    @Expose
    var states: List<List<String>>? = null
}