package com.example.android.api

import com.example.android.models.Info
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// Temp class for searching panel
data class Vehicle(
    val imageResId: String,
    val plateNumber: String,
    val Picture: String,
    val PictureThumbnail: String,
    val entryTime: String?
)

data class VehicleResponse(
    val _id: String,
    val licensePlate: String,
    val entryTime: String,
    val picture: String?,
    val pictureThumbnail: String?
)

data class UpdatePaymentRequest(
    val _id: String,
    val total: Int,
    val parkingHours: String
)

interface CarPlateApi {
    @GET("action/test")
    fun getTest(): Call<Void>

    @POST("action/saveDetect")
    fun saveDetect(@Body info: Info): Call<Info>

    @GET("action/vehicle")
    fun getVehicles(): Call<List<VehicleResponse>>

    @GET("action/searchVehicles")
    fun searchVehicles(@Query("labelName") labelName: String): Call<List<VehicleResponse>>

    @PUT("action/updatePayment/{id}")
    fun updatePayment(
        @Path("id") id: String,
        @Body paymentData: UpdatePaymentRequest
    ): Call<Void>
}
