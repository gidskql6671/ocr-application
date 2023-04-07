package com.example.ocr_application.retrofit

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val host = "http://34.64.157.192:8080/"

    fun getApiService(): RetrofitInterface = getInstance().create(RetrofitInterface::class.java)

    private fun getInstance(): Retrofit {
        val gson = GsonBuilder().setLenient().create()

        return Retrofit.Builder()
            .baseUrl(host)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}