package com.example.ocr_application.retrofit

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface RetrofitInterface {

    @GET("fake")
    fun fake(@Query("input") input: String): Call<String>
}