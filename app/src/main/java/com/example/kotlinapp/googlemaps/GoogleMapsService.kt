package com.example.kotlinapp.googlemaps

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleMapsService {

    @GET("geocode/json")
    fun findAddress(@Query("address") address: String): Call<GoogleMapsResults.Results>

}
