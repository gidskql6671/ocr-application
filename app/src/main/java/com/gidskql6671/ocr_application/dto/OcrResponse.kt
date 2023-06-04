package com.gidskql6671.ocr_application.dto

import com.google.gson.annotations.SerializedName

data class OcrResponse(
    @SerializedName("origin_string") val originString: String,
    @SerializedName("correct_string") val correctString: String,
    @SerializedName("answer_percent") val answerPercent: Double,
)
