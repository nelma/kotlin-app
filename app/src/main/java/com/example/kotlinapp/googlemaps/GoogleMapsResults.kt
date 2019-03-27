package com.example.kotlinapp.googlemaps

import com.squareup.moshi.Json

object GoogleMapsResults {

    data class Results(
        @field:Json(name = "results")
        val addresses: List<Address>,
        @field:Json(name = "status")
        val status: String? = null
    )

    data class Address(
        @field:Json(name = "formatted_address")
        val formattedAddress: String? = null,
        @field:Json(name = "geometry")
        val geometry: Geometry? = null
    )

    data class Geometry(
        @field:Json(name = "location")
        val location: Location? = null
    )

    data class Location(
        @field:Json(name = "lat")
        val latitude: Double = 0.0,
        @field:Json(name = "lng")
        val longitude: Double = 0.0
    )

}
