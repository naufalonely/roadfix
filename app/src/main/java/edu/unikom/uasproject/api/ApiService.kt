package edu.unikom.uasproject.api

import edu.unikom.uasproject.model.NominatimResponse
import edu.unikom.uasproject.model.UpdateResponse
import edu.unikom.uasproject.model.User
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Query

interface ApiService {
    @GET("user/profile")
    fun getUserProfile(): Call<User>

    @PUT("user/profile")
    fun updateProfile(@Body user: User): Call<UpdateResponse>
}

interface NominatimApi {
    @GET("reverse")
    suspend fun reverseGeocode(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("format") format: String = "json"
    ): Response<NominatimResponse>
}