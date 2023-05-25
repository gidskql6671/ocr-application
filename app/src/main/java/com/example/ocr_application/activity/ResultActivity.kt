package com.example.ocr_application.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.ocr_application.databinding.ActivityResultBinding
import com.example.ocr_application.dto.OcrResponse
import com.example.ocr_application.retrofit.RetrofitClient
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


class ResultActivity : AppCompatActivity() {

    private lateinit var correctPercentTextView: TextView
    private lateinit var originTextView: TextView
    private lateinit var correctTextView: TextView
    private lateinit var loadingFrame: FrameLayout

    private lateinit var binding: ActivityResultBinding
    private var imagePath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        correctPercentTextView = binding.correctPercentTextView
        originTextView = binding.originText
        correctTextView = binding.correctText
        loadingFrame = binding.loadingFrame

        imagePath = intent.extras!!.getString("imagePath")!!
        val correctText = intent.extras!!.getString("correctText")

        binding.btnPicture.setOnClickListener {
            val popupIntent = Intent(this, ImagePopupActivity::class.java)
            popupIntent.putExtra("imagePath", imagePath)
            startActivity(popupIntent)
        }

        if (correctText == null) {
            callOcr()
        } else{
            callOcr(correctText)
        }

//        callOcrMock()
    }

    private fun callOcrMock() {
        val correctPercent = 0.5 * 100
        correctPercentTextView.text = String.format("맞춤법 정답률은 %.2f%%입니다.", correctPercent)
    }

    private fun callOcr() {
        val file = File(imagePath)

        RetrofitClient.getApiService().ocr(getMultipartData(file))
            .enqueue(object: Callback<OcrResponse> {
                override fun onResponse(call: Call<OcrResponse>, response: Response<OcrResponse>) {
                    if (response.isSuccessful.not()) {
                        originTextView.text = "문장을 인식하는데 실패했습니다. 다시 시도해주세요."
                        correctTextView.text = "문장을 인식하는데 실패했습니다. 다시 시도해주세요."
                        loadingFrame.visibility = View.GONE

                        return
                    }

                    response.body()?.let {
                        Log.d("dong_request", it.toString())

                        val correctPercent = it.answerPercent * 100

                        correctPercentTextView.text = String.format("맞춤법 정답률은 %.2f%%입니다.", correctPercent)
                        originTextView.text = it.originString
                        correctTextView.text = it.correctString
                        loadingFrame.visibility = View.GONE
                    }
                }

                override fun onFailure(call: Call<OcrResponse>, t: Throwable) {
                    originTextView.text = "문장을 인식하는데 실패했습니다. 다시 시도해주세요."
                    correctTextView.text = "문장을 인식하는데 실패했습니다. 다시 시도해주세요."
                    loadingFrame.visibility = View.GONE

                    Log.e("dong_request", t.toString())
                }
            })

    }

    private fun callOcr(correctText: String) {
        val file = File(imagePath)
        correctTextView.text = correctText

        RetrofitClient.getApiService().ocrWithCorrect(getMultipartData(file), getMultipartData(correctText))
            .enqueue(object: Callback<OcrResponse> {
                override fun onResponse(call: Call<OcrResponse>, response: Response<OcrResponse>) {
                    if (response.isSuccessful.not()) {
                        originTextView.text = "문장을 인식하는데 실패했습니다. 다시 시도해주세요."
                        loadingFrame.visibility = View.GONE

                        return
                    }

                    response.body()?.let {
                        Log.d("dong_request", it.toString())

                        val correctPercent = it.answerPercent * 100

                        correctPercentTextView.text = String.format("맞춤법 정답률은 %.2f%%입니다.", correctPercent)
                        originTextView.text = it.originString
                        loadingFrame.visibility = View.GONE
                    }
                }

                override fun onFailure(call: Call<OcrResponse>, t: Throwable) {
                    originTextView.text = "문장을 인식하는데 실패했습니다. 다시 시도해주세요."
                    loadingFrame.visibility = View.GONE

                    Log.e("dong_request", t.toString())
                }
            })
    }

    private fun getMultipartData(image: File): MultipartBody.Part {
        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), image)

        return MultipartBody.Part.createFormData("image", image.name, requestFile)
    }

    private fun getMultipartData(correctText: String): MultipartBody.Part {
        return MultipartBody.Part.createFormData("correctText", correctText)
    }
}