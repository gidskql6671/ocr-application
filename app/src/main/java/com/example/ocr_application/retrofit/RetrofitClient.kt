package com.example.ocr_application.retrofit

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitClient {
    private const val host = "http://34.64.239.253/"

    fun getApiService(): RetrofitInterface = getInstance().create(RetrofitInterface::class.java)

    private fun getInstance(): Retrofit {
        val gson = GsonBuilder().setLenient().create()

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(90, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(host)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}