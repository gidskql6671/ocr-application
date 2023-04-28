package com.example.ocr_application.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.ocr_application.ResultPageFragmentAdapter
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

//    lateinit var correctTextView: TextView
//    lateinit var originTextView: TextView
    lateinit var correctPercentTextView: TextView
    lateinit var pager: ViewPager2

    private lateinit var binding: ActivityResultBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pager = binding.pager.also {
            it.adapter = ResultPageFragmentAdapter(
                this,
                "여기에 텍스트 인식 결과가 표시됩니다.",
                "여기에 텍스트 인식 결과의 정답이 표시됩니다."
            )

            it.currentItem = 0
            it.offscreenPageLimit = 2
            it.registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                    if (positionOffsetPixels == 0) {
                        it.currentItem = position
                    }
                }
            })
        }

        correctPercentTextView = binding.correctPercentTextView

        binding.btnPicture.setOnClickListener {
            val popupIntent = Intent(this, ImagePopupActivity::class.java)
            popupIntent.putExtra("imagePath", intent.extras!!.getString("imagePath")!!)
            startActivity(popupIntent)
        }

//        callOcr(file)
        callOcrMock()
    }

    private fun callOcrMock() {
        pager.adapter = ResultPageFragmentAdapter(
            this,
            "여기에 텍스트 인식 결과가 표시됩니다.",
            "여기에 텍스트 인식 결과의 정답이 표시됩니다."
        )

        val correctPercent = 0.5 * 100
        correctPercentTextView.text = String.format("맞춤법 정답률은 %.2f%%입니다.", correctPercent)
    }

    private fun callOcr(file: File) {
        RetrofitClient.getApiService().ocr(getMultipartData(file))
            .enqueue(object: Callback<OcrResponse> {
                override fun onResponse(call: Call<OcrResponse>, response: Response<OcrResponse>) {
                    if (response.isSuccessful.not()) {
                        return
                    }

                    response.body()?.let {
                        Log.d("dong_request", it.toString())

                        pager.adapter = ResultPageFragmentAdapter(
                            this@ResultActivity,
                            it.originString,
                            it.correctString
                        )
                        val correctPercent = it.answerPercent * 100

                        correctPercentTextView.text = String.format("맞춤법 정답률은 %.2f%%입니다.", correctPercent)
                    }
                }

                override fun onFailure(call: Call<OcrResponse>, t: Throwable) {
                    Log.d("dong_request", t.toString())
                }
            })

    }

    private fun getMultipartData(image: File): MultipartBody.Part {
        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), image)

        return MultipartBody.Part.createFormData("image", image.name, requestFile)
    }
}