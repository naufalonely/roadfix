package edu.unikom.uasproject.api

import edu.unikom.uasproject.model.UpdateResponse
import edu.unikom.uasproject.model.User
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.http.PUT

interface ApiService {
    @GET("user/profile")
    fun getUserProfile(): Call<User>

    @PUT("user/profile")
    fun updateProfile(@Body user: User): Call<UpdateResponse>
}