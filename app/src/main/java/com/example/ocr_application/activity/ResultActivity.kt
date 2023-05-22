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
import com.google.android.material.tabs.TabLayoutMediator
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


class ResultActivity : AppCompatActivity() {

    private lateinit var correctPercentTextView: TextView
    private lateinit var pager: ViewPager2
    private lateinit var binding: ActivityResultBinding
    private var imagePath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imagePath = intent.extras!!.getString("imagePath")!!
        initPager()
        correctPercentTextView = binding.correctPercentTextView

        binding.btnPicture.setOnClickListener {
            val popupIntent = Intent(this, ImagePopupActivity::class.java)
            popupIntent.putExtra("imagePath", imagePath)
            startActivity(popupIntent)
        }

        callOcr()
//        callOcrMock()
    }

    private fun initPager() {
        pager = binding.pager.also {
            it.adapter = ResultPageFragmentAdapter(
                this,
                originText = "여기에 텍스트 인식 결과가 표시됩니다.",
                correctText = "여기에 텍스트 인식 결과의 정답이 표시됩니다.",
                isLoding = true
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

            TabLayoutMediator(binding.tabLayout, it) { tab, position ->
                if (position == 0) {
                    tab.text = "텍스트 인식 결과"
                }
                else {
                    tab.text = "맞춤법 검사 결과"
                }
            }.attach()
        }
    }

    private fun callOcrMock() {
        pager.adapter = ResultPageFragmentAdapter(
            this,
            originText = "여기에 텍스트 인식 결과가 표시됩니다.",
            correctText = "여기에 텍스트 인식 결과의 정답이 표시됩니다.",
            isLoding = false
        )

        val correctPercent = 0.5 * 100
        correctPercentTextView.text = String.format("맞춤법 정답률은 %.2f%%입니다.", correctPercent)
    }

    private fun callOcr() {
        val file = File(imagePath)

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