package com.smarttoolfactory.opensky.api.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class AllStateVectorRequest(@field:SerializedName("lamin")
                            @field:Expose
                            val latitudeMin: String, @field:SerializedName("lomin")
                            @field:Expose
                            val longitudeMin: String, @field:SerializedName("lamax")
                            @field:Expose
                            val latitudeMax: String, @field:SerializedName("lomax")
                            @field:Expose
                            val longitudeMax: String)
