package com.example.ocr_application.retrofit

import com.example.ocr_application.dto.OcrResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*


interface RetrofitInterface {

    @GET("fake")
    fun fake(@Query("input") input: String): Call<String>

    @Multipart
    @POST("/ocr")
    fun ocr(@Part image: MultipartBody.Part): Call<OcrResponse>

    @Multipart
    @POST("/ocr")
    fun ocrWithCorrect(@Part image: MultipartBody.Part, @Part correctText: MultipartBody.Part): Call<OcrResponse>


    @GET("/problem")
    fun problem(@Query("studentGrade") studentGrade: Int, @Query("problemGrade") problemGrade: Int): Call<String>
}