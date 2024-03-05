package com.example.data_collect2

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface RetrofitAPI {

    @POST("acc")
    fun postData(@Body dataModal: DataModal?): Call<DataModal?>?
    @POST("gyro")
    fun postGyro(@Body gyroModal: GyroModal?): Call<GyroModal?>?
    @Multipart
    @POST("upload")
    fun upload(@Part file: MultipartBody.Part)
}